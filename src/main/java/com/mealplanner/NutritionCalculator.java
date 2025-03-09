package com.mealplanner;

import java.util.HashMap;
import java.util.Map;

public class NutritionCalculator {
    private UserProfile userProfile;
    private Map<String, NutrientRatio> diseaseNutrientRatios;

    public NutritionCalculator(UserProfile userProfile) {
        this.userProfile = userProfile;
        initializeDiseaseNutrientRatios();
    }

    private void initializeDiseaseNutrientRatios() {
        diseaseNutrientRatios = new HashMap<>();
        
        // 高血压
        diseaseNutrientRatios.put("hypertension", new NutrientRatio(0.525, 0.175, 0.275));
        // 2型糖尿病
        diseaseNutrientRatios.put("diabetes", new NutrientRatio(0.475, 0.175, 0.30));
        // 高血脂
        diseaseNutrientRatios.put("hyperlipidemia", new NutrientRatio(0.525, 0.175, 0.275));
        // 痛风
        diseaseNutrientRatios.put("gout", new NutrientRatio(0.575, 0.135, 0.275));
        // 慢性肾病(无透析)
        diseaseNutrientRatios.put("ckd", new NutrientRatio(0.525, 0.125, 0.325));
        // 慢性肾病(透析)
        diseaseNutrientRatios.put("ckd_dialysis", new NutrientRatio(0.525, 0.225, 0.325));
        // 脂肪肝
        diseaseNutrientRatios.put("fatty_liver", new NutrientRatio(0.475, 0.175, 0.275));
        // 冠心病
        diseaseNutrientRatios.put("coronary_heart", new NutrientRatio(0.525, 0.175, 0.275));
    }

    // 计算营养素比例
    public NutrientRatio calculateNutrientRatio() {
        String[] conditions = userProfile.getHealthConditions();
        if (conditions == null || conditions.length == 0) {
            // 默认健康人群的营养素比例
            return new NutrientRatio(0.525, 0.175, 0.275);
        }

        // 如果有多种疾病，取最严格的限制
        double minCarbs = 1.0, minProtein = 1.0, minFat = 1.0;
        double maxCarbs = 0.0, maxProtein = 0.0, maxFat = 0.0;

        for (String condition : conditions) {
            NutrientRatio ratio = diseaseNutrientRatios.get(condition.toLowerCase());
            if (ratio != null) {
                minCarbs = Math.min(minCarbs, ratio.getCarbRatio());
                minProtein = Math.min(minProtein, ratio.getProteinRatio());
                minFat = Math.min(minFat, ratio.getFatRatio());
                
                maxCarbs = Math.max(maxCarbs, ratio.getCarbRatio());
                maxProtein = Math.max(maxProtein, ratio.getProteinRatio());
                maxFat = Math.max(maxFat, ratio.getFatRatio());
            }
        }

        // 使用最保守的比例
        return new NutrientRatio(minCarbs, minProtein, minFat);
    }

    // 计算每日营养需求
    public DailyNutrientNeeds calculateDailyNutrientNeeds() {
        double tdee = userProfile.calculateTDEE();
        NutrientRatio ratio = calculateNutrientRatio();

        // 计算各营养素的克数
        double carbsGrams = (tdee * ratio.getCarbRatio()) / 4.0;  // 4 kcal/g
        double proteinGrams = (tdee * ratio.getProteinRatio()) / 4.0;  // 4 kcal/g
        double fatGrams = (tdee * ratio.getFatRatio()) / 9.0;  // 9 kcal/g

        // 计算微量元素需求
        double sodiumMg = 2000.0;  // 默认限制钠摄入
        double potassiumMg = 3500.0;  // 默认钾摄入
        double calciumMg = 1000.0;  // 默认钙摄入
        double magnesiumMg = userProfile.getGender().equalsIgnoreCase("male") ? 400.0 : 310.0;  // 镁：性别相关

        return new DailyNutrientNeeds(tdee, carbsGrams, proteinGrams, fatGrams,
                                    calciumMg, potassiumMg, sodiumMg, magnesiumMg);
    }
}

// 营养素比例类
class NutrientRatio {
    private double carbRatio;
    private double proteinRatio;
    private double fatRatio;

    public NutrientRatio(double carbRatio, double proteinRatio, double fatRatio) {
        this.carbRatio = carbRatio;
        this.proteinRatio = proteinRatio;
        this.fatRatio = fatRatio;
    }

    public double getCarbRatio() { return carbRatio; }
    public double getProteinRatio() { return proteinRatio; }
    public double getFatRatio() { return fatRatio; }
}

// 每日营养需求类
class DailyNutrientNeeds {
    private double totalCalories;  // 总热量(kcal)
    private double carbohydrates;  // 碳水化合物(g)
    private double protein;        // 蛋白质(g)
    private double fat;            // 脂肪(g)
    private double calcium;        // 钙(mg)
    private double potassium;      // 钾(mg)
    private double sodium;         // 钠(mg)
    private double magnesium;      // 镁(mg)

    public DailyNutrientNeeds(double totalCalories, double carbohydrates, 
                             double protein, double fat, double calcium,
                             double potassium, double sodium, double magnesium) {
        this.totalCalories = totalCalories;
        this.carbohydrates = carbohydrates;
        this.protein = protein;
        this.fat = fat;
        this.calcium = calcium;
        this.potassium = potassium;
        this.sodium = sodium;
        this.magnesium = magnesium;
    }

    // Getters
    public double getTotalCalories() { return totalCalories; }
    public double getCarbohydrates() { return carbohydrates; }
    public double getProtein() { return protein; }
    public double getFat() { return fat; }
    public double getCalcium() { return calcium; }
    public double getPotassium() { return potassium; }
    public double getSodium() { return sodium; }
    public double getMagnesium() { return magnesium; }
} 