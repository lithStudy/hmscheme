package com.mealplanner.dietary;

import com.mealplanner.Food;
import com.mealplanner.UserProfile;

/**
 * 辣度偏好因素
 * 用于检查食物的辣度是否符合用户的接受范围
 */
public class SpicyLevelFactor implements DietaryFactor {
    
    private static final int PRIORITY = 80; // 较高优先级
    
    @Override
    public boolean isFoodSuitable(Food food, UserProfile userProfile) {
        if (userProfile == null || food == null) {
            return true;
        }
        
        // 检查食物的辣度是否超过用户的接受范围
        return userProfile.acceptsSpicyLevel(food.getSpicyLevel());
    }
    
    @Override
    public double calculateScoreAdjustment(Food food, UserProfile userProfile) {
        if (userProfile == null || food == null) {
            return 1.0;
        }
        
        // 如果用户喜欢辣，且食物辣度接近用户偏好，则提高评分
        int userPreference = userProfile.getSpicyPreference();
        int foodLevel = food.getSpicyLevel();
        
        if (userPreference > 0 && foodLevel > 0) {
            // 用户喜欢辣，食物也是辣的
            if (foodLevel == userPreference) {
                return 1.2; // 辣度完全匹配，提高评分
            } else if (Math.abs(foodLevel - userPreference) == 1) {
                return 1.1; // 辣度接近，略微提高评分
            }
        } else if (userPreference == 0 && foodLevel == 0) {
            return 1.1; // 用户不喜欢辣，食物也不辣，略微提高评分
        }
        
        return 1.0; // 默认不调整评分
    }
    
    @Override
    public String getName() {
        return "辣度偏好因素";
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean isMandatory() {
        return true; // 辣度是强制性因素，超过用户接受范围的食物会被排除
    }
} 