package com.mealplanner.model;

import lombok.Getter;

/**
 * 营养元素类，存储食物的营养成分信息
 */
@Getter
public class Nutrition {
    public double calories;      // 卡路里(kcal)
    public double carbohydrates; // 碳水化合物(g)
    public double protein;       // 蛋白质(g)
    public double fat;           // 脂肪(g)
    public double calcium;       // 钙(mg)
    public double potassium;     // 钾(mg)
    public double sodium;        // 钠(mg)
    public double magnesium;     // 镁(mg)
    public double iron;          // 铁(mg)
    public double phosphorus;    // 磷(mg)

    public Nutrition(double calories, double carbohydrates, double protein, double fat,
                    double calcium, double potassium, double sodium, double magnesium) {
        this.calories = calories;
        this.carbohydrates = carbohydrates;
        this.protein = protein;
        this.fat = fat;
        this.calcium = calcium;
        this.potassium = potassium;
        this.sodium = sodium;
        this.magnesium = magnesium;
        this.iron = 0;
        this.phosphorus = 0;
        calculateCalories();
    }
    
    public Nutrition(double calories, double carbohydrates, double protein, double fat,
                    double calcium, double potassium, double sodium, double magnesium,
                    double iron, double phosphorus) {
        this.calories = calories;
        this.carbohydrates = carbohydrates;
        this.protein = protein;
        this.fat = fat;
        this.calcium = calcium;
        this.potassium = potassium;
        this.sodium = sodium;
        this.magnesium = magnesium;
        this.iron = iron;
        this.phosphorus = phosphorus;
        calculateCalories();
    }

    private void calculateCalories() {
        // 使用标准卡路里计算公式
        this.calories = (carbohydrates * 4) + (protein * 4) + (fat * 9);
    }

    /**
     * 按比例缩放营养元素
     * @param ratio 缩放比例
     * @return 新的营养元素对象
     */
    public Nutrition scale(double ratio) {
        return new Nutrition(
            calories * ratio,
            carbohydrates * ratio,
            protein * ratio,
            fat * ratio,
            calcium * ratio,
            potassium * ratio,
            sodium * ratio,
            magnesium * ratio,
            iron * ratio,
            phosphorus * ratio
        );
    }

    /**
     * 将两个营养元素对象相加
     * @param other 另一个营养元素对象
     * @return 相加后的新营养元素对象
     */
    public Nutrition add(Nutrition other) {
        return new Nutrition(
            this.calories + other.calories,
            this.carbohydrates + other.carbohydrates,
            this.protein + other.protein,
            this.fat + other.fat,
            this.calcium + other.calcium,
            this.potassium + other.potassium,
            this.sodium + other.sodium,
            this.magnesium + other.magnesium,
            this.iron + other.iron,
            this.phosphorus + other.phosphorus
        );
    }

    /**
     * 将两个营养元素对象相减
     * @param other 另一个营养元素对象
     * @return 相减后的新营养元素对象
     */
    public Nutrition subtract(Nutrition other) {
        return new Nutrition(
            this.calories - other.calories,
            this.carbohydrates - other.carbohydrates,
            this.protein - other.protein,
            this.fat - other.fat,
            this.calcium - other.calcium,
            this.potassium - other.potassium,
            this.sodium - other.sodium,
            this.magnesium - other.magnesium,
            this.iron - other.iron,
            this.phosphorus - other.phosphorus
        );
    }
    
    /**
     * 获取铁含量
     * @return 铁含量(mg)
     */
    public double getIron() {
        return iron;
    }
    
    /**
     * 获取磷含量
     * @return 磷含量(mg)
     */
    public double getPhosphorus() {
        return phosphorus;
    }
} 