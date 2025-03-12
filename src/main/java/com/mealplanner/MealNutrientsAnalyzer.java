package com.mealplanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mealplanner.logging.MealPlannerLogger;

/**
 * 膳食营养素分析工具类
 * 用于生成详细的营养素分析报告
 */
public class MealNutrientsAnalyzer {
    
    /**
     * 生成并输出膳食营养分析报告
     * @param meal 膳食中的食物列表
     * @param targetNutrients 目标营养素
     */
    public static void generateReport(List<Food> meal, MealNutrients targetNutrients) {
        if (meal == null || meal.isEmpty()) {
            MealPlannerLogger.warning("无法生成分析报告：膳食为空");
            return;
        }
        
        MealPlannerLogger.section("膳食营养分析报告");
        
        // 计算总营养素
        MealNutrients actualNutrients = calculateTotalNutrients(meal);
        
        // 输出总体摘要
        outputNutrientSummary(actualNutrients, targetNutrients);
        
        // 输出各食物的营养素贡献
        outputFoodContributions(meal, actualNutrients);
        
        // 输出营养素平衡分析
        outputNutrientBalanceAnalysis(actualNutrients);
        
        MealPlannerLogger.divider();
    }
    
    /**
     * 计算食物列表的总营养成分
     * @param foods 食物列表
     * @return 总营养成分
     */
    private static MealNutrients calculateTotalNutrients(List<Food> foods) {
        double totalCalories = 0;
        double totalCarbs = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalCalcium = 0;
        double totalPotassium = 0;
        double totalSodium = 0;
        double totalMagnesium = 0;
        
        for (Food food : foods) {
            totalCalories += food.getCalories();
            totalCarbs += food.getCarbohydrates();
            totalProtein += food.getProtein();
            totalFat += food.getFat();
            totalCalcium += food.getCalcium();
            totalPotassium += food.getPotassium();
            totalSodium += food.getSodium();
            totalMagnesium += food.getMagnesium();
        }
        
        return new MealNutrients(
            totalCalories,
            totalCarbs,
            totalProtein,
            totalFat,
            totalCalcium,
            totalPotassium,
            totalSodium,
            totalMagnesium
        );
    }
    
    /**
     * 输出营养素总体摘要
     * @param actual 实际营养素
     * @param target 目标营养素
     */
    private static void outputNutrientSummary(MealNutrients actual, MealNutrients target) {
        MealPlannerLogger.info("【营养素摘要】");
        MealPlannerLogger.increaseIndent();
        
        outputNutrientComparison("热量", actual.calories, target.calories, "大卡");
        outputNutrientComparison("碳水化合物", actual.carbohydrates, target.carbohydrates, "克");
        outputNutrientComparison("蛋白质", actual.protein, target.protein, "克");
        outputNutrientComparison("脂肪", actual.fat, target.fat, "克");
        outputNutrientComparison("钙", actual.calcium, target.calcium, "毫克");
        outputNutrientComparison("钾", actual.potassium, target.potassium, "毫克");
        outputNutrientComparison("钠", actual.sodium, target.sodium, "毫克");
        outputNutrientComparison("镁", actual.magnesium, target.magnesium, "毫克");
        
        // 计算三大营养素比例
        double totalEnergy = actual.carbohydrates * 4 + actual.protein * 4 + actual.fat * 9;
        if (totalEnergy > 0) {
            double carbPercentage = (actual.carbohydrates * 4 / totalEnergy) * 100;
            double proteinPercentage = (actual.protein * 4 / totalEnergy) * 100;
            double fatPercentage = (actual.fat * 9 / totalEnergy) * 100;
            
            MealPlannerLogger.info("三大营养素比例：");
            MealPlannerLogger.increaseIndent();
            MealPlannerLogger.info(String.format("碳水化合物: %.1f%%", carbPercentage));
            MealPlannerLogger.info(String.format("蛋白质: %.1f%%", proteinPercentage));
            MealPlannerLogger.info(String.format("脂肪: %.1f%%", fatPercentage));
            MealPlannerLogger.decreaseIndent();
        }
        
        MealPlannerLogger.decreaseIndent();
    }
    
    /**
     * 输出单个营养素的比较
     * @param name 营养素名称
     * @param actual 实际值
     * @param target 目标值
     * @param unit 单位
     */
    private static void outputNutrientComparison(String name, double actual, double target, String unit) {
        double percentage = target > 0 ? (actual / target) * 100 : 0;
        String status;
        
        if (percentage >= 90 && percentage <= 110) {
            status = "达标";
        } else if (percentage > 110) {
            status = "超标";
        } else if (percentage >= 70) {
            status = "接近";
        } else {
            status = "不足";
        }
        
        MealPlannerLogger.info(String.format("%s: %.1f/%s %.1f %s (%.1f%%) - %s",
            name, actual, ".".repeat(Math.max(0, 4 - String.valueOf((int)actual).length())), 
            target, unit, percentage, status));
    }
    
