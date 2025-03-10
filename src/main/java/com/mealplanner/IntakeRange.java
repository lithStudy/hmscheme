package com.mealplanner;

/**
 * 食物摄入量范围类，定义食物的推荐摄入量范围
 */
public class IntakeRange {
    private double minIntake;    // 最小摄入量(g)
    private double maxIntake;    // 最大摄入量(g)
    private double defaultIntake; // 默认推荐摄入量(g)
    
    /**
     * 创建一个食物摄入量范围对象
     * @param minIntake 最小摄入量(g)
     * @param maxIntake 最大摄入量(g)
     * @param defaultIntake 默认推荐摄入量(g)
     */
    public IntakeRange(double minIntake, double maxIntake, double defaultIntake) {
        if (minIntake > maxIntake) {
            throw new IllegalArgumentException("最小摄入量不能大于最大摄入量");
        }
        if (defaultIntake < minIntake || defaultIntake > maxIntake) {
            throw new IllegalArgumentException("默认摄入量必须在最小和最大摄入量范围内");
        }
        
        this.minIntake = minIntake;
        this.maxIntake = maxIntake;
        this.defaultIntake = defaultIntake;
    }
    
    /**
     * 检查给定的摄入量是否在范围内
     * @param intake 要检查的摄入量
     * @return 是否在范围内
     */
    public boolean isWithinRange(double intake) {
        return intake >= minIntake && intake <= maxIntake;
    }
    
    /**
     * 将给定的摄入量调整到范围内
     * @param intake 要调整的摄入量
     * @return 调整后的摄入量
     */
    public double adjustToRange(double intake) {
        return Math.max(minIntake, Math.min(maxIntake, intake));
    }
    
    // Getters
    public double getMinIntake() { return minIntake; }
    public double getMaxIntake() { return maxIntake; }
    public double getDefaultIntake() { return defaultIntake; }
} 