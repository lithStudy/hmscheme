package com.mealplanner.genetic;


import com.mealplanner.NutritionCalculator;
import com.mealplanner.foodmanage.NutritionDataParser;
import com.mealplanner.genetic.algorithm.NSGAIIMealPlanner;
import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.util.NSGAIIConfiguration;
import com.mealplanner.genetic.util.NutrientObjectiveConfig;
import com.mealplanner.model.Food;
import com.mealplanner.model.Nutrition;
import com.mealplanner.model.UserProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * NSGA-II多目标遗传算法膳食规划演示类
 */
public class GeneticMealPlannerDemo {
    
    public static void main(String[] args) {
        System.out.println("NSGA-II多目标遗传算法膳食规划演示");
        System.out.println("====================================");
        
        // 加载食物数据库
        List<Food> foodDatabase = loadFoodDatabase();
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
        NutritionCalculator nutritionCalculator = new NutritionCalculator(userProfile);
        Nutrition dailyNeeds = nutritionCalculator.calculateDailyNutrientNeeds();
        //一餐的目标摄入量
        Nutrition targetNutrients=dailyNeeds.scale(0.35);
        
        // 创建NSGA-II膳食规划器
        NSGAIIMealPlanner planner = new NSGAIIMealPlanner(config, foodDatabase, userProfile);

        // 配置营养素达成率范围，考虑用户的健康状况
        NutrientObjectiveConfig.configureNutrientAchievementRates(planner, userProfile);
        
        // 执行算法
        System.out.println("\n开始执行NSGA-II多目标遗传算法...");
        List<MealSolution> solutions = planner.generateMeal(targetNutrients, true);
        
        // 显示结果
        displayResults(solutions, targetNutrients, planner);
        
        // 交互式展示选定的解决方案
        interactiveResultsDisplay(solutions, targetNutrients, planner);
    }
    
