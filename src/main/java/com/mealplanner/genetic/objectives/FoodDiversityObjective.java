package com.mealplanner.genetic.objectives;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.FoodCategory;
import com.mealplanner.model.Nutrition;

import java.util.*;

/**
 * 多样性目标类，专注于评估膳食解决方案的食物多样性和食物组合合理性
 */
public class FoodDiversityObjective extends AbstractObjectiveEvaluator {
    // 类别多样性权重
    private double categoryWeight = 0.5;
    
    // 食物特性多样性权重，暂时不考虑特性多样性
    private double attributeWeight = 0;
    
    // 食物组合合理性权重：主食、蔬菜、蛋奶等组合
    private double foodCombinationWeight = 0.2;
    
    // 类别覆盖率在类别多样性中的权重
    private double categoryCoverageWeight = 0.2;
    
    // 类别分布在类别多样性中的权重
    private double categoryDistributionWeight = 0.8;
    // 理想的类别分布
    private Map<FoodCategory, Double> idealCategoryDistribution;
    
    /**
     * 构造函数
     */
    public FoodDiversityObjective() {
        super("diversity_objective", 0.2);
        initializeIdealDistribution();
    }
    
    /**
     * 构造函数
     * @param weight 目标权重
     */
    public FoodDiversityObjective(double weight) {
        super("diversity_objective", weight);
        initializeIdealDistribution();
    }
    
    /**
     * 初始化理想的食物类别分布
     */
    private void initializeIdealDistribution() {
        idealCategoryDistribution = new HashMap<>();
        idealCategoryDistribution.put(FoodCategory.STAPLE, 0.20);      // 主食
        idealCategoryDistribution.put(FoodCategory.VEGETABLE, 0.30);   // 蔬菜
        idealCategoryDistribution.put(FoodCategory.FRUIT, 0.15);       // 水果
        idealCategoryDistribution.put(FoodCategory.MEAT, 0.15);        // 肉类
        idealCategoryDistribution.put(FoodCategory.FISH, 0.05);        // 鱼类
        idealCategoryDistribution.put(FoodCategory.EGG, 0.05);         // 蛋类
        idealCategoryDistribution.put(FoodCategory.MILK, 0.05);        // 乳制品
        idealCategoryDistribution.put(FoodCategory.OIL, 0.05);         // 油脂
    }
    
    /**
     * 评估解决方案的多样性
     * @param solution 解决方案
     * @param targetNutrients 目标营养素（此参数在多样性评估中不使用，但需要实现接口）
     * @return 目标值
     */
    @Override
    public ObjectiveValue evaluate(MealSolution solution, Nutrition targetNutrients) {
        return evaluate(solution);
    }
    
    /**
     * 评估解决方案的多样性（不需要目标营养素参数的版本）
     * @param solution 解决方案
     * @return 目标值
     */
    public ObjectiveValue evaluate(MealSolution solution) {
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty()) {
            return new ObjectiveValue(getName(), 0.0, getWeight());
        }

        // 计算类别多样性得分
        double categoryScore = evaluateCategoryDiversity(genes);
        
        // 计算食物特性多样性得分
        double attributeScore = evaluateAttributeDiversity(genes);
        
        // 计算食物组合合理性得分
        double foodCombinationScore = evaluateFoodCombination(genes);
        
        // 计算加权总分
        double totalScore = categoryScore * categoryWeight + 
                           attributeScore * attributeWeight + 
                           foodCombinationScore * foodCombinationWeight;
        
