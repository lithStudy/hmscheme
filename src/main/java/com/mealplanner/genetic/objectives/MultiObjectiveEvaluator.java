package com.mealplanner.genetic.objectives;

import com.mealplanner.model.UserProfile;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.Nutrition;
import java.util.*;

/**
 * 多目标评价器，评估膳食解决方案在多个目标上的表现
 */
public class MultiObjectiveEvaluator {
    // 各个目标评估器
    private List<NutrientObjective> nutrientObjectives = new ArrayList<>();
    private PreferenceObjective preferenceObjective;
    private DiversityObjective diversityObjective;
    private BalanceObjective balanceObjective;
    
    // 其他目标权重
    private double preferenceWeight = 0.2;
    private double diversityWeight = 0.2;
    private double balanceWeight = 0.2;
    
    // 评分阈值
    private double goodEnoughThreshold = 0.8;
    
    /**
     * 构造函数
     * @param userProfile 用户档案
     */
    public MultiObjectiveEvaluator(UserProfile userProfile) {
        // 初始化偏好目标评估器,用于评估食物是否符合用户偏好
        this.preferenceObjective = new PreferenceObjective(userProfile);
        // 初始化多样性目标评估器,用于评估食物种类的多样性
        this.diversityObjective = new DiversityObjective();
        // 初始化平衡性目标评估器,用于评估营养素的平衡性
        this.balanceObjective = new BalanceObjective();
        // 初始化营养元素评估器，用于评估每个营养元素成分的合理性
        this.nutrientObjectives = NutrientObjective.createStandardNutrientObjectives(userProfile);
    }
    
    /**
     * 评估解决方案在所有目标上的表现
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 目标值列表
     */
    public List<ObjectiveValue> evaluate(MealSolution solution, Nutrition targetNutrients) {
        List<ObjectiveValue> objectiveValues = new ArrayList<>();
        
        // 评估营养素目标
        for (NutrientObjective objective : nutrientObjectives) {
            ObjectiveValue value = objective.evaluate(solution, targetNutrients);
            objectiveValues.add(value);
        }
        
        // 评估用户偏好目标
        ObjectiveValue preferenceValue = preferenceObjective.evaluate(solution, targetNutrients);
        preferenceValue.setWeight(preferenceWeight);
        objectiveValues.add(preferenceValue);
        
        // 评估多样性目标
        ObjectiveValue diversityValue = diversityObjective.evaluate(solution, targetNutrients);
        diversityValue.setWeight(diversityWeight);
        objectiveValues.add(diversityValue);
        
        // 评估平衡目标
        ObjectiveValue balanceValue = balanceObjective.evaluate(solution, targetNutrients);
        balanceValue.setWeight(balanceWeight);
        objectiveValues.add(balanceValue);
        
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
            if (value.isHardConstraint() && !value.isHardConstraintSatisfied()) {
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
    public PreferenceObjective getPreferenceObjective() {
        return preferenceObjective;
    }
    
    /**
     * 获取多样性目标
     * @return 多样性目标
     */
    public DiversityObjective getDiversityObjective() {
        return diversityObjective;
    }
    
    /**
     * 获取平衡目标
     * @return 平衡目标
     */
    public BalanceObjective getBalanceObjective() {
        return balanceObjective;
    }
    
    
    /**
     * 设置用户偏好权重
     * @param weight 权重
     */
    public void setPreferenceWeight(double weight) {
        this.preferenceWeight = weight;
        if (preferenceObjective != null) {
            preferenceObjective.setWeight(weight);
        }
    }
    
    /**
     * 设置多样性权重
     * @param weight 权重
     */
    public void setDiversityWeight(double weight) {
        this.diversityWeight = weight;
        if (diversityObjective != null) {
            diversityObjective.setWeight(weight);
        }
    }
    
    /**
     * 设置平衡权重
     * @param weight 权重
     */
    public void setBalanceWeight(double weight) {
        this.balanceWeight = weight;
        if (balanceObjective != null) {
            balanceObjective.setWeight(weight);
        }
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