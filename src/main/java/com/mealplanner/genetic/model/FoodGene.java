package com.mealplanner.genetic.model;

import com.mealplanner.model.Food;

/**
 * 表示膳食解决方案中的食物基因
 * 包含食物和其摄入量
 */
public class FoodGene {
    // 食物
    private Food food;
    
    // 摄入量(g)
    private double intake;
    
    /**
     * 构造函数
     * @param food 食物
     * @param intake 摄入量(g)
     */
    public FoodGene(Food food, double intake) {
        this.food = food;
        this.intake = intake;
    }
    
    /**
     * 创建基因的拷贝
     * @return 基因拷贝
     */
    public FoodGene copy() {
        return new FoodGene(this.food, this.intake);
    }
    
    /**
     * 获取食物
     * @return 食物
     */
    public Food getFood() {
        return food;
    }
    
    /**
     * 设置食物
     * @param food 食物
     */
    public void setFood(Food food) {
        this.food = food;
    }
    
    /**
     * 获取摄入量
     * @return 摄入量(g)
     */
    public double getIntake() {
        return intake;
    }
    
    /**
     * 设置摄入量
     * @param intake 摄入量(g)
     */
    public void setIntake(double intake) {
        // 将摄入量四舍五入为整数
        this.intake = Math.round(intake);
    }
    
    /**
     * 应用食物摄入量的变异
     * @param lowerBound 下限
     * @param upperBound 上限
     * @param mutationStrength 变异强度(0-1)
     */
    public void mutateIntake(double lowerBound, double upperBound, double mutationStrength) {
        if (lowerBound >= upperBound) {
            return;
        }
        
        // 计算当前摄入量在范围内的位置(0-1)
        double normalizedPosition = (intake - lowerBound) / (upperBound - lowerBound);
        
        // 生成随机偏移，基于变异强度
        double offset = (Math.random() * 2 - 1) * mutationStrength;
        
        // 计算新的位置
        double newPosition = normalizedPosition + offset;
        
        // 确保位置在0-1范围内
        newPosition = Math.max(0, Math.min(1, newPosition));
        
        // 将位置转换回实际摄入量并四舍五入为整数
        intake = Math.round(lowerBound + newPosition * (upperBound - lowerBound));
    }
    
    /**
     * 获取该食物在当前摄入量下的营养素估计值
     * 以每克为单位
     * @param nutrientName 营养素名称
     * @return 营养素量
     */
    public double getNutrientAmount(String nutrientName) {
        if (food == null) {
            return 0;
        }
        
        double per100g = 0;
        switch (nutrientName.toLowerCase()) {
            case "calories":
                per100g = food.getNutrition().getCalories();
                break;
            case "carbohydrates":
                per100g = food.getNutrition().getCarbohydrates();
                break;
            case "protein":
                per100g = food.getNutrition().getProtein();
                break;
            case "fat":
                per100g = food.getNutrition().getFat();
                break;
            case "calcium":
                per100g = food.getNutrition().getCalcium();
                break;
            case "potassium":
                per100g = food.getNutrition().getPotassium();
                break;
            case "sodium":
                per100g = food.getNutrition().getSodium();
                break;
            case "magnesium":
                per100g = food.getNutrition().getMagnesium();
                break;
            default:
                return 0;
        }
        
        // 转换为当前摄入量
        return per100g * (intake / 100.0);
    }
    
    @Override
    public String toString() {
        return "FoodGene[" + food.getName() + ", " + String.format("%.1f", intake) + "g]";
    }
}