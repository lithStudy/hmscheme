package com.mealplanner.dietary;

import com.mealplanner.Food;
import com.mealplanner.UserProfile;

/**
 * 口味偏好因素
 * 用于根据用户的口味偏好调整食物评分
 */
public class FlavorPreferenceFactor implements DietaryFactor {
    
    private static final int PRIORITY = 60; // 中等优先级
    private static final double PREFERRED_FLAVOR_BONUS = 1.2; // 喜欢的口味加分
    
    @Override
    public boolean isFoodSuitable(Food food, UserProfile userProfile) {
        // 口味偏好不是强制性因素，所有食物都是适合的
        return true;
    }
    
    @Override
    public double calculateScoreAdjustment(Food food, UserProfile userProfile) {
        if (userProfile == null || food == null) {
            return 1.0;
        }
        
        // 检查食物是否具有用户喜欢的口味
        for (String flavor : food.getFlavorProfiles()) {
            if (userProfile.likesFlavor(flavor)) {
                return PREFERRED_FLAVOR_BONUS; // 食物具有用户喜欢的口味，提高评分
            }
        }
        
        return 1.0; // 没有特别的口味匹配，不调整评分
    }
    
    @Override
    public String getName() {
        return "口味偏好因素";
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean isMandatory() {
        return false; // 口味偏好不是强制性因素
    }
} 