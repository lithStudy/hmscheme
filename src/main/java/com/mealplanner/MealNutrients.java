package com.mealplanner;

import lombok.Getter;

/**
 * 表示一餐的营养素需求或含量
 * 作为独立的公共类，可以在不同包之间使用
 */
@Getter
public class MealNutrients {
    public double calories;      // 卡路里(kcal)
    public double carbohydrates; // 碳水化合物(g)
    public double protein;       // 蛋白质(g)
    public double fat;           // 脂肪(g)
    public double calcium;       // 钙(mg)
    public double potassium;     // 钾(mg)
    public double sodium;        // 钠(mg)
    public double magnesium;     // 镁(mg)

    /**
     * 创建一个膳食营养素对象
     * @param calories 卡路里(kcal)
     * @param carbohydrates 碳水化合物(g)
     * @param protein 蛋白质(g)
     * @param fat 脂肪(g)
     * @param calcium 钙(mg)
     * @param potassium 钾(mg)
     * @param sodium 钠(mg)
     * @param magnesium 镁(mg)
     */
    public MealNutrients(double calories, double carbohydrates, double protein, 
                      double fat, double calcium, double potassium, 
                      double sodium, double magnesium) {
        this.calories = calories;
        this.carbohydrates = carbohydrates;
        this.protein = protein;
        this.fat = fat;
        this.calcium = calcium;
        this.potassium = potassium;
        this.sodium = sodium;
        this.magnesium = magnesium;
    }
    
    /**
     * 复制一个膳食营养素对象
     * @param other 要复制的对象
     * @return 新的膳食营养素对象
     */
    public static MealNutrients copy(MealNutrients other) {
        return new MealNutrients(
            other.calories,
            other.carbohydrates,
            other.protein,
            other.fat,
            other.calcium,
            other.potassium,
            other.sodium,
            other.magnesium
        );
    }
    
    /**
     * 创建一个给定比例的膳食营养素对象
     * @param nutrients 原始营养素
     * @param ratio 比例
     * @return 新的膳食营养素对象
     */
    public static MealNutrients scale(MealNutrients nutrients, double ratio) {
        return new MealNutrients(
            nutrients.calories * ratio,
            nutrients.carbohydrates * ratio,
            nutrients.protein * ratio,
            nutrients.fat * ratio,
            nutrients.calcium * ratio,
            nutrients.potassium * ratio,
            nutrients.sodium * ratio,
            nutrients.magnesium * ratio
        );
    }
    
    /**
     * 将两个膳食营养素对象相加
     * @param a 第一个营养素对象
     * @param b 第二个营养素对象
     * @return 相加后的新对象
     */
    public static MealNutrients add(MealNutrients a, MealNutrients b) {
        return new MealNutrients(
            a.calories + b.calories,
            a.carbohydrates + b.carbohydrates,
            a.protein + b.protein,
            a.fat + b.fat,
            a.calcium + b.calcium,
            a.potassium + b.potassium,
            a.sodium + b.sodium,
            a.magnesium + b.magnesium
        );
    }
    
    /**
     * 从一个膳食营养素对象中减去另一个
     * @param a 被减数
     * @param b 减数
     * @return 相减后的新对象
     */
    public static MealNutrients subtract(MealNutrients a, MealNutrients b) {
        return new MealNutrients(
            Math.max(0, a.calories - b.calories),
            Math.max(0, a.carbohydrates - b.carbohydrates),
            Math.max(0, a.protein - b.protein),
            Math.max(0, a.fat - b.fat),
            Math.max(0, a.calcium - b.calcium),
            Math.max(0, a.potassium - b.potassium),
            Math.max(0, a.sodium - b.sodium),
            Math.max(0, a.magnesium - b.magnesium)
        );
    }
    
    @Override
    public String toString() {
        return String.format(
            "营养素: 热量=%.1f大卡, 碳水=%.1fg, 蛋白质=%.1fg, 脂肪=%.1fg, " +
            "钙=%.1fmg, 钾=%.1fmg, 钠=%.1fmg, 镁=%.1fmg",
            calories, carbohydrates, protein, fat, calcium, potassium, sodium, magnesium);
    }
} 