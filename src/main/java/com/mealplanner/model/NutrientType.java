package com.mealplanner.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

/**
 * 营养素类型枚举
 * 定义了系统中支持的所有营养素类型
 */
@Getter
public enum NutrientType {
    CALORIES("calories", "热量", true, 3.0, new double[]{0.9, 1.1}),
    CARBOHYDRATES("carbohydrates", "碳水化合物", true, 1.0, new double[]{0.85, 1.15}),
    PROTEIN("protein", "蛋白质", true, 1.0, new double[]{0.9, 1.2}),
    FAT("fat", "脂肪", true, 0.8, new double[]{0.7, 1.1}),
    CALCIUM("calcium", "钙", false, 0.7, new double[]{0.8, 1.5}),
    POTASSIUM("potassium", "钾", false, 0.7, new double[]{0.8, 1.5}),
    SODIUM("sodium", "钠", true, 0.8, new double[]{0.5, 1.0}),
    MAGNESIUM("magnesium", "镁", false, 0.7, new double[]{0.8, 1.5}),
    IRON("iron", "铁", false, 0.7, new double[]{0.8, 1.5}),
    PHOSPHORUS("phosphorus", "磷", false, 0.7, new double[]{0.8, 1.5});
    
    // 营养素的英文名称
    private final String name;
    
    // 营养素的中文名称
    private final String displayName;
    
    // 是否默认惩罚过量
    private final boolean defaultPenalizeExcess;
    
    // 默认权重
    private final double defaultWeight;
    
    // 默认达成率范围 [最小达成率, 最大达成率]
    private final double[] defaultAchievementRange;
    
    // 存储疾病特定的营养素达成率调整
    private static final Map<HealthConditionType, Map<NutrientType, double[]>> DISEASE_NUTRIENT_RATES = new HashMap<>();
    
    static {
        initializeDiseaseNutrientRates();
    }
    
    /**
     * 构造函数
     */
    NutrientType(String name, String displayName, boolean defaultPenalizeExcess, double defaultWeight, double[] defaultAchievementRange) {
        this.name = name;
        this.displayName = displayName;
        this.defaultPenalizeExcess = defaultPenalizeExcess;
        this.defaultWeight = defaultWeight;
        this.defaultAchievementRange = defaultAchievementRange;
    }
    
    /**
     * 初始化疾病特定的营养素达成率调整
     */
    private static void initializeDiseaseNutrientRates() {
        // 糖尿病
        Map<NutrientType, double[]> diabetesRates = new HashMap<>();
        diabetesRates.put(CALORIES, new double[]{0.9, 1.0});       // 控制热量
        diabetesRates.put(CARBOHYDRATES, new double[]{0.7, 0.9});  // 严格限制碳水
        diabetesRates.put(PROTEIN, new double[]{1.0, 1.2});        // 适当增加蛋白质
        diabetesRates.put(FAT, new double[]{0.8, 1.0});            // 控制脂肪
        diabetesRates.put(SODIUM, new double[]{0.5, 0.9});         // 限制钠
        DISEASE_NUTRIENT_RATES.put(HealthConditionType.DIABETES, diabetesRates);
        
        // 高血压
        Map<NutrientType, double[]> hypertensionRates = new HashMap<>();
        hypertensionRates.put(SODIUM, new double[]{0.3, 0.7});     // 严格限制钠
        hypertensionRates.put(POTASSIUM, new double[]{1.0, 1.5});  // 增加钾
        hypertensionRates.put(FAT, new double[]{0.7, 0.9});        // 限制脂肪
        DISEASE_NUTRIENT_RATES.put(HealthConditionType.HYPERTENSION, hypertensionRates);
        
        // 肾病
        Map<NutrientType, double[]> kidneyDiseaseRates = new HashMap<>();
        kidneyDiseaseRates.put(PROTEIN, new double[]{0.6, 0.8});   // 限制蛋白质
        kidneyDiseaseRates.put(SODIUM, new double[]{0.4, 0.8});    // 限制钠
        kidneyDiseaseRates.put(POTASSIUM, new double[]{0.6, 0.9}); // 限制钾
        kidneyDiseaseRates.put(PHOSPHORUS, new double[]{0.6, 0.9});// 限制磷
        DISEASE_NUTRIENT_RATES.put(HealthConditionType.KIDNEY_DISEASE, kidneyDiseaseRates);
        
        // 骨质疏松
        Map<NutrientType, double[]> osteoporosisRates = new HashMap<>();
        osteoporosisRates.put(CALCIUM, new double[]{1.2, 1.8});    // 增加钙
        DISEASE_NUTRIENT_RATES.put(HealthConditionType.OSTEOPOROSIS, osteoporosisRates);
        
        // 心脏病
        Map<NutrientType, double[]> heartDiseaseRates = new HashMap<>();
        heartDiseaseRates.put(FAT, new double[]{0.6, 0.8});        // 严格限制脂肪
        heartDiseaseRates.put(SODIUM, new double[]{0.4, 0.7});     // 限制钠
        DISEASE_NUTRIENT_RATES.put(HealthConditionType.HEART_DISEASE, heartDiseaseRates);
    }
    
