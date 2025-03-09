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