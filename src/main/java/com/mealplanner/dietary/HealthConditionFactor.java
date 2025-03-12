package com.mealplanner.dietary;

import com.mealplanner.Food;
import com.mealplanner.UserProfile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康状况因素
 * 用于根据用户的健康状况调整食物评分
 */
public class HealthConditionFactor implements DietaryFactor {
    
    private static final int PRIORITY = 85; // 较高优先级
    
    // 健康状况与食物类别的关系映射
    private static final Map<String, FoodRecommendation> HEALTH_FOOD_MAP = new HashMap<>();
    
    static {
        // 初始化健康状况与食物推荐/限制的映射
        
        // 高血压
        HEALTH_FOOD_MAP.put("hypertension", new FoodRecommendation(
            new String[]{"vegetable", "fruit", "fish"}, // 推荐食物类别
            new String[]{"oil", "meat"}, // 限制食物类别
            new String[]{"sodium"} // 限制营养素
        ));
        
        // 糖尿病
        HEALTH_FOOD_MAP.put("diabetes", new FoodRecommendation(
            new String[]{"vegetable", "protein"}, 
            new String[]{"staple", "fruit"}, 
            new String[]{"carbohydrates", "sugar"}
        ));
        
        // 高血脂
        HEALTH_FOOD_MAP.put("hyperlipidemia", new FoodRecommendation(
            new String[]{"vegetable", "fruit", "fish"}, 
            new String[]{"oil", "meat", "egg"}, 
            new String[]{"fat", "cholesterol"}
        ));
        
        // 肥胖
        HEALTH_FOOD_MAP.put("obesity", new FoodRecommendation(
            new String[]{"vegetable", "protein"}, 
            new String[]{"staple", "oil", "sugar"}, 
            new String[]{"calories", "fat", "carbohydrates"}
        ));
        
        // 骨质疏松
        HEALTH_FOOD_MAP.put("osteoporosis", new FoodRecommendation(
            new String[]{"milk", "fish", "egg"}, 
            new String[]{}, 
            new String[]{}
        ));
    }
    
    @Override
    public boolean isFoodSuitable(Food food, UserProfile userProfile) {
        // 健康状况因素不是强制性的，所有食物都是适合的
        return true;
    }
    
    @Override
    public double calculateScoreAdjustment(Food food, UserProfile userProfile) {
        if (userProfile == null || food == null || userProfile.getHealthConditions() == null) {
            return 1.0;
        }
        
        double score = 1.0;
        String foodCategory = food.getCategory();
        
        // 检查用户的每种健康状况
        for (String condition : userProfile.getHealthConditions()) {
            FoodRecommendation recommendation = HEALTH_FOOD_MAP.get(condition.toLowerCase());
            if (recommendation != null) {
                // 如果食物类别在推荐列表中，提高评分
                if (Arrays.asList(recommendation.recommendedCategories).contains(foodCategory)) {
                    score *= 1.3; // 大幅提高评分
                }
                
                // 如果食物类别在限制列表中，降低评分
                if (Arrays.asList(recommendation.restrictedCategories).contains(foodCategory)) {
                    score *= 0.7; // 降低评分
                }
                
                // 检查食物的营养成分是否在限制列表中
                for (String nutrient : recommendation.restrictedNutrients) {
                    if (hasHighNutrient(food, nutrient)) {
                        score *= 0.8; // 降低评分
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * 检查食物是否含有高量的特定营养素
     * @param food 食物
     * @param nutrient 营养素名称
     * @return 如果含有高量的该营养素则返回true
     */
    private boolean hasHighNutrient(Food food, String nutrient) {
        switch (nutrient.toLowerCase()) {
            case "sodium":
                return food.getSodium() > 500; // 钠含量高于500mg
            case "fat":
                return food.getFat() > 15; // 脂肪含量高于15g
            case "carbohydrates":
                return food.getCarbohydrates() > 30; // 碳水含量高于30g
            case "sugar":
                // 假设有糖含量的属性
                return false; // 需要实现
            case "cholesterol":
                // 假设有胆固醇含量的属性
                return false; // 需要实现
            case "calories":
                return food.getCalories() > 300; // 热量高于300kcal
            default:
                return false;
        }
    }
    
    @Override
    public String getName() {
        return "健康状况因素";
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean isMandatory() {
        return false; // 健康状况因素不是强制性的
    }
    
    /**
     * 食物推荐类，用于存储健康状况对应的食物推荐和限制
     */
    private static class FoodRecommendation {
        String[] recommendedCategories; // 推荐的食物类别
        String[] restrictedCategories;  // 限制的食物类别
        String[] restrictedNutrients;   // 限制的营养素
        
        public FoodRecommendation(String[] recommendedCategories, String[] restrictedCategories, String[] restrictedNutrients) {
            this.recommendedCategories = recommendedCategories;
            this.restrictedCategories = restrictedCategories;
            this.restrictedNutrients = restrictedNutrients;
        }
    }
} 