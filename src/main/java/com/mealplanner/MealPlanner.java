package com.mealplanner;

import java.io.IOException;
import java.util.*;

import com.mealplanner.foodmanage.NutritionDataParser;

public class MealPlanner {
    private List<Food> foodDatabase;
    private NutritionCalculator nutritionCalculator;
    private static final double MEAL_RATIO_BREAKFAST = 0.3;  // 早餐占比30%
    private static final double MEAL_RATIO_LUNCH = 0.35;     // 午餐占比35%
    private static final double MEAL_RATIO_DINNER = 0.35;    // 晚餐占比35%

    // 营养素权重
    private double weightCalorie = 1.0;   // 热量权重
    private double weightCarb = 1.0;      // 碳水化合物权重
    private double weightProtein = 1.0;   // 蛋白质权重
    private double weightFat = 1.0;       // 脂肪权重
    private double weightCalcium = 0.7;   // 钙权重
    private double weightPotassium = 0.7; // 钾权重
    private double weightSodium = 0.8;    // 钠权重
    private double weightMagnesium = 0.7; // 镁权重
    private double weightPhosphorus = 0.5; // 磷权重
    private double weightIron = 0.5;      // 铁权重

    public MealPlanner(UserProfile userProfile) {
        this.nutritionCalculator = new NutritionCalculator(userProfile);
        initializeFoodDatabase();
        adjustWeightsByHealthConditions(userProfile.getHealthConditions());
    }

    /**
     * 根据用户健康状况调整营养素权重
     * @param healthConditions 用户健康状况数组
     */
    private void adjustWeightsByHealthConditions(String[] healthConditions) {
        if (healthConditions == null || healthConditions.length == 0) {
            return; // 没有特殊健康状况，使用默认权重
        }

        for (String condition : healthConditions) {
            switch (condition.toLowerCase()) {
                case "hypertension": // 高血压
                    weightSodium = 1.5;    // 增加钠的权重（更严格控制）
                    weightPotassium = 1.2; // 增加钾的权重（鼓励摄入）
                    break;
                    
                case "diabetes": // 糖尿病
                    weightCalorie=1.8;
                    weightCarb = 1.5;      // 增加碳水的权重（更严格控制）
                    weightFat = 1.2;       // 增加脂肪的权重
                    break;
                    
                case "hyperlipidemia": // 高血脂
                    weightFat = 1.5;       // 增加脂肪的权重（更严格控制）
                    break;
                    
                case "gout": // 痛风
                    weightProtein = 1.3;   // 增加蛋白质的权重（控制某些蛋白质来源）
                    break;
                    
                case "ckd": // 慢性肾病（无透析）
                    weightProtein = 1.5;   // 增加蛋白质的权重（严格控制）
                    weightPotassium = 1.3; // 增加钾的权重
                    weightPhosphorus = 1.3; // 增加磷的权重
                    break;
                    
                case "ckd_dialysis": // 慢性肾病（透析）
                    weightProtein = 1.2;   // 增加蛋白质的权重（但不同于无透析）
                    weightPotassium = 1.5; // 增加钾的权重（更严格控制）
                    break;
                    
                case "osteoporosis": // 骨质疏松
                    weightCalcium = 1.5;   // 增加钙的权重
                    weightMagnesium = 1.2; // 增加镁的权重
                    break;
                    
                case "anemia": // 贫血
                    weightIron = 1.5;      // 增加铁的权重
                    break;
            }
        }
    }

