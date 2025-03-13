package com.mealplanner.model;

public class UserProfile {
    private double weight;      // 体重(kg)
    private double height;      // 身高(cm)
    private int age;           // 年龄
    private String gender;     // 性别
    private double activityLevel; // 活动系数
    private String[] healthConditions; // 健康状况（多种慢病）
    
    // 新增属性
    private String[] allergies;          // 过敏食物
    private String[] religiousBeliefs;   // 宗教信仰
    private String[] flavorPreferences;  // 口味偏好
    private String[] dislikedFoods;      // 不喜欢的食物
    private int spicyPreference;         // 辣度偏好（0-5，0表示不能接受辣）
    private String[] cookingMethodPreferences; // 偏好的烹饪方式

    public UserProfile(double weight, double height, int age, String gender, 
                      double activityLevel, String[] healthConditions) {
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
    
    /**
     * 创建一个完整的用户档案（包含所有属性）
     */
    public UserProfile(double weight, double height, int age, String gender, 
                      double activityLevel, String[] healthConditions,
                      String[] allergies, String[] religiousBeliefs, 
                      String[] flavorPreferences, String[] dislikedFoods,
                      int spicyPreference, String[] cookingMethodPreferences) {
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.healthConditions = healthConditions != null ? healthConditions : new String[0];
        this.allergies = allergies != null ? allergies : new String[0];
        this.religiousBeliefs = religiousBeliefs != null ? religiousBeliefs : new String[0];
        this.flavorPreferences = flavorPreferences != null ? flavorPreferences : new String[0];
        this.dislikedFoods = dislikedFoods != null ? dislikedFoods : new String[0];
        this.spicyPreference = spicyPreference;
        this.cookingMethodPreferences = cookingMethodPreferences != null ? cookingMethodPreferences : new String[0];
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

    // Getters
    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public double getActivityLevel() { return activityLevel; }
    public String[] getHealthConditions() {
        return healthConditions;
    }
    
    // Getters for new fields
    public String[] getAllergies() { return allergies; }
    public String[] getReligiousBeliefs() { return religiousBeliefs; }
    public String[] getFlavorPreferences() { return flavorPreferences; }
    public String[] getDislikedFoods() { return dislikedFoods; }
    public int getSpicyPreference() { return spicyPreference; }
    public String[] getCookingMethodPreferences() { return cookingMethodPreferences; }
    
    // Setters
    /**
     * 设置健康状况
     * @param healthConditions 健康状况数组
     */
    public void setHealthConditions(String[] healthConditions) {
        this.healthConditions = healthConditions;
    }
    
    /**
     * 设置过敏食物
     * @param allergies 过敏食物数组
     */
    public void setAllergies(String[] allergies) {
        this.allergies = allergies;
    }
    
    /**
     * 设置宗教信仰
     * @param religiousBeliefs 宗教信仰数组
     */
    public void setReligiousBeliefs(String[] religiousBeliefs) {
        this.religiousBeliefs = religiousBeliefs;
    }
    
    /**
     * 设置口味偏好
     * @param flavorPreferences 口味偏好数组
     */
    public void setFlavorPreferences(String[] flavorPreferences) {
        this.flavorPreferences = flavorPreferences;
    }
    
    /**
     * 设置不喜欢的食物
     * @param dislikedFoods 不喜欢的食物数组
     */
    public void setDislikedFoods(String[] dislikedFoods) {
        this.dislikedFoods = dislikedFoods;
    }
    
    /**
     * 设置辣度偏好
     * @param spicyPreference 辣度偏好（0-5）
     */
    public void setSpicyPreference(int spicyPreference) {
        this.spicyPreference = Math.max(0, Math.min(5, spicyPreference));
    }
    
    /**
     * 设置偏好的烹饪方式
     * @param cookingMethodPreferences 偏好的烹饪方式数组
     */
    public void setCookingMethodPreferences(String[] cookingMethodPreferences) {
        this.cookingMethodPreferences = cookingMethodPreferences;
    }
} 