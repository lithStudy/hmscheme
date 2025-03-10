package com.mealplanner;

public class UserProfile {
    private double weight;      // 体重(kg)
    private double height;      // 身高(cm)
    private int age;           // 年龄
    private String gender;     // 性别
    private double activityLevel; // 活动系数
    private String[] healthConditions; // 健康状况（多种慢病）

    public UserProfile(double weight, double height, int age, String gender, 
                      double activityLevel, String[] healthConditions) {
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.healthConditions = healthConditions;
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

    // Getters
    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public double getActivityLevel() { return activityLevel; }
    public String[] getHealthConditions() {
        return healthConditions;
    }
    
    /**
     * 设置健康状况
     * @param healthConditions 健康状况数组
     */
    public void setHealthConditions(String[] healthConditions) {
        this.healthConditions = healthConditions;
    }
} 