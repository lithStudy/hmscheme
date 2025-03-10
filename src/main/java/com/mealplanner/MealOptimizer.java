package com.mealplanner;

import java.util.ArrayList;
import java.util.List;

/**
 * 膳食优化器，用于对已选食物组合进行多轮优化
 */
public class MealOptimizer {
    private static final int MAX_ITERATIONS = 10; // 最大迭代次数
    private static final double CONVERGENCE_THRESHOLD = 0.01; // 收敛阈值
    
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
        
        // 记录上一轮的总体评分，用于判断收敛
        double previousScore = calculateMealScore(optimizedFoods, targetNutrients);
        
        System.out.println("开始进行多轮优化，初始评分: " + previousScore);
        
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
            
            System.out.printf("迭代 %d: 评分 %.4f (改善: %.4f)\n", 
                    iteration + 1, currentScore, currentScore - previousScore);
            
            // 检查是否收敛（分数改善很小或没有改善）
            if (Math.abs(currentScore - previousScore) < CONVERGENCE_THRESHOLD) {
                System.out.println("优化已收敛，提前结束");
                break;
            }
            
            previousScore = currentScore;
        }
        
        System.out.println("多轮优化完成，最终评分: " + calculateMealScore(optimizedFoods, targetNutrients));
        return optimizedFoods;
    }
    
    /**
     * 计算膳食的总体评分
     * @param foods 食物列表
     * @param targetNutrients 目标营养需求
     * @return 综合评分
     */
    private double calculateMealScore(List<Food> foods, MealNutrients targetNutrients) {
        // 计算所有食物的总营养成分
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
        
        // 计算各营养素的匹配度分数
        double calorieScore = scoreNutrient(totalCalories, targetNutrients.calories);
        double carbScore = scoreNutrient(totalCarbs, targetNutrients.carbohydrates);
        double proteinScore = scoreNutrient(totalProtein, targetNutrients.protein);
        double fatScore = scoreNutrient(totalFat, targetNutrients.fat);
        double calciumScore = scoreNutrient(totalCalcium, targetNutrients.calcium);
        double potassiumScore = scoreNutrient(totalPotassium, targetNutrients.potassium);
        double sodiumScore = scoreNutrient(totalSodium, targetNutrients.sodium);
        double magnesiumScore = scoreNutrient(totalMagnesium, targetNutrients.magnesium);
        
        // 计算加权平均分数
        return (calorieScore * 1.0 + 
                carbScore * 1.0 + 
                proteinScore * 1.0 + 
                fatScore * 1.0 + 
                calciumScore * 0.7 + 
                potassiumScore * 0.7 + 
                sodiumScore * 0.8 + 
                magnesiumScore * 0.7) / 6.9; // 权重总和
    }
    
    /**
     * 评分单个营养素的匹配度
     * @param actual 实际值
     * @param target 目标值
     * @return 分数（0-1之间）
     */
    private double scoreNutrient(double actual, double target) {
        if (target == 0) {
            return actual == 0 ? 1.0 : 0.0;
        }
        
        double ratio = actual / target;
        
        // 使用高斯函数计算分数，当比率接近1时分数最高
        return Math.exp(-Math.pow(ratio - 1.0, 2) / 0.5);
    }
} 