    private void initializeFoodDatabase() {
        foodDatabase = new ArrayList<>();

        NutritionDataParser parser = new NutritionDataParser();
        try {
            foodDatabase = parser.convertToFoodObjects(parser.parseNutritionDataFromFile("src/main/resources/all.xlsx"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // // 主食类
        // foodDatabase.add(new Food("糙米", "staple", 
        //     new Nutrition(75.0, 7.5, 2.0, 10.0, 150.0, 5.0, 100.0),
        //     new Portion(100.0, "克", 100.0)));
            
        // foodDatabase.add(new Food("全麦面包", "staple", 
        //     new Nutrition(50.0, 8.0, 3.0, 20.0, 120.0, 400.0, 80.0),
        //     new Portion(60.0, "片", 2.0)));
            
        // foodDatabase.add(new Food("燕麦", "staple", 
        //     new Nutrition(66.0, 16.9, 6.9, 54.0, 429.0, 2.0, 177.0),
        //     new Portion(50.0, "克", 50.0)));
            
        // foodDatabase.add(new Food("藜麦", "staple", 
        //     new Nutrition(64.0, 14.1, 6.1, 47.0, 563.0, 5.0, 197.0),
        //     new Portion(80.0, "克", 80.0)));
            
        // foodDatabase.add(new Food("红薯", "staple", 
        //     new Nutrition(20.0, 1.6, 0.1, 30.0, 337.0, 55.0, 25.0),
        //     new Portion(150.0, "个", 1.0)));
        
        // // 蔬菜类
        // foodDatabase.add(new Food("西兰花", "vegetables", 
        //     new Nutrition(7.0, 2.8, 0.4, 47.0, 316.0, 33.0, 21.0),
        //     new Portion(100.0, "克", 100.0)));
            
        // foodDatabase.add(new Food("菠菜", "vegetables", 
        //     new Nutrition(3.6, 2.9, 0.4, 99.0, 558.0, 79.0, 79.0),
        //     new Portion(100.0, "克", 100.0)));
            
        // foodDatabase.add(new Food("胡萝卜", "vegetables", 
        //     new Nutrition(9.6, 0.9, 0.2, 33.0, 320.0, 69.0, 12.0),
        //     new Portion(80.0, "根", 1.0)));
            
        // foodDatabase.add(new Food("青椒", "vegetables", 
        //     new Nutrition(4.6, 1.0, 0.3, 7.0, 175.0, 3.0, 12.0),
        //     new Portion(70.0, "个", 1.0)));
            
        // foodDatabase.add(new Food("西红柿", "vegetables", 
        //     new Nutrition(3.9, 0.9, 0.2, 10.0, 237.0, 5.0, 11.0),
        //     new Portion(150.0, "个", 1.0)));
        
        // // 蛋白质类
        // foodDatabase.add(new Food("鸡胸肉", "protein", 
        //     new Nutrition(0.0, 23.1, 1.2, 12.0, 256.0, 45.0, 23.0),
        //     new Portion(100.0, "克", 100.0)));
            
        // foodDatabase.add(new Food("三文鱼", "protein", 
        //     new Nutrition(0.0, 20.0, 13.0, 9.0, 363.0, 50.0, 27.0),
        //     new Portion(100.0, "克", 100.0)));
            
        // foodDatabase.add(new Food("鸡蛋", "protein", 
        //     new Nutrition(1.1, 13.0, 11.0, 56.0, 138.0, 142.0, 12.0),
        //     new Portion(50.0, "个", 1.0)));
            
        // foodDatabase.add(new Food("虾", "protein", 
        //     new Nutrition(0.2, 24.0, 0.3, 52.0, 259.0, 119.0, 39.0),
        //     new Portion(100.0, "克", 100.0)));
            
        // foodDatabase.add(new Food("瘦牛肉", "protein", 
        //     new Nutrition(0.0, 26.0, 3.0, 5.0, 318.0, 54.0, 21.0),
        //     new Portion(100.0, "克", 100.0)));
        
        // // 豆类
        // foodDatabase.add(new Food("豆腐", "beans", 
        //     new Nutrition(1.9, 8.1, 4.8, 350.0, 120.0, 7.0, 30.0),
        //     new Portion(100.0, "克", 100.0)));
            
        // foodDatabase.add(new Food("黑豆", "beans", 
        //     new Nutrition(63.0, 21.0, 1.0, 102.0, 1483.0, 2.0, 171.0),
        //     new Portion(50.0, "克", 50.0)));
            
        // foodDatabase.add(new Food("鹰嘴豆", "beans", 
        //     new Nutrition(63.0, 19.0, 6.0, 49.0, 875.0, 24.0, 115.0),
        //     new Portion(50.0, "克", 50.0)));
            
        // foodDatabase.add(new Food("扁豆", "beans", 
        //     new Nutrition(63.0, 25.0, 1.0, 19.0, 955.0, 2.0, 71.0),
        //     new Portion(50.0, "克", 50.0)));
        
        // // 水果类
        // foodDatabase.add(new Food("苹果", "fruits", 
        //     new Nutrition(14.0, 0.3, 0.2, 6.0, 107.0, 1.0, 5.0),
        //     new Portion(150.0, "个", 1.0)));
            
        // foodDatabase.add(new Food("香蕉", "fruits", 
        //     new Nutrition(23.0, 1.1, 0.3, 5.0, 358.0, 1.0, 27.0),
        //     new Portion(120.0, "根", 1.0)));
            
        // foodDatabase.add(new Food("橙子", "fruits", 
        //     new Nutrition(11.8, 0.9, 0.1, 40.0, 181.0, 0.0, 10.0),
        //     new Portion(180.0, "个", 1.0)));
            
        // foodDatabase.add(new Food("蓝莓", "fruits", 
        //     new Nutrition(14.5, 0.7, 0.3, 6.0, 77.0, 1.0, 6.0),
        //     new Portion(100.0, "克", 100.0)));
            
        // foodDatabase.add(new Food("草莓", "fruits", 
        //     new Nutrition(7.7, 0.7, 0.3, 16.0, 153.0, 1.0, 13.0),
        //     new Portion(100.0, "克", 100.0)));
        
        // // 乳制品
        // foodDatabase.add(new Food("低脂牛奶", "dairy", 
        //     new Nutrition(4.8, 3.4, 1.0, 122.0, 156.0, 42.0, 11.0),
        //     new Portion(250.0, "杯", 1.0)));
            
        // foodDatabase.add(new Food("酸奶", "dairy", 
        //     new Nutrition(4.7, 3.5, 1.5, 121.0, 155.0, 46.0, 12.0),
        //     new Portion(200.0, "杯", 1.0)));
            
        // foodDatabase.add(new Food("奶酪", "dairy", 
        //     new Nutrition(1.3, 7.0, 9.0, 721.0, 98.0, 621.0, 28.0),
        //     new Portion(30.0, "片", 1.0)));
        
        // // 坚果类
        // foodDatabase.add(new Food("杏仁", "nuts", 
        //     new Nutrition(21.6, 21.2, 49.9, 269.0, 733.0, 1.0, 270.0),
        //     new Portion(30.0, "克", 30.0)));
            
        // foodDatabase.add(new Food("核桃", "nuts", 
        //     new Nutrition(13.7, 15.2, 65.2, 98.0, 441.0, 2.0, 158.0),
        //     new Portion(30.0, "克", 30.0)));
    }

    /**
     * 生成每日膳食计划
     * @return 每日膳食计划
     */
    public DailyMealPlan generateDailyMealPlan() {
        DailyNutrientNeeds dailyNeeds = nutritionCalculator.calculateDailyNutrientNeeds();
        
        // 为每餐分配营养需求
        MealNutrients breakfastNeeds = calculateMealNutrients(dailyNeeds, MEAL_RATIO_BREAKFAST);
        MealNutrients lunchNeeds = calculateMealNutrients(dailyNeeds, MEAL_RATIO_LUNCH);
        MealNutrients dinnerNeeds = calculateMealNutrients(dailyNeeds, MEAL_RATIO_DINNER);

        // 用于跟踪已使用的食物
        Set<String> usedFoods = new HashSet<>();

        // 生成每餐食谱
        List<Food> breakfast = generateMeal(breakfastNeeds, usedFoods, true);
        List<Food> lunch = generateMeal(lunchNeeds, usedFoods, true);
        List<Food> dinner = generateMeal(dinnerNeeds, usedFoods, true);

        // 创建每日膳食计划
        DailyMealPlan dailyPlan = new DailyMealPlan(breakfast, lunch, dinner);
        
        // 打印详细的营养素成分（用于调试）
        printDetailedNutrition(dailyPlan, dailyNeeds);
        
        return dailyPlan;
    }
    
    /**
     * 打印详细的营养素成分
     * @param plan 每日膳食计划
     * @param dailyNeeds 每日营养需求
     */
    private void printDetailedNutrition(DailyMealPlan plan, DailyNutrientNeeds dailyNeeds) {
        System.out.println("\n========== 食谱营养素详细分析（调试信息）==========");
        
        // 计算每餐的实际营养素
        NutrientSummary breakfastSummary = calculateMealNutrientSummary(plan.getBreakfast());
        NutrientSummary lunchSummary = calculateMealNutrientSummary(plan.getLunch());
        NutrientSummary dinnerSummary = calculateMealNutrientSummary(plan.getDinner());
        
        // 计算全天总营养素
        NutrientSummary dailySummary = breakfastSummary.add(lunchSummary).add(dinnerSummary);
        
        // 打印每餐营养素
        System.out.println("\n早餐营养素详情:");
        printNutrientSummary(breakfastSummary);
        
        System.out.println("\n午餐营养素详情:");
        printNutrientSummary(lunchSummary);
        
        System.out.println("\n晚餐营养素详情:");
        printNutrientSummary(dinnerSummary);
        
        // 打印全天总营养素与目标对比
        System.out.println("\n全天营养素总结与目标对比:");
        System.out.printf("热量: %.1f kcal / %.1f kcal (%.1f%%)\n", 
                dailySummary.calories, dailyNeeds.getTotalCalories(), 
                (dailySummary.calories / dailyNeeds.getTotalCalories()) * 100);
                
        System.out.printf("碳水: %.1f g / %.1f g (%.1f%%)\n", 
                dailySummary.carbs, dailyNeeds.getCarbohydrates(), 
                (dailySummary.carbs / dailyNeeds.getCarbohydrates()) * 100);
                
        System.out.printf("蛋白质: %.1f g / %.1f g (%.1f%%)\n", 
                dailySummary.protein, dailyNeeds.getProtein(), 
                (dailySummary.protein / dailyNeeds.getProtein()) * 100);
                
        System.out.printf("脂肪: %.1f g / %.1f g (%.1f%%)\n", 
                dailySummary.fat, dailyNeeds.getFat(), 
                (dailySummary.fat / dailyNeeds.getFat()) * 100);
                
        System.out.printf("钙: %.1f mg / %.1f mg (%.1f%%)\n", 
                dailySummary.calcium, dailyNeeds.getCalcium(), 
                (dailySummary.calcium / dailyNeeds.getCalcium()) * 100);
                
        System.out.printf("钾: %.1f mg / %.1f mg (%.1f%%)\n", 
                dailySummary.potassium, dailyNeeds.getPotassium(), 
                (dailySummary.potassium / dailyNeeds.getPotassium()) * 100);
                
        System.out.printf("钠: %.1f mg / %.1f mg (%.1f%%)\n", 
                dailySummary.sodium, dailyNeeds.getSodium(), 
                (dailySummary.sodium / dailyNeeds.getSodium()) * 100);
                
        System.out.printf("镁: %.1f mg / %.1f mg (%.1f%%)\n", 
                dailySummary.magnesium, dailyNeeds.getMagnesium(), 
                (dailySummary.magnesium / dailyNeeds.getMagnesium()) * 100);
        
        // 打印营养素权重信息
        System.out.println("\n当前营养素权重设置:");
        System.out.printf("热量: %.1f, 碳水: %.1f, 蛋白质: %.1f, 脂肪: %.1f\n", 
                weightCalorie, weightCarb, weightProtein, weightFat);
        System.out.printf("钙: %.1f, 钾: %.1f, 钠: %.1f, 镁: %.1f, 磷: %.1f, 铁: %.1f\n", 
                weightCalcium, weightPotassium, weightSodium, weightMagnesium, weightPhosphorus, weightIron);
        
        System.out.println("\n========== 食谱营养素分析结束 ==========");
    }
    
    /**
     * 打印营养素摘要
     * @param summary 营养素摘要
     */
    private void printNutrientSummary(NutrientSummary summary) {
        System.out.printf("热量: %.1f kcal\n", summary.calories);
        System.out.printf("碳水: %.1f g (%.1f%%)\n", summary.carbs, summary.getCarbsPercentage());
        System.out.printf("蛋白质: %.1f g (%.1f%%)\n", summary.protein, summary.getProteinPercentage());
        System.out.printf("脂肪: %.1f g (%.1f%%)\n", summary.fat, summary.getFatPercentage());
        System.out.printf("微量元素: 钙 %.1f mg, 钾 %.1f mg, 钠 %.1f mg, 镁 %.1f mg\n", 
                summary.calcium, summary.potassium, summary.sodium, summary.magnesium);
    }
    
    /**
     * 计算一餐的营养素摘要
     * @param foods 食物列表
     * @return 营养素摘要
     */
    private NutrientSummary calculateMealNutrientSummary(List<Food> foods) {
        double calories = 0;
        double carbs = 0;
        double protein = 0;
        double fat = 0;
        double calcium = 0;
        double potassium = 0;
        double sodium = 0;
        double magnesium = 0;
        
        for (Food food : foods) {
            calories += food.getCalories();
            carbs += food.getCarbohydrates();
            protein += food.getProtein();
            fat += food.getFat();
            calcium += food.getCalcium();
            potassium += food.getPotassium();
            sodium += food.getSodium();
            magnesium += food.getMagnesium();
        }
        
        return new NutrientSummary(calories, carbs, protein, fat, calcium, potassium, sodium, magnesium);
    }

    private MealNutrients calculateMealNutrients(DailyNutrientNeeds dailyNeeds, double ratio) {
        return new MealNutrients(
            dailyNeeds.getTotalCalories() * ratio,
            dailyNeeds.getCarbohydrates() * ratio,
            dailyNeeds.getProtein() * ratio,
            dailyNeeds.getFat() * ratio,
            dailyNeeds.getCalcium() * ratio,
            dailyNeeds.getPotassium() * ratio,
            dailyNeeds.getSodium() * ratio,
            dailyNeeds.getMagnesium() * ratio
        );
    }

    private List<Food> generateMeal(MealNutrients targetNutrients, Set<String> usedFoods, boolean requireStaple) {
        // 用于存储一餐的食物列表
        List<Food> meal = new ArrayList<>();
        // 用于记录每个食物类别的数量
        Map<String, Integer> categoryCount = new HashMap<>();
        // 剩余热量
        double remainingCalories = targetNutrients.calories;
        
        // 如果需要主食，先选择主食
        if (requireStaple) {
            Food staple = findBestStapleFood(targetNutrients, usedFoods);
            if (staple != null) {
                meal.add(staple);
                remainingCalories -= staple.getCalories();
                usedFoods.add(staple.getName());
                categoryCount.put("staple", 1);
                targetNutrients = subtractNutrients(targetNutrients, staple);
            }
        }
        
        // 选择其他食物
        while (remainingCalories > 0 && meal.size() < 5) {
            Food bestFood = findBestFood(targetNutrients, categoryCount, usedFoods);
            if (bestFood == null) break;
            
            meal.add(bestFood);
            remainingCalories -= bestFood.getCalories();
            usedFoods.add(bestFood.getName());
            categoryCount.put(bestFood.getCategory(), 
                            categoryCount.getOrDefault(bestFood.getCategory(), 0) + 1);
            
            targetNutrients = subtractNutrients(targetNutrients, bestFood);
        }
        
        return meal;
    }

    private Food findBestStapleFood(MealNutrients targetNutrients, Set<String> usedFoods) {
        Food bestStaple = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Food food : foodDatabase) {
            if (food.getCategory().equals("staple") && !usedFoods.contains(food.getName())) {
                double score = calculateFoodScore(food, targetNutrients);
                if (score > bestScore) {
                    bestScore = score;
                    bestStaple = food;
                }
            }
        }
        
        return bestStaple;
    }

    private Food findBestFood(MealNutrients targetNutrients, Map<String, Integer> categoryCount, Set<String> usedFoods) {
        Food bestFood = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Food food : foodDatabase) {
            // 跳过已使用的食物
            if (usedFoods.contains(food.getName())) {
                continue;
            }
            
            // 限制每类食物的数量
            if (categoryCount.getOrDefault(food.getCategory(), 0) >= 2) {
                continue;
            }
            
            // 计算食物评分
            double score = calculateFoodScore(food, targetNutrients);
            if (score > bestScore) {
                bestScore = score;
                bestFood = food;
            }
        }
        
        return bestFood;
    }

    private double calculateFoodScore(Food food, MealNutrients target) {
        // 计算食物与目标营养需求的匹配度
        
        // 主要营养素评分
        double calorieScore = calculateNutrientScore(food.getCalories(), target.calories);
        double carbScore = calculateNutrientScore(food.getCarbohydrates(), target.carbohydrates);
        double proteinScore = calculateNutrientScore(food.getProtein(), target.protein);
        double fatScore = calculateNutrientScore(food.getFat(), target.fat);
        
        // 微量元素评分
        double calciumScore = calculateNutrientScore(food.getCalcium(), target.calcium);
        double potassiumScore = calculateNutrientScore(food.getPotassium(), target.potassium);
        double sodiumScore = calculateNutrientScore(food.getSodium(), target.sodium);
        double magnesiumScore = calculateNutrientScore(food.getMagnesium(), target.magnesium);
        
        // 计算总权重
        double totalWeight = weightCalorie + weightCarb + weightProtein + weightFat +
                            weightCalcium + weightPotassium + weightSodium + weightMagnesium;
        
        // 加权平均评分
        double weightedScore = (calorieScore * weightCalorie +
                               carbScore * weightCarb +
                               proteinScore * weightProtein +
                               fatScore * weightFat +
                               calciumScore * weightCalcium +
                               potassiumScore * weightPotassium +
                               sodiumScore * weightSodium +
                               magnesiumScore * weightMagnesium) / totalWeight;
        
        return weightedScore;
    }
    
    /**
     * 计算单个营养素的评分
     * @param actual 实际值
     * @param target 目标值
     * @return 评分（0-1之间）
     */
    private double calculateNutrientScore(double actual, double target) {
        // 避免除以零
        if (target == 0) {
            return actual == 0 ? 1.0 : 0.0;
        }
        
        // 计算实际值与目标值的比率
        double ratio = actual / target;
        
        // 使用改进的评分函数，对过量和不足的惩罚不同
        if (ratio <= 1.0) {
            // 不足的情况，线性惩罚
            return ratio;
        } else {
            // 过量的情况，指数惩罚（过量越多，惩罚越严重）
            return Math.exp(-(ratio - 1.0));
        }
    }

    private MealNutrients subtractNutrients(MealNutrients target, Food food) {
        return new MealNutrients(
            target.calories - food.getCalories(),
            target.carbohydrates - food.getCarbohydrates(),
            target.protein - food.getProtein(),
            target.fat - food.getFat(),
            target.calcium - food.getCalcium(),
            target.potassium - food.getPotassium(),
            target.sodium - food.getSodium(),
            target.magnesium - food.getMagnesium()
        );
    }
}

class MealNutrients {
    double calories;      // 卡路里(kcal)
    double carbohydrates; // 碳水化合物(g)
    double protein;       // 蛋白质(g)
    double fat;           // 脂肪(g)
    double calcium;       // 钙(mg)
    double potassium;     // 钾(mg)
    double sodium;        // 钠(mg)
    double magnesium;     // 镁(mg)