    /**
     * 配置营养素达成率范围
     * @param userProfile 用户档案（包含健康状况）
     * @return 营养素达成率映射
     */
    public static Map<NutrientType, double[]> configureNutrientAchievementRates(UserProfile userProfile) {
        // 创建营养素达成率映射，使用默认值
        Map<NutrientType, double[]> nutrientRates = new HashMap<>();
        for (NutrientType nutrient : values()) {
            nutrientRates.put(nutrient, nutrient.defaultAchievementRange.clone());
        }
        
        // 根据用户健康状况调整营养素达成率
        if (userProfile != null && userProfile.getHealthConditions() != null) {
            HealthConditionType[] healthConditions = HealthConditionType.fromNames(userProfile.getHealthConditions());
            
            // 创建临时映射，存储所有疾病对每个营养素的要求
            Map<NutrientType, List<double[]>> allDiseaseRequirements = new HashMap<>();
            
            // 收集所有疾病对每个营养素的要求
            for (HealthConditionType condition : healthConditions) {
                if (condition == null) continue;
                
                Map<NutrientType, double[]> diseaseRates = DISEASE_NUTRIENT_RATES.get(condition);
                if (diseaseRates != null) {
                    for (Map.Entry<NutrientType, double[]> entry : diseaseRates.entrySet()) {
                        NutrientType nutrient = entry.getKey();
                        double[] range = entry.getValue();
                        
                        allDiseaseRequirements.computeIfAbsent(nutrient, k -> new ArrayList<>()).add(range);
                    }
                }
            }
            
            // 对于每个营养素，综合考虑所有疾病的要求，取最严格的限制
            for (Map.Entry<NutrientType, List<double[]>> entry : allDiseaseRequirements.entrySet()) {
                NutrientType nutrient = entry.getKey();
                List<double[]> ranges = entry.getValue();
                
                if (!ranges.isEmpty()) {
                    double minRate = ranges.get(0)[0];
                    double maxRate = ranges.get(0)[1];
                    
                    for (int i = 1; i < ranges.size(); i++) {
                        double[] range = ranges.get(i);
                        minRate = Math.max(minRate, range[0]); // 取最大的下限
                        maxRate = Math.min(maxRate, range[1]); // 取最小的上限
                    }
                    
                    if (minRate <= maxRate) {
                        nutrientRates.put(nutrient, new double[]{minRate, maxRate});
                    } else {
                        double middleValue = (minRate + maxRate) / 2;
                        nutrientRates.put(nutrient, new double[]{middleValue, middleValue});
                    }
                }
            }
        }
        
        return nutrientRates;
    }
    
