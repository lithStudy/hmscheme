package com.mealplanner.genetic.objectives;

import com.mealplanner.model.UserProfile;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.NutrientType;
import java.util.*;

/**
 * 多目标评价器，评估膳食解决方案在多个目标上的表现
 */
public class MultiObjectiveEvaluator {
    // 各个目标评估器
    private List<NutrientObjective> nutrientObjectives = new ArrayList<>();
    private UserPreferenceObjective preferenceObjective;
    private FoodDiversityObjective diversityObjective;
    private NutrientBalanceObjective balanceObjective;
    
    // 评分阈值
    private double goodEnoughThreshold = 0.8;
    
    /**
     * 构造函数
     * @param userProfile 用户档案
     */
    public MultiObjectiveEvaluator(UserProfile userProfile) {
        // 初始化营养素评估器
        this.nutrientObjectives = NutrientObjective.createStandardNutrientObjectives(userProfile);
        // 初始化偏好目标评估器,用于评估食物是否符合用户偏好
        this.preferenceObjective = new UserPreferenceObjective(userProfile);
        // 初始化多样性目标评估器,用于评估食物种类的多样性
        this.diversityObjective = new FoodDiversityObjective();
        // 初始化平衡性目标评估器,用于评估营养素的平衡性
        this.balanceObjective = new NutrientBalanceObjective(userProfile);
    }
    
    /**
     * 评估解决方案在所有目标上的表现
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 目标值列表
     */
    public List<ObjectiveValue> evaluate(MealSolution solution, Map<NutrientType, Double> targetNutrients) {
        List<ObjectiveValue> objectiveValues = new ArrayList<>();
        // 评估营养素目标
        for (NutrientObjective objective : nutrientObjectives) {
            objectiveValues.add(objective.evaluate(solution, targetNutrients));
        }
        
        // 评估偏好目标
        objectiveValues.add(preferenceObjective.evaluate(solution, targetNutrients));
        
        // 评估多样性目标
        objectiveValues.add(diversityObjective.evaluate(solution, targetNutrients));
        
        // 评估平衡性目标
        objectiveValues.add(balanceObjective.evaluate(solution, targetNutrients));
        
        return objectiveValues;
    }
    
    /**
     * 计算解决方案的总体加权评分
     * @param solution 解决方案
     * @return 总体评分（0-1之间）
     */
    public double calculateOverallScore(MealSolution solution) {
        List<ObjectiveValue> objectiveValues = solution.getObjectiveValues();
        
        if (objectiveValues == null || objectiveValues.isEmpty()) {
            return 0;
        }
        
        double totalWeightedScore = 0;
        double totalWeight = 0;
        
        for (ObjectiveValue value : objectiveValues) {
            totalWeightedScore += value.getWeightedValue();
            totalWeight += value.getWeight();
        }
        
        return totalWeight > 0 ? totalWeightedScore / totalWeight : 0;
    }
    
    /**
     * 检查解决方案是否足够好
     * @param solution 解决方案
     * @return 是否足够好
     */
    public boolean isSolutionGoodEnough(MealSolution solution) {
        // 检查所有硬性约束是否满足
        for (ObjectiveValue value : solution.getObjectiveValues()) {
            if (!value.isHardConstraintSatisfied()) {
                return false;
            }
        }
        
        // 检查总体评分是否达到阈值
        double overallScore = calculateOverallScore(solution);
        return overallScore >= goodEnoughThreshold;
    }
    
    /**
     * 获取营养素目标
     * @return 营养素目标列表
     */
    public List<NutrientObjective> getNutrientObjectives() {
        return nutrientObjectives;
    }
    
    /**
     * 获取用户偏好目标
     * @return 用户偏好目标
     */
    public UserPreferenceObjective getPreferenceObjective() {
        return preferenceObjective;
    }
    
    /**
     * 获取多样性目标
     * @return 多样性目标
     */
    public FoodDiversityObjective getDiversityObjective() {
        return diversityObjective;
    }
    
    /**
     * 获取平衡目标
     * @return 平衡目标
     */
    public NutrientBalanceObjective getBalanceObjective() {
        return balanceObjective;
    }
    
    
    
    /**
     * 设置"足够好"的阈值
     * @param threshold 阈值
     */
    public void setGoodEnoughThreshold(double threshold) {
        this.goodEnoughThreshold = threshold;
    }
    
    /**
     * 获取"足够好"的阈值
     * @return 阈值
     */
    public double getGoodEnoughThreshold() {
        return goodEnoughThreshold;
    }
}