    public MealNutrients(double calories, double carbohydrates, double protein, 
                        double fat, double calcium, double potassium, 
                        double sodium, double magnesium) {
        this.calories = calories;
        this.carbohydrates = carbohydrates;
        this.protein = protein;
        this.fat = fat;
        this.calcium = calcium;
        this.potassium = potassium;
        this.sodium = sodium;
        this.magnesium = magnesium;
    }
}

class DailyMealPlan {
    private List<Food> breakfast;
    private List<Food> lunch;
    private List<Food> dinner;

    public DailyMealPlan(List<Food> breakfast, List<Food> lunch, List<Food> dinner) {
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
    }
    
    // 添加getter方法
    public List<Food> getBreakfast() { return breakfast; }
    public List<Food> getLunch() { return lunch; }
    public List<Food> getDinner() { return dinner; }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("每日膳食计划:\n\n");
        
        // 早餐
        sb.append("早餐 (6:30-8:30):\n");
        for (Food food : breakfast) {
            sb.append(String.format("- %s %s\n", food.getName(), food.getPortionDescription()));
        }
        sb.append(getMealNutritionSummary(breakfast));
        
        // 午餐
        sb.append("\n午餐 (11:30-13:30):\n");
        for (Food food : lunch) {
            sb.append(String.format("- %s %s\n", food.getName(), food.getPortionDescription()));
        }
        sb.append(getMealNutritionSummary(lunch));
        