    /**
     * 获取营养素权重映射
     */
    public static Map<NutrientType, Double> getNutrientWeights(UserProfile userProfile) {
        Map<NutrientType, Double> weights = new HashMap<>();
        for (NutrientType nutrient : values()) {
            weights.put(nutrient, nutrient.defaultWeight);
        }
        
        if (userProfile != null && userProfile.getHealthConditions() != null) {
            adjustWeightsByHealthConditions(userProfile.getHealthConditions(), weights);
        }
        
        return weights;
    }
    
    /**
     * 根据用户健康状况调整营养素权重
     * @param healthConditionNames 健康状况名称数组
     * @param nutrientWeights 营养素权重映射
     */
    private static void adjustWeightsByHealthConditions(String[] healthConditionNames, Map<NutrientType, Double> nutrientWeights) {
        if (healthConditionNames == null || healthConditionNames.length == 0) {
            return; // 没有特殊健康状况，使用默认权重
        }
        
        // 将字符串数组转换为枚举数组
        HealthConditionType[] healthConditions = HealthConditionType.fromNames(healthConditionNames);
        
        for (HealthConditionType condition : healthConditions) {
            if (condition == null) continue;
            
            switch (condition) {
                case HYPERTENSION: // 高血压
                    nutrientWeights.put(SODIUM, 1.5);    // 增加钠的权重（更严格控制）
                    nutrientWeights.put(POTASSIUM, 1.2); // 增加钾的权重（鼓励摄入）
                    break;
                    
                case DIABETES: // 糖尿病
                    nutrientWeights.put(CALORIES, 1.3);   // 增加热量的权重
                    nutrientWeights.put(CARBOHYDRATES, 1.5); // 增加碳水的权重（更严格控制）
                    nutrientWeights.put(FAT, 1.2);       // 增加脂肪的权重
                    break;
                    
                case HYPERLIPIDEMIA: // 高血脂
                    nutrientWeights.put(FAT, 1.5);       // 增加脂肪的权重（更严格控制）
                    break;
                    
                case GOUT: // 痛风
                    nutrientWeights.put(PROTEIN, 1.3);   // 增加蛋白质的权重（控制某些蛋白质来源）
                    break;
                    
                case CKD: // 慢性肾病（无透析）
                    nutrientWeights.put(PROTEIN, 1.5);   // 增加蛋白质的权重（严格控制）
                    nutrientWeights.put(POTASSIUM, 1.3); // 增加钾的权重
                    nutrientWeights.put(PHOSPHORUS, 1.3); // 增加磷的权重
                    break;
                    
                case CKD_DIALYSIS: // 慢性肾病（透析）
                    nutrientWeights.put(PROTEIN, 1.2);   // 增加蛋白质的权重（但不同于无透析）
                    nutrientWeights.put(POTASSIUM, 1.5); // 增加钾的权重（更严格控制）
                    break;
                    
                case OSTEOPOROSIS: // 骨质疏松
                    nutrientWeights.put(CALCIUM, 1.5);   // 增加钙的权重
                    nutrientWeights.put(MAGNESIUM, 1.2); // 增加镁的权重
                    break;
                    
                case ANEMIA: // 贫血
                    nutrientWeights.put(IRON, 1.5);      // 增加铁的权重
                    break;
                    
                case KIDNEY_DISEASE: // 肾病
                    nutrientWeights.put(PROTEIN, 1.4);   // 增加蛋白质的权重（严格控制）
                    nutrientWeights.put(POTASSIUM, 1.4); // 增加钾的权重
                    nutrientWeights.put(PHOSPHORUS, 1.4); // 增加磷的权重
                    nutrientWeights.put(SODIUM, 1.3);    // 增加钠的权重
                    break;
                    
                case HEART_DISEASE: // 心脏病
                    nutrientWeights.put(FAT, 1.4);       // 增加脂肪的权重（严格控制）
                    nutrientWeights.put(SODIUM, 1.4);    // 增加钠的权重（严格控制）
                    break;
            }
        }
    }
} 