    /**
     * 加载食物数据库
     * @return 食物列表
     */
    private static List<Food> loadFoodDatabase() {
        List<Food> foodDatabase = new ArrayList<>();
        
        NutritionDataParser parser = new NutritionDataParser();
        try {
            foodDatabase = parser.convertToFoodObjects(parser.parseNutritionDataFromFile("src/main/resources/all.xlsx"));
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
        
        // // 获取用户输入 
        // Scanner scanner = new Scanner(System.in);
        
        // System.out.println("\n创建用户档案");
        // System.out.println("--------------------");
        
        // System.out.print("姓名（默认：测试用户）：");
        // name = scanner.nextLine().trim();
        // if (name.isEmpty()) name = "测试用户";
        
        // System.out.print("性别（M/F）（默认：M）：");
        // gender = scanner.nextLine().trim().toUpperCase();
        // if (!gender.equals("M") && !gender.equals("F")) gender = "M";
        
        // System.out.print("年龄（默认：30）：");
        // String ageStr = scanner.nextLine().trim();
        // if (!ageStr.isEmpty()) {
        //     try {
        //         age = Integer.parseInt(ageStr);
        //     } catch (NumberFormatException e) {
        //         System.out.println("使用默认年龄：30");
        //     }
        // }
        
        // System.out.print("身高(cm)（默认：170）：");
        // String heightStr = scanner.nextLine().trim();
        // if (!heightStr.isEmpty()) {
        //     try {
        //         height = Integer.parseInt(heightStr);
        //     } catch (NumberFormatException e) {
        //         System.out.println("使用默认身高：170cm");
        //     }
        // }
        
        // System.out.print("体重(kg)（默认：65）：");
        // String weightStr = scanner.nextLine().trim();
        // if (!weightStr.isEmpty()) {
        //     try {
        //         weight = Integer.parseInt(weightStr);
        //     } catch (NumberFormatException e) {
        //         System.out.println("使用默认体重：65kg");
        //     }
        // }
        
        // System.out.print("活动水平（1-5，1=久坐，5=高强度）（默认：3）：");
        // String activityStr = scanner.nextLine().trim();
        // if (!activityStr.isEmpty()) {
        //     try {
        //         activity = Integer.parseInt(activityStr);
        //         if (activity < 1 || activity > 5) activity = 3;
        //     } catch (NumberFormatException e) {
        //         System.out.println("使用默认活动水平：3");
        //     }
        // }
        
        System.out.println("\n您的用户档案已创建：");
        System.out.println("姓名：" + name);
        System.out.println("性别：" + (gender.equals("M") ? "男" : "女"));
        System.out.println("年龄：" + age);
        System.out.println("身高：" + height + "cm");
        System.out.println("体重：" + weight + "kg");
        System.out.println("活动水平系数：" + activity);
        
        return new UserProfile(weight, height,age,gender, activity,new String[]{"hypertension", "diabetes"});

    }
    
    /**
     * 选择算法配置
     * @return 算法配置
     */
    private static NSGAIIConfiguration selectConfiguration() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n选择算法配置");
        System.out.println("--------------------");
        System.out.println("1. 小型配置（速度快，质量较低）");
        System.out.println("2. 标准配置（速度和质量平衡）");
        System.out.println("3. 大型配置（速度慢，质量高）");
        System.out.print("请选择（默认：3）：");
        
        //默认选择大型配置  
        String choice = "3";
        // String choice = scanner.nextLine().trim();
        
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
     * 创建目标营养素
     * @return 目标营养素
     */
    private static Nutrition createTargetNutrients() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n设置目标营养素（针对一餐）");
        System.out.println("--------------------");
        System.out.println("请按回车使用默认值，或输入新值");
        
        System.out.print("热量(kcal)（默认：600）：");
        String caloriesStr = scanner.nextLine().trim();
        double calories = 600;
        if (!caloriesStr.isEmpty()) {
            try {
                calories = Double.parseDouble(caloriesStr);
            } catch (NumberFormatException e) {
                System.out.println("使用默认值：600kcal");
            }
        }
        
        System.out.print("碳水化合物(g)（默认：75）：");
        String carbsStr = scanner.nextLine().trim();
        double carbs = 75;
        if (!carbsStr.isEmpty()) {
            try {
                carbs = Double.parseDouble(carbsStr);
            } catch (NumberFormatException e) {
                System.out.println("使用默认值：75g");
            }
        }
        
        System.out.print("蛋白质(g)（默认：25）：");
        String proteinStr = scanner.nextLine().trim();
        double protein = 25;
        if (!proteinStr.isEmpty()) {
            try {
                protein = Double.parseDouble(proteinStr);
            } catch (NumberFormatException e) {
                System.out.println("使用默认值：25g");
            }
        }
        
        System.out.print("脂肪(g)（默认：20）：");
        String fatStr = scanner.nextLine().trim();
        double fat = 20;
        if (!fatStr.isEmpty()) {
            try {
                fat = Double.parseDouble(fatStr);
            } catch (NumberFormatException e) {
                System.out.println("使用默认值：20g");
            }
        }
        
        // 微量元素使用默认值
        double calcium = 300;    // 钙
        double potassium = 1000; // 钾
        double sodium = 800;     // 钠
        double magnesium = 120;  // 镁
        
        System.out.println("\n目标营养素已设置：");
        System.out.println("热量：" + calories + " kcal");
        System.out.println("碳水化合物：" + carbs + " g");
        System.out.println("蛋白质：" + protein + " g");
        System.out.println("脂肪：" + fat + " g");
        System.out.println("钙：" + calcium + " mg");
        System.out.println("钾：" + potassium + " mg");
        System.out.println("钠：" + sodium + " mg");
        System.out.println("镁：" + magnesium + " mg");
        
        return new Nutrition(calories, carbs, protein, fat, calcium, potassium, sodium, magnesium);
    }
    
