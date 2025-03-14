package com.mealplanner.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 营养素类型枚举
 * 定义了系统中支持的所有营养素类型
 */
public enum NutrientType {
    CALORIES("calories", "热量", true, 3.0),
    CARBOHYDRATES("carbohydrates", "碳水化合物", true, 1.0),
    PROTEIN("protein", "蛋白质", true, 1.0),
    FAT("fat", "脂肪", true, 0.8),
    CALCIUM("calcium", "钙", false, 0.7),
    POTASSIUM("potassium", "钾", false, 0.7),
    SODIUM("sodium", "钠", true, 0.8),
    MAGNESIUM("magnesium", "镁", false, 0.7),
    IRON("iron", "铁", false, 0.7),
    PHOSPHORUS("phosphorus", "磷", false, 0.7);
    
    // 营养素的英文名称
    private final String name;
    
    // 营养素的中文名称
    private final String displayName;
    
    // 是否默认惩罚过量
    private final boolean defaultPenalizeExcess;
    
    // 默认权重
    private final double defaultWeight;
    
    /**
     * 构造函数
     * @param name 营养素名称
     * @param displayName 显示名称
     * @param defaultPenalizeExcess 默认是否惩罚过量
     * @param defaultWeight 默认权重
     */
    NutrientType(String name, String displayName, boolean defaultPenalizeExcess, double defaultWeight) {
        this.name = name;
        this.displayName = displayName;
        this.defaultPenalizeExcess = defaultPenalizeExcess;
        this.defaultWeight = defaultWeight;
    }
    
    /**
     * 获取营养素名称
     * @return 营养素名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 获取默认是否惩罚过量
     * @return 默认是否惩罚过量
     */
    public boolean isDefaultPenalizeExcess() {
        return defaultPenalizeExcess;
    }
    
    /**
     * 获取默认权重
     * @return 默认权重
     */
    public double getDefaultWeight() {
        return defaultWeight;
    }
    
    /**
     * 根据名称获取营养素类型
     * @param name 营养素名称
     * @return 营养素类型，如果找不到则返回null
     */
    public static NutrientType fromName(String name) {
        if (name == null) {
            return null;
        }
        
        for (NutrientType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        
        return null;
    }

    public static Map<NutrientType, Double> getNutrientWeights(UserProfile userProfile) {
        Map<NutrientType, Double> nutrientWeights = new HashMap<>();
        
        // 使用枚举中定义的默认权重
        for (NutrientType type : values()) {
            nutrientWeights.put(type, type.getDefaultWeight());
        }

        if (userProfile != null) {
            adjustWeightsByHealthConditions(userProfile.getHealthConditions(), nutrientWeights);
        }

        return nutrientWeights;
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