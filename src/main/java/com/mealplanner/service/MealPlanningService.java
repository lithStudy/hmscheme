package com.mealplanner.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mealplanner.config.AppConfig;
import com.mealplanner.export.MealSolutionExcelExporter;
import com.mealplanner.foodmanage.NutritionDataParser;
import com.mealplanner.genetic.algorithm.NSGAIIMealPlanner;
import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.objectives.refactored.MultiObjectiveEvaluator;
import com.mealplanner.genetic.util.NSGAIIConfiguration;
import com.mealplanner.genetic.util.NSGAIILogger;
import com.mealplanner.model.Food;
import com.mealplanner.model.FoodCategory;
import com.mealplanner.model.HealthConditionType;
import com.mealplanner.model.NutrientType;
import com.mealplanner.model.UserProfile;

/**
 * 膳食规划服务类
 * 负责协调整个膳食规划流程，包括：
 * - 加载食物数据库
 * - 创建用户档案
 * - 配置优化后的目标评估器
 * - 执行NSGA-II多目标遗传算法
 * - 导出和展示结果
 */
public class MealPlanningService {
    
    // 优化后的多目标评估器
    private MultiObjectiveEvaluator objectiveEvaluator;

    /**
     * 运行膳食规划服务的主方法
     * 执行完整的膳食规划流程
     */
    public void run() {
        // 1. 加载食物数据库
        List<Food> foodDatabase = loadFoodDatabase(AppConfig.FOODS_EXCEL);
        
        // 2. 根据配置排除特定食物类别（如水果、油类等）
        Set<FoodCategory> excluded = AppConfig.EXCLUDED_CATEGORIES;
        if (!excluded.isEmpty()) {
            foodDatabase.removeIf(food -> excluded.contains(food.getCategory()));
        }
        if (foodDatabase.isEmpty()) {
            System.out.println("无法加载食物数据库，程序退出。");
            return;
        }
        System.out.println("成功加载食物数据库，共包含 " + foodDatabase.size() + " 种食物。");

        // 3. 根据配置创建用户档案
        UserProfile userProfile = createUserProfileFromConfig();

        // 4. 创建并配置优化后的目标评估器
        this.objectiveEvaluator = new MultiObjectiveEvaluator(userProfile);
        configureObjectiveEvaluator();

        // 5. 选择算法配置档案（small/standard/large）
        NSGAIIConfiguration algoConfig = selectConfiguration(AppConfig.ALGO_PROFILE);
        Long seed = AppConfig.RANDOM_SEED;
        if (seed != null) {
            algoConfig.setRandomSeed(seed);
        }

        // 6. 创建NSGA-II膳食规划器
        NSGAIIMealPlanner planner = new NSGAIIMealPlanner(algoConfig, foodDatabase, userProfile);
        
        // 7. 配置日志记录器设置
        NSGAIILogger logger = planner.getLogger();
        logger.setLogLevel(AppConfig.LOG_LEVEL);
        logger.setVerboseObjectives(AppConfig.LOG_VERBOSE_OBJECTIVES);
        logger.setVerbosePopulation(AppConfig.LOG_VERBOSE_POPULATION);

        // 9. 计算单餐营养目标（将每日需求按餐次分摊）
        Map<NutrientType, Double> dailyNeeds = NutrientType.getDailyIntakes(userProfile);
        double ratio = 1.0 / Math.max(1, AppConfig.MEALS_PER_DAY);
        Map<NutrientType, Double> targetNutrients = dailyNeeds.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() * ratio));

        // 10. 显示目标评估器统计信息
        System.out.println("\n=== 目标评估器配置信息 ===");
        objectiveEvaluator.printObjectiveStatistics();

        // 11. 执行NSGA-II多目标遗传算法
        boolean requireStaple = AppConfig.REQUIRE_STAPLE;
        System.out.println("\n开始执行NSGA-II多目标遗传算法...");
        
        // 使用优化后的评估器运行算法
        List<MealSolution> solutions = planner.generateMeal(targetNutrients, requireStaple);

        // 12. 解决方案已经使用优化后的评估器评估，无需重新评估

        // 13. 导出结果到Excel文件
        MealSolutionExcelExporter.export(solutions, targetNutrients, userProfile, AppConfig.EXPORT_EXCEL);

        // 14. 在控制台展示结果
        displayResults(solutions, targetNutrients, planner);
    }

    /**
     * 从Excel文件加载食物数据库
     * @param excelPath Excel文件路径
     * @return 食物对象列表
     */
    private List<Food> loadFoodDatabase(String excelPath) {
        List<Food> foodDatabase = new ArrayList<>();
        NutritionDataParser parser = new NutritionDataParser();
        try {
            // 解析Excel文件并转换为Food对象
            foodDatabase = parser.convertToFoodObjects(parser.parseNutritionDataFromFile(excelPath));
        } catch (IOException e) {
            System.err.println("加载食物数据库失败：" + e.getMessage());
            e.printStackTrace();
        }
        return foodDatabase;
    }

    /**
     * 根据配置文件创建用户档案
     * @return 用户档案对象
     */
    private UserProfile createUserProfileFromConfig() {
        // 从配置获取用户基本信息
        String gender = AppConfig.USER_GENDER;
        int age = AppConfig.USER_AGE;
        int height = AppConfig.USER_HEIGHT_CM;
        int weight = AppConfig.USER_WEIGHT_KG;
        double activity = AppConfig.USER_ACTIVITY;  // 活动水平系数（1.2~1.9）
        HealthConditionType[] conditions = AppConfig.USER_HEALTH_CONDITIONS;

        // 显示用户档案信息
        System.out.println("\n您的用户档案已创建：");
        System.out.println("性别：" + (gender.equalsIgnoreCase("M") || gender.equalsIgnoreCase("male") ? "男" : "女"));
        System.out.println("年龄：" + age);
        System.out.println("身高：" + height + "cm");
        System.out.println("体重：" + weight + "kg");
        System.out.println("活动水平系数：" + activity);

        return new UserProfile(weight, height, age, gender, activity, conditions);
    }

    /**
     * 配置优化后的目标评估器
     */
    private void configureObjectiveEvaluator() {
        // 使用内置的权重配置
        objectiveEvaluator.customizeWeights();
        
        // 设置评分阈值（使用默认值0.8）
        objectiveEvaluator.setGoodEnoughThreshold(0.8);
        
        System.out.println("目标评估器配置完成");
    }



    /**
     * 显示优化后的目标评分详情
     * @param objectiveValues 目标值列表
     */
    private void displayOptimizedObjectiveScores(List<ObjectiveValue> objectiveValues) {
        System.out.println("  详细目标评分:");
        
        // 按类别分组显示
        List<ObjectiveValue> nutrientObjectives = new ArrayList<>();
        ObjectiveValue balanceObjective = null;
        ObjectiveValue qualityObjective = null;
        ObjectiveValue preferenceObjective = null;
        
        for (ObjectiveValue value : objectiveValues) {
            String name = value.getName();
            if (name.contains("nutrient") || name.contains("CALORIES") || name.contains("PROTEIN") || 
                name.contains("FAT") || name.contains("CARBOHYDRATES") || name.contains("FIBER") ||
                name.contains("CALCIUM") || name.contains("IRON") || name.contains("VITAMIN") || 
                name.contains("SODIUM")) {
                nutrientObjectives.add(value);
            } else if (name.contains("balance")) {
                balanceObjective = value;
            } else if (name.contains("quality")) {
                qualityObjective = value;
            } else if (name.contains("preference")) {
                preferenceObjective = value;
            }
        }
        
        // 显示营养素目标（简化显示）
        if (!nutrientObjectives.isEmpty()) {
            double avgNutrientScore = nutrientObjectives.stream()
                .mapToDouble(ObjectiveValue::getValue).average().orElse(0.0);
            System.out.println("    营养素目标平均分: " + Math.round(avgNutrientScore * 1000) / 1000.0);
        }
        
        // 显示其他主要目标
        if (balanceObjective != null) {
            System.out.println("    营养平衡得分: " + Math.round(balanceObjective.getValue() * 1000) / 1000.0 +
                             (balanceObjective.isHardConstraint() ? " [硬约束]" : ""));
        }
        
        if (qualityObjective != null) {
            System.out.println("    食物质量得分: " + Math.round(qualityObjective.getValue() * 1000) / 1000.0);
        }
        
        if (preferenceObjective != null) {
            System.out.println("    用户偏好得分: " + Math.round(preferenceObjective.getValue() * 1000) / 1000.0 +
                             (preferenceObjective.isHardConstraint() ? " [硬约束]" : ""));
        }
    }

    /**
     * 根据配置档案名称选择对应的算法配置
     * @param profile 配置档案名称（small/standard/large）
     * @return NSGA-II算法配置对象
     */
    private NSGAIIConfiguration selectConfiguration(String profile) {
        switch (profile) {
            case "small":
                System.out.println("已选择小型配置");
                return NSGAIIConfiguration.createSmallConfiguration();
            case "standard":
                System.out.println("已选择标准配置");
                return NSGAIIConfiguration.createStandardConfiguration();
            case "large":
            default:
                System.out.println("已选择大型配置");
                return NSGAIIConfiguration.createLargeConfiguration();
        }
    }

    /**
     * 在控制台显示算法运行结果
     * @param solutions 帕累托最优解列表
     * @param targetNutrients 目标营养素需求
     * @param planner NSGA-II规划器实例
     */
    private void displayResults(List<MealSolution> solutions, Map<NutrientType, Double> targetNutrients, NSGAIIMealPlanner planner) {
        System.out.println("\n算法执行完成");
        System.out.println("====================================");
        System.out.println("找到 " + solutions.size() + " 个帕累托最优解");
        
        if (solutions.isEmpty()) {
            System.out.println("没有找到可行的解决方案。");
            return;
        }
        
        // 显示解决方案质量统计
        displaySolutionQualityStatistics(solutions, planner);
        
        // 显示前3个最优解的详细信息
        int displayCount = Math.min(3, solutions.size());
        for (int i = 0; i < displayCount; i++) {
            MealSolution solution = solutions.get(i);
            System.out.println("\n解决方案 #" + (i + 1) + ":");
            displaySolutionSummary(solution, targetNutrients, planner);
        }
    }

    /**
     * 显示解决方案质量统计信息
     * @param solutions 解决方案列表
     * @param planner NSGA-II规划器实例
     */
    private void displaySolutionQualityStatistics(List<MealSolution> solutions, NSGAIIMealPlanner planner) {
        int safeCount = 0;
        int goodEnoughCount = 0;
        double avgScore = 0.0;
        
        MultiObjectiveEvaluator evaluator = planner.getObjectiveEvaluator();
        
        for (MealSolution solution : solutions) {
            // 检查安全性
            if (evaluator.getUserPreferenceObjective().isSafe(solution)) {
                safeCount++;
            }
            
            // 检查是否足够好
            if (evaluator.isSolutionGoodEnough(solution)) {
                goodEnoughCount++;
            }
            
            // 累计评分
            avgScore += evaluator.calculateOverallScore(solution);
        }
        
        avgScore /= solutions.size();
        
        System.out.println("\n解决方案质量统计:");
        System.out.println("- 安全的解决方案: " + safeCount + "/" + solutions.size() + 
                          " (" + Math.round((double)safeCount / solutions.size() * 100) + "%)");
        System.out.println("- 达标的解决方案: " + goodEnoughCount + "/" + solutions.size() + 
                          " (" + Math.round((double)goodEnoughCount / solutions.size() * 100) + "%)");
        System.out.println("- 平均得分: " + Math.round(avgScore * 1000) / 1000.0);
    }

    /**
     * 显示单个解决方案的详细摘要信息
     * @param solution 膳食解决方案
     * @param targetNutrients 目标营养素需求
     * @param planner NSGA-II规划器实例
     */
    private void displaySolutionSummary(MealSolution solution, Map<NutrientType, Double> targetNutrients, NSGAIIMealPlanner planner) {
        // 1. 显示总体评分
        MultiObjectiveEvaluator evaluator = planner.getObjectiveEvaluator();
        double score = evaluator.calculateOverallScore(solution);
        boolean isGoodEnough = evaluator.isSolutionGoodEnough(solution);
        boolean isSafe = evaluator.getUserPreferenceObjective().isSafe(solution);
        
        System.out.println("总体得分: " + Math.round(score * 1000) / 1000.0 +
                " (" + Math.round(score * 100) + "%)");
        System.out.println("安全性: " + (isSafe ? "安全" : "存在风险"));
        System.out.println("质量评级: " + (isGoodEnough ? "优秀" : "需要改进"));

        // 2. 显示食物列表和摄入量
        System.out.println("\n食物列表：");
        displayFoodList(solution);

        // 3. 显示营养素达成情况对比
        displayNutrientComparison(solution, targetNutrients, planner);

        // 4. 显示三大营养素热量占比
        displayMacroNutrientRatio(solution);

        // 5. 显示目标评分详情
        System.out.println("\n目标评分：");
        displayOptimizedObjectiveScores(solution.getObjectiveValues());
        
        // 6. 显示食物质量分析
        displayFoodQualityAnalysis(solution);
    }

    /**
     * 显示食物列表
     */
    private void displayFoodList(MealSolution solution) {
        Map<FoodCategory, List<FoodGene>> foodsByCategory = solution.getFoodGenes().stream()
            .collect(Collectors.groupingBy(gene -> gene.getFood().getCategory()));
        
        for (Map.Entry<FoodCategory, List<FoodGene>> entry : foodsByCategory.entrySet()) {
            System.out.println("  " + entry.getKey() + ":");
            for (FoodGene gene : entry.getValue()) {
                System.out.println("    - " + gene.getFood().getName() +
                        " (" + Math.round(gene.getIntake()) + "g)");
            }
        }
    }

    /**
     * 显示营养素对比
     */
    private void displayNutrientComparison(MealSolution solution, Map<NutrientType, Double> targetNutrients, NSGAIIMealPlanner planner) {
        Map<NutrientType, Double> actualNutrients = solution.calculateTotalNutrients();
        System.out.println("\n营养素达成情况:");

        // 只显示主要营养素
        NutrientType[] mainNutrients = {
            NutrientType.CALORIES, NutrientType.PROTEIN, NutrientType.FAT, 
            NutrientType.CARBOHYDRATES, NutrientType.FIBER, NutrientType.CALCIUM,
            NutrientType.IRON, NutrientType.VITAMIN_C, NutrientType.SODIUM
        };

        Map<NutrientType, double[]> nutrientRates = NutrientType.getNutrientRates(planner.getUserProfile());
        for (NutrientType nutrientType : mainNutrients) {
            String unit = nutrientType.getUnit();
            String name = nutrientType.getDisplayName();
            double actualValue = actualNutrients.get(nutrientType);
            double targetValue = targetNutrients.get(nutrientType);
            double achievement = targetValue > 0 ? (actualValue / targetValue * 100) : 0;
            double[] range = nutrientRates.get(nutrientType);
            
            System.out.println("  " + name + ": " + Math.round(actualValue * 10) / 10.0 + 
                    "/" + Math.round(targetValue * 10) / 10.0 + unit + 
                    " (" + Math.round(achievement) + "%" + 
                    formatAchievementStatus(achievement, range[0] * 100, range[1] * 100) + ")");
        }
    }

    /**
     * 显示宏量营养素比例
     */
    private void displayMacroNutrientRatio(MealSolution solution) {
        Map<NutrientType, Double> actualNutrients = solution.calculateTotalNutrients();
        double carbsCalories = actualNutrients.get(NutrientType.CARBOHYDRATES) * 4;
        double proteinCalories = actualNutrients.get(NutrientType.PROTEIN) * 4;
        double fatCalories = actualNutrients.get(NutrientType.FAT) * 9;
        double totalMacroCalories = carbsCalories + proteinCalories + fatCalories;
        
        if (totalMacroCalories > 0) {
            System.out.println("\n宏量营养素热量比例：");
            System.out.println("  碳水: " + Math.round(carbsCalories / totalMacroCalories * 100) + "%");
            System.out.println("  蛋白质: " + Math.round(proteinCalories / totalMacroCalories * 100) + "%");
            System.out.println("  脂肪: " + Math.round(fatCalories / totalMacroCalories * 100) + "%");
        }
    }

    /**
     * 显示食物质量分析
     */
    private void displayFoodQualityAnalysis(MealSolution solution) {
        System.out.println("\n食物质量分析:");
        
        // 类别多样性分析
        Map<FoodCategory, Long> categoryCount = solution.getFoodGenes().stream()
            .collect(Collectors.groupingBy(gene -> gene.getFood().getCategory(), Collectors.counting()));
        
        System.out.println("  类别覆盖: " + categoryCount.size() + " 种类别");
        System.out.println("  类别分布: " + categoryCount.entrySet().stream()
            .map(entry -> entry.getKey() + "(" + entry.getValue() + ")")
            .collect(Collectors.joining(", ")));
        
        // 食物重复性检查
        Map<String, Long> foodCount = solution.getFoodGenes().stream()
            .collect(Collectors.groupingBy(gene -> gene.getFood().getName(), Collectors.counting()));
        
        long repeatedFoods = foodCount.values().stream().filter(count -> count > 1).count();
        if (repeatedFoods > 0) {
            System.out.println("  重复食物: " + repeatedFoods + " 种");
        } else {
            System.out.println("  食物多样性: 良好（无重复）");
        }
    }

    /**
     * 格式化营养素达成状态显示
     * @param achievement 实际达成率
     * @param minRate 最低达成率要求
     * @param maxRate 最高达成率限制
     * @return 格式化的状态标识
     */
    private String formatAchievementStatus(double achievement, double minRate, double maxRate) {
        if (achievement < minRate) {
            return " [不足]";
        } else if (achievement > maxRate) {
            return " [过量]";
        } else {
            return " [达标]";
        }
    }

    /**
     * 获取目标评估器
     * @return 目标评估器
     */
    public MultiObjectiveEvaluator getObjectiveEvaluator() {
        return objectiveEvaluator;
    }

    /**
     * 解决方案质量报告类
     */
    public static class SolutionQualityReport {
        private List<Double> scores = new ArrayList<>();
        private int safeCount = 0;
        private int goodEnoughCount = 0;
        
        public void addSolution(double score, boolean isSafe, boolean isGoodEnough) {
            scores.add(score);
            if (isSafe) safeCount++;
            if (isGoodEnough) goodEnoughCount++;
        }
        
        public double getAverageScore() {
            return scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
        
        public double getBestScore() {
            return scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        }
        
        public int getTotalSolutions() {
            return scores.size();
        }
        
        public int getSafeCount() {
            return safeCount;
        }
        
        public int getGoodEnoughCount() {
            return goodEnoughCount;
        }
        
        public double getSafetyRate() {
            return getTotalSolutions() > 0 ? (double) safeCount / getTotalSolutions() : 0.0;
        }
        
        public double getQualityRate() {
            return getTotalSolutions() > 0 ? (double) goodEnoughCount / getTotalSolutions() : 0.0;
        }
        
        public void printReport() {
            System.out.println("=== 解决方案质量报告 ===");
            System.out.println("总解决方案数: " + getTotalSolutions());
            System.out.println("平均得分: " + Math.round(getAverageScore() * 1000) / 1000.0);
            System.out.println("最高得分: " + Math.round(getBestScore() * 1000) / 1000.0);
            System.out.println("安全率: " + Math.round(getSafetyRate() * 100) + "%");
            System.out.println("优秀率: " + Math.round(getQualityRate() * 100) + "%");
        }
    }
}

