package com.mealplanner.genetic.util;

import com.mealplanner.model.NutrientRatio;
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
        NutrientRatio ratio = NutrientRatio.calculateNutrientRatio(userProfile);

        // 计算各营养素的克数
        double carbsGrams = (tdee * ratio.getCarbRatio()) / 4.0;  // 4 kcal/g
        double proteinGrams = (tdee * ratio.getProteinRatio()) / 4.0;  // 4 kcal/g
        double fatGrams = (tdee * ratio.getFatRatio()) / 9.0;  // 9 kcal/g

        // 计算微量元素需求
        double sodiumMg = 2000.0;  // 默认限制钠摄入
        double potassiumMg = 3500.0;  // 默认钾摄入
        double calciumMg = 1000.0;  // 默认钙摄入
        double magnesiumMg = userProfile.getGender().equalsIgnoreCase("male") ? 400.0 : 310.0;  // 镁：性别相关
        double ironMg = userProfile.getGender().equalsIgnoreCase("male") ? 8.0 : 18.0;  // 铁：性别相关
        double phosphorusMg = 700.0;  // 默认磷摄入

        return new Nutrition(tdee, carbsGrams, proteinGrams, fatGrams,
                            calciumMg, potassiumMg, sodiumMg, magnesiumMg,
                            ironMg, phosphorusMg);
    }


}