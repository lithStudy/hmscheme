package com.mealplanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class NutritionCalculator {
    private UserProfile userProfile;
    private Map<String, NutrientRatio> diseaseNutrientRatios;
    private Map<String, Map<String, NutrientLimit>> diseaseNutrientLimits;

    public NutritionCalculator(UserProfile userProfile) {
        this.userProfile = userProfile;
        initializeDiseaseNutrientRatios();
        initializeDiseaseNutrientLimits();
    }

    /**
     * 获取用户档案
     * @return 用户档案
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }

    /**
     * 获取疾病营养素限制映射
     * @return 疾病到营养素限制的映射
     */
    public Map<String, Map<String, NutrientLimit>> getDiseaseNutrientLimits() {
        return diseaseNutrientLimits;
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

    /**
     * 初始化各种疾病的营养素限制
     */
    private void initializeDiseaseNutrientLimits() {
        diseaseNutrientLimits = new HashMap<>();
        
        // 为每种疾病创建营养素限制映射
        Map<String, NutrientLimit> hypertensionLimits = new HashMap<>();
        hypertensionLimits.put("sodium", new NutrientLimit(500.0, 2000.0)); // 高血压钠限制：500-2000mg
        hypertensionLimits.put("potassium", new NutrientLimit(2000.0, 5000.0)); // 高血压钾建议：2000-5000mg
        diseaseNutrientLimits.put("hypertension", hypertensionLimits);
        
        Map<String, NutrientLimit> diabetesLimits = new HashMap<>();
        diabetesLimits.put("carbohydrates", new NutrientLimit(50.0, 250.0)); // 糖尿病碳水限制
        diseaseNutrientLimits.put("diabetes", diabetesLimits);
        
        Map<String, NutrientLimit> hyperlipidemiaLimits = new HashMap<>();
        hyperlipidemiaLimits.put("fat", new NutrientLimit(20.0, 60.0)); // 高血脂脂肪限制
        diseaseNutrientLimits.put("hyperlipidemia", hyperlipidemiaLimits);
        
        Map<String, NutrientLimit> goutLimits = new HashMap<>();
        goutLimits.put("protein", new NutrientLimit(30.0, 70.0)); // 痛风蛋白质限制
        diseaseNutrientLimits.put("gout", goutLimits);
        
        Map<String, NutrientLimit> ckdLimits = new HashMap<>();
        ckdLimits.put("protein", new NutrientLimit(30.0, 70.0)); // 慢性肾病蛋白质限制
        ckdLimits.put("potassium", new NutrientLimit(1000.0, 2500.0)); // 慢性肾病钾限制
        ckdLimits.put("sodium", new NutrientLimit(500.0, 2000.0)); // 慢性肾病钠限制
        diseaseNutrientLimits.put("ckd", ckdLimits);
        
        // 可以继续添加其他疾病的限制...
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

    /**
     * 获取特定营养素的限制范围
     * @param nutrientName 营养素名称
     * @return 营养素限制范围，如果没有特定限制则返回null
     */
    public NutrientLimit getNutrientLimit(String nutrientName) {
        String[] conditions = userProfile.getHealthConditions();
        if (conditions == null || conditions.length == 0) {
            return null; // 健康人群没有特定限制
        }

        // 寻找最严格的限制
        double strictestMin = Double.MIN_VALUE;
        double strictestMax = Double.MAX_VALUE;
        boolean hasLimit = false;

        for (String condition : conditions) {
            Map<String, NutrientLimit> limits = diseaseNutrientLimits.get(condition.toLowerCase());
            if (limits != null && limits.containsKey(nutrientName)) {
                NutrientLimit limit = limits.get(nutrientName);
                strictestMin = Math.max(strictestMin, limit.getMinValue());
                strictestMax = Math.min(strictestMax, limit.getMaxValue());
                hasLimit = true;
            }
        }

        return hasLimit ? new NutrientLimit(strictestMin, strictestMax) : null;
    }

    /**
     * 获取所有营养素的限制
     * @return 营养素名称到限制的映射
     */
    public Map<String, NutrientLimit> getAllNutrientLimits() {
        Map<String, NutrientLimit> allLimits = new HashMap<>();
        String[] conditions = userProfile.getHealthConditions();
        
        if (conditions == null || conditions.length == 0) {
            return allLimits; // 健康人群返回空映射
        }

        // 收集所有可能的营养素名称
        Set<String> allNutrients = new HashSet<>();
        for (String condition : conditions) {
            Map<String, NutrientLimit> limits = diseaseNutrientLimits.get(condition.toLowerCase());
            if (limits != null) {
                allNutrients.addAll(limits.keySet());
            }
        }

        // 为每个营养素找到最严格的限制
        for (String nutrient : allNutrients) {
            allLimits.put(nutrient, getNutrientLimit(nutrient));
        }

        return allLimits;
    }

    // 计算每日营养需求
    public MealNutrients calculateDailyNutrientNeeds() {
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

        return new MealNutrients(tdee, carbsGrams, proteinGrams, fatGrams,
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

/**
 * 营养素限制类
 */
class NutrientLimit {
    private double minValue; // 最小值
    private double maxValue; // 最大值

    public NutrientLimit(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public double getMinValue() { return minValue; }
    public double getMaxValue() { return maxValue; }
    
    /**
     * 检查值是否在限制范围内
     * @param value 要检查的值
     * @return 是否在范围内
     */
    public boolean isWithinLimit(double value) {
        return value >= minValue && value <= maxValue;
    }
    
    /**
     * 计算值与限制的偏差程度
     * @param value 要检查的值
     * @return 偏差分数（0表示在范围内，负值表示偏离范围，绝对值越大偏离越严重）
     */
    public double calculateDeviationScore(double value) {
        if (value < minValue) {
            // 避免除以零
            if (minValue == 0) {
                return -1.0; // 如果最小值为0，直接返回-1表示严重偏离
            }
            return -1.0 * (minValue - value) / minValue; // 归一化的负偏差
        } else if (value > maxValue) {
            // 避免除以零
            if (maxValue == 0) {
                return -1.0; // 如果最大值为0，直接返回-1表示严重偏离
            }
            return -1.0 * (value - maxValue) / maxValue; // 归一化的负偏差
        } else {
            return 0.0; // 在范围内
        }
    }
}

// 每日营养需求类
// public static class MealNutrients {
//     private double totalCalories;  // 总热量(kcal)
//     private double carbohydrates;  // 碳水化合物(g)
//     private double protein;        // 蛋白质(g)
//     private double fat;            // 脂肪(g)
//     private double calcium;        // 钙(mg)
//     private double potassium;      // 钾(mg)
//     private double sodium;         // 钠(mg)
//     private double magnesium;      // 镁(mg)

//     public MealNutrients(double totalCalories, double carbohydrates, 
//                              double protein, double fat, double calcium,
//                              double potassium, double sodium, double magnesium) {
//         this.totalCalories = totalCalories;
//         this.carbohydrates = carbohydrates;
//         this.protein = protein;
//         this.fat = fat;
//         this.calcium = calcium;
//         this.potassium = potassium;
//         this.sodium = sodium;
//         this.magnesium = magnesium;
//     }

//     // Getters
//     public double getCalories() { return totalCalories; }
//     public double getCarbohydrates() { return carbohydrates; }
//     public double getProtein() { return protein; }
//     public double getFat() { return fat; }
//     public double getCalcium() { return calcium; }
//     public double getPotassium() { return potassium; }
//     public double getSodium() { return sodium; }
//     public double getMagnesium() { return magnesium; }
// } 