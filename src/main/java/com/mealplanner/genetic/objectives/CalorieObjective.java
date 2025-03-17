package com.mealplanner.genetic.objectives;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.objectives.NutrientScoring.CalorieScoringStrategy;
import com.mealplanner.genetic.objectives.NutrientScoring.NutrientScoringStrategy;
import com.mealplanner.model.Nutrition;
import com.mealplanner.model.NutrientType;

import java.util.Map;

/**
 * 热量目标类，专门评估解决方案的热量达成情况
 */
public class CalorieObjective extends AbstractObjectiveEvaluator {
    // 热量达成率范围
    private final double[] calorieRates;
    
    // 硬性约束阈值，确保所有解决方案必须满足更高的热量要求
    private double hardConstraintThreshold = 0.9; // 热量使用更高的阈值
    
    private final NutrientScoringStrategy scoringStrategy;
    
    /**
     * 构造函数
     * @param name 目标名称
     * @param weight 目标权重
     * @param nutrientRates 营养素达成率范围映射
     */
    public CalorieObjective(String name, double weight, Map<NutrientType, double[]> nutrientRates) {
        super(name, weight);
        this.calorieRates = nutrientRates.getOrDefault(NutrientType.CALORIES, new double[]{0.9, 1.1});
        this.scoringStrategy = new CalorieScoringStrategy();
    }
    
    @Override
    public ObjectiveValue evaluate(MealSolution solution, Nutrition targetNutrients) {
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        return evaluate(solution, actualNutrients, targetNutrients);
    }
    
    /**
     * 评估解决方案的热量达成情况
     * @param solution 解决方案
     * @param actualNutrients 实际营养素
     * @param targetNutrients 目标营养素
     * @return 目标值
     */
    public ObjectiveValue evaluate(MealSolution solution, Nutrition actualNutrients, Nutrition targetNutrients) {
        double actual = actualNutrients.getCalories();
        double target = targetNutrients.getCalories();
        
        // 计算热量得分
        double score = calculateCalorieScore(actual, target);
        
        // 创建目标值对象，使用带硬性约束的构造函数
        return new ObjectiveValue(getName(), score, getWeight(), true, hardConstraintThreshold);
    }
    
    /**
     * 计算热量匹配度评分
     * @param actual 实际热量
     * @param target 目标热量
     * @return 评分（0-1之间）
     */
    private double calculateCalorieScore(double actual, double target) {
        // 避免除以零
        if (target == 0) {
            return actual == 0 ? 1.0 : 0.0;
        }
        
        double ratio = actual / target;
        return scoringStrategy.calculateScore(ratio, calorieRates[0], calorieRates[1]);
    }
    
    /**
     * 获取硬性约束阈值
     * @return 硬性约束阈值
     */
    public double getHardConstraintThreshold() {
        return hardConstraintThreshold;
    }
    
    /**
     * 设置硬性约束阈值
     * @param hardConstraintThreshold 硬性约束阈值
     */
    public void setHardConstraintThreshold(double hardConstraintThreshold) {
        this.hardConstraintThreshold = hardConstraintThreshold;
    }
} 