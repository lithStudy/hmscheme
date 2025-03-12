package com.mealplanner.genetic.objectives;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;

import java.util.*;

/**
 * 平衡目标类，评估膳食解决方案的营养平衡和合理性
 */
public class BalanceObjective {
    // 目标名称
    private final String name = "balance_objective";
    
    // 目标权重
    private double weight;
    
    // 宏量营养素比例权重
    private double macroRatioWeight = 0.5;
    
    // 食物组合合理性权重
    private double foodCombinationWeight = 0.3;
    
    // 摄入量合理性权重
    private double intakeRationalityWeight = 0.2;
    
    // 理想的宏量营养素比例
    private double idealCarbPercentage = 0.55; // 碳水占55%
    private double idealProteinPercentage = 0.20; // 蛋白质占20%
    private double idealFatPercentage = 0.25; // 脂肪占25%
    
    // 摄入量合理性评估的基准值
    private double baselineCaloriesPerMeal = 600; // 一餐基准热量
    
    /**
     * 构造函数
     */
    public BalanceObjective() {
        this.weight = 0.2; // 默认权重
    }
    
    /**
     * 构造函数
     * @param weight 目标权重
     */
    public BalanceObjective(double weight) {
        this.weight = weight;
    }
    
    /**
     * 评估解决方案的营养平衡
     * @param solution 解决方案
     * @return 目标值
     */
    public ObjectiveValue evaluate(MealSolution solution) {
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty()) {
            return new ObjectiveValue(name, 0.0, weight);
        }
        
        // 计算宏量营养素比例得分
        double macroRatioScore = evaluateMacroNutrientRatio(solution);
        
        // 计算食物组合合理性得分
        double foodCombinationScore = evaluateFoodCombination(genes);
        
        // 计算摄入量合理性得分
        double intakeRationalityScore = evaluateIntakeRationality(solution);
        
        // 计算加权总分
        double totalScore = macroRatioScore * macroRatioWeight + 
                           foodCombinationScore * foodCombinationWeight + 
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
     * 评估食物组合的合理性
     * @param genes 食物基因列表
     * @return 食物组合合理性得分（0-1之间）
     */
    private double evaluateFoodCombination(List<FoodGene> genes) {
        // 检查是否包含必要的食物类别
        boolean hasStaple = false;
        boolean hasVegetable = false;
        boolean hasProteinSource = false;
        
        // 食物类别计数
        Map<String, Integer> categoryCount = new HashMap<>();
        
        for (FoodGene gene : genes) {
            String category = gene.getFood().getCategory();
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
            
            if ("staple".equals(category)) {
                hasStaple = true;
            } else if ("vegetable".equals(category)) {
                hasVegetable = true;
            } else if ("meat".equals(category) || "fish".equals(category) || 
                       "egg".equals(category) || "bean".equals(category)) {
                hasProteinSource = true;
            }
        }
        
        // 基础得分：是否包含基本营养成分
        double baseScore = 0;
        if (hasStaple) baseScore += 0.3;
        if (hasVegetable) baseScore += 0.3;
        if (hasProteinSource) baseScore += 0.3;
        
        // 食物类别多样性得分
        double diversityScore = 0;
        int distinctCategories = categoryCount.size();
        
        if (distinctCategories >= 4) {
            diversityScore = 0.1;
        } else if (distinctCategories == 3) {
            diversityScore = 0.07;
        } else if (distinctCategories == 2) {
            diversityScore = 0.05;
        }
        
        // 检查类别平衡性
        double balanceScore = 0;
        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            if (entry.getValue() > 3) {
                // 扣分：某一类别食物过多
                balanceScore -= 0.05 * (entry.getValue() - 3);
            }
        }
        
        // 总分
        return Math.max(0, Math.min(1, baseScore + diversityScore + balanceScore));
    }
    
    /**
     * 评估摄入量的合理性
     * @param solution 解决方案
     * @return 摄入量合理性得分（0-1之间）
     */
    private double evaluateIntakeRationality(MealSolution solution) {
        // 获取总热量
        double totalCalories = solution.calculateTotalNutrients().calories;
        
        // 评分：热量接近基准值
        double caloriesScore = 1.0 - Math.min(1.0, Math.abs(totalCalories - baselineCaloriesPerMeal) / baselineCaloriesPerMeal);
        
        // 评分：食物数量合理
        int foodCount = solution.getFoodGenes().size();
        double foodCountScore;
        
        if (foodCount >= 3 && foodCount <= 6) {
            // 理想的食物数量
            foodCountScore = 1.0;
        } else if (foodCount < 3) {
            // 食物太少
            foodCountScore = 0.5 + (foodCount / 6.0);
        } else {
            // 食物太多
            foodCountScore = 0.5 * (10 - foodCount) / 4.0;
        }
        
        foodCountScore = Math.max(0, Math.min(1, foodCountScore));
        
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
        
        // 综合评分
        return caloriesScore * 0.5 + foodCountScore * 0.3 + intakeRationalityScore * 0.2;
    }
    
    /**
     * 获取目标名称
     * @return 目标名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取目标权重
     * @return 目标权重
     */
    public double getWeight() {
        return weight;
    }
    
    /**
     * 设置目标权重
     * @param weight 目标权重
     */
    public void setWeight(double weight) {
        this.weight = weight;
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
    }
    
    /**
     * 获取食物组合合理性权重
     * @return 食物组合合理性权重
     */
    public double getFoodCombinationWeight() {
        return foodCombinationWeight;
    }
    
    /**
     * 设置食物组合合理性权重
     * @param foodCombinationWeight 食物组合合理性权重
     */
    public void setFoodCombinationWeight(double foodCombinationWeight) {
        this.foodCombinationWeight = foodCombinationWeight;
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
    
    /**
     * 设置一餐基准热量
     * @param calories 基准热量
     */
    public void setBaselineCaloriesPerMeal(double calories) {
        this.baselineCaloriesPerMeal = calories;
    }
    
    /**
     * 获取一餐基准热量
     * @return 基准热量
     */
    public double getBaselineCaloriesPerMeal() {
        return baselineCaloriesPerMeal;
    }
}