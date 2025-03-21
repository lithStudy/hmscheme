package com.mealplanner.genetic.util;

import java.util.Map;

import com.mealplanner.model.NutrientRatio;
import com.mealplanner.model.NutrientType;
import com.mealplanner.model.Nutrition;
import com.mealplanner.model.UserProfile;

public class NutritionCalculator {
    private UserProfile userProfile;

    public NutritionCalculator(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    /**
     * 获取用户档案
     * @return 用户档案
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }

    // 计算每日营养需求
    public Nutrition calculateDailyNutrientNeeds() {
        double tdee = userProfile.calculateTDEE();
        

        

        
    }


}