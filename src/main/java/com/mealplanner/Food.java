package com.mealplanner;

/**
 * 食物类，表示一种食物及其营养和份量信息
 */
public class Food {
    private String name;         // 食物名称
    private String category;     // 食物类别
    private Nutrition nutrition; // 营养信息（每100克）
    private Portion portion;     // 份量信息

    /**
     * 创建一个食物对象
     * @param name 食物名称
     * @param category 食物类别
     * @param nutrition 营养信息
     * @param portion 份量信息
     */
    public Food(String name, String category, Nutrition nutrition, Portion portion) {
        this.name = name;
        this.category = category;
        this.nutrition = nutrition;
        this.portion = portion;
    }

    /**
     * 获取食物的实际营养成分（根据份量计算）
     * @return 实际营养成分
     */
    public Nutrition getActualNutrition() {
        // 计算实际重量与100克的比例
        double ratio = portion.getWeight() / 100.0;
        // 按比例缩放营养成分
        return nutrition.scale(ratio);
    }

    /**
     * 创建具有特定摄入量的食物对象
     * @param intakeWeight 实际摄入重量(g)
     * @return 新的食物对象，具有指定摄入量
     */
    public Food withIntake(double intakeWeight) {
        // 创建一个新的份量对象
        Portion newPortion;
        if ("克".equals(portion.getDisplayUnit()) || "g".equals(portion.getDisplayUnit())) {
            // 如果单位是克，直接使用新的摄入量
            newPortion = new Portion(intakeWeight, portion.getDisplayUnit(), intakeWeight);
        } else {
            // 如果是其他单位，按比例计算显示数量
            double ratio = intakeWeight / portion.getWeight();
            newPortion = new Portion(
                intakeWeight,
                portion.getDisplayUnit(),
                portion.getDisplayAmount() * ratio
            );
        }
        
        // 返回新的食物对象，使用相同的名称、类别和营养信息，但有新的份量
        return new Food(name, category, nutrition, newPortion);
    }

    /**
     * 获取食物的推荐摄入量范围
     * @return 推荐摄入量范围
     */
    public IntakeRange getRecommendedIntakeRange() {
        switch (category) {
            case "staple":       // 主食
                return new IntakeRange(80.0, 150.0, 100.0);  // 主食80-150g，默认100g
            case "vegetable":    // 蔬菜
                return new IntakeRange(100.0, 250.0, 150.0); // 蔬菜100-250g，默认150g
            case "fruit":        // 水果
                return new IntakeRange(100.0, 250.0, 150.0); // 水果100-250g，默认150g
            case "meat":         // 肉类
                return new IntakeRange(50.0, 100.0, 75.0);   // 肉类50-100g，默认75g
            case "fish":         // 鱼类
                return new IntakeRange(50.0, 100.0, 75.0);   // 鱼类50-100g，默认75g
            case "egg":          // 蛋类
                return new IntakeRange(25.0, 75.0, 50.0);    // 蛋类25-75g，默认50g
            case "milk":         // 乳制品
                return new IntakeRange(100.0, 300.0, 200.0); // 乳制品100-300g，默认200g
            case "oil":          // 油脂
                return new IntakeRange(5.0, 15.0, 10.0);     // 油脂5-15g，默认10g
            default:             // 默认
                return new IntakeRange(25.0, 75.0, 50.0);    // 其他食物25-75g，默认50g
        }
    }

    /**
     * 根据营养需求计算最佳摄入量
     * @param targetNutrients 目标营养素需求
     * @return 最佳摄入量(g)
     */
    public double calculateOptimalIntake(MealNutrients targetNutrients) {
        IntakeRange range = getRecommendedIntakeRange();
        double optimalIntake = range.getDefaultIntake();

        // 根据营养素需求调整摄入量
        if (nutrition.getCalories() > 0) {
            // 基于热量需求调整
            double calorieRatio = targetNutrients.calories / (nutrition.getCalories() * range.getDefaultIntake() / 100.0);
            double calorieBasedIntake = range.getDefaultIntake() * calorieRatio;
            
            // 基于蛋白质需求调整
            double proteinRatio = targetNutrients.protein / (nutrition.getProtein() * range.getDefaultIntake() / 100.0);
            double proteinBasedIntake = range.getDefaultIntake() * proteinRatio;
            
            // 基于碳水需求调整
            double carbRatio = targetNutrients.carbohydrates / (nutrition.getCarbohydrates() * range.getDefaultIntake() / 100.0);
            double carbBasedIntake = range.getDefaultIntake() * carbRatio;
            
            // 综合考虑不同营养素需求，计算加权平均值
            double weightCalorie = 1.0;
            double weightProtein = 1.0;
            double weightCarb = 1.0;
            
            optimalIntake = (calorieBasedIntake * weightCalorie + 
                           proteinBasedIntake * weightProtein + 
                           carbBasedIntake * weightCarb) / 
                          (weightCalorie + weightProtein + weightCarb);
        }
        
        // 确保最佳摄入量在允许范围内
        return range.adjustToRange(optimalIntake);
    }

    /**
     * 获取食物的推荐摄入量（根据食物类别）
     * @return 推荐摄入量(g)
     */
    public double getRecommendedIntake() {
        return getRecommendedIntakeRange().getDefaultIntake();
    }

    /**
     * 获取食物的份量描述
     * @return 格式化的份量描述字符串
     */
    public String getPortionDescription() {
        return portion.getDescription();
    }

    // Getters
    public String getName() { return name; }
    public String getCategory() { return category; }
    public Nutrition getNutrition() { return nutrition; }
    public Portion getPortion() { return portion; }
    
    // 便捷方法，直接获取营养成分
    public double getCarbohydrates() { return getActualNutrition().getCarbohydrates(); }
    public double getProtein() { return getActualNutrition().getProtein(); }
    public double getFat() { return getActualNutrition().getFat(); }
    public double getCalcium() { return getActualNutrition().getCalcium(); }
    public double getPotassium() { return getActualNutrition().getPotassium(); }
    public double getSodium() { return getActualNutrition().getSodium(); }
    public double getMagnesium() { return getActualNutrition().getMagnesium(); }
    public double getCalories() { return getActualNutrition().getCalories(); }
    public double getWeight() { return portion.getWeight(); }
} 