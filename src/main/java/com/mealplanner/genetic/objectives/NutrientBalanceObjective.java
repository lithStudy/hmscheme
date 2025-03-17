package com.mealplanner.genetic.objectives;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.NutrientRatio;
import com.mealplanner.model.Nutrition;
import com.mealplanner.model.UserProfile;

import java.util.*;

/**
 * 平衡目标类，专注于评估膳食解决方案的营养平衡和摄入量合理性
 */
public class NutrientBalanceObjective extends AbstractObjectiveEvaluator {
    // 宏量营养素比例权重
    private double macroRatioWeight = 0.6;
    // 摄入量合理性权重
    private double intakeRationalityWeight = 0.4;
// 综合评分：热量合理性占60%，食物摄入量合理性占40%
    private double intakeRationalCaloriesWeight = 0.6;
    private double intakeRationalFoodWeight = 0.4;


    // 理想的宏量营养素比例
    private double idealCarbPercentage; // 碳水占60%
    private double idealProteinPercentage; // 蛋白质占15%
    private double idealFatPercentage; // 脂肪占25%
    
    /**
     * 构造函数
     */
    public NutrientBalanceObjective(UserProfile userProfile) {
        super("balance_objective", 0.2);

        NutrientRatio ratio = NutrientRatio.calculateNutrientRatio(userProfile);
        this.idealCarbPercentage = ratio.getCarbRatio();
        this.idealProteinPercentage = ratio.getProteinRatio();
        this.idealFatPercentage = ratio.getFatRatio();
    }
    
    /**
     * 构造函数
     * @param weight 目标权重
     */
    public NutrientBalanceObjective(double weight) {
        super("balance_objective", weight);
    }
    
    /**
     * 评估解决方案的营养平衡
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 目标值
     */
    @Override
    public ObjectiveValue evaluate(MealSolution solution, Nutrition targetNutrients) {
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty()) {
            return new ObjectiveValue(name, 0.0, weight);
        }
        
        // 计算宏量营养素比例得分
        double macroRatioScore = evaluateMacroNutrientRatio(solution);
        
        // 计算摄入量合理性得分
        double intakeRationalityScore = evaluateIntakeRationality(solution, targetNutrients);
        
        // 计算加权总分
        double totalScore = macroRatioScore * macroRatioWeight + 
                           intakeRationalityScore * intakeRationalityWeight;
        
