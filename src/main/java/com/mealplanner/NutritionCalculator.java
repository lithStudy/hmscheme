package com.mealplanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mealplanner.model.HealthConditionType;
import com.mealplanner.model.NutrientLimit;
import com.mealplanner.model.Nutrition;
import com.mealplanner.model.UserProfile;

import java.util.HashSet;

public class NutritionCalculator {
    private UserProfile userProfile;
    private Map<HealthConditionType, NutrientRatio> diseaseNutrientRatios;
    private Map<HealthConditionType, Map<String, NutrientLimit>> diseaseNutrientLimits;

    public NutritionCalculator(UserProfile userProfile) {
        this.userProfile = userProfile;
        initializeDiseaseNutrientRatios();
        initializeDiseaseNutrientLimits();
    }

    /**
     * 获取疾病营养素限制映射
     * @return 疾病到营养素限制的映射
     */
    public Map<HealthConditionType, Map<String, NutrientLimit>> getDiseaseNutrientLimits() {
        return diseaseNutrientLimits;
    }

    private void initializeDiseaseNutrientRatios() {
        diseaseNutrientRatios = new HashMap<>();
        
        // 高血压
        diseaseNutrientRatios.put(HealthConditionType.HYPERTENSION, new NutrientRatio(0.525, 0.175, 0.275));
        // 2型糖尿病
        diseaseNutrientRatios.put(HealthConditionType.DIABETES, new NutrientRatio(0.475, 0.175, 0.30));
        // 高血脂
        diseaseNutrientRatios.put(HealthConditionType.HYPERLIPIDEMIA, new NutrientRatio(0.525, 0.175, 0.275));
        // 痛风
        diseaseNutrientRatios.put(HealthConditionType.GOUT, new NutrientRatio(0.575, 0.135, 0.275));
        // 慢性肾病(无透析)
        diseaseNutrientRatios.put(HealthConditionType.CKD, new NutrientRatio(0.525, 0.125, 0.325));
        // 慢性肾病(透析)
        diseaseNutrientRatios.put(HealthConditionType.CKD_DIALYSIS, new NutrientRatio(0.525, 0.225, 0.325));
        // 脂肪肝
        diseaseNutrientRatios.put(HealthConditionType.FATTY_LIVER, new NutrientRatio(0.475, 0.175, 0.275));
        // 冠心病
        diseaseNutrientRatios.put(HealthConditionType.CORONARY_HEART, new NutrientRatio(0.525, 0.175, 0.275));
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
        diseaseNutrientLimits.put(HealthConditionType.HYPERTENSION, hypertensionLimits);
        
        Map<String, NutrientLimit> diabetesLimits = new HashMap<>();
        diabetesLimits.put("carbohydrates", new NutrientLimit(50.0, 250.0)); // 糖尿病碳水限制
        diseaseNutrientLimits.put(HealthConditionType.DIABETES, diabetesLimits);
        
        Map<String, NutrientLimit> hyperlipidemiaLimits = new HashMap<>();
        hyperlipidemiaLimits.put("fat", new NutrientLimit(20.0, 60.0)); // 高血脂脂肪限制
        diseaseNutrientLimits.put(HealthConditionType.HYPERLIPIDEMIA, hyperlipidemiaLimits);
        
        Map<String, NutrientLimit> goutLimits = new HashMap<>();
        goutLimits.put("protein", new NutrientLimit(30.0, 70.0)); // 痛风蛋白质限制
        diseaseNutrientLimits.put(HealthConditionType.GOUT, goutLimits);
        
        Map<String, NutrientLimit> ckdLimits = new HashMap<>();
        ckdLimits.put("protein", new NutrientLimit(30.0, 70.0)); // 慢性肾病蛋白质限制
        ckdLimits.put("potassium", new NutrientLimit(1000.0, 2500.0)); // 慢性肾病钾限制
        ckdLimits.put("sodium", new NutrientLimit(500.0, 2000.0)); // 慢性肾病钠限制
        diseaseNutrientLimits.put(HealthConditionType.CKD, ckdLimits);
        
        // 可以继续添加其他疾病的限制...
    }

    // 计算营养素比例
    public NutrientRatio calculateNutrientRatio() {
        HealthConditionType[] conditions = userProfile.getHealthConditions();
        if (conditions == null || conditions.length == 0) {
            // 默认健康人群的营养素比例
            return new NutrientRatio(0.525, 0.175, 0.275);
        }

        // 如果有多种疾病，取最严格的限制
        double minCarbs = 1.0, minProtein = 1.0, minFat = 1.0;
        double maxCarbs = 0.0, maxProtein = 0.0, maxFat = 0.0;

        for (HealthConditionType condition : conditions) {
            if (condition == null) continue;
            
            NutrientRatio ratio = diseaseNutrientRatios.get(condition);
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
        HealthConditionType[] conditions = userProfile.getHealthConditions();
        if (conditions == null || conditions.length == 0) {
            return null; // 健康人群没有特定限制
        }

        // 寻找最严格的限制
        double strictestMin = Double.MIN_VALUE;
        double strictestMax = Double.MAX_VALUE;
        boolean hasLimit = false;

        for (HealthConditionType condition : conditions) {
            if (condition == null) continue;
            
            Map<String, NutrientLimit> limits = diseaseNutrientLimits.get(condition);
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
        HealthConditionType[] conditions = userProfile.getHealthConditions();
        
        if (conditions == null || conditions.length == 0) {
            return allLimits; // 健康人群返回空映射
        }

        // 收集所有可能的营养素名称
        Set<String> allNutrients = new HashSet<>();
        for (HealthConditionType condition : conditions) {
            if (condition == null) continue;
            
            Map<String, NutrientLimit> limits = diseaseNutrientLimits.get(condition);
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
    public Nutrition calculateDailyNutrientNeeds() {
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
        double ironMg = userProfile.getGender().equalsIgnoreCase("male") ? 8.0 : 18.0;  // 铁：性别相关
        double phosphorusMg = 700.0;  // 默认磷摄入

        return new Nutrition(tdee, carbsGrams, proteinGrams, fatGrams,
                            calciumMg, potassiumMg, sodiumMg, magnesiumMg,
                            ironMg, phosphorusMg);
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
