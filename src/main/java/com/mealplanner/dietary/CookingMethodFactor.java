package com.mealplanner.dietary;

import com.mealplanner.Food;
import com.mealplanner.UserProfile;

/**
 * 烹饪方式偏好因素
 * 用于根据用户偏好的烹饪方式调整食物评分
 */
public class CookingMethodFactor implements DietaryFactor {
    
    private static final int PRIORITY = 50; // 中等优先级
    private static final double PREFERRED_METHOD_BONUS = 1.1; // 喜欢的烹饪方式加分
    
    @Override
    public boolean isFoodSuitable(Food food, UserProfile userProfile) {
        // 烹饪方式偏好不是强制性因素，所有食物都是适合的
        return true;
    }
    
    @Override
    public double calculateScoreAdjustment(Food food, UserProfile userProfile) {
        if (userProfile == null || food == null) {
            return 1.0;
        }
        
        // 检查食物是否使用用户喜欢的烹饪方式
        for (String method : food.getCookingMethods()) {
            if (userProfile.prefersCookingMethod(method)) {
                return PREFERRED_METHOD_BONUS; // 食物使用用户喜欢的烹饪方式，提高评分
            }
        }
        
        return 1.0; // 没有特别的烹饪方式匹配，不调整评分
    }
    
    @Override
    public String getName() {
        return "烹饪方式偏好因素";
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean isMandatory() {
        return false; // 烹饪方式偏好不是强制性因素
    }
} 