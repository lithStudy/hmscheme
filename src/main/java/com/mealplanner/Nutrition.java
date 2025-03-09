package com.mealplanner;

/**
 * 营养元素类，存储食物的营养成分信息
 */
public class Nutrition {
    private double carbohydrates; // 碳水化合物(g)
    private double protein;       // 蛋白质(g)
    private double fat;           // 脂肪(g)
    private double calcium;       // 钙(mg)
    private double potassium;     // 钾(mg)
    private double sodium;        // 钠(mg)
    private double magnesium;     // 镁(mg)
    private double calories;      // 卡路里(kcal)

    public Nutrition(double carbohydrates, double protein, double fat,
                    double calcium, double potassium, double sodium, double magnesium) {
        this.carbohydrates = carbohydrates;
        this.protein = protein;
        this.fat = fat;
        this.calcium = calcium;
        this.potassium = potassium;
        this.sodium = sodium;
        this.magnesium = magnesium;
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
            carbohydrates * ratio,
            protein * ratio,
            fat * ratio,
            calcium * ratio,
            potassium * ratio,
            sodium * ratio,
            magnesium * ratio
        );
    }

    /**
     * 将两个营养元素对象相加
     * @param other 另一个营养元素对象
     * @return 相加后的新营养元素对象
     */
    public Nutrition add(Nutrition other) {
        return new Nutrition(
            this.carbohydrates + other.carbohydrates,
            this.protein + other.protein,
            this.fat + other.fat,
            this.calcium + other.calcium,
            this.potassium + other.potassium,
            this.sodium + other.sodium,
            this.magnesium + other.magnesium
        );
    }

    /**
     * 将两个营养元素对象相减
     * @param other 另一个营养元素对象
     * @return 相减后的新营养元素对象
     */
    public Nutrition subtract(Nutrition other) {
        return new Nutrition(
            this.carbohydrates - other.carbohydrates,
            this.protein - other.protein,
            this.fat - other.fat,
            this.calcium - other.calcium,
            this.potassium - other.potassium,
            this.sodium - other.sodium,
            this.magnesium - other.magnesium
        );
    }

    // Getters
    public double getCarbohydrates() { return carbohydrates; }
    public double getProtein() { return protein; }
    public double getFat() { return fat; }
    public double getCalcium() { return calcium; }
    public double getPotassium() { return potassium; }
    public double getSodium() { return sodium; }
    public double getMagnesium() { return magnesium; }
    public double getCalories() { return calories; }
} 