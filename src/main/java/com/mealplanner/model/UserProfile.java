package com.mealplanner.model;

import lombok.Getter;

@Getter
public class UserProfile {
    private double weight;      // 体重(kg)
    private double height;      // 身高(cm)
    private int age;           // 年龄
    private String gender;     // 性别
    private double activityLevel; // 活动系数
    private HealthConditionType[] healthConditions; // 健康状况（多种慢病）
    
    // 新增属性
    private String[] allergies;          // 过敏食物
    private String[] religiousBeliefs;   // 宗教信仰
    private String[] flavorPreferences;  // 口味偏好
    private String[] dislikedFoods;      // 不喜欢的食物
    private int spicyPreference;         // 辣度偏好（0-5，0表示不能接受辣）
    private String[] cookingMethodPreferences; // 偏好的烹饪方式

    public UserProfile(double weight, double height, int age, String gender, 
                      double activityLevel, HealthConditionType[] healthConditions) {
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.healthConditions = healthConditions;
        this.allergies = new String[0];
        this.religiousBeliefs = new String[0];
        this.flavorPreferences = new String[0];
        this.dislikedFoods = new String[0];
        this.spicyPreference = 2; // 默认中等辣度
        this.cookingMethodPreferences = new String[0];
    }
    

    // 计算BMR（基础代谢率）- 使用Mifflin-St Jeor公式
    public double calculateBMR() {
        if (gender.equalsIgnoreCase("male")) {
            return (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            return (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }
    }

    // 计算TDEE（每日总能量消耗）
    public double calculateTDEE() {
        return calculateBMR() * activityLevel;
    }
    
    /**
     * 检查用户是否对特定食物过敏
     * @param allergen 要检查的过敏原
     * @return 如果用户对该食物过敏则返回true
     */
    public boolean isAllergicTo(String allergen) {
        for (String a : allergies) {
            if (a.equalsIgnoreCase(allergen)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查用户是否有特定宗教限制
     * @param foodRestriction 要检查的食物限制
     * @return 如果用户有该宗教限制则返回true
     */
    public boolean hasReligiousRestrictionFor(String foodRestriction) {
        for (String belief : religiousBeliefs) {
            // 根据宗教信仰判断食物限制
            if (belief.equalsIgnoreCase("islam") && 
                (foodRestriction.equalsIgnoreCase("pork") || foodRestriction.equalsIgnoreCase("alcohol"))) {
                return true;
            } else if (belief.equalsIgnoreCase("hinduism") && 
                      foodRestriction.equalsIgnoreCase("beef")) {
                return true;
            } else if (belief.equalsIgnoreCase("buddhism") && 
                      (foodRestriction.equalsIgnoreCase("meat") || foodRestriction.equalsIgnoreCase("onion") || 
                       foodRestriction.equalsIgnoreCase("garlic"))) {
                return true;
            } else if (belief.equalsIgnoreCase("judaism") && 
                      (foodRestriction.equalsIgnoreCase("pork") || foodRestriction.equalsIgnoreCase("shellfish") || 
                       foodRestriction.equalsIgnoreCase("mixing_meat_dairy"))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查用户是否喜欢特定口味
     * @param flavor 要检查的口味
     * @return 如果用户喜欢该口味则返回true
     */
    public boolean likesFlavor(String flavor) {
        for (String f : flavorPreferences) {
            if (f.equalsIgnoreCase(flavor)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查用户是否不喜欢特定食物
     * @param food 要检查的食物
     * @return 如果用户不喜欢该食物则返回true
     */
    public boolean dislikesFood(String food) {
        for (String f : dislikedFoods) {
            if (f.equalsIgnoreCase(food)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查用户是否喜欢特定烹饪方式
     * @param method 要检查的烹饪方式
     * @return 如果用户喜欢该烹饪方式则返回true
     */
    public boolean prefersCookingMethod(String method) {
        for (String m : cookingMethodPreferences) {
            if (m.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查食物的辣度是否符合用户偏好
     * @param foodSpicyLevel 食物的辣度等级
     * @return 如果辣度符合用户偏好则返回true
     */
    public boolean acceptsSpicyLevel(int foodSpicyLevel) {
        return foodSpicyLevel <= spicyPreference;
    }

    
} 