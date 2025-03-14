package com.mealplanner.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

/**
 * 膳食方案Excel导出数据模型
 */
@ContentRowHeight(20)
@HeadRowHeight(25)
public class MealSolutionExcelData {
    
    // 基本信息
    @ExcelProperty("方案ID")
    @ColumnWidth(10)
    private String solutionId;
    
    @ExcelProperty("食物清单")
    @ColumnWidth(50)
    private String foodList;
    
    // 营养素比较情况
    @ExcelProperty("热量(kcal)达成率")
    @ColumnWidth(15)
    private String caloriesAchievement;
    
    @ExcelProperty("碳水(g)达成率")
    @ColumnWidth(15)
    private String carbsAchievement;
    
    @ExcelProperty("蛋白质(g)达成率")
    @ColumnWidth(15)
    private String proteinAchievement;
    
    @ExcelProperty("脂肪(g)达成率")
    @ColumnWidth(15)
    private String fatAchievement;
    
    @ExcelProperty("钙(mg)达成率")
    @ColumnWidth(15)
    private String calciumAchievement;
    
    @ExcelProperty("钾(mg)达成率")
    @ColumnWidth(15)
    private String potassiumAchievement;
    
    @ExcelProperty("钠(mg)达成率")
    @ColumnWidth(15)
    private String sodiumAchievement;
    
    @ExcelProperty("镁(mg)达成率")
    @ColumnWidth(15)
    private String magnesiumAchievement;
    
    // 三大营养素热量比例
    @ExcelProperty("碳水热量比例")
    @ColumnWidth(15)
    private String carbsCaloriePercentage;
    
    @ExcelProperty("蛋白质热量比例")
    @ColumnWidth(15)
    private String proteinCaloriePercentage;
    
    @ExcelProperty("脂肪热量比例")
    @ColumnWidth(15)
    private String fatCaloriePercentage;
    
    // 目标评分
    @ExcelProperty("营养素达成率得分")
    @ColumnWidth(15)
    private String nutrientScore;
    
    @ExcelProperty("营养素偏离度")
    @ColumnWidth(15)
    private String deviationScore;
    
    @ExcelProperty("平衡目标得分")
    @ColumnWidth(15)
    private String balanceScore;
    
    @ExcelProperty("多样性目标得分")
    @ColumnWidth(15)
    private String diversityScore;
    
    @ExcelProperty("偏好目标得分")
    @ColumnWidth(15)
    private String preferenceScore;
    
    @ExcelProperty("总体评分")
    @ColumnWidth(15)
    private String overallScore;

    // Getters and Setters
    public String getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(String solutionId) {
        this.solutionId = solutionId;
    }

    public String getFoodList() {
        return foodList;
    }

    public void setFoodList(String foodList) {
        this.foodList = foodList;
    }

    public String getCaloriesAchievement() {
        return caloriesAchievement;
    }

    public void setCaloriesAchievement(String caloriesAchievement) {
        this.caloriesAchievement = caloriesAchievement;
    }

    public String getCarbsAchievement() {
        return carbsAchievement;
    }

    public void setCarbsAchievement(String carbsAchievement) {
        this.carbsAchievement = carbsAchievement;
    }

    public String getProteinAchievement() {
        return proteinAchievement;
    }

    public void setProteinAchievement(String proteinAchievement) {
        this.proteinAchievement = proteinAchievement;
    }

    public String getFatAchievement() {
        return fatAchievement;
    }

    public void setFatAchievement(String fatAchievement) {
        this.fatAchievement = fatAchievement;
    }

    public String getCalciumAchievement() {
        return calciumAchievement;
    }

    public void setCalciumAchievement(String calciumAchievement) {
        this.calciumAchievement = calciumAchievement;
    }

    public String getPotassiumAchievement() {
        return potassiumAchievement;
    }

    public void setPotassiumAchievement(String potassiumAchievement) {
        this.potassiumAchievement = potassiumAchievement;
    }

    public String getSodiumAchievement() {
        return sodiumAchievement;
    }

    public void setSodiumAchievement(String sodiumAchievement) {
        this.sodiumAchievement = sodiumAchievement;
    }

    public String getMagnesiumAchievement() {
        return magnesiumAchievement;
    }

    public void setMagnesiumAchievement(String magnesiumAchievement) {
        this.magnesiumAchievement = magnesiumAchievement;
    }

    public String getCarbsCaloriePercentage() {
        return carbsCaloriePercentage;
    }

    public void setCarbsCaloriePercentage(String carbsCaloriePercentage) {
        this.carbsCaloriePercentage = carbsCaloriePercentage;
    }

    public String getProteinCaloriePercentage() {
        return proteinCaloriePercentage;
    }

    public void setProteinCaloriePercentage(String proteinCaloriePercentage) {
        this.proteinCaloriePercentage = proteinCaloriePercentage;
    }

    public String getFatCaloriePercentage() {
        return fatCaloriePercentage;
    }

    public void setFatCaloriePercentage(String fatCaloriePercentage) {
        this.fatCaloriePercentage = fatCaloriePercentage;
    }

    public String getNutrientScore() {
        return nutrientScore;
    }

    public void setNutrientScore(String nutrientScore) {
        this.nutrientScore = nutrientScore;
    }

    public String getDeviationScore() {
        return deviationScore;
    }

    public void setDeviationScore(String deviationScore) {
        this.deviationScore = deviationScore;
    }

    public String getBalanceScore() {
        return balanceScore;
    }

    public void setBalanceScore(String balanceScore) {
        this.balanceScore = balanceScore;
    }

    public String getDiversityScore() {
        return diversityScore;
    }

    public void setDiversityScore(String diversityScore) {
        this.diversityScore = diversityScore;
    }

    public String getPreferenceScore() {
        return preferenceScore;
    }

    public void setPreferenceScore(String preferenceScore) {
        this.preferenceScore = preferenceScore;
    }

    public String getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(String overallScore) {
        this.overallScore = overallScore;
    }
} 