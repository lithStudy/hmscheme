package com.mealplanner.model;

public class NutrientLimit {
    private double minValue; // 最小值
    private double maxValue; // 最大值

    public NutrientLimit(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public double getMinValue() { return minValue; }
    public double getMaxValue() { return maxValue; }
    
    /**
     * 检查值是否在限制范围内
     * @param value 要检查的值
     * @return 是否在范围内
     */
    public boolean isWithinLimit(double value) {
        return value >= minValue && value <= maxValue;
    }
    
    /**
     * 计算值与限制的偏差程度
     * @param value 要检查的值
     * @return 偏差分数（0表示在范围内，负值表示偏离范围，绝对值越大偏离越严重）
     */
    public double calculateDeviationScore(double value) {
        if (value < minValue) {
            // 避免除以零
            if (minValue == 0) {
                return -1.0; // 如果最小值为0，直接返回-1表示严重偏离
            }
            return -1.0 * (minValue - value) / minValue; // 归一化的负偏差
        } else if (value > maxValue) {
            // 避免除以零
            if (maxValue == 0) {
                return -1.0; // 如果最大值为0，直接返回-1表示严重偏离
            }
            return -1.0 * (value - maxValue) / maxValue; // 归一化的负偏差
        } else {
            return 0.0; // 在范围内
        }
    }
}
