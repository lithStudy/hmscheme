package com.mealplanner;

/**
 * 食物份量类，存储食物的份量信息
 */
public class Portion {
    private double weight;       // 食物重量(g)
    private String displayUnit;  // 显示单位（如"个"、"片"、"克"等）
    private double displayAmount; // 显示数量（如1个、2片等）

    /**
     * 创建一个食物份量对象
     * @param weight 食物重量(g)
     * @param displayUnit 显示单位
     * @param displayAmount 显示数量
     */
    public Portion(double weight, String displayUnit, double displayAmount) {
        this.weight = weight;
        this.displayUnit = displayUnit;
        this.displayAmount = displayAmount;
    }

    /**
     * 创建一个以克为单位的食物份量对象
     * @param weight 食物重量(g)
     */
    public Portion(double weight) {
        this(weight, "克", weight);
    }

    /**
     * 获取份量描述
     * @return 格式化的份量描述字符串
     */
    public String getDescription() {
        if (displayUnit.equals("克") || displayUnit.equals("g")) {
            return String.format("%.0f%s", displayAmount, displayUnit);
        } else {
            return String.format("%.1f%s (约%.0f克)", displayAmount, displayUnit, weight);
        }
    }

    /**
     * 按比例缩放份量
     * @param ratio 缩放比例
     * @return 新的份量对象
     */
    public Portion scale(double ratio) {
        return new Portion(
            weight * ratio,
            displayUnit,
            displayAmount * ratio
        );
    }

    // Getters
    public double getWeight() { return weight; }
    public String getDisplayUnit() { return displayUnit; }
    public double getDisplayAmount() { return displayAmount; }
} 