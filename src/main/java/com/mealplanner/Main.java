package com.mealplanner;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 创建用户档案
        UserProfile userProfile = ;new UserProfile(
            70.0,   // 体重(kg)
            170.0,  // 身高(cm)
            30,     // 年龄
            "male", // 性别
            1.55,   // 活动系数（中度活动）
            new String[]{"hypertension", "diabetes"} // 健康状况
        )

        // 创建膳食规划器
        MealPlanner mealPlanner = new MealPlanner(userProfile);

        // 生成每日膳食计划
        DailyMealPlan dailyPlan = mealPlanner.generateDailyMealPlan();

        // 打印膳食计划
        System.out.println(dailyPlan.toString());

        // 计算营养需求
        NutritionCalculator calculator = new NutritionCalculator(userProfile);
        MealNutrients needs = calculator.calculateDailyNutrientNeeds();

        // 打印营养需求
        System.out.println("\n每日营养需求:");
        System.out.printf("总热量: %.0f kcal\n", needs.getCalories());
        System.out.printf("碳水化合物: %.0f g\n", needs.getCarbohydrates());
        System.out.printf("蛋白质: %.0f g\n", needs.getProtein());
        System.out.printf("脂肪: %.0f g\n", needs.getFat());
        System.out.printf("钙: %.0f mg\n", needs.getCalcium());
        System.out.printf("钾: %.0f mg\n", needs.getPotassium());
        System.out.printf("钠: %.0f mg\n", needs.getSodium());
        System.out.printf("镁: %.0f mg\n", needs.getMagnesium());
        
        // 测试动态摄入量计算功能
        testDynamicIntakeCalculation();
        
        // 测试膳食多轮优化功能
        testMealOptimization();
    }
    
    /**
     * 测试动态摄入量计算功能
     */
    private static void testDynamicIntakeCalculation() {
        System.out.println("\n===== 测试动态摄入量计算功能 =====");
        
        // 创建一个测试食物
        Nutrition nutrition = new Nutrition(20.0, 5.0, 2.0, 30.0, 200.0, 50.0, 20.0);
        Portion portion = new Portion(100.0, "克", 100.0);
        Food rice = new Food("米饭", "staple", nutrition, portion);
        
        // 创建不同的目标营养需求
        MealNutrients lowCalorie = new MealNutrients(200.0, 40.0, 15.0, 5.0, 300.0, 800.0, 300.0, 150.0);
        MealNutrients mediumCalorie = new MealNutrients(400.0, 60.0, 20.0, 10.0, 400.0, 1000.0, 400.0, 200.0);
        MealNutrients highCalorie = new MealNutrients(800.0, 100.0, 30.0, 20.0, 500.0, 1200.0, 500.0, 250.0);
        
        // 计算不同需求下的最佳摄入量
        double lowIntake = rice.calculateOptimalIntake(lowCalorie);
        double mediumIntake = rice.calculateOptimalIntake(mediumCalorie);
        double highIntake = rice.calculateOptimalIntake(highCalorie);
        
        // 打印结果
        System.out.println("食物: " + rice.getName() + " (" + rice.getCategory() + ")");
        System.out.println("摄入量范围: " + rice.getRecommendedIntakeRange().getMinIntake() + 
                          "g - " + rice.getRecommendedIntakeRange().getMaxIntake() + "g (默认: " +
                          rice.getRecommendedIntakeRange().getDefaultIntake() + "g)");
        System.out.println("低热量需求(200kcal)下的最佳摄入量: " + Math.round(lowIntake) + "g");
        System.out.println("中等热量需求(400kcal)下的最佳摄入量: " + Math.round(mediumIntake) + "g");
        System.out.println("高热量需求(800kcal)下的最佳摄入量: " + Math.round(highIntake) + "g");
        
        // 创建不同摄入量的食物对象并计算营养成分
        Food lowRice = rice.withIntake(lowIntake);
        Food mediumRice = rice.withIntake(mediumIntake);
        Food highRice = rice.withIntake(highIntake);
        
        System.out.println("\n不同摄入量下的营养成分:");
        System.out.printf("低摄入量(%dg): 热量 %.1f kcal, 碳水 %.1f g, 蛋白质 %.1f g\n",
                Math.round(lowIntake), lowRice.getCalories(), lowRice.getCarbohydrates(), lowRice.getProtein());
        System.out.printf("中摄入量(%dg): 热量 %.1f kcal, 碳水 %.1f g, 蛋白质 %.1f g\n",
                Math.round(mediumIntake), mediumRice.getCalories(), mediumRice.getCarbohydrates(), mediumRice.getProtein());
        System.out.printf("高摄入量(%dg): 热量 %.1f kcal, 碳水 %.1f g, 蛋白质 %.1f g\n",
                Math.round(highIntake), highRice.getCalories(), highRice.getCarbohydrates(), highRice.getProtein());
    }
    
    /**
     * 测试膳食多轮优化功能
     */
    private static void testMealOptimization() {
        System.out.println("\n===== 测试膳食多轮优化功能 =====");
        
        // 创建测试用的食物列表
        List<Food> foods = new ArrayList<>();
        
        // 添加几种食物
        Nutrition rice_n = new Nutrition(25.0, 2.5, 0.3, 5.0, 30.0, 10.0, 5.0);
        foods.add(new Food("米饭", "staple", rice_n, new Portion(100.0)));
        
        Nutrition veg_n = new Nutrition(5.0, 1.0, 0.2, 20.0, 150.0, 5.0, 10.0);
        foods.add(new Food("青菜", "vegetable", veg_n, new Portion(100.0)));
        
        Nutrition meat_n = new Nutrition(0.0, 15.0, 10.0, 10.0, 200.0, 80.0, 15.0);
        foods.add(new Food("鸡肉", "meat", meat_n, new Portion(100.0)));
        
        // 设置目标营养需求
        MealNutrients target = new MealNutrients(400.0, 50.0, 25.0, 10.0, 300.0, 500.0, 200.0, 100.0);
        
        // 打印初始状态
        System.out.println("优化前的食物及营养总和:");
        printFoodsAndNutrition(foods);
        
        // 创建优化器并进行优化
        MealOptimizer optimizer = new MealOptimizer();
        List<Food> optimizedFoods = optimizer.optimizeMeal(foods, target);
        
        // 打印优化结果
        System.out.println("\n多轮优化后的食物及营养总和:");
        printFoodsAndNutrition(optimizedFoods);
        
        // 打印目标营养需求，以便比较
        System.out.println("\n目标营养需求:");
        System.out.printf("热量: %.1f kcal, 碳水: %.1f g, 蛋白质: %.1f g, 脂肪: %.1f g\n",
                target.calories, target.carbohydrates, target.protein, target.fat);
    }
    
    /**
     * 打印食物列表及其总营养成分
     * @param foods 食物列表
     */
    private static void printFoodsAndNutrition(List<Food> foods) {
        double totalCalories = 0;
        double totalCarbs = 0;
        double totalProtein = 0;
        double totalFat = 0;
        
        for (Food food : foods) {
            System.out.printf("%s: %.0fg - 热量 %.1f kcal, 碳水 %.1f g, 蛋白质 %.1f g, 脂肪 %.1f g\n",
                    food.getName(), food.getWeight(), food.getCalories(), 
                    food.getCarbohydrates(), food.getProtein(), food.getFat());
            
            totalCalories += food.getCalories();
            totalCarbs += food.getCarbohydrates();
            totalProtein += food.getProtein();
            totalFat += food.getFat();
        }
        
        System.out.printf("\n总计: 热量 %.1f kcal, 碳水 %.1f g, 蛋白质 %.1f g, 脂肪 %.1f g\n",
                totalCalories, totalCarbs, totalProtein, totalFat);
    }
} 