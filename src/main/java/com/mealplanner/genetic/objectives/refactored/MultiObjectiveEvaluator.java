package com.mealplanner.genetic.objectives.refactored;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.objectives.core.ObjectiveEvaluator;
import com.mealplanner.model.NutrientType;
import com.mealplanner.model.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 优化后的多目标评估器
 * 简化目标结构，消除重叠，提高效率
 */
public class MultiObjectiveEvaluator {
    
    // 汇总的目标评估器
    private List<ObjectiveEvaluator> objectives;
    
    // 各个目标组
    private List<NutrientContentObjective> nutrientObjectives;
    private NutrientBalanceObjective balanceObjective;
    private FoodQualityObjective foodQualityObjective;
    private UserPreferenceObjective userPreferenceObjective;
    
    // 评分阈值
    private double goodEnoughThreshold = 0.8;
    
    /**
     * 构造函数
     * @param userProfile 用户档案
     */
    public MultiObjectiveEvaluator(UserProfile userProfile) {
        // 初始化营养素目标
        createNutrientObjectives(userProfile);
        
        // 初始化优化后的营养平衡目标
        this.balanceObjective = new NutrientBalanceObjective(userProfile, 0.3);

        // 初始化食物质量目标
        this.foodQualityObjective = new FoodQualityObjective(0.2);

        // 初始化用户偏好目标
        this.userPreferenceObjective = new UserPreferenceObjective(userProfile, 0.2);

        //汇总
        this.objectives = new ArrayList<>();
        this.objectives.addAll(nutrientObjectives);
        this.objectives.add(balanceObjective);
        this.objectives.add(foodQualityObjective);
        this.objectives.add(userPreferenceObjective);

        System.out.println("优化后的多目标评估器已初始化");
        System.out.println("目标总数: " + objectives.size());
    }
    
    /**
     * 创建营养素目标
     */
    private void createNutrientObjectives(UserProfile userProfile) {
        this.nutrientObjectives = new ArrayList<>();
        // 为主要营养素分配更高权重
        for (NutrientType nutrient : NutrientContentObjective.mainNutrients) {
            NutrientContentObjective objective = new NutrientContentObjective(nutrient, userProfile, 0.06);
            nutrientObjectives.add(objective);
            objectives.add(objective);
        }
        
        // 为微量营养素分配较低权重
        for (NutrientType nutrient : NutrientContentObjective.microNutrients) {
            NutrientContentObjective objective = new NutrientContentObjective(nutrient, userProfile, 0.02);
            nutrientObjectives.add(objective);
            objectives.add(objective);
        }
    }
    
    /**
     * 评估解决方案在所有目标上的表现
     */
    public List<ObjectiveValue> evaluate(MealSolution solution, Map<NutrientType, Double> targetNutrients) {
        List<ObjectiveValue> objectiveValues = new ArrayList<>();
        
        for (ObjectiveEvaluator objective : objectives) {
            objectiveValues.add(objective.evaluate(solution, targetNutrients));
        }
        
        return objectiveValues;
    }
    
    /**
     * 计算解决方案的总体加权评分
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
     */
    public boolean isSolutionGoodEnough(MealSolution solution) {
        // 首先检查安全性
        if (!userPreferenceObjective.isSafe(solution)) {
            return false;
        }
        
        // 检查其他硬性约束
        for (ObjectiveValue value : solution.getObjectiveValues()) {
            if (value.isHardConstraint() && !value.isHardConstraintSatisfied()) {
                return false;
            }
        }
        
        // 检查总体评分
        double overallScore = calculateOverallScore(solution);
        return overallScore >= goodEnoughThreshold;
    }
    
    /**
     * 获取目标统计信息
     */
    public void printObjectiveStatistics() {
        System.out.println("=== 目标统计信息 ===");
        
        double totalWeight = 0;
        int hardConstraintCount = 0;
        
        for (ObjectiveEvaluator objective : objectives) {
            totalWeight += objective.getWeight();
            if (objective.isHardConstraint()) {
                hardConstraintCount++;
            }
        }
        
        System.out.println("目标总数: " + objectives.size());
        System.out.println("硬约束目标数: " + hardConstraintCount);
        System.out.println("软约束目标数: " + (objectives.size() - hardConstraintCount));
        System.out.println("权重总和: " + Math.round(totalWeight * 100) / 100.0);
        
        // 计算权重分布
        double nutrientWeight = 0;
        for (NutrientContentObjective objective : nutrientObjectives) {
            nutrientWeight += objective.getWeight();
        }
        
        System.out.println("\n权重分布:");
        System.out.println("- 营养素目标权重总和: " + Math.round(nutrientWeight * 100) / 100.0);
        System.out.println("- 营养平衡目标权重: " + Math.round(balanceObjective.getWeight() * 100) / 100.0);
        System.out.println("- 食物质量目标权重: " + Math.round(foodQualityObjective.getWeight() * 100) / 100.0);
        System.out.println("- 用户偏好目标权重: " + Math.round(userPreferenceObjective.getWeight() * 100) / 100.0);
    }
    
    /**
     * 自定义目标权重
     */
    public void customizeWeights() {
        // 营养平衡目标的子维度权重
        balanceObjective.setDimensionWeights(0.5, 0.3, 0.2);
        
        // 食物质量目标的子维度权重
        foodQualityObjective.setDimensionWeights(0.4, 0.4, 0.2);
        
        // 用户偏好目标的子维度权重
        userPreferenceObjective.setDimensionWeights(0.6, 0.25, 0.15);
    }
    
    /**
     * 设置阈值
     */
    public void setGoodEnoughThreshold(double threshold) {
        this.goodEnoughThreshold = threshold;
    }
    
    public double getGoodEnoughThreshold() {
        return goodEnoughThreshold;
    }
    
    /**
     * 添加自定义目标
     */
    public void addCustomObjective(ObjectiveEvaluator objective) {
        objectives.add(objective);
    }
    
    /**
     * 移除目标
     */
    public void removeObjective(ObjectiveEvaluator objective) {
        objectives.remove(objective);
    }
    
    /**
     * 获取所有目标
     */
    public List<ObjectiveEvaluator> getObjectives() {
        return new ArrayList<>(objectives);
    }
    
    public UserPreferenceObjective getUserPreferenceObjective() {
        return userPreferenceObjective;
    }
    
}