        return new ObjectiveValue(name, totalScore, weight);
    }
    
    /**
     * 评估宏量营养素比例
     * @param solution 解决方案
     * @return 宏量营养素比例得分（0-1之间）
     */
    private double evaluateMacroNutrientRatio(MealSolution solution) {
        // 获取当前膳食的总营养素
        double carbs = solution.calculateTotalNutrients().carbohydrates;
        double protein = solution.calculateTotalNutrients().protein;
        double fat = solution.calculateTotalNutrients().fat;
        
        // 计算宏量营养素热量
        double carbsCalories = carbs * 4;
        double proteinCalories = protein * 4;
        double fatCalories = fat * 9;
        double totalCalories = carbsCalories + proteinCalories + fatCalories;
        
        if (totalCalories == 0) {
            return 0;
        }
        
        // 计算实际比例
        double actualCarbPercentage = carbsCalories / totalCalories;
        double actualProteinPercentage = proteinCalories / totalCalories;
        double actualFatPercentage = fatCalories / totalCalories;
        
        // 计算与理想比例的偏差
        double carbDeviation = Math.abs(actualCarbPercentage - idealCarbPercentage);
        double proteinDeviation = Math.abs(actualProteinPercentage - idealProteinPercentage);
        double fatDeviation = Math.abs(actualFatPercentage - idealFatPercentage);
        
        // 计算偏差总和（最大偏差为2，全部偏差100%）
        double totalDeviation = carbDeviation + proteinDeviation + fatDeviation;
        
        // 将偏差转换为得分（偏差越小，得分越高）
        double score = Math.max(0, 1 - totalDeviation / 1.0);
        
        return score;
    }
    
    /**
     * 评估摄入量的合理性
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 摄入量合理性得分（0-1之间）
     */
    private double evaluateIntakeRationality(MealSolution solution, Nutrition targetNutrients) {
        // 获取总热量
        double totalCalories = solution.calculateTotalNutrients().calories;
        
        // 评分：热量接近目标值
        double caloriesScore = 1.0 - Math.min(1.0, Math.abs(totalCalories - targetNutrients.calories) / targetNutrients.calories);
        
        // 评分：单个食物摄入量合理性
        double intakeRationalityScore = 0;
        int validFoods = 0;
        
        for (FoodGene gene : solution.getFoodGenes()) {
            double intake = gene.getIntake();
            double minIntake = gene.getFood().getRecommendedIntakeRange().getMinIntake();
            double maxIntake = gene.getFood().getRecommendedIntakeRange().getMaxIntake();
            double defaultIntake = gene.getFood().getRecommendedIntakeRange().getDefaultIntake();
            
            // 计算当前摄入量与推荐范围的合理性
            if (intake >= minIntake && intake <= maxIntake) {
                // 在推荐范围内
                double normalizedPosition = (intake - minIntake) / (maxIntake - minIntake);
                double idealPosition = (defaultIntake - minIntake) / (maxIntake - minIntake);
                
                // 接近默认推荐量得高分
                double foodScore = 1.0 - Math.min(1.0, Math.abs(normalizedPosition - idealPosition) * 2);
                intakeRationalityScore += foodScore;
                validFoods++;
            }
        }
        
        if (validFoods > 0) {
            intakeRationalityScore /= validFoods;
        } else {
            intakeRationalityScore = 0;
        }
        
        // 综合评分：热量合理性占比，食物摄入量合理性占比
        return caloriesScore * intakeRationalCaloriesWeight + intakeRationalityScore * intakeRationalFoodWeight;
    }
    
    /**
     * 获取宏量营养素比例权重
     * @return 宏量营养素比例权重
     */
    public double getMacroRatioWeight() {
        return macroRatioWeight;
    }
    
    /**
     * 设置宏量营养素比例权重
     * @param macroRatioWeight 宏量营养素比例权重
     */
    public void setMacroRatioWeight(double macroRatioWeight) {
        this.macroRatioWeight = macroRatioWeight;
        // 确保权重总和为1
        this.intakeRationalityWeight = 1 - macroRatioWeight;
    }
    
    /**
     * 获取摄入量合理性权重
     * @return 摄入量合理性权重
     */
    public double getIntakeRationalityWeight() {
        return intakeRationalityWeight;
    }
    
    /**
     * 设置摄入量合理性权重
     * @param intakeRationalityWeight 摄入量合理性权重
     */
    public void setIntakeRationalityWeight(double intakeRationalityWeight) {
        this.intakeRationalityWeight = intakeRationalityWeight;
        // 确保权重总和为1
        this.macroRatioWeight = 1 - intakeRationalityWeight;
    }
    
    /**
     * 设置理想的宏量营养素比例
     * @param carbPercentage 碳水化合物百分比
     * @param proteinPercentage 蛋白质百分比
     * @param fatPercentage 脂肪百分比
     */
    public void setIdealMacroRatio(double carbPercentage, double proteinPercentage, double fatPercentage) {
        double sum = carbPercentage + proteinPercentage + fatPercentage;
        
        if (Math.abs(sum - 1.0) > 0.001) {
            // 总和不为1，需要标准化
            this.idealCarbPercentage = carbPercentage / sum;
            this.idealProteinPercentage = proteinPercentage / sum;
            this.idealFatPercentage = fatPercentage / sum;
        } else {
            this.idealCarbPercentage = carbPercentage;
            this.idealProteinPercentage = proteinPercentage;
            this.idealFatPercentage = fatPercentage;
        }
    }
}