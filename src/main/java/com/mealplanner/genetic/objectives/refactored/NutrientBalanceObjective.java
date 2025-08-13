package com.mealplanner.genetic.objectives.refactored;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.objectives.core.BaseObjectiveEvaluator;
import com.mealplanner.model.NutrientRatio;
import com.mealplanner.model.NutrientType;
import com.mealplanner.model.UserProfile;

import java.util.Map;

/**
 * 优化后的营养平衡目标
 * 整合宏量营养素比例和摄入量合理性评估
 * 
 * 职责：
 * 1. 宏量营养素比例平衡（碳水/蛋白/脂肪）
 * 2. 总热量接近目标值
 * 3. 单个食物摄入量在合理范围内
 */
public class NutrientBalanceObjective extends BaseObjectiveEvaluator {
    
    // 各维度权重
    private double macroRatioWeight = 0.5;          // 宏量比例权重
    private double totalCaloriesWeight = 0.3;       // 总热量权重
    private double portionReasonableWeight = 0.2;   // 食物分量合理性权重
    
    // 理想的宏量营养素比例
    private double idealCarbPercentage;
    private double idealProteinPercentage;
    private double idealFatPercentage;
    
    /**
     * 构造函数
     * @param userProfile 用户档案
     * @param weight 目标权重
     */
    public NutrientBalanceObjective(UserProfile userProfile, double weight) {
        super("nutrient_balance", weight);
        
        // 从用户档案计算理想比例
        NutrientRatio ratio = NutrientRatio.calculateNutrientRatio(userProfile);
        this.idealCarbPercentage = ratio.getCarbRatio();
        this.idealProteinPercentage = ratio.getProteinRatio();
        this.idealFatPercentage = ratio.getFatRatio();
    }
    
    @Override
    public ObjectiveValue evaluate(MealSolution solution, Map<NutrientType, Double> targetNutrients) {
        // 评估宏量营养素比例
        double macroScore = evaluateMacroRatio(solution);
        
        // 评估总热量合理性
        double caloriesScore = evaluateTotalCalories(solution, targetNutrients);
        
        // 评估食物分量合理性
        double portionScore = evaluatePortionReasonableness(solution);
        
        // 综合得分
        double totalScore = macroScore * macroRatioWeight +
                          caloriesScore * totalCaloriesWeight +
                          portionScore * portionReasonableWeight;
        
        return createObjectiveValue(totalScore);
    }
    
    /**
     * 评估宏量营养素比例
     */
    private double evaluateMacroRatio(MealSolution solution) {
        Map<NutrientType, Double> nutrients = solution.calculateTotalNutrients();
        
        double carbs = nutrients.getOrDefault(NutrientType.CARBOHYDRATES, 0.0);
        double protein = nutrients.getOrDefault(NutrientType.PROTEIN, 0.0);
        double fat = nutrients.getOrDefault(NutrientType.FAT, 0.0);
        
        // 计算各营养素提供的热量
        double carbsCalories = carbs * 4;
        double proteinCalories = protein * 4;
        double fatCalories = fat * 9;
        double totalCalories = carbsCalories + proteinCalories + fatCalories;
        
        if (totalCalories == 0) return 0.0;
        
        // 计算实际比例
        double actualCarbRatio = carbsCalories / totalCalories;
        double actualProteinRatio = proteinCalories / totalCalories;
        double actualFatRatio = fatCalories / totalCalories;
        
        // 计算与理想比例的匹配度
        double carbMatch = 1.0 - Math.abs(actualCarbRatio - idealCarbPercentage) / idealCarbPercentage;
        double proteinMatch = 1.0 - Math.abs(actualProteinRatio - idealProteinPercentage) / idealProteinPercentage;
        double fatMatch = 1.0 - Math.abs(actualFatRatio - idealFatPercentage) / idealFatPercentage;
        
        // 加权平均（蛋白质稍微重要一些）
        return Math.max(0, carbMatch * 0.3 + proteinMatch * 0.4 + fatMatch * 0.3);
    }
    
