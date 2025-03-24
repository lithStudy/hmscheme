package com.mealplanner;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mealplanner.export.MealSolutionExcelExporter;
import com.mealplanner.foodmanage.NutritionDataParser;
import com.mealplanner.genetic.algorithm.NSGAIIMealPlanner;
import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.util.NSGAIIConfiguration;
import com.mealplanner.model.Food;
import com.mealplanner.model.FoodCategory;
import com.mealplanner.model.HealthConditionType;
import com.mealplanner.model.NutrientType;
import com.mealplanner.model.UserProfile;

/**
 * NSGA-II多目标遗传算法膳食规划演示类
 */
public class GeneticMealPlannerDemo {
    
    public static void main(String[] args) {
        System.out.println("NSGA-II多目标遗传算法膳食规划演示");
        System.out.println("====================================");
        
        // 加载食物数据库
        List<Food> foodDatabase = loadFoodDatabase();
        // 过滤掉水果、油脂、糕点类别的食物
        foodDatabase.removeIf(food -> food.getCategory() == FoodCategory.FRUIT 
            || food.getCategory() == FoodCategory.OIL
            || food.getCategory()==FoodCategory.PASTRY
            || food.getCategory()==FoodCategory.MILK);
        if (foodDatabase.isEmpty()) {
            System.out.println("无法加载食物数据库，程序退出。");
            return;
        }
        
        System.out.println("成功加载食物数据库，共包含 " + foodDatabase.size() + " 种食物。");
        
        // 创建用户档案
        UserProfile userProfile = createUserProfile();
        
        // 创建算法配置
        NSGAIIConfiguration config = selectConfiguration();
        
        // 为每餐分配营养需求
        Map<NutrientType, Double> dailyNeeds = NutrientType.getDailyIntakes(userProfile);
        //一餐的目标摄入量
        Map<NutrientType, Double> targetNutrients= dailyNeeds.entrySet().stream()
        .collect(Collectors.toMap(
            entry -> entry.getKey(),
            entry -> entry.getValue() * 0.35
        ));
        
        // 创建NSGA-II膳食规划器
        NSGAIIMealPlanner planner = new NSGAIIMealPlanner(config, foodDatabase, userProfile);
        
        // 执行算法
        System.out.println("\n开始执行NSGA-II多目标遗传算法...");
        List<MealSolution> solutions = planner.generateMeal(targetNutrients, true);
        
        //导出到excel
        MealSolutionExcelExporter.export(solutions, targetNutrients,userProfile);
        
        // 显示结果
        displayResults(solutions, targetNutrients, planner);
    }
    
