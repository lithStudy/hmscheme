package com.mealplanner.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康状况/疾病类型枚举
 * 定义了系统中支持的所有健康状况/疾病类型
 */
public enum HealthConditionType {
    HYPERTENSION("hypertension", "高血压"),
    DIABETES("diabetes", "糖尿病"),
    HYPERLIPIDEMIA("hyperlipidemia", "高血脂"),
    GOUT("gout", "痛风"),
    CKD("ckd", "慢性肾病(无透析)"),
    CKD_DIALYSIS("ckd_dialysis", "慢性肾病(透析)"),
    FATTY_LIVER("fatty_liver", "脂肪肝"),
    CORONARY_HEART("coronary_heart", "冠心病"),
    OSTEOPOROSIS("osteoporosis", "骨质疏松"),
    ANEMIA("anemia", "贫血"),
    KIDNEY_DISEASE("kidney_disease", "肾病"),
    HEART_DISEASE("heart_disease", "心脏病");
    
    // 健康状况/疾病的英文名称
    private final String name;
    
    // 健康状况/疾病的中文名称
    private final String displayName;
    
    // 静态映射，用于根据名称快速查找枚举值
    private static final Map<String, HealthConditionType> nameMap = new HashMap<>();
    
    // 初始化静态映射
    static {
        for (HealthConditionType type : values()) {
            nameMap.put(type.name.toLowerCase(), type);
        }
    }
    
    /**
     * 构造函数
     * @param name 健康状况/疾病名称
     * @param displayName 显示名称
     */
    HealthConditionType(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
    
    /**
     * 获取健康状况/疾病名称
     * @return 健康状况/疾病名称
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
     * 根据名称获取健康状况/疾病类型
     * @param name 健康状况/疾病名称
     * @return 健康状况/疾病类型，如果找不到则返回null
     */
    public static HealthConditionType fromName(String name) {
        if (name == null) {
            return null;
        }
        
        return nameMap.get(name.toLowerCase());
    }
    
    /**
     * 将字符串数组转换为健康状况/疾病类型数组
     * @param names 健康状况/疾病名称数组
     * @return 健康状况/疾病类型数组
     */
    public static HealthConditionType[] fromNames(String[] names) {
        if (names == null || names.length == 0) {
            return new HealthConditionType[0];
        }
        
        HealthConditionType[] types = new HealthConditionType[names.length];
        for (int i = 0; i < names.length; i++) {
            types[i] = fromName(names[i]);
        }
        
        return types;
    }
} 