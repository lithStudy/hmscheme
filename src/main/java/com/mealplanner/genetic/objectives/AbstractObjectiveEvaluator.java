package com.mealplanner.genetic.objectives;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.Nutrition;

/**
 * 目标评估器抽象类，定义评估膳食解决方案的基本方法和通用实现
 */
public abstract class AbstractObjectiveEvaluator {
    
    // 目标名称
    protected String name;
    
    // 目标权重
    protected double weight;
    
    /**
     * 构造函数
     * @param name 目标名称
     * @param weight 目标权重
     */
    public AbstractObjectiveEvaluator(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }
    
    /**
     * 评估解决方案
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 目标值
     */
    public abstract ObjectiveValue evaluate(MealSolution solution, Nutrition targetNutrients);
    
    /**
     * 获取目标名称
     * @return 目标名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置目标名称
     * @param name 目标名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取目标权重
     * @return 目标权重
     */
    public double getWeight() {
        return weight;
    }
    
    /**
     * 设置目标权重
     * @param weight 目标权重
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
} 