package com.mealplanner.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class NutrientRatio {
    private double carbRatio;
    private double proteinRatio;
    private double fatRatio;

        
    // 理想的宏量营养素比例
    private double defaultCarbPercentage = 0.60; // 碳水占60%
    private double defaultProteinPercentage = 0.15; // 蛋白质占15%
    private double defaultFatPercentage = 0.25; // 脂肪占25%

    static Map<HealthConditionType, NutrientRatio> diseaseNutrientRatios;

    static{
        initializeDiseaseNutrientRatios();
    }

    public NutrientRatio(){
        carbRatio=defaultCarbPercentage;
        proteinRatio=defaultProteinPercentage;
        fatRatio=defaultFatPercentage;
    }

    public NutrientRatio(double carbRatio, double proteinRatio, double fatRatio) {
        this.carbRatio = carbRatio;
        this.proteinRatio = proteinRatio;
        this.fatRatio = fatRatio;
    }

    // 计算营养素比例
    public static NutrientRatio calculateNutrientRatio(UserProfile userProfile) {
        HealthConditionType[] conditions = userProfile.getHealthConditions();
        if (conditions == null || conditions.length == 0) {
            // 默认健康人群的营养素比例
            return new NutrientRatio();
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

        // 使用最保守的比例（取最小值）
        double carbRatio = minCarbs;
        double proteinRatio = minProtein;
        double fatRatio = minFat;
        
        // 计算三种营养素比例总和
        double totalRatio = carbRatio + proteinRatio + fatRatio;
        
        // 应用等比例缩放法确保总比例为100%
        // 这种方法保持了三种营养素之间的相对比例关系不变
        if (Math.abs(totalRatio - 1.0) > 0.001) { // 允许0.1%的误差
            // 如果总比例不等于100%，进行等比例缩放
            double scaleFactor = 1.0 / totalRatio;
            carbRatio *= scaleFactor;
            proteinRatio *= scaleFactor;
            fatRatio *= scaleFactor;
            
            // 处理舍入误差，确保精确等于100%
            double roundedTotal = carbRatio + proteinRatio + fatRatio;
            if (Math.abs(roundedTotal - 1.0) > 0.0001) {
                // 将剩余的微小误差添加到碳水化合物比例中
                carbRatio += (1.0 - roundedTotal);
            }
        }

        return new NutrientRatio(carbRatio, proteinRatio, fatRatio);
    }
        
        
    private static void initializeDiseaseNutrientRatios() {

        diseaseNutrientRatios= new HashMap<>();
        
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
}
