package com.mealplanner.genetic.model;

/**
 * 表示多目标优化问题中的一个目标值
 */
public class ObjectiveValue {
    // 目标名称
    private String name;
    
    // 目标值（越大越好）
    private double value;
    
    // 目标权重
    private double weight;
    
    // 是否为硬性约束（必须满足的目标）
    private boolean isHardConstraint;
    
    // 硬性约束的阈值
    private double hardConstraintThreshold;
    
    /**
     * 构造函数
     * @param name 目标名称
     * @param value 目标值
     * @param weight 目标权重
     */
    public ObjectiveValue(String name, double value, double weight) {
        this.name = name;
        this.value = value;
        this.weight = weight;
        this.isHardConstraint = false;
        this.hardConstraintThreshold = 0;
    }
    
    /**
     * 构造函数（带硬性约束）
     * @param name 目标名称
     * @param value 目标值
     * @param weight 目标权重
     * @param isHardConstraint 是否为硬性约束
     * @param hardConstraintThreshold 硬性约束阈值
     */
    public ObjectiveValue(String name, double value, double weight, boolean isHardConstraint, double hardConstraintThreshold) {
        this.name = name;
        this.value = value;
        this.weight = weight;
        this.isHardConstraint = isHardConstraint;
        this.hardConstraintThreshold = hardConstraintThreshold;
    }
    
    /**
     * 创建对象的拷贝
     * @return 对象拷贝
     */
    public ObjectiveValue copy() {
        return new ObjectiveValue(
                this.name,
                this.value,
                this.weight,
                this.isHardConstraint,
                this.hardConstraintThreshold
        );
    }
    
    /**
     * 检查硬性约束是否满足
     * @return 是否满足
     */
    public boolean isHardConstraintSatisfied() {
        if (!isHardConstraint) {
            return true; // 不是硬性约束，总是满足
        }
        
        return value >= hardConstraintThreshold;
    }
    
    /**
     * 获取加权值
     * @return 加权后的目标值
     */
    public double getWeightedValue() {
        return value * weight;
    }
    
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
     * 获取目标值
     * @return 目标值
     */
    public double getValue() {
        return value;
    }
    
    /**
     * 设置目标值
     * @param value 目标值
     */
    public void setValue(double value) {
        this.value = value;
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
    
    /**
     * 检查是否为硬性约束
     * @return 是否为硬性约束
     */
    public boolean isHardConstraint() {
        return isHardConstraint;
    }
    
    /**
     * 设置是否为硬性约束
     * @param hardConstraint 是否为硬性约束
     */
    public void setHardConstraint(boolean hardConstraint) {
        this.isHardConstraint = hardConstraint;
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
    
    @Override
    public String toString() {
        return "ObjectiveValue[" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", weight=" + weight +
                ", isHardConstraint=" + isHardConstraint +
                ", hardConstraintThreshold=" + hardConstraintThreshold +
                ']';
    }
} 