package com.mealplanner.genetic.objectives.util;

import java.util.Map;

/**
 * 目标评估工具类
 * 提供通用的辅助方法
 */
public class ObjectiveUtils {
    
    /**
     * 归一化分数到[0,1]范围
     * @param value 原始值
     * @param min 最小值
     * @param max 最大值
     * @return 归一化后的分数
     */
    public static double normalize(double value, double min, double max) {
        if (max <= min) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }
    
    /**
     * 计算与目标值的匹配度（带容差）
     * @param actual 实际值
     * @param target 目标值
     * @param minRate 最小达成率
     * @param maxRate 最大达成率
     * @return 匹配度分数（0-1）
     */
    public static double calculateMatchScore(double actual, double target, double minRate, double maxRate) {
        if (target == 0) {
            return actual == 0 ? 1.0 : 0.0;
        }
        
        double ratio = actual / target;
        
        // 在范围内：根据距离中心点的距离给予0.8-1.0的分数
        if (ratio >= minRate && ratio <= maxRate) {
            double centerPoint = (minRate + maxRate) / 2;
            double maxDistance = (maxRate - minRate) / 2;
            double distance = Math.abs(ratio - centerPoint);
            return 1.0 - (distance / maxDistance) * 0.2;
        }
        
        // 低于最小值：使用平方根函数
        if (ratio < minRate) {
            return Math.max(0, 0.8 * Math.sqrt(ratio / minRate));
        }
        
        // 高于最大值：使用指数衰减
        return Math.max(0, 0.8 * Math.exp(-(ratio - maxRate) / maxRate));
    }
    
    /**
     * 计算权重归一化
     * @param weights 权重映射
     * @return 归一化后的权重映射
     */
    public static Map<String, Double> normalizeWeights(Map<String, Double> weights) {
        double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum > 0) {
            weights.replaceAll((k, v) -> v / sum);
        }
        return weights;
    }
    
    /**
     * 计算加权平均分
     * @param scores 分数映射
     * @param weights 权重映射
     * @return 加权平均分
     */
    public static double calculateWeightedAverage(Map<String, Double> scores, Map<String, Double> weights) {
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            String key = entry.getKey();
            double score = entry.getValue();
            double weight = weights.getOrDefault(key, 1.0);
            
            totalWeightedScore += score * weight;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? totalWeightedScore / totalWeight : 0.0;
    }
    
    /**
     * 计算标准差
     * @param values 数值数组
     * @return 标准差
     */
    public static double calculateStandardDeviation(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }
        
        double mean = 0.0;
        for (double value : values) {
            mean += value;
        }
        mean /= values.length;
        
        double variance = 0.0;
        for (double value : values) {
            variance += Math.pow(value - mean, 2);
        }
        variance /= values.length;
        
        return Math.sqrt(variance);
    }
    
    /**
     * 计算香农熵（用于多样性评估）
     * @param distribution 分布（概率）
     * @return 香农熵
     */
    public static double calculateShannonEntropy(double[] distribution) {
        double entropy = 0.0;
        for (double p : distribution) {
            if (p > 0) {
                entropy -= p * Math.log(p) / Math.log(2);
            }
        }
        return entropy;
    }
}