        // 晚餐
        sb.append("\n晚餐 (17:30-19:30):\n");
        for (Food food : dinner) {
            sb.append(String.format("- %s %s\n", food.getName(), food.getPortionDescription()));
        }
        sb.append(getMealNutritionSummary(dinner));
        
        // 全天营养总结
        sb.append("\n全天营养总结:\n");
        List<Food> allFoods = new ArrayList<>();
        allFoods.addAll(breakfast);
        allFoods.addAll(lunch);
        allFoods.addAll(dinner);
        sb.append(getMealNutritionSummary(allFoods));
        
        return sb.toString();
    }
    
    private String getMealNutritionSummary(List<Food> foods) {
        double totalCalories = 0;
        double totalCarbs = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalCalcium = 0;
        double totalPotassium = 0;
        double totalSodium = 0;
        double totalMagnesium = 0;
        
        for (Food food : foods) {
            totalCalories += food.getCalories();
            totalCarbs += food.getCarbohydrates();
            totalProtein += food.getProtein();
            totalFat += food.getFat();
            totalCalcium += food.getCalcium();
            totalPotassium += food.getPotassium();
            totalSodium += food.getSodium();
            totalMagnesium += food.getMagnesium();
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  营养总结: %.0f卡路里, %.1fg碳水, %.1fg蛋白质, %.1fg脂肪\n", 
                 totalCalories, totalCarbs, totalProtein, totalFat));
        sb.append(String.format("  微量元素: %.0fmg钙, %.0fmg钾, %.0fmg钠, %.0fmg镁\n", 
                 totalCalcium, totalPotassium, totalSodium, totalMagnesium));
        
