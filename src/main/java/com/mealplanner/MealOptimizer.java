package com.mealplanner;

import java.util.ArrayList;
import java.util.List;

import com.mealplanner.dietary.DietaryEvaluator;
import com.mealplanner.logging.MealPlannerLogger;
import com.mealplanner.nutrition.NutrientRangeAdjuster;

/**
 * 膳食优化器，用于对已选食物组合进行多轮优化
 */
public class MealOptimizer {
    private static final int MAX_ITERATIONS = 10; // 最大迭代次数
    private static final double CONVERGENCE_THRESHOLD = 0.01; // 收敛阈值
    
    // 新增属性
    private UserProfile userProfile; // 用户档案
    private double preferenceWeight = 0.3; // 偏好因素权重
    private DietaryEvaluator dietaryEvaluator; // 饮食评估器
    private NutrientRangeAdjuster nutrientAdjuster; // 营养素范围调整器
    
    /**
     * 创建膳食优化器
     */
    public MealOptimizer() {
        this.userProfile = null;
        this.dietaryEvaluator = null;
        this.nutrientAdjuster = new NutrientRangeAdjuster();
    }
    
    /**
     * 创建膳食优化器（带用户档案）
     * @param userProfile 用户档案
     */
    public MealOptimizer(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.dietaryEvaluator = new DietaryEvaluator(userProfile);
        this.nutrientAdjuster = new NutrientRangeAdjuster();
    }
    
    /**
     * 设置用户档案
     * @param userProfile 用户档案
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        if (userProfile != null) {
            this.dietaryEvaluator = new DietaryEvaluator(userProfile);
        } else {
            this.dietaryEvaluator = null;
        }
    }
    
    /**
     * 优化一餐的食物摄入量
     * @param foods 已选食物列表
     * @param targetNutrients 目标营养需求
     * @return 优化后的食物列表
     */
    public List<Food> optimizeMeal(List<Food> foods, MealNutrients targetNutrients) {
        if (foods.isEmpty()) {
            return foods;
        }
        
        // 复制一份食物列表，以免修改原始列表
        List<Food> optimizedFoods = new ArrayList<>(foods);
        
        // 重置营养素调整器
        nutrientAdjuster.reset();
        
        // 记录上一轮的总体评分，用于判断收敛
        double previousScore = calculateMealScore(optimizedFoods, targetNutrients);
        
        // 使用Logger开始优化日志
        MealPlannerLogger.startOptimization(previousScore);
        
        // 多轮优化
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // 逐个调整每种食物的摄入量
            for (int i = 0; i < optimizedFoods.size(); i++) {
                Food food = optimizedFoods.get(i);
                
                // 获取当前食物的摄入量范围
                IntakeRange range = food.getRecommendedIntakeRange();
                
                // 尝试不同的摄入量，找到最优值
                double bestIntake = food.getPortion().getWeight();
                double bestScore = previousScore;
                
                // 在范围内以5%的步长尝试不同摄入量
                double step = (range.getMaxIntake() - range.getMinIntake()) / 20.0;
                for (double intake = range.getMinIntake(); intake <= range.getMaxIntake(); intake += step) {
                    // 创建新的食物对象，使用当前尝试的摄入量
                    Food tempFood = food.withIntake(intake);
                    
                    // 临时替换食物列表中的对象
                    List<Food> tempFoods = new ArrayList<>(optimizedFoods);
                    tempFoods.set(i, tempFood);
                    
                    // 计算新的总体评分
                    double score = calculateMealScore(tempFoods, targetNutrients);
                    
                    // 记录详细的食物评分日志
                    MealPlannerLogger.foodScored(food.getName() + " (摄入量 " + intake + "g)", score);
                    
                    // 如果找到更好的摄入量，则记录下来
                    if (score > bestScore) {
                        bestScore = score;
                        bestIntake = intake;
                    }
                }
                
                // 使用找到的最佳摄入量更新食物
                optimizedFoods.set(i, food.withIntake(bestIntake));
            }
            
            // 计算当前总体评分
            double currentScore = calculateMealScore(optimizedFoods, targetNutrients);
            double improvement = currentScore - previousScore;
            
            // 使用Logger记录迭代日志
            MealPlannerLogger.optimizationIteration(iteration + 1, currentScore, improvement);
            
            // 如果分数没有显著改善，考虑放宽营养素要求
            if (Math.abs(improvement) < CONVERGENCE_THRESHOLD) {
                if (nutrientAdjuster.increaseDeviation()) {
                    // 使用Logger记录放宽营养素要求
                    MealPlannerLogger.nutrientRequirementRelaxed(nutrientAdjuster.getCurrentDeviation());
                    // 继续优化
                } else {
                    MealPlannerLogger.info("优化已收敛，提前结束");
                    break;
                }
            }
            
            previousScore = currentScore;
        }
        
        // 使用Logger记录优化完成
        double finalScore = calculateMealScore(optimizedFoods, targetNutrients);
        MealPlannerLogger.completeOptimization(finalScore, nutrientAdjuster.getCurrentDeviation());
        
        // 添加营养分析
        MealNutrients actualNutrients = calculateTotalNutrients(optimizedFoods);
        MealPlannerLogger.nutrientAnalysis(actualNutrients, targetNutrients);
        