    /**
     * 评估总热量合理性
     */
    private double evaluateTotalCalories(MealSolution solution, Map<NutrientType, Double> targetNutrients) {
        double actualCalories = solution.calculateTotalNutrients().get(NutrientType.CALORIES);
        double targetCalories = targetNutrients.get(NutrientType.CALORIES);
        
        if (targetCalories == 0) return actualCalories == 0 ? 1.0 : 0.0;
        
        double deviationRatio = Math.abs(actualCalories - targetCalories) / targetCalories;
        
        // 分段评分
        if (deviationRatio <= 0.05) {
            return 1.0;  // 偏差5%以内，满分
        } else if (deviationRatio <= 0.1) {
            return 0.9 - (deviationRatio - 0.05) * 2.0;  // 5%-10%，0.8-0.9分
        } else if (deviationRatio <= 0.2) {
            return 0.8 - (deviationRatio - 0.1) * 2.0;   // 10%-20%，0.6-0.8分
        } else {
            return Math.max(0.2, 0.6 * Math.exp(-(deviationRatio - 0.2) * 3));
        }
    }
    
    /**
     * 评估食物分量合理性
     */
    private double evaluatePortionReasonableness(MealSolution solution) {
        if (solution.getFoodGenes().isEmpty()) return 1.0;
        
        double totalScore = 0.0;
        int validFoods = 0;
        
        for (FoodGene gene : solution.getFoodGenes()) {
            double intake = gene.getIntake();
            double minIntake = gene.getFood().getRecommendedIntakeRange().getMinIntake();
            double maxIntake = gene.getFood().getRecommendedIntakeRange().getMaxIntake();
            double defaultIntake = gene.getFood().getRecommendedIntakeRange().getDefaultIntake();
            
            double foodScore;
            if (intake < minIntake || intake > maxIntake) {
                // 超出推荐范围，给予较低分数但不为0
                if (intake < minIntake) {
                    double ratio = intake / minIntake;
                    foodScore = Math.max(0.2, ratio * 0.6);  // 最低0.2分
                } else {
                    double excessRatio = (intake - maxIntake) / maxIntake;
                    foodScore = Math.max(0.2, 0.6 * Math.exp(-excessRatio));
                }
            } else {
                // 在推荐范围内，根据接近默认值程度评分
                double range = maxIntake - minIntake;
                if (range > 0) {
                    double normalizedIntake = (intake - minIntake) / range;
                    double normalizedDefault = (defaultIntake - minIntake) / range;
                    double distance = Math.abs(normalizedIntake - normalizedDefault);
                    foodScore = 1.0 - distance * 0.3;  // 0.7-1.0分
                } else {
                    foodScore = 1.0;
                }
            }
            
            totalScore += foodScore;
            validFoods++;
        }
        
        return validFoods > 0 ? totalScore / validFoods : 1.0;
    }
    
    /**
     * 设置维度权重
     */
    public void setDimensionWeights(double macroWeight, double caloriesWeight, double portionWeight) {
        double sum = macroWeight + caloriesWeight + portionWeight;
        if (sum > 0) {
            this.macroRatioWeight = macroWeight / sum;
            this.totalCaloriesWeight = caloriesWeight / sum;
            this.portionReasonableWeight = portionWeight / sum;
        }
    }
    
    /**
     * 设置理想宏量比例
     */
    public void setIdealMacroRatio(double carbRatio, double proteinRatio, double fatRatio) {
        double sum = carbRatio + proteinRatio + fatRatio;
        if (Math.abs(sum - 1.0) > 0.001) {
            // 归一化
            this.idealCarbPercentage = carbRatio / sum;
            this.idealProteinPercentage = proteinRatio / sum;
            this.idealFatPercentage = fatRatio / sum;
        } else {
            this.idealCarbPercentage = carbRatio;
            this.idealProteinPercentage = proteinRatio;
            this.idealFatPercentage = fatRatio;
        }
    }
}
