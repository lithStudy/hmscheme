package com.mealplanner.dietary;

import com.mealplanner.Food;
import com.mealplanner.UserProfile;

/**
 * 不喜欢食物因素
 * 用于检查食物是否是用户不喜欢的
 */
public class DislikedFoodFactor implements DietaryFactor {
    
    private static final int PRIORITY = 90; // 较高优先级
    
    @Override
    public boolean isFoodSuitable(Food food, UserProfile userProfile) {
        if (userProfile == null || food == null) {
            return true;
        }
        
        // 检查食物是否是用户不喜欢的
        return !userProfile.dislikesFood(food.getName());
    }
    
    @Override
    public double calculateScoreAdjustment(Food food, UserProfile userProfile) {
        // 不喜欢的食物是强制性因素，不通过isFoodSuitable的食物会被直接排除
        // 所以这里不需要调整评分
        return 1.0;
    }
    
    @Override
    public String getName() {
        return "不喜欢食物因素";
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean isMandatory() {
        return true; // 不喜欢的食物是强制性因素
    }
} 