    /**
     * 显示结果
     * @param solutions 解决方案列表
     * @param targetNutrients 目标营养素
     * @param planner 膳食规划器
     */
    private static void displayResults(List<MealSolution> solutions, Nutrition targetNutrients, NSGAIIMealPlanner planner) {
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
    private static void displaySolutionSummary(MealSolution solution, Nutrition targetNutrients, NSGAIIMealPlanner planner) {
        // 获取营养素达成率阈值
        // double minRate = planner.getMinNutrientAchievementRate() * 100; // 转换为百分比
        // double maxRate = planner.getMaxNutrientAchievementRate() * 100; // 转换为百分比
        
        // 计算并显示平均营养素达成率得分
        double nutrientScore = planner.calculateSolutionNutrientScore(solution, targetNutrients);
        System.out.println("平均营养素达成率得分: " + String.format("%.2f", nutrientScore) + 
                         " (" + String.format("%.1f", nutrientScore * 100) + "%)");
        
        // 显示食物列表
        System.out.println("食物列表：");
        for (FoodGene gene : solution.getFoodGenes()) {
            System.out.println("  - " + gene.getFood().getName() + 
                             " (" + String.format("%d", (int)gene.getIntake()) + "g), " +
                             "类别: " + gene.getFood().getCategory());
        }
        
        // 显示营养素总值和目标比较
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        System.out.println("\n营养素比较 (实际 / 目标)：");
        
        // 计算并展示所有营养素的达成率
        double caloriesAchievement = actualNutrients.getCalories() / targetNutrients.getCalories() * 100;
        double carbsAchievement = actualNutrients.getCarbohydrates() / targetNutrients.getCarbohydrates() * 100;
        double proteinAchievement = actualNutrients.getProtein() / targetNutrients.getProtein() * 100;
        double fatAchievement = actualNutrients.getFat() / targetNutrients.getFat() * 100;
        double calciumAchievement = actualNutrients.getCalcium() / targetNutrients.getCalcium() * 100;
        double potassiumAchievement = actualNutrients.getPotassium() / targetNutrients.getPotassium() * 100;
        double sodiumAchievement = actualNutrients.getSodium() / targetNutrients.getSodium() * 100;
        double magnesiumAchievement = actualNutrients.getMagnesium() / targetNutrients.getMagnesium() * 100;
        
        // 获取各营养素的达成率范围
        double[] caloriesRange = planner.getNutrientAchievementRate("calories");
        double[] carbsRange = planner.getNutrientAchievementRate("carbohydrates");
        double[] proteinRange = planner.getNutrientAchievementRate("protein");
        double[] fatRange = planner.getNutrientAchievementRate("fat");
        double[] calciumRange = planner.getNutrientAchievementRate("calcium");
        double[] potassiumRange = planner.getNutrientAchievementRate("potassium");
        double[] sodiumRange = planner.getNutrientAchievementRate("sodium");
        double[] magnesiumRange = planner.getNutrientAchievementRate("magnesium");
        
        // 热量
        System.out.println("  热量: " + String.format("%.1f", actualNutrients.getCalories()) + 
                         " / " + String.format("%.1f", targetNutrients.getCalories()) + " kcal, " +
                         "达成率: " + String.format("%.1f%%", caloriesAchievement) + 
                         formatAchievementStatus(caloriesAchievement, caloriesRange[0] * 100, caloriesRange[1] * 100));
        
        // 碳水化合物
        System.out.println("  碳水: " + String.format("%.1f", actualNutrients.getCarbohydrates()) + 
                         " / " + String.format("%.1f", targetNutrients.getCarbohydrates()) + " g, " +
                         "达成率: " + String.format("%.1f%%", carbsAchievement) + 
                         formatAchievementStatus(carbsAchievement, carbsRange[0] * 100, carbsRange[1] * 100));
        
        // 蛋白质
        System.out.println("  蛋白质: " + String.format("%.1f", actualNutrients.getProtein()) + 
                         " / " + String.format("%.1f", targetNutrients.getProtein()) + " g, " +
                         "达成率: " + String.format("%.1f%%", proteinAchievement) + 
                         formatAchievementStatus(proteinAchievement, proteinRange[0] * 100, proteinRange[1] * 100));
        
        // 脂肪
        System.out.println("  脂肪: " + String.format("%.1f", actualNutrients.getFat()) + 
                         " / " + String.format("%.1f", targetNutrients.getFat()) + " g, " +
                         "达成率: " + String.format("%.1f%%", fatAchievement) + 
                         formatAchievementStatus(fatAchievement, fatRange[0] * 100, fatRange[1] * 100));
        
        // 钙
        System.out.println("  钙: " + String.format("%.1f", actualNutrients.getCalcium()) + 
                         " / " + String.format("%.1f", targetNutrients.getCalcium()) + " mg, " +
                         "达成率: " + String.format("%.1f%%", calciumAchievement) + 
                         formatAchievementStatus(calciumAchievement, calciumRange[0] * 100, calciumRange[1] * 100));
        
        // 钾
        System.out.println("  钾: " + String.format("%.1f", actualNutrients.getPotassium()) + 
                         " / " + String.format("%.1f", targetNutrients.getPotassium()) + " mg, " +
                         "达成率: " + String.format("%.1f%%", potassiumAchievement) + 
                         formatAchievementStatus(potassiumAchievement, potassiumRange[0] * 100, potassiumRange[1] * 100));
        
        // 钠
        System.out.println("  钠: " + String.format("%.1f", actualNutrients.getSodium()) + 
                         " / " + String.format("%.1f", targetNutrients.getSodium()) + " mg, " +
                         "达成率: " + String.format("%.1f%%", sodiumAchievement) + 
                         formatAchievementStatus(sodiumAchievement, sodiumRange[0] * 100, sodiumRange[1] * 100));
        
        // 镁
        System.out.println("  镁: " + String.format("%.1f", actualNutrients.getMagnesium()) + 
                         " / " + String.format("%.1f", targetNutrients.getMagnesium()) + " mg, " +
                         "达成率: " + String.format("%.1f%%", magnesiumAchievement) + 
                         formatAchievementStatus(magnesiumAchievement, magnesiumRange[0] * 100, magnesiumRange[1] * 100));
        
        // 计算三大营养素的热量比例
        double carbsCalories = actualNutrients.getCarbohydrates() * 4;
        double proteinCalories = actualNutrients.getProtein() * 4;
        double fatCalories = actualNutrients.getFat() * 9;
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
    
    /**
     * 格式化达成率状态（使用默认阈值）
     * @param achievement 达成率百分比
     * @return 状态描述
     */
    private static String formatAchievementStatus(double achievement) {
        return formatAchievementStatus(achievement, 70, 130);
    }
    
    /**
     * 交互式显示结果
     * @param solutions 解决方案列表
     * @param targetNutrients 目标营养素
     * @param planner 膳食规划器
     */
    private static void interactiveResultsDisplay(List<MealSolution> solutions, Nutrition targetNutrients, NSGAIIMealPlanner planner) {
        if (solutions.isEmpty()) {
            return;
        }
        
        Scanner scanner = new Scanner(System.in);
        int currentIndex = 0;
        
        while (true) {
            System.out.println("\n====================================");
            System.out.println("当前解决方案：#" + (currentIndex + 1) + " / " + solutions.size());
            displaySolutionSummary(solutions.get(currentIndex), targetNutrients, planner);
            
            System.out.println("\n操作选项：");
            System.out.println("1. 查看下一个解决方案");
            System.out.println("2. 查看上一个解决方案");
            System.out.println("3. 查看详细目标值");
            System.out.println("4. 退出");
            System.out.print("请选择：");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    // 下一个解决方案
                    currentIndex = (currentIndex + 1) % solutions.size();
                    break;
                case "2":
                    // 上一个解决方案
                    currentIndex = (currentIndex - 1 + solutions.size()) % solutions.size();
                    break;
                case "3":
                    // 显示详细目标值
                    displayDetailedObjectives(solutions.get(currentIndex));
                    break;
                case "4":
                    // 退出
                    System.out.println("感谢使用NSGA-II多目标遗传算法膳食规划演示！");
                    return;
                default:
                    System.out.println("无效选择，请重新输入。");
            }
        }
    }
    
    /**
     * 显示详细目标值
     * @param solution 解决方案
     */
    private static void displayDetailedObjectives(MealSolution solution) {
        System.out.println("\n详细目标值：");
        System.out.println("====================================");
        
        for (ObjectiveValue objective : solution.getObjectiveValues()) {
            System.out.println(objective.getName() + ":");
            System.out.println("  值: " + String.format("%.4f", objective.getValue()));
            System.out.println("  权重: " + String.format("%.2f", objective.getWeight()));
            System.out.println("  加权值: " + String.format("%.4f", objective.getWeightedValue()));
            System.out.println("  是否硬性约束: " + (objective.isHardConstraint() ? "是" : "否"));
            if (objective.isHardConstraint()) {
                System.out.println("  硬性约束阈值: " + String.format("%.2f", objective.getHardConstraintThreshold()));
                System.out.println("  约束是否满足: " + (objective.isHardConstraintSatisfied() ? "是" : "否"));
            }
            System.out.println();
        }
        
        System.out.println("按回车键继续...");
        new Scanner(System.in).nextLine();
    }
} 