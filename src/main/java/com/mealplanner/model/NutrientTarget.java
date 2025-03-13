package com.mealplanner.model;

/**
 * 营养素目标类，存储营养素的目标范围和评分参数
 */
public class NutrientTarget {
    private String name;           // 营养素名称
    private double minValue;       // 最小推荐摄入量
    private double targetValue;    // 目标摄入量
    private double maxValue;       // 最大推荐摄入量
    private double weight;         // 评分权重
    private boolean isLimitingNutrient; // 是否为限制性营养素（如钠）
    
    /**
     * 创建一个营养素目标
     * @param name 营养素名称
     * @param minValue 最小推荐摄入量
     * @param targetValue 目标摄入量
     * @param maxValue 最大推荐摄入量
     * @param weight 评分权重
     * @param isLimitingNutrient 是否为限制性营养素
     */
    public NutrientTarget(String name, double minValue, double targetValue, double maxValue, 
                         double weight, boolean isLimitingNutrient) {
        this.name = name;
        this.minValue = minValue;
        this.targetValue = targetValue;
        this.maxValue = maxValue;
        this.weight = weight;
        this.isLimitingNutrient = isLimitingNutrient;
    }
    
    /**
     * 创建一个简化的营养素目标（最小值=目标值，最大值=目标值*1.5）
     * @param name 营养素名称
     * @param targetValue 目标摄入量
     * @param weight 评分权重
     * @param isLimitingNutrient 是否为限制性营养素
     */
    public NutrientTarget(String name, double targetValue, double weight, boolean isLimitingNutrient) {
        this.name = name;
        this.targetValue = targetValue;
        this.weight = weight;
        this.isLimitingNutrient = isLimitingNutrient;
        
        if (isLimitingNutrient) {
            // 对于限制性营养素（如钠），最小值为0，最大值为目标值
            this.minValue = 0;
            this.maxValue = targetValue;
        } else {
            // 对于非限制性营养素，最小值为目标值的80%，最大值为目标值的150%
            this.minValue = targetValue * 0.8;
            this.maxValue = targetValue * 1.5;
        }
    }
    
    /**
     * 计算实际摄入量的评分
     * @param actualValue 实际摄入量
     * @return 评分（0-1之间）
     */
    public double calculateScore(double actualValue) {
        // 如果实际值在目标范围内，得分为1
        if (actualValue >= minValue && actualValue <= maxValue) {
            return 1.0;
        }
        
        if (actualValue < minValue) {
            // 不足的情况
            double ratio = actualValue / minValue;
            return ratio; // 线性惩罚
        } else {
            // 过量的情况
            double excessRatio = (actualValue - maxValue) / maxValue;
            
            if (isLimitingNutrient) {
                // 对限制性营养素（如钠）过量采用更严格的指数惩罚
                return Math.exp(-2.0 * excessRatio);
            } else {
                // 对非限制性营养素过量采用较轻的指数惩罚
                return Math.exp(-excessRatio);
            }
        }
    }
    
    /**
     * 获取加权评分
     * @param actualValue 实际摄入量
     * @return 加权评分
     */
    public double getWeightedScore(double actualValue) {
        return calculateScore(actualValue) * weight;
    }
    
    /**
     * 调整目标值（按比例调整最小值和最大值）
     * @param newTargetValue 新的目标值
     */
    public void adjustTargetValue(double newTargetValue) {
        double ratio = newTargetValue / this.targetValue;
        this.targetValue = newTargetValue;
        
        if (isLimitingNutrient) {
            // 对于限制性营养素，最小值保持为0，最大值等于目标值
            this.maxValue = newTargetValue;
        } else {
            // 对于非限制性营养素，按比例调整最小值和最大值
            this.minValue = this.minValue * ratio;
            this.maxValue = this.maxValue * ratio;
        }
    }
    
    /**
     * 按比例调整目标值
     * @param ratio 调整比例
     */
    public void adjustTargetValueByRatio(double ratio) {
        adjustTargetValue(this.targetValue * ratio);
    }
    
    // Getters
    public String getName() { return name; }
    public double getMinValue() { return minValue; }
    public double getTargetValue() { return targetValue; }
    public double getMaxValue() { return maxValue; }
    public double getWeight() { return weight; }
    public boolean isLimitingNutrient() { return isLimitingNutrient; }
    
    // Setter for weight
    public void setWeight(double weight) { this.weight = weight; }
} 