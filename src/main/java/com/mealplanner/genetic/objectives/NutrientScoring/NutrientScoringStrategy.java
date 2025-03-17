package com.mealplanner.genetic.objectives.NutrientScoring;

/**
 * 营养素评分策略接口
 */
public interface NutrientScoringStrategy {
    /**
     * 计算营养素得分
     * @param ratio 实际/目标比率
     * @param minRate 最小达成率
     * @param maxRate 最大达成率
     * @return 得分（0-1之间）
     */
    double calculateScore(double ratio, double minRate, double maxRate);
} 