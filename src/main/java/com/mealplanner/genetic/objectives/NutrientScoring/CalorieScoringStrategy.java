package com.mealplanner.genetic.objectives.NutrientScoring;

/**
 * 热量评分策略
 * 在范围内使用距离中心点的线性插值
 * 范围外使用更严格的二次函数和指数衰减进行惩罚
 */
public class CalorieScoringStrategy implements NutrientScoringStrategy {
    
    @Override
    public double calculateScore(double ratio, double minRate, double maxRate) {
        // 在范围内部，根据距离中心点的距离给予0.8-1.0的分数
        if (ratio >= minRate && ratio <= maxRate) {
            double centerPoint = (minRate + maxRate) / 2;
            double maxDistance = (maxRate - minRate) / 2;
            double distance = Math.abs(ratio - centerPoint);
            return 1.0 - (distance / maxDistance) * 0.2;
        }
        
        // 如果低于最小达成率，使用二次函数快速降低分数
        if (ratio < minRate) {
            double normalizedRatio = ratio / minRate;
            return Math.max(0, 0.8 * normalizedRatio * normalizedRatio);
        }
        
        // 如果超过最大达成率，使用指数衰减快速降低分数
        return Math.max(0, 0.8 * Math.exp(-(ratio - maxRate) / maxRate));
    }
} 