        return new ObjectiveValue(getName(), totalScore, getWeight());
    }
    
    /**
     * 评估食物类别多样性
     * @param genes 食物基因列表
     * @return 类别多样性得分（0-1之间）
     */
    private double evaluateCategoryDiversity(List<FoodGene> genes) {
        // 统计各类别食物数量
        Map<FoodCategory, Integer> categoryCount = new HashMap<>();
        for (FoodGene gene : genes) {
            FoodCategory category = gene.getFood().getCategory();
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }
        
        // 计算类别覆盖率
        double coverageScore = (double) categoryCount.size() / idealCategoryDistribution.size();
        
        // 计算类别分布与理想分布的差异
        double distributionScore = 0;
        if (!categoryCount.isEmpty()) {
            // 当前分布
            Map<FoodCategory, Double> actualDistribution = new HashMap<>();
            for (Map.Entry<FoodCategory, Integer> entry : categoryCount.entrySet()) {
                actualDistribution.put(entry.getKey(), (double) entry.getValue() / genes.size());
            }
            
            // 计算与理想分布的相似度
            double similarity = 0;
            for (FoodCategory category : idealCategoryDistribution.keySet()) {
                double ideal = idealCategoryDistribution.get(category);
                double actual = actualDistribution.getOrDefault(category, 0.0);
                // 使用 1 - 绝对差异 作为相似度，增加差异的惩罚力度
                similarity += 1 - Math.min(1, Math.abs(ideal - actual) * 3); // 从2增加到3，增加惩罚力度
            }
            
            distributionScore = similarity / idealCategoryDistribution.size();
        }
        
        // 综合覆盖率和分布得分
        return (coverageScore * categoryCoverageWeight + distributionScore * categoryDistributionWeight);
    }
    
    /**
     * 评估食物特性多样性
     * @param genes 食物基因列表
     * @return 特性多样性得分（0-1之间）
     */
    private double evaluateAttributeDiversity(List<FoodGene> genes) {
        // 收集所有食物的烹饪方式
        Set<String> cookingMethods = new HashSet<>();
        
        // 收集所有食物的口味特性
        Set<String> flavorProfiles = new HashSet<>();
        
        // 辣度水平多样性
        Set<Integer> spicyLevels = new HashSet<>();
        
        for (FoodGene gene : genes) {
            // 收集烹饪方式
            for (String method : gene.getFood().getCookingMethods()) {
                cookingMethods.add(method);
            }
            
            // 收集口味特性
            for (String flavor : gene.getFood().getFlavorProfiles()) {
                flavorProfiles.add(flavor);
            }
            
            // 收集辣度水平
            spicyLevels.add(gene.getFood().getSpicyLevel());
        }
        
        // 计算烹饪方式多样性得分
        double cookingMethodsScore = cookingMethods.size() >= 2 ? 1.0 : 
                                    cookingMethods.size() / 2.0;
        
        // 计算口味特性多样性得分
        double flavorProfilesScore = flavorProfiles.size() >= 3 ? 1.0 : 
                                    flavorProfiles.size() / 3.0;
        
        // 计算辣度水平多样性得分
        double spicyLevelsScore = spicyLevels.size() >= 2 ? 1.0 : 
                                 spicyLevels.size() / 2.0;
        
        // 综合各特性得分
        return (cookingMethodsScore * 0.4 + flavorProfilesScore * 0.4 + spicyLevelsScore * 0.2);
    }
    
    /**
     * 评估食物组合的合理性
     * @param genes 食物基因列表
     * @return 食物组合合理性得分（0-1之间）
     */
    private double evaluateFoodCombination(List<FoodGene> genes) {
        boolean hasStaple = false;
        boolean hasVegetable = false;
        boolean hasProteinSource = false;
        
        for (FoodGene gene : genes) {
            FoodCategory category = gene.getFood().getCategory();
            
            if (FoodCategory.STAPLE.equals(category)) {
                hasStaple = true;
            } else if (FoodCategory.VEGETABLE.equals(category)) {
                hasVegetable = true;
            } else if (FoodCategory.MEAT.equals(category) || FoodCategory.FISH.equals(category) || 
                       FoodCategory.EGG.equals(category) || FoodCategory.BEAN.equals(category)) {
                hasProteinSource = true;
            }
        }
        
        // 基础得分：是否包含基本营养成分
        double baseScore = 0;
        if (hasStaple) baseScore += 0.3;
        if (hasVegetable) baseScore += 0.3;
        if (hasProteinSource) baseScore += 0.3;
        
        // 检查类别平衡性
        double balanceScore = 0;
        Map<FoodCategory, Integer> categoryCount = new HashMap<>();
        for (FoodGene gene : genes) {
            FoodCategory category = gene.getFood().getCategory();
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }
        
        for (Map.Entry<FoodCategory, Integer> entry : categoryCount.entrySet()) {
            if (entry.getValue() > 3) {
                // 扣分：某一类别食物过多
                balanceScore -= 0.05 * (entry.getValue() - 3);
            }
        }
        
        // 总分
        return Math.max(0, Math.min(1, baseScore + balanceScore));
    }
    
    /**
     * 获取类别多样性权重
     * @return 类别多样性权重
     */
    public double getCategoryWeight() {
        return categoryWeight;
    }
    
    /**
     * 设置类别多样性权重
     * @param categoryWeight 类别多样性权重
     */
    public void setCategoryWeight(double categoryWeight) {
        this.categoryWeight = categoryWeight;
        // 调整其他权重，确保总和为1
        double remainingWeight = 1 - categoryWeight;
        double ratio = attributeWeight / (attributeWeight + foodCombinationWeight);
        this.attributeWeight = remainingWeight * ratio;
        this.foodCombinationWeight = remainingWeight * (1 - ratio);
    }
    
    /**
     * 获取食物特性多样性权重
     * @return 食物特性多样性权重
     */
    public double getAttributeWeight() {
        return attributeWeight;
    }
    
    /**
     * 设置食物特性多样性权重
     * @param attributeWeight 食物特性多样性权重
     */
    public void setAttributeWeight(double attributeWeight) {
        this.attributeWeight = attributeWeight;
        // 调整其他权重，确保总和为1
        double remainingWeight = 1 - attributeWeight;
        double ratio = categoryWeight / (categoryWeight + foodCombinationWeight);
        this.categoryWeight = remainingWeight * ratio;
        this.foodCombinationWeight = remainingWeight * (1 - ratio);
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
        // 调整其他权重，确保总和为1
        double remainingWeight = 1 - foodCombinationWeight;
        double ratio = categoryWeight / (categoryWeight + attributeWeight);
        this.categoryWeight = remainingWeight * ratio;
        this.attributeWeight = remainingWeight * (1 - ratio);
    }
    
    /**
     * 设置理想的类别分布
     * @param distribution 类别分布映射
     */
    public void setIdealCategoryDistribution(Map<FoodCategory, Double> distribution) {
        this.idealCategoryDistribution = new HashMap<>(distribution);
    }
    
    /**
     * 获取理想的类别分布
     * @return 类别分布映射
     */
    public Map<FoodCategory, Double> getIdealCategoryDistribution() {
        return new HashMap<>(idealCategoryDistribution);
    }
    
    /**
     * 调整类别分布在多样性评分中的权重
     * @param categoryDistributionWeight 类别分布权重（在类别多样性评分中的权重）
     * @param categoryWeight 类别多样性在总体多样性评分中的权重
     */
    public void adjustCategoryDistributionWeight(double categoryDistributionWeight, double categoryWeight) {
        if (categoryDistributionWeight < 0 || categoryDistributionWeight > 1) {
            throw new IllegalArgumentException("类别分布权重必须在0到1之间");
        }
        if (categoryWeight < 0 || categoryWeight > 1) {
            throw new IllegalArgumentException("类别多样性权重必须在0到1之间");
        }
        
        // 设置类别分布在类别多样性评分中的权重
        double coverageWeight = 1 - categoryDistributionWeight;
        this.categoryDistributionWeight = categoryDistributionWeight;
        this.categoryCoverageWeight = coverageWeight;
        
        // 修改类别多样性在总体多样性中的权重
        setCategoryWeight(categoryWeight);
        
        System.out.println("已调整类别分布权重：");
        System.out.println("- 类别分布在类别多样性中的权重: " + categoryDistributionWeight);
        System.out.println("- 类别覆盖率在类别多样性中的权重: " + coverageWeight);
        System.out.println("- 类别多样性在总体多样性中的权重: " + categoryWeight);
        System.out.println("- 食物特性多样性在总体多样性中的权重: " + attributeWeight);
        System.out.println("- 食物组合合理性在总体多样性中的权重: " + foodCombinationWeight);
    }
} 