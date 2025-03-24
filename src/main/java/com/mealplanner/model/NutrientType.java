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
    CALORIES("calories", "热量", "kcal", true, 3.0, new double[]{0.9, 1.1}, 2000.0),
    CARBOHYDRATES("carbohydrates", "碳水化合物", "g", true, 1.0, new double[]{0.85, 1.15}, 250.0),
    PROTEIN("protein", "蛋白质", "g", true, 1.0, new double[]{0.9, 1.2}, 70.0),
    FAT("fat", "脂肪", "g", true, 0.8, new double[]{0.7, 1.1}, 60.0),
    FIBER("fiber", "膳食纤维", "g", true, 0.7, new double[]{0.6, 1.5}, 30.0),
    CALCIUM("calcium", "钙", "mg", false, 0.7, new double[]{0.8, 1.5}, 1000.0),
    POTASSIUM("potassium", "钾", "mg", false, 0.7, new double[]{0.8, 1.5}, 3500.0),
    SODIUM("sodium", "钠", "mg", true, 0.8, new double[]{0.5, 1.0}, 2000.0),
    MAGNESIUM("magnesium", "镁", "mg", false, 0.7, new double[]{0.8, 1.5}, 350.0),
    IRON("iron", "铁", "mg", false, 0.7, new double[]{0.5, 1.5}, 10.0),
    PHOSPHORUS("phosphorus", "磷", "mg", false, 0.7, new double[]{0.5, 1.5}, 700.0),
    // ZINC("zinc", "锌", "mg", false, 0.7, new double[]{0.8, 1.5}, 10.0),
    // VITAMIN_A("vitamin_a", "维生素A", "μg", false, 0.7, new double[]{0.8, 1.5}, 800.0),
    // VITAMIN_C("vitamin_c", "维生素C", "mg", false, 0.7, new double[]{0.8, 1.5}, 80.0),
    // VITAMIN_D("vitamin_d", "维生素D", "μg", false, 0.7, new double[]{0.8, 1.5}, 10.0),
    // VITAMIN_E("vitamin_e", "维生素E", "mg", false, 0.7, new double[]{0.8, 1.5}, 15.0),
    ;
    
    // 营养素的英文名称
    private final String name;
    
    // 营养素的中文名称
    private final String displayName;
    
    // 营养素的单位
    private final String unit;
    
    // 是否默认惩罚过量
    private final boolean defaultPenalizeExcess;
    
    // 默认权重
    private final double defaultWeight;
    
    // 默认达成率范围 [最小达成率, 最大达成率]
    private final double[] defaultAchievementRange;
    
    // 默认每日摄入量（单位取决于营养素类型：热量kcal，宏量营养素g，微量元素mg或μg）
    private final double defaultDailyIntake;
    
    // 存储疾病特定的营养素达成率调整
    private static final Map<HealthConditionType, Map<NutrientType, double[]>> DISEASE_NUTRIENT_RATES = new HashMap<>();
    
    // 疾病相关的营养素调整因子
    private static final Map<String, Map<NutrientType, Double>> HEALTH_CONDITION_ADJUSTMENT = new HashMap<>();
    
    static {
        initializeDiseaseNutrientRates();
        initializeDiseaseNutrientIntake();
    }
    
    /**
     * 构造函数
     */
    NutrientType(String name, String displayName, String unit, boolean defaultPenalizeExcess, 
                double defaultWeight, double[] defaultAchievementRange, double defaultDailyIntake) {
        this.name = name;
        this.displayName = displayName;
        this.unit = unit;
        this.defaultPenalizeExcess = defaultPenalizeExcess;
        this.defaultWeight = defaultWeight;
        this.defaultAchievementRange = defaultAchievementRange;
        this.defaultDailyIntake = defaultDailyIntake;
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
     * 初始化疾病相关的营养素摄入量调整因子
     */
    private static void initializeDiseaseNutrientIntake() {
        // 高血压调整
        Map<NutrientType, Double> hypertensionAdjustments = new HashMap<>();
        hypertensionAdjustments.put(SODIUM, 0.6); // 高血压患者钠摄入量降至正常人的60%
        hypertensionAdjustments.put(POTASSIUM, 1.2); // 高血压患者钾摄入量增至正常人的120%
        hypertensionAdjustments.put(CALCIUM, 1.1); // 钙略微增加
        HEALTH_CONDITION_ADJUSTMENT.put("hypertension", hypertensionAdjustments);
        
        // 糖尿病调整
        Map<NutrientType, Double> diabetesAdjustments = new HashMap<>();
        diabetesAdjustments.put(CARBOHYDRATES, 0.7); // 糖尿病患者碳水化合物摄入量降至正常人的70%
        diabetesAdjustments.put(FIBER, 1.3); // 增加膳食纤维摄入
        diabetesAdjustments.put(FAT, 0.9); // 减少脂肪摄入
        HEALTH_CONDITION_ADJUSTMENT.put("diabetes", diabetesAdjustments);
        
        // 骨质疏松调整
        Map<NutrientType, Double> osteoporosisAdjustments = new HashMap<>();
        osteoporosisAdjustments.put(CALCIUM, 1.5); // 骨质疏松患者钙摄入量增至正常人的150%
        // osteoporosisAdjustments.put(VITAMIN_D, 2.0); // 维生素D增加一倍
        osteoporosisAdjustments.put(MAGNESIUM, 1.2); // 增加镁摄入
        HEALTH_CONDITION_ADJUSTMENT.put("osteoporosis", osteoporosisAdjustments);
    }
    
    
    /**
     * 获取健康状况名称数组
     * @param healthConditions 健康状况类型数组
     * @return 健康状况名称数组
     */
    private static String[] getHealthConditionNames(HealthConditionType[] healthConditions) {
        if (healthConditions == null || healthConditions.length == 0) {
            return new String[0];
        }
        
        String[] names = new String[healthConditions.length];
        for (int i = 0; i < healthConditions.length; i++) {
            names[i] = healthConditions[i].getName();
        }
        
        return names;
    }
    
    /**
     * 根据用户配置文件获取个体化的每日营养素摄入量
     * @param userProfile 用户配置文件
     * @return 个体化后的每日摄入量
     */
    public double getPersonalizedDailyIntake(UserProfile userProfile) {
        // 从默认值开始
        double baseIntake = defaultDailyIntake;
        
        // 根据性别调整
        if (userProfile.getGender().equalsIgnoreCase("F")) {
            // 女性铁需求量通常高于男性
            if (this == IRON) {
                baseIntake = 18.0; // 女性铁摄入量推荐值
            }
            
            if (this == MAGNESIUM) {
                baseIntake = 310.0; // 女性镁摄入量推荐值
            }

        }
        
        // 根据年龄调整
        if (userProfile.getAge() > 50) {
            // 50岁以上钙需求增加
            if (this == CALCIUM) {
                baseIntake *= 1.2;
            }
        }
        
        // // 根据体重调整蛋白质需求
        // if (this == PROTEIN) {
        //     // 蛋白质推荐量：0.8g/kg体重/天
        //     baseIntake = userProfile.getWeight() * 0.8;
        //     // 运动者增加蛋白质摄入
        //     if (userProfile.getActivityLevel() > 1.7) {
        //         baseIntake *= 1.3;
        //     }
        // }
        
        // // 根据活动水平调整热量需求
        // if (this == CALORIES) {
        //     baseIntake *= userProfile.getActivityLevel();
        // }
        
        // 根据疾病情况调整
        String[] healthConditionNames = getHealthConditionNames(userProfile.getHealthConditions());
        for (String condition : healthConditionNames) {
            if (HEALTH_CONDITION_ADJUSTMENT.containsKey(condition)) {
                Double factor = HEALTH_CONDITION_ADJUSTMENT.get(condition).get(this);
                if (factor != null) {
                    baseIntake *= factor;
                }
            }
        }
        
        return baseIntake;
    }
    
    /**
     * 获取所有营养素的个体化每日摄入量
     * @param userProfile 用户配置文件
     * @return 营养素-摄入量映射
     */
    public static Map<NutrientType, Double> getAllPersonalizedDailyIntakes(UserProfile userProfile) {
        Map<NutrientType, Double> results = new HashMap<>();
        
        for (NutrientType nutrient : values()) {
            results.put(nutrient, nutrient.getPersonalizedDailyIntake(userProfile));
        }
        
        return results;
    }
    
    /**
     * 配置基于用户配置文件的营养素达成率范围
     * @param userProfile 用户配置文件
     * @return 营养素达成率范围映射
     */
    public static Map<NutrientType, double[]> configureNutrientAchievementRates(UserProfile userProfile) {
        // 使用现有的getNutrientRates方法
        return getNutrientRates(userProfile);
    }
    
   

    public static Map<NutrientType, Double> getDailyIntakes(UserProfile userProfile) {
        Map<NutrientType, Double> macronutrientIntakes = NutrientType.getAllNutrientIntakes(userProfile);
        return macronutrientIntakes;
    }


    
    /**
     * 获取所有营养素的每日摄入量建议
     * @param userProfile 用户配置文件
     * @return 宏量营养素每日摄入量建议
     */
    private static Map<NutrientType, Double> getAllNutrientIntakes(UserProfile userProfile) {
        Map<NutrientType, Double> intakes = new HashMap<>();

        double tdee = userProfile.calculateTDEE();
        NutrientRatio ratio = NutrientRatio.calculateNutrientRatio(userProfile);

        // 计算各营养素的克数
        double carbsGrams = (tdee * ratio.getCarbRatio()) / 4.0;  // 4 kcal/g
        double proteinGrams = (tdee * ratio.getProteinRatio()) / 4.0;  // 4 kcal/g
        double fatGrams = (tdee * ratio.getFatRatio()) / 9.0;  // 9 kcal/g

        intakes.put(CALORIES, tdee);
        intakes.put(CARBOHYDRATES, carbsGrams);
        intakes.put(PROTEIN, proteinGrams);
        intakes.put(FAT, fatGrams);


        // 使用循环整合微量元素
        for(NutrientType nutrient : values()) {
            if(nutrient!=CALORIES && nutrient!=CARBOHYDRATES && nutrient!=PROTEIN && nutrient!=FAT) {
                intakes.put(nutrient, nutrient.getPersonalizedDailyIntake(userProfile));
            }
        }

        return intakes;
    }
    
    /**
     * 配置营养素达成率范围
     * @param userProfile 用户档案（包含健康状况）
     * @return 营养素达成率映射
     */
    public static Map<NutrientType, double[]> getNutrientRates(UserProfile userProfile) {
        // 创建营养素达成率映射，使用默认值
        Map<NutrientType, double[]> nutrientRates = new HashMap<>();
        for (NutrientType nutrient : values()) {
            nutrientRates.put(nutrient, nutrient.defaultAchievementRange.clone());
        }
        
        // 根据用户健康状况调整营养素达成率
        if (userProfile != null && userProfile.getHealthConditions() != null) {
            HealthConditionType[] healthConditions = userProfile.getHealthConditions();
            
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
    private static void adjustWeightsByHealthConditions(HealthConditionType[] healthConditions, Map<NutrientType, Double> nutrientWeights) {
        if (healthConditions == null || healthConditions.length == 0) {
            return; // 没有特殊健康状况，使用默认权重
        }
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

    /**
     * 获取营养素默认权重
     * @return 营养素默认权重
     */
    public static Map<NutrientType, Double> initNutrientItem() {
        Map<NutrientType, Double> nutrientItems = new HashMap<>();
        for (NutrientType nutrientType : values()) {
            nutrientItems.put(nutrientType, 0.0);
        }
        return nutrientItems;
    }
} 