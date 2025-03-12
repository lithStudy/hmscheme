package com.mealplanner.nutrition;

import com.mealplanner.MealNutrients;

/**
 * 营养素范围调整器
 * 用于在找不到满足严格要求的食物时放宽营养素要求
 */
public class NutrientRangeAdjuster {
    
    // 默认的允许偏差范围
    private static final double DEFAULT_INITIAL_DEVIATION = 0.1; // 初始偏差10%
    private static final double DEFAULT_MAX_DEVIATION = 0.3; // 最大偏差30%
    private static final double DEFAULT_STEP_SIZE = 0.05; // 每次增加5%
    
    private double initialDeviation; // 初始允许偏差
    private double maxDeviation; // 最大允许偏差
    private double stepSize; // 每次调整的步长
    private double currentDeviation; // 当前偏差值
    
    /**
     * 创建一个默认参数的营养素范围调整器
     */
    public NutrientRangeAdjuster() {
        this(DEFAULT_INITIAL_DEVIATION, DEFAULT_MAX_DEVIATION, DEFAULT_STEP_SIZE);
    }
    
    /**
     * 创建一个自定义参数的营养素范围调整器
     * @param initialDeviation 初始偏差值（0-1之间）
     * @param maxDeviation 最大偏差值（0-1之间）
     * @param stepSize 每次增加的步长（0-1之间）
     */
    public NutrientRangeAdjuster(double initialDeviation, double maxDeviation, double stepSize) {
        this.initialDeviation = clamp(initialDeviation, 0, 1);
        this.maxDeviation = clamp(maxDeviation, initialDeviation, 1);
        this.stepSize = clamp(stepSize, 0, maxDeviation);
        this.currentDeviation = initialDeviation;
    }
    
    /**
     * 重置当前偏差到初始值
     */
    public void reset() {
        this.currentDeviation = initialDeviation;
    }
    
    /**
     * 增加当前的偏差值，使范围更宽松
     * @return 如果偏差值仍在允许范围内则返回true，否则返回false
     */
    public boolean increaseDeviation() {
        if (currentDeviation < maxDeviation) {
            currentDeviation = Math.min(currentDeviation + stepSize, maxDeviation);
            return true;
        }
        return false;
    }
    
    /**
     * 创建一个放宽要求后的营养目标
     * @param original 原始营养目标
     * @return 放宽要求后的营养目标范围
     */
    public NutrientRange createRelaxedRange(MealNutrients original) {
        return new NutrientRange(
            multiplyByFactor(original, 1 - currentDeviation), // 下限
            multiplyByFactor(original, 1 + currentDeviation)  // 上限
        );
    }
    
    /**
     * 检查营养值是否在放宽的范围内
     * @param actual 实际营养值
     * @param target 目标营养值
     * @return 如果在范围内则返回true
     */
    public boolean isWithinRelaxedRange(double actual, double target) {
        double lowerBound = target * (1 - currentDeviation);
        double upperBound = target * (1 + currentDeviation);
        return actual >= lowerBound && actual <= upperBound;
    }
    
    /**
     * 将营养素目标乘以指定因子
     * @param nutrients 原始营养素目标
     * @param factor 乘数因子
     * @return 调整后的营养素目标
     */
    private MealNutrients multiplyByFactor(MealNutrients nutrients, double factor) {
        return new MealNutrients(
            nutrients.calories * factor,
            nutrients.carbohydrates * factor,
            nutrients.protein * factor,
            nutrients.fat * factor,
            nutrients.calcium * factor,
            nutrients.potassium * factor,
            nutrients.sodium * factor,
            nutrients.magnesium * factor
        );
    }
    
    /**
     * 将值限制在指定范围内
     * @param value 待限制的值
     * @param min 最小值
     * @param max 最大值
     * @return 限制后的值
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * 获取当前偏差值
     * @return 当前偏差值
     */
    public double getCurrentDeviation() {
        return currentDeviation;
    }
    
    /**
     * 设置当前偏差值
     * @param deviation 偏差值
     */
    public void setCurrentDeviation(double deviation) {
        this.currentDeviation = clamp(deviation, initialDeviation, maxDeviation);
    }
    
    /**
     * 营养素范围类，表示营养素的上下限
     */
    public static class NutrientRange {
        private MealNutrients lowerBound;
        private MealNutrients upperBound;
        
        public NutrientRange(MealNutrients lowerBound, MealNutrients upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
        
        public MealNutrients getLowerBound() {
            return lowerBound;
        }
        
        public MealNutrients getUpperBound() {
            return upperBound;
        }
        
        /**
         * 检查给定的营养素值是否在范围内
         * @param nutrients 待检查的营养素值
         * @return 如果在范围内则返回true
         */
        public boolean isWithinRange(MealNutrients nutrients) {
            return isWithinBounds(nutrients.calories, lowerBound.calories, upperBound.calories) &&
                   isWithinBounds(nutrients.carbohydrates, lowerBound.carbohydrates, upperBound.carbohydrates) &&
                   isWithinBounds(nutrients.protein, lowerBound.protein, upperBound.protein) &&
                   isWithinBounds(nutrients.fat, lowerBound.fat, upperBound.fat) &&
                   isWithinBounds(nutrients.calcium, lowerBound.calcium, upperBound.calcium) &&
                   isWithinBounds(nutrients.potassium, lowerBound.potassium, upperBound.potassium) &&
                   isWithinBounds(nutrients.sodium, lowerBound.sodium, upperBound.sodium) &&
                   isWithinBounds(nutrients.magnesium, lowerBound.magnesium, upperBound.magnesium);
        }
        
        private boolean isWithinBounds(double value, double lower, double upper) {
            return value >= lower && value <= upper;
        }
    }
} 