        return optimizedFoods;
    }
    
    /**
     * 计算食物列表的总营养成分
     * @param foods 食物列表
     * @return 总营养成分
     */
    private MealNutrients calculateTotalNutrients(List<Food> foods) {
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
        
        return new MealNutrients(
            totalCalories,
            totalCarbs,
            totalProtein,
            totalFat,
            totalCalcium,
            totalPotassium,
            totalSodium,
            totalMagnesium
        );
    }
    
    /**
     * 计算膳食的总体评分
     * @param foods 食物列表
     * @param targetNutrients 目标营养需求
     * @return 综合评分
     */
    private double calculateMealScore(List<Food> foods, MealNutrients targetNutrients) {
        // 计算所有食物的总营养成分
        MealNutrients actualNutrients = calculateTotalNutrients(foods);
        
        // 计算各营养素的匹配度分数，考虑当前的偏差容忍度
        double calorieScore = scoreNutrientWithTolerance(actualNutrients.calories, targetNutrients.calories);
        double carbScore = scoreNutrientWithTolerance(actualNutrients.carbohydrates, targetNutrients.carbohydrates);
        double proteinScore = scoreNutrientWithTolerance(actualNutrients.protein, targetNutrients.protein);
        double fatScore = scoreNutrientWithTolerance(actualNutrients.fat, targetNutrients.fat);
        double calciumScore = scoreNutrientWithTolerance(actualNutrients.calcium, targetNutrients.calcium);
        double potassiumScore = scoreNutrientWithTolerance(actualNutrients.potassium, targetNutrients.potassium);
        double sodiumScore = scoreNutrientWithTolerance(actualNutrients.sodium, targetNutrients.sodium);
        double magnesiumScore = scoreNutrientWithTolerance(actualNutrients.magnesium, targetNutrients.magnesium);
        
        // 计算营养素匹配度的加权平均分数
        double nutritionScore = (calorieScore * 1.0 + 
                carbScore * 1.0 + 
                proteinScore * 1.0 + 
                fatScore * 1.0 + 
                calciumScore * 0.7 + 
                potassiumScore * 0.7 + 
                sodiumScore * 0.8 + 
                magnesiumScore * 0.7) / 6.9; // 权重总和
        
        // 如果没有饮食评估器，直接返回营养素匹配度分数
        if (dietaryEvaluator == null) {
            return nutritionScore;
        }
        
        // 计算用户偏好匹配度分数
        double preferenceScore = calculatePreferenceScore(foods);
        
        // 综合考虑营养素匹配度和用户偏好
        return nutritionScore * (1 - preferenceWeight) + preferenceScore * preferenceWeight;
    }
    
    /**
     * 评分单个营养素的匹配度，考虑偏差容忍度
     * @param actual 实际值
     * @param target 目标值
     * @return 分数（0-1之间）
     */
    private double scoreNutrientWithTolerance(double actual, double target) {
        if (target == 0) {
            return actual == 0 ? 1.0 : 0.0;
        }
        
        // 获取当前允许的偏差范围
        double currentDeviation = nutrientAdjuster.getCurrentDeviation();
        
        // 计算比率
        double ratio = actual / target;
        
        // 计算比率与理想比率1.0的偏差
        double deviation = Math.abs(ratio - 1.0);
        
        // 如果偏差在允许范围内，给予较高分数
        if (deviation <= currentDeviation) {
            // 根据偏差程度给予分数，偏差越小分数越高
            return 1.0 - (deviation / currentDeviation) * 0.2;
        } else {
            // 使用高斯函数计算偏差较大时的分数，形成平滑过渡
            double variance = 0.5 + currentDeviation * 0.5; // 根据当前偏差调整方差
            return Math.exp(-Math.pow(ratio - 1.0, 2) / variance);
        }
    }
    
    /**
     * 计算食物组合与用户偏好的匹配度
     * @param foods 食物列表
     * @return 偏好匹配度分数（0-1之间）
     */
    private double calculatePreferenceScore(List<Food> foods) {
        if (dietaryEvaluator == null || foods.isEmpty()) {
            return 1.0; // 如果没有饮食评估器或食物列表为空，返回最高分
        }
        
        double totalScore = 0.0;
        
        for (Food food : foods) {
            // 使用饮食评估器计算食物的偏好评分
            double foodScore = dietaryEvaluator.calculatePreferenceScore(food);
            totalScore += foodScore;
            
            // 记录每种食物的偏好评分
            MealPlannerLogger.foodScored(food.getName() + " (偏好评分)", foodScore);
        }
        
        // 计算平均分数并确保在0-1范围内
        return Math.min(1.0, totalScore / foods.size());
    }
    
    /**
     * 设置偏好因素权重
     * @param weight 权重值（0-1之间）
     */
    public void setPreferenceWeight(double weight) {
        this.preferenceWeight = Math.max(0.0, Math.min(1.0, weight));
    }
    
    /**
     * 获取偏好因素权重
     * @return 权重值
     */
    public double getPreferenceWeight() {
        return preferenceWeight;
    }
    
    /**
     * 获取饮食评估器
     * @return 饮食评估器
     */
    public DietaryEvaluator getDietaryEvaluator() {
        return dietaryEvaluator;
    }
    
    /**
     * 获取营养素范围调整器
     * @return 营养素范围调整器
     */
    public NutrientRangeAdjuster getNutrientAdjuster() {
        return nutrientAdjuster;
    }
    
    /**
     * 设置营养素范围调整器
     * @param adjuster 营养素范围调整器
     */
    public void setNutrientAdjuster(NutrientRangeAdjuster adjuster) {
        this.nutrientAdjuster = adjuster != null ? adjuster : new NutrientRangeAdjuster();
    }
} 