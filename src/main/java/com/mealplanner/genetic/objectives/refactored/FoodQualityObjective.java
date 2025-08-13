package com.mealplanner.genetic.objectives.refactored;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.objectives.core.BaseObjectiveEvaluator;
import com.mealplanner.model.FoodCategory;
import com.mealplanner.model.NutrientType;

import java.util.*;

/**
 * 食物质量目标
 * 综合评估食物的多样性、搭配合理性和质量水平
 * 
 * 整合了原来的：
 * - 组合合理性（营养结构的完整性）
 * - 烹饪方式等特性多样性（避免过于单调）
 */
public class FoodQualityObjective extends BaseObjectiveEvaluator {
    
    // 各维度权重
    private double nutritionCompletenessWeight = 0.8;  // 营养完整性权重
    private double varietyWeight = 0.2;                // 适度多样性权重
    
    /**
     * 构造函数
     * @param weight 目标权重
     */
    public FoodQualityObjective(double weight) {
        super("food_quality", weight);
    }
    

    @Override
    public ObjectiveValue evaluate(MealSolution solution, Map<NutrientType, Double> targetNutrients) {
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty()) {
            return createObjectiveValue(0.0);
        }

        // 评估营养完整性
        double completenessScore = evaluateNutritionalCompleteness(genes);
        
        // 评估适度多样性
        double varietyScore = evaluateModerateVariety(genes);
        
        // 综合得分
        double totalScore = completenessScore * nutritionCompletenessWeight +
                          varietyScore * varietyWeight;
        
        return createObjectiveValue(totalScore);
    }
    
    /**
     * 评估营养完整性
     * 确保基本的营养结构完整（主食+蔬菜+蛋白质来源）
     */
    private double evaluateNutritionalCompleteness(List<FoodGene> genes) {
        boolean hasCarbohydrateSource = false;  // 碳水化合物来源
        boolean hasVegetableSource = false;     // 蔬菜来源
        boolean hasProteinSource = false;       // 蛋白质来源
        boolean hasFruitSource = false;         // 水果来源（可选但有益）
        
        for (FoodGene gene : genes) {
            FoodCategory category = gene.getFood().getCategory();
            
            switch (category) {
                case STAPLE:
                    hasCarbohydrateSource = true;
                    break;
                case VEGETABLE:
                    hasVegetableSource = true;
                    break;
                case MEAT:
                case FISH:
                case EGG:
                case BEAN:
                case MILK:
                    hasProteinSource = true;
                    break;
                case FRUIT:
                    hasFruitSource = true;
                    break;
                case OIL:
                case MUSHROOM:
                case PASTRY:
                case OTHER:
                    // 其他类别不影响基本营养结构评分
                    break;
            }
        }
        
        // 基础营养结构评分
        double score = 0.0;
        if (hasCarbohydrateSource) score += 0.3;  // 碳水化合物最重要
        if (hasVegetableSource) score += 0.3;     // 蔬菜同样重要
        if (hasProteinSource) score += 0.3;       // 蛋白质必需
        if (hasFruitSource) score += 0.1;         // 水果有益但非必需
        
        return Math.min(1.0, score);
    }
    
    /**
     * 评估适度多样性
     * 避免过于单调，但不过分追求复杂性
     */
    private double evaluateModerateVariety(List<FoodGene> genes) {
        // 统计烹饪方式多样性
        Set<String> cookingMethods = new HashSet<>();
        for (FoodGene gene : genes) {
            for (String method : gene.getFood().getCookingMethods()) {
                cookingMethods.add(method);
            }
        }
        
        // 理想的烹饪方式数量：2-4种
        int methodCount = cookingMethods.size();
        double methodScore;
        if (methodCount <= 1) {
            methodScore = 0.3;  // 太单调
        } else if (methodCount <= 4) {
            methodScore = 0.5 + (methodCount - 1) * 0.17;  // 0.67-1.0
        } else {
            methodScore = Math.max(0.8, 1.0 - (methodCount - 4) * 0.05);  // 过多扣分
        }
        
        // 检查是否有过度重复的食物
        Map<String, Integer> foodCount = new HashMap<>();
        for (FoodGene gene : genes) {
            String foodName = gene.getFood().getName();
            foodCount.put(foodName, foodCount.getOrDefault(foodName, 0) + 1);
        }
        
        double repetitionPenalty = 0.0;
        for (int count : foodCount.values()) {
            if (count > 2) {  // 同一食物出现超过2次
                repetitionPenalty += (count - 2) * 0.1;
            }
        }
        
        return Math.max(0.2, methodScore - repetitionPenalty);
    }
    

    /**
     * 设置维度权重
     */
    public void setDimensionWeights(double categoryWeight, double completenessWeight, double varietyWeight) {
        double sum = categoryWeight + completenessWeight + varietyWeight;
        if (sum > 0) {
            this.nutritionCompletenessWeight = completenessWeight / sum;
            this.varietyWeight = varietyWeight / sum;
        }
    }
    

}
