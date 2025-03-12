package com.mealplanner.dietary;

import com.mealplanner.Food;
import com.mealplanner.UserProfile;

/**
 * 宗教饮食限制因素
 * 用于检查食物是否符合用户的宗教饮食限制
 */
public class ReligiousRestrictionFactor implements DietaryFactor {
    
    private static final int PRIORITY = 95; // 非常高的优先级，仅次于过敏
    
    @Override
    public boolean isFoodSuitable(Food food, UserProfile userProfile) {
        if (userProfile == null || food == null) {
            return true;
        }
        
        // 检查食物是否违反用户的宗教限制
        for (String restriction : food.getReligiousRestrictions()) {
            if (userProfile.hasReligiousRestrictionFor(restriction)) {
                return false; // 食物违反用户的宗教限制
            }
        }
        
        return true;
    }
    
    @Override
    public double calculateScoreAdjustment(Food food, UserProfile userProfile) {
        // 宗教限制是强制性因素，不通过isFoodSuitable的食物会被直接排除
        // 所以这里不需要调整评分
        return 1.0;
    }
    
    @Override
    public String getName() {
        return "宗教限制因素";
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean isMandatory() {
        return true; // 宗教限制是强制性因素
    }
} 