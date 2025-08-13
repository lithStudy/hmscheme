package com.mealplanner.genetic.objectives.core;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.NutrientType;

import java.util.Map;

/**
 * 目标评估器基础实现类
 * 提供通用的目标评估功能
 */
public abstract class BaseObjectiveEvaluator implements ObjectiveEvaluator {
    
    protected String name;
    protected double weight;
    protected boolean isHardConstraint;
    protected double hardConstraintThreshold;
    
    /**
     * 构造函数
     * @param name 目标名称
     * @param weight 目标权重
     */
    public BaseObjectiveEvaluator(String name, double weight) {
        this.name = name;
        this.weight = weight;
        this.isHardConstraint = false;
        this.hardConstraintThreshold = 0.8;
    }
    
    /**
     * 构造函数（支持硬约束）
     * @param name 目标名称
     * @param weight 目标权重
     * @param isHardConstraint 是否为硬约束
     * @param hardConstraintThreshold 硬约束阈值
     */
    public BaseObjectiveEvaluator(String name, double weight, boolean isHardConstraint, double hardConstraintThreshold) {
        this.name = name;
        this.weight = weight;
        this.isHardConstraint = isHardConstraint;
        this.hardConstraintThreshold = hardConstraintThreshold;
    }
    
    @Override
    public abstract ObjectiveValue evaluate(MealSolution solution, Map<NutrientType, Double> targetNutrients);
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public boolean isHardConstraint() {
        return isHardConstraint;
    }
    
    @Override
    public double getHardConstraintThreshold() {
        return hardConstraintThreshold;
    }
    
    public void setHardConstraintThreshold(double hardConstraintThreshold) {
        this.hardConstraintThreshold = hardConstraintThreshold;
    }
    
    /**
     * 创建目标值对象的便捷方法
     * @param score 评分
     * @return 目标值对象
     */
    protected ObjectiveValue createObjectiveValue(double score) {
        if (isHardConstraint) {
            return new ObjectiveValue(name, score, weight, true, hardConstraintThreshold);
        } else {
            return new ObjectiveValue(name, score, weight);
        }
    }
    
    /**
     * 归一化分数到[0,1]范围
     * @param value 原始值
     * @param min 最小值
     * @param max 最大值
     * @return 归一化后的分数
     */
    protected double normalize(double value, double min, double max) {
        if (max <= min) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }
    
    /**
     * 计算与目标值的匹配度
     * @param actual 实际值
     * @param target 目标值
     * @param tolerance 容差（比例）
     * @return 匹配度分数（0-1）
     */
    protected double calculateMatchScore(double actual, double target, double tolerance) {
        if (target == 0) {
            return actual == 0 ? 1.0 : 0.0;
        }
        double ratio = actual / target;
        double deviation = Math.abs(ratio - 1.0);
        if (deviation <= tolerance) {
            return 1.0 - (deviation / tolerance) * 0.2; // 在容差内，分数为0.8-1.0
        } else {
            return Math.max(0.0, 0.8 * (1.0 / (1.0 + deviation - tolerance)));
        }
    }
}
