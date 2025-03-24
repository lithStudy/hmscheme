package com.mealplanner.model;

import com.alibaba.excel.util.StringUtils;

/**
 * 食物类别枚举
 */
public enum FoodCategory {
    STAPLE("主食", "谷物类", new IntakeRange(100.0, 200.0, 150.0), 0),       // 主食80-150g，默认100g
    VEGETABLE("蔬菜", "蔬菜类", new IntakeRange(100.0, 200.0, 150.0), 0.3),   // 蔬菜100-250g，默认150g
    FRUIT("水果", "水果类", new IntakeRange(100.0, 250.0, 150.0), 0),       // 水果100-250g，默认150g
    MEAT("肉类", "肉类", new IntakeRange(50.0, 100.0, 75.0), 0.2),           // 肉类50-100g，默认75g
    FISH("鱼贝类", "鱼贝类", new IntakeRange(50.0, 100.0, 75.0), 0.2),           // 鱼类50-100g，默认75g
    EGG("蛋类", "蛋类", new IntakeRange(25.0, 75.0, 50.0), 0.05),             // 蛋类25-75g，默认50g
    MILK("乳制品", "乳品类", new IntakeRange(100.0, 300.0, 200.0), 0),      // 乳制品100-300g，默认200g
    OIL("油脂", "油脂类", new IntakeRange(5.0, 15.0, 10.0), 0),            // 油脂5-15g，默认10g
    PASTRY("糕点", "糕饼点心类", new IntakeRange(25.0, 75.0, 50.0), 0.0),      // 糕点25-75g，默认50g
    MUSHROOM("蘑菇", "菇类", new IntakeRange(50.0, 100.0, 75.0), 0.15),       // 蘑菇50-100g，默认75g
    BEAN("豆类", "豆类", new IntakeRange(50.0, 100.0, 75.0), 0.1),           // 豆类50-100g，默认75g
    OTHER("其他", "其他", new IntakeRange(25.0, 75.0, 50.0), 0.0);           // 其他食物25-75g，默认50g
    
    private final String displayName;
    private final String traditionalChineseName;
    private final IntakeRange recommendedIntakeRange;
    private final double selectionProbability; // 选中概率
    
    FoodCategory(String displayName, String traditionalChineseName, IntakeRange recommendedIntakeRange, double selectionProbability) {
        this.displayName = displayName;
        this.traditionalChineseName = traditionalChineseName;
        this.recommendedIntakeRange = recommendedIntakeRange;
        this.selectionProbability = selectionProbability;
    }
    
    /**
     * 获取显示名称
     * @return 中文显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 获取繁体中文名称
     * @return 繁体中文名称
     */
    public String getTraditionalChineseName() {
        return traditionalChineseName;
    }
    
    /**
     * 获取推荐摄入量范围
     * @return 推荐摄入量范围
     */
    public IntakeRange getRecommendedIntakeRange() {
        return recommendedIntakeRange;
    }
    
    /**
     * 获取选中概率
     * @return 选中概率
     */
    public double getSelectionProbability() {
        return selectionProbability;
    }
    
    /**
     * 根据名称获取枚举值
     * @param name 枚举名称（忽略大小写）
     * @return 对应的枚举值，如果不存在则返回OTHER
     */
    public static FoodCategory fromString(String name) {
        if (name == null) {
            return OTHER;
        }
        
        try {
            return FoodCategory.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
    
    /**
     * 根据繁体中文食品分类名称获取对应的枚举值
     * @param traditionalChineseName 繁体中文食品分类名称
     * @return 对应的枚举值，如果不存在则返回OTHER
     */
    public static FoodCategory fromChineseName(String traditionalChineseName) {
        if (StringUtils.isEmpty(traditionalChineseName)) {
            return OTHER;
        }
        
        for (FoodCategory category : FoodCategory.values()) {
            if (category.traditionalChineseName.equals(traditionalChineseName)) {
                return category;
            }
        }
        
        return OTHER;
    }
} 