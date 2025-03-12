package com.mealplanner.dietary;

import com.mealplanner.Food;
import com.mealplanner.UserProfile;

/**
 * 饮食影响因素接口
 * 所有影响食谱生成的因素都应实现此接口
 */
public interface DietaryFactor {
    
    /**
     * 检查食物是否符合该饮食因素的要求
     * @param food 要检查的食物
     * @param userProfile 用户档案
     * @return 如果食物符合要求则返回true，否则返回false
     */
    boolean isFoodSuitable(Food food, UserProfile userProfile);
    
    /**
     * 计算食物在该饮食因素下的评分调整
     * @param food 要评分的食物
     * @param userProfile 用户档案
     * @return 评分调整系数（大于1表示提高评分，小于1表示降低评分，等于1表示不调整）
     */
    double calculateScoreAdjustment(Food food, UserProfile userProfile);
    
    /**
     * 获取该饮食因素的名称
     * @return 饮食因素名称
     */
    String getName();
    
    /**
     * 获取该饮食因素的优先级
     * 优先级越高，在食物筛选时越早被考虑
     * @return 优先级（0-100，数值越大优先级越高）
     */
    int getPriority();
    
    /**
     * 判断该因素是否为强制性的
     * 强制性因素会直接排除不符合要求的食物，而非强制性因素只会影响评分
     * @return 如果是强制性因素则返回true，否则返回false
     */
    boolean isMandatory();
} 