package com.mealplanner.genetic.objectives.NutrientScoring;

/**
 * 默认营养素评分策略
 * 在范围内使用距离中心点的线性插值
 * 范围外使用平方根函数进行惩罚
 */
public class DefaultNutrientScoringStrategy implements NutrientScoringStrategy {
    
    @Override
    public double calculateScore(double ratio, double minRate, double maxRate) {
        // 在范围内部，根据距离中心点的距离给予0.8-1.0的分数
        if (ratio >= minRate && ratio <= maxRate) {
            double centerPoint = (minRate + maxRate) / 2;
            double maxDistance = (maxRate - minRate) / 2;
            double distance = Math.abs(ratio - centerPoint);
            return 1.0 - (distance / maxDistance) * 0.2;
        }
        
        // 如果低于最小达成率，使用平方根函数使分数下降更平缓
        if (ratio < minRate) {
            return Math.max(0, 0.8 * Math.sqrt(ratio / minRate));
        }
        
        // 如果超过最大达成率，使用线性衰减
        return Math.max(0, 0.8 * maxRate / ratio);
    }
} 