    /**
     * 输出各食物的营养素贡献
     * @param foods 食物列表
     * @param totalNutrients 总营养素
     */
    private static void outputFoodContributions(List<Food> foods, MealNutrients totalNutrients) {
        MealPlannerLogger.info("\n【食物营养贡献】");
        MealPlannerLogger.increaseIndent();
        
        for (Food food : foods) {
            MealPlannerLogger.info(food.getName() + " " + food.getPortionDescription() + ":");
            MealPlannerLogger.increaseIndent();
            
            double caloriePercentage = totalNutrients.calories > 0 ? 
                (food.getCalories() / totalNutrients.calories) * 100 : 0;
            double proteinPercentage = totalNutrients.protein > 0 ? 
                (food.getProtein() / totalNutrients.protein) * 100 : 0;
            double carbPercentage = totalNutrients.carbohydrates > 0 ? 
                (food.getCarbohydrates() / totalNutrients.carbohydrates) * 100 : 0;
            double fatPercentage = totalNutrients.fat > 0 ? 
                (food.getFat() / totalNutrients.fat) * 100 : 0;
            
            MealPlannerLogger.info(String.format("热量: %.1f 大卡 (%.1f%%)", 
                food.getCalories(), caloriePercentage));
            MealPlannerLogger.info(String.format("碳水: %.1f 克 (%.1f%%)", 
                food.getCarbohydrates(), carbPercentage));
            MealPlannerLogger.info(String.format("蛋白质: %.1f 克 (%.1f%%)", 
                food.getProtein(), proteinPercentage));
            MealPlannerLogger.info(String.format("脂肪: %.1f 克 (%.1f%%)", 
                food.getFat(), fatPercentage));
            
            MealPlannerLogger.decreaseIndent();
        }
        
        MealPlannerLogger.decreaseIndent();
    }
    
    /**
     * 输出营养素平衡分析
     * @param nutrients 营养素
     */
    private static void outputNutrientBalanceAnalysis(MealNutrients nutrients) {
        MealPlannerLogger.info("\n【营养素平衡分析】");
        MealPlannerLogger.increaseIndent();
        
        // 分析三大营养素比例
        double totalEnergy = nutrients.carbohydrates * 4 + nutrients.protein * 4 + nutrients.fat * 9;
        if (totalEnergy > 0) {
            double carbPercentage = (nutrients.carbohydrates * 4 / totalEnergy) * 100;
            double proteinPercentage = (nutrients.protein * 4 / totalEnergy) * 100;
            double fatPercentage = (nutrients.fat * 9 / totalEnergy) * 100;
            
            List<String> recommendations = new ArrayList<>();
            
            // 碳水化合物建议范围：45-65%
            if (carbPercentage < 45) {
                recommendations.add("碳水化合物摄入偏低，建议增加全谷类、水果等食物");
            } else if (carbPercentage > 65) {
                recommendations.add("碳水化合物摄入偏高，建议适当减少精制谷物、糖类摄入");
            }
            
            // 蛋白质建议范围：10-35%
            if (proteinPercentage < 10) {
                recommendations.add("蛋白质摄入不足，建议增加鱼肉、蛋奶、豆类等优质蛋白来源");
            } else if (proteinPercentage > 35) {
                recommendations.add("蛋白质摄入偏高，可适当减少肉类摄入");
            }
            
            // 脂肪建议范围：20-35%
            if (fatPercentage < 20) {
                recommendations.add("脂肪摄入不足，建议适当增加坚果、橄榄油等健康脂肪来源");
            } else if (fatPercentage > 35) {
                recommendations.add("脂肪摄入偏高，建议减少油炸食品和高脂食物");
            }
            
            // 输出建议
            if (!recommendations.isEmpty()) {
                MealPlannerLogger.info("营养素分布建议：");
                for (String recommendation : recommendations) {
                    MealPlannerLogger.info("- " + recommendation);
                }
            } else {
                MealPlannerLogger.info("三大营养素比例平衡良好");
            }
        }
        
        // 分析微量元素
        List<String> microNutrientSuggestions = new ArrayList<>();
        
        // 钠摄入建议：<2300mg/天
        if (nutrients.sodium > 2300) {
            microNutrientSuggestions.add("钠摄入偏高，建议减少盐和加工食品的摄入");
        }
        
        // 钾摄入建议：3500-4700mg/天
        if (nutrients.potassium < 1500) { // 假设这是一餐，约占每日需求的1/3
            microNutrientSuggestions.add("钾摄入不足，建议增加香蕉、土豆、绿叶蔬菜等摄入");
        }
        
        // 钙摄入建议：1000-1200mg/天
        if (nutrients.calcium < 300) { // 假设这是一餐，约占每日需求的1/3
            microNutrientSuggestions.add("钙摄入不足，建议增加奶制品、豆制品、绿叶蔬菜等摄入");
        }
        
        // 输出微量元素建议
        if (!microNutrientSuggestions.isEmpty()) {
            MealPlannerLogger.info("微量元素建议：");
            for (String suggestion : microNutrientSuggestions) {
                MealPlannerLogger.info("- " + suggestion);
            }
        }
        
        MealPlannerLogger.decreaseIndent();
    }
} 