    /**
     * 加载食物数据库
     * @return 食物列表
     */
    private static List<Food> loadFoodDatabase() {
        List<Food> foodDatabase = new ArrayList<>();
        
        NutritionDataParser parser = new NutritionDataParser();
        try {
            foodDatabase = parser.convertToFoodObjects(parser.parseNutritionDataFromFile("src/main/resources/foods.xlsx"));
        } catch (IOException e) {
            System.err.println("加载食物数据库失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        return foodDatabase;
    }
    
    /**
     * 创建用户档案
     * @return 用户档案
     */
    private static UserProfile createUserProfile() {
        //默认用户信息
        String name = "测试用户";
        String gender = "M";
        int age = 30;
        int height = 170;
        int weight = 65;
        double activity = 1.55;
        
        
        System.out.println("\n您的用户档案已创建：");
        System.out.println("姓名：" + name);
        System.out.println("性别：" + (gender.equals("M") ? "男" : "女"));
        System.out.println("年龄：" + age);
        System.out.println("身高：" + height + "cm");
        System.out.println("体重：" + weight + "kg");
        System.out.println("活动水平系数：" + activity);
        
        return new UserProfile(weight, height,age,gender, activity,new HealthConditionType[]{HealthConditionType.HYPERTENSION, HealthConditionType.DIABETES});

    }
    
    /**
     * 选择算法配置
     * @return 算法配置
     */
    private static NSGAIIConfiguration selectConfiguration() {
        //默认选择大型配置  
        String choice = "3";
        
        switch (choice) {
            case "1":
                System.out.println("已选择小型配置");
                return NSGAIIConfiguration.createSmallConfiguration();
            case "2":
                System.out.println("已选择标准配置");
                return NSGAIIConfiguration.createStandardConfiguration();
            case "3":
            default:
                System.out.println("已选择大型配置");
                return NSGAIIConfiguration.createLargeConfiguration();
        }
    }
    
    /**
     * 显示结果
     * @param solutions 解决方案列表
     * @param targetNutrients 目标营养素
     * @param planner 膳食规划器
     */
    private static void displayResults(List<MealSolution> solutions, Map<NutrientType, Double> targetNutrients, NSGAIIMealPlanner planner) {
        System.out.println("\n算法执行完成");
        System.out.println("====================================");
        System.out.println("找到 " + solutions.size() + " 个帕累托最优解");
        
        if (solutions.isEmpty()) {
            System.out.println("没有找到可行的解决方案。");
            return;
        }
        
        // 显示前3个解决方案的简要信息
        int displayCount = Math.min(3, solutions.size());
        for (int i = 0; i < displayCount; i++) {
            MealSolution solution = solutions.get(i);
            System.out.println("\n解决方案 #" + (i + 1) + ":");
            displaySolutionSummary(solution, targetNutrients, planner);
        }
    }
    
    /**
     * 显示解决方案摘要
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @param planner 膳食规划器
     */
    private static void displaySolutionSummary(MealSolution solution, Map<NutrientType, Double> targetNutrients, NSGAIIMealPlanner planner) {
        // 获取营养素达成率阈值
        // double minRate = planner.getMinNutrientAchievementRate() * 100; // 转换为百分比
        // double maxRate = planner.getMaxNutrientAchievementRate() * 100; // 转换为百分比
        
        // 计算并显示平均营养素达成率得分
        double nutrientScore = planner.calculateAverageObjectiveScore(solution);
        System.out.println("平均得分: " + String.format("%.2f", nutrientScore) + 
                         " (" + String.format("%.1f", nutrientScore * 100) + "%)");
        
        // 显示食物列表
        System.out.println("食物列表：");
        for (FoodGene gene : solution.getFoodGenes()) {
            System.out.println("  - " + gene.getFood().getName() + 
                             " (" + String.format("%d", (int)gene.getIntake()) + "g), " +
                             "类别: " + gene.getFood().getCategory());
        }
        
        // 显示营养素总值和目标比较
        Map<NutrientType, Double> actualNutrients = solution.calculateTotalNutrients();
        System.out.println("\n营养素比较 (实际 / 目标)：");
        
        // 获取各营养素的达成率范围
        Map<NutrientType, double[]> nutrientRates = NutrientType.getNutrientRates(planner.getUserProfile());
        
        // // 定义需要显示的营养素列表
        // List<NutrientType> nutrientsToDisplay = Arrays.asList(
        //     NutrientType.CALORIES,
        //     NutrientType.CARBOHYDRATES,
        //     NutrientType.PROTEIN,
        //     NutrientType.FAT,
        //     NutrientType.CALCIUM,
        //     NutrientType.POTASSIUM,
        //     NutrientType.SODIUM,
        //     NutrientType.MAGNESIUM,
        //     NutrientType.IRON,
        //     NutrientType.PHOSPHORUS,
        //     NutrientType.ZINC,
        //     NutrientType.VITAMIN_A,
        //     NutrientType.VITAMIN_C,
        //     NutrientType.VITAMIN_D,
        //     NutrientType.VITAMIN_E
        // );

        // 遍历显示每种营养素的信息
        for (NutrientType nutrientType : NutrientType.values()) {
            String unit = nutrientType.getUnit();
            String name = nutrientType.getDisplayName();
            
            double actualValue = actualNutrients.get(nutrientType);
            double targetValue = targetNutrients.get(nutrientType);
            
            double achievement = targetValue > 0 ? (actualValue / targetValue * 100) : 0;
            double[] range = nutrientRates.get(nutrientType);
            
            System.out.println(String.format("  %s: %.1f / %.1f %s, 达成率: %.1f%% %s",
                name,
                actualValue,
                targetValue,
                unit,
                achievement,
                formatAchievementStatus(achievement, range[0] * 100, range[1] * 100)
            ));
        }
        // 计算三大营养素的热量比例
        double carbsCalories = actualNutrients.get(NutrientType.CARBOHYDRATES) * 4;
        double proteinCalories = actualNutrients.get(NutrientType.PROTEIN) * 4;
        double fatCalories = actualNutrients.get(NutrientType.FAT) * 9;
        double totalMacroCalories = carbsCalories + proteinCalories + fatCalories;
        
        if (totalMacroCalories > 0) {
            System.out.println("\n三大营养素热量比例：");
            System.out.println("  碳水: " + String.format("%.1f%%", carbsCalories / totalMacroCalories * 100));
            System.out.println("  蛋白质: " + String.format("%.1f%%", proteinCalories / totalMacroCalories * 100));
            System.out.println("  脂肪: " + String.format("%.1f%%", fatCalories / totalMacroCalories * 100));
        }
        
        // 显示目标值摘要
        System.out.println("\n目标评分：");
        for (ObjectiveValue objective : solution.getObjectiveValues()) {
            System.out.println("  " + objective.getName() + ": " + 
                             String.format("%.3f", objective.getValue()));
        }
    }
    
    /**
     * 格式化达成率状态
     * @param achievement 达成率百分比
     * @param minRate 最低达成率阈值（百分比形式）
     * @param maxRate 最高达成率阈值（百分比形式）
     * @return 状态描述
     */
    private static String formatAchievementStatus(double achievement, double minRate, double maxRate) {
        if (achievement < minRate) {
            return " [不足]";
        } else if (achievement > maxRate) {
            return " [过量]";
        } else {
            return " [达标]";
        }
    }
    
} 