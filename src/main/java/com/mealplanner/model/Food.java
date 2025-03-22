package com.mealplanner.model;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
/**
 * 食物类，表示一种食物及其营养和份量信息
 */
@Builder
@Getter
@AllArgsConstructor
public class Food {
    private String name;         // 食物名称
    private FoodCategory category;     // 食物类别
    private Map<NutrientType, Double> nutritionItems; // 营养素列表
    private Portion portion;     // 份量信息
    
    // 新增属性
    private String[] allergens;       // 过敏原（如花生、海鲜、乳制品等）
    private String[] religiousRestrictions; // 宗教限制（如猪肉、牛肉、海鲜等）
    private String[] flavorProfiles;  // 口味特性（如甜、咸、辣、酸等）
    private String[] cookingMethods;  // 烹饪方式（如煎、炒、蒸、炖等）
    private int spicyLevel;           // 辣度等级（0-5，0表示不辣）


    /**
     * 创建一个食物对象
     * @param name 食物名称
     * @param category 食物类别
     * @param nutrition 营养信息
     * @param portion 份量信息
     */
    public Food(String name, FoodCategory category, Map<NutrientType, Double> nutritionItems, Portion portion) {
        this.name = name;
        this.category = category;
        this.nutritionItems = nutritionItems;
        this.portion = portion;
        this.allergens = new String[0];
        this.religiousRestrictions = new String[0];
        this.flavorProfiles = new String[0];
        this.cookingMethods = new String[0];
        this.spicyLevel = 0;
        //如果营养元素不存在，补充默认值0
        for (NutrientType nutrientType : NutrientType.values()) {
            if (!nutritionItems.containsKey(nutrientType)) {
                nutritionItems.put(nutrientType, 0.0);
            }
        }
    }

    /**
     * 兼容旧代码的构造函数，接受字符串类别
     * @param name 食物名称
     * @param categoryStr 食物类别字符串
     * @param nutrition 营养信息
     * @param portion 份量信息
     */
    public Food(String name, String categoryStr, Map<NutrientType, Double> nutritionItems, Portion portion) {
        this(name, FoodCategory.fromString(categoryStr), nutritionItems, portion);
    }

    /**
     * 获取食物的推荐摄入量范围
     * @return 推荐摄入量范围
     */
    public IntakeRange getRecommendedIntakeRange() {
        return category.getRecommendedIntakeRange();
    }
} 