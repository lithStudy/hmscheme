package com.mealplanner.foodmanage;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import com.mealplanner.model.Food;
import com.mealplanner.model.Portion;
import com.mealplanner.model.FoodCategory;
import com.mealplanner.model.NutrientType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 营养数据模型类，使用EasyExcel注解映射Excel列
 */
@Data
public class NutritionData {
    
    // 基本信息
    @ExcelProperty("樣品編號")
    private String sampleId;
    
    @ExcelProperty("食品分類")
    private String foodCategory;
    
    @ExcelProperty("樣品名稱")
    private String sampleName;
    
    @ExcelProperty("內容物描述")
    private String contentDescription;
    
    @ExcelProperty("俗名")
    private String commonName;
    
    @ExcelProperty("廢棄率(%)")
    private String wasteRate;
    
    // 主要营养素
    @ExcelProperty("熱量(kcal)")
    private String calories;
    
    @ExcelProperty("修正熱量(kcal)")
    private String adjustedCalories;
    
    @ExcelProperty("水分(g)")
    private String water;
    
    @ExcelProperty("粗蛋白(g)")
    private String protein;
    
    @ExcelProperty("粗脂肪(g)")
    private String fat;    
    
    @ExcelProperty("灰分(g)")
    private String ash;
    
    @ExcelProperty("總碳水化合物(g)")
    private String carbohydrates;
    
    @ExcelProperty("膳食纖維(g)")
    private String dietaryFiber;
    
    @ExcelProperty("糖質總量(g)")
    private String totalSugar;
    
    // 糖类
    @ExcelProperty("葡萄糖(g)")
    private String glucose;
    
    @ExcelProperty("果糖(g)")
    private String fructose;
    
    @ExcelProperty("半乳糖(g)")
    private String galactose;
    
    @ExcelProperty("麥芽糖(g)")
    private String maltose;
    
    @ExcelProperty("蔗糖(g)")
    private String sucrose;
    
    @ExcelProperty("乳糖(g)")
    private String lactose;
    
    // 矿物质
    @ExcelProperty("鈉(mg)")
    private String sodium;
    
    @ExcelProperty("鉀(mg)")
    private String potassium;
    
    @ExcelProperty("鈣(mg)")
    private String calcium;
    
    @ExcelProperty("鎂(mg)")
    private String magnesium;
    
    @ExcelProperty("鐵(mg)")
    private String iron;
    
    @ExcelProperty("鋅(mg)")
    private String zinc;
    
    @ExcelProperty("磷(mg)")
    private String phosphorus;
    
    @ExcelProperty("銅(mg)")
    private String copper;
    
    @ExcelProperty("錳(mg)")
    private String manganese;
    
    // 维生素
    @ExcelProperty("維生素B1(mg)")
    private String vitaminB1;
    
    @ExcelProperty("維生素B2(mg)")
    private String vitaminB2;
    
    @ExcelProperty("維生素C(mg)")
    private String vitaminC;
    
    // 胆固醇
    @ExcelProperty("膽固醇(mg)")
    private String cholesterol;

    // 脂肪酸
    @ExcelProperty("飽和脂肪(g)")
    private String saturatedFat;
    
    @ExcelProperty("脂肪酸S總量(mg)")
    private String totalFattyAcids;
    
    @ExcelProperty("脂肪酸M總量(mg)")
    private String totalFattyAcidsM;

    @ExcelProperty("脂肪酸P總量(mg)")
    private String totalFattyAcidsP;
    
    /**
     * 转换为Food对象
     * @return Food对象
     */
    public Food toFood() {
        try {
            // 解析基本信息
            String name = sampleName != null ? sampleName : "未知食物";
            FoodCategory category;

            category = FoodCategory.fromChineseName(foodCategory);
            
            
            // 解析营养成分
            double caloriesValue = parseDouble(calories);
            double carbsValue = parseDouble(carbohydrates);
            double proteinValue = parseDouble(protein);
            double fatValue = parseDouble(fat);
            double calciumValue = parseDouble(calcium);
            double potassiumValue = parseDouble(potassium);
            double sodiumValue = parseDouble(sodium);
            double magnesiumValue = parseDouble(magnesium);
            double ironValue = parseDouble(iron);
            double phosphorusValue = parseDouble(phosphorus);
            
            // 创建营养项列表
            Map<NutrientType, Double> nutritionMap = new HashMap<>();
            nutritionMap.put(NutrientType.CALORIES, caloriesValue);
            nutritionMap.put(NutrientType.CARBOHYDRATES, carbsValue);
            nutritionMap.put(NutrientType.PROTEIN, proteinValue);
            nutritionMap.put(NutrientType.FAT, fatValue);
            nutritionMap.put(NutrientType.CALCIUM, calciumValue);
            nutritionMap.put(NutrientType.POTASSIUM, potassiumValue);
            nutritionMap.put(NutrientType.SODIUM, sodiumValue);
            nutritionMap.put(NutrientType.MAGNESIUM, magnesiumValue);
            nutritionMap.put(NutrientType.IRON, ironValue);
            nutritionMap.put(NutrientType.PHOSPHORUS, phosphorusValue);
            
            // 份量信息 - 假设标准份量为100克
            Portion portion = new Portion(100.0, "克", 100.0);
            
            // 创建并返回Food对象
            return new Food(name, category, nutritionMap, portion);
        } catch (Exception e) {
            System.err.println("转换数据失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 安全地解析字符串为double
     */
    private double parseDouble(String value) {
        try {
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
} 