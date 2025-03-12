package com.mealplanner.dietary;

import com.mealplanner.Food;
import com.mealplanner.UserProfile;

import java.util.List;

/**
 * 饮食评估器
 * 用于评估食物是否符合用户的饮食要求，并计算食物的偏好评分
 */
public class DietaryEvaluator {
    
    private DietaryFactorRegistry registry;
    private UserProfile userProfile;
    
    /**
     * 创建一个饮食评估器
     * @param userProfile 用户档案
     */
    public DietaryEvaluator(UserProfile userProfile) {
        this.registry = DietaryFactorRegistry.getInstance();
        this.userProfile = userProfile;
    }
    
    /**
     * 检查食物是否符合用户的饮食要求
     * @param food 要检查的食物
     * @return 如果食物符合所有强制性饮食因素的要求则返回true，否则返回false
     */
    public boolean isFoodSuitable(Food food) {
        if (food == null || userProfile == null) {
            return true;
        }
        
        // 检查所有强制性饮食因素
        List<DietaryFactor> mandatoryFactors = registry.getMandatoryFactors();
        for (DietaryFactor factor : mandatoryFactors) {
            if (!factor.isFoodSuitable(food, userProfile)) {
                return false; // 如果不符合任何一个强制性因素的要求，则食物不适合
            }
        }
        
        return true;
    }
    
    /**
     * 计算食物的偏好评分调整系数
     * @param food 要评分的食物
     * @return 偏好评分调整系数（大于1表示提高评分，小于1表示降低评分，等于1表示不调整）
     */
    public double calculatePreferenceScore(Food food) {
        if (food == null || userProfile == null) {
            return 1.0;
        }
        
        double score = 1.0;
        
        // 应用所有非强制性饮食因素的评分调整
        List<DietaryFactor> nonMandatoryFactors = registry.getNonMandatoryFactors();
        for (DietaryFactor factor : nonMandatoryFactors) {
            score *= factor.calculateScoreAdjustment(food, userProfile);
        }
        
        return score;
    }
    
    /**
     * 获取用户档案
     * @return 用户档案
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }
    
    /**
     * 设置用户档案
     * @param userProfile 用户档案
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
    
    /**
     * 获取饮食因素注册表
     * @return 饮食因素注册表
     */
    public DietaryFactorRegistry getRegistry() {
        return registry;
    }
    
    /**
     * 注册一个新的饮食因素
     * @param factor 要注册的饮食因素
     */
    public void registerFactor(DietaryFactor factor) {
        registry.registerFactor(factor);
    }
    
    /**
     * 移除一个饮食因素
     * @param factorName 要移除的饮食因素名称
     * @return 如果成功移除则返回true，否则返回false
     */
    public boolean removeFactor(String factorName) {
        return registry.removeFactor(factorName);
    }
} 