        // 计算三大营养素比例
        double totalNutrientCalories = (totalCarbs * 4) + (totalProtein * 4) + (totalFat * 9);
        double carbsPercentage = (totalCarbs * 4) / totalNutrientCalories * 100;
        double proteinPercentage = (totalProtein * 4) / totalNutrientCalories * 100;
        double fatPercentage = (totalFat * 9) / totalNutrientCalories * 100;
        
        sb.append(String.format("  营养素比例: 碳水%.1f%%, 蛋白质%.1f%%, 脂肪%.1f%%\n", 
                 carbsPercentage, proteinPercentage, fatPercentage));
        
        return sb.toString();
    }
}

/**
 * 营养素摘要类，用于存储和计算营养素信息
 */
class NutrientSummary {
    double calories;
    double carbs;
    double protein;
    double fat;
    double calcium;
    double potassium;
    double sodium;
    double magnesium;
    
    public NutrientSummary(double calories, double carbs, double protein, double fat,
                          double calcium, double potassium, double sodium, double magnesium) {
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
        this.calcium = calcium;
        this.potassium = potassium;
        this.sodium = sodium;
        this.magnesium = magnesium;
    }
    
    /**
     * 计算碳水化合物占总热量的百分比
     * @return 百分比
     */
    public double getCarbsPercentage() {
        double totalCalories = (carbs * 4) + (protein * 4) + (fat * 9);
        return totalCalories > 0 ? (carbs * 4 / totalCalories) * 100 : 0;
    }
    
    /**
     * 计算蛋白质占总热量的百分比
     * @return 百分比
     */
    public double getProteinPercentage() {
        double totalCalories = (carbs * 4) + (protein * 4) + (fat * 9);
        return totalCalories > 0 ? (protein * 4 / totalCalories) * 100 : 0;
    }
    
    /**
     * 计算脂肪占总热量的百分比
     * @return 百分比
     */
    public double getFatPercentage() {
        double totalCalories = (carbs * 4) + (protein * 4) + (fat * 9);
        return totalCalories > 0 ? (fat * 9 / totalCalories) * 100 : 0;
    }
    
    /**
     * 将两个营养素摘要相加
     * @param other 另一个营养素摘要
     * @return 相加后的新营养素摘要
     */
    public NutrientSummary add(NutrientSummary other) {
        return new NutrientSummary(
            this.calories + other.calories,
            this.carbs + other.carbs,
            this.protein + other.protein,
            this.fat + other.fat,
            this.calcium + other.calcium,
            this.potassium + other.potassium,
            this.sodium + other.sodium,
            this.magnesium + other.magnesium
        );
    }
} 