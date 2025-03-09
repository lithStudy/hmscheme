package com.mealplanner;

public class Main {
    public static void main(String[] args) {
        // 创建用户档案
        UserProfile userProfile = new UserProfile(
            70.0,   // 体重(kg)
            170.0,  // 身高(cm)
            30,     // 年龄
            "male", // 性别
            1.55,   // 活动系数（中度活动）
            new String[]{"hypertension", "diabetes"} // 健康状况
        );

        // 创建膳食规划器
        MealPlanner mealPlanner = new MealPlanner(userProfile);

        // 生成每日膳食计划
        DailyMealPlan dailyPlan = mealPlanner.generateDailyMealPlan();

        // 打印膳食计划
        System.out.println(dailyPlan.toString());

        // 计算营养需求
        NutritionCalculator calculator = new NutritionCalculator(userProfile);
        DailyNutrientNeeds needs = calculator.calculateDailyNutrientNeeds();

        // 打印营养需求
        System.out.println("\n每日营养需求:");
        System.out.printf("总热量: %.0f kcal\n", needs.getTotalCalories());
        System.out.printf("碳水化合物: %.0f g\n", needs.getCarbohydrates());
        System.out.printf("蛋白质: %.0f g\n", needs.getProtein());
        System.out.printf("脂肪: %.0f g\n", needs.getFat());
        System.out.printf("钙: %.0f mg\n", needs.getCalcium());
        System.out.printf("钾: %.0f mg\n", needs.getPotassium());
        System.out.printf("钠: %.0f mg\n", needs.getSodium());
        System.out.printf("镁: %.0f mg\n", needs.getMagnesium());
    }
} 