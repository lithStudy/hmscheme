package com.mealplanner.dietary;

import com.mealplanner.Food;
import com.mealplanner.UserProfile;

/**
 * 食物过敏因素
 * 用于检查食物是否含有用户过敏的成分
 */
public class AllergyFactor implements DietaryFactor {
    
    private static final int PRIORITY = 100; // 最高优先级，因为过敏是健康安全问题
    
    @Override
    public boolean isFoodSuitable(Food food, UserProfile userProfile) {
        if (userProfile == null || food == null) {
            return true;
        }
        
        // 检查食物是否含有用户过敏的成分
        for (String allergen : food.getAllergens()) {
            if (userProfile.isAllergicTo(allergen)) {
                return false; // 食物含有用户过敏的成分
            }
        }
        
        return true;
    }
    
    @Override
    public double calculateScoreAdjustment(Food food, UserProfile userProfile) {
        // 过敏是强制性因素，不通过isFoodSuitable的食物会被直接排除
        // 所以这里不需要调整评分
        return 1.0;
    }
    
    @Override
    public String getName() {
        return "过敏因素";
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean isMandatory() {
        return true; // 过敏是强制性因素
    }
} 