package com.mealplanner.genetic.objectives.core;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.NutrientType;

import java.util.Map;

/**
 * 目标评估器接口
 * 定义所有目标评估器的基本契约
 */
public interface ObjectiveEvaluator {
    
    /**
     * 评估解决方案
     * @param solution 待评估的解决方案
     * @param targetNutrients 目标营养素
     * @return 目标值
     */
    ObjectiveValue evaluate(MealSolution solution, Map<NutrientType, Double> targetNutrients);
    
    /**
     * 获取目标名称
     * @return 目标名称
     */
    String getName();
    
    /**
     * 获取目标权重
     * @return 目标权重
     */
    double getWeight();
    
    /**
     * 判断是否为硬约束目标
     * @return 是否为硬约束
     */
    default boolean isHardConstraint() {
        return false;
    }
    
    /**
     * 获取硬约束阈值
     * @return 硬约束阈值
     */
    default double getHardConstraintThreshold() {
        return 0.8;
    }
}
