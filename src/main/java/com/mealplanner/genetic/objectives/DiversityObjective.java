package com.mealplanner.genetic.objectives;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.Food;
import com.mealplanner.model.FoodCategory;

import java.util.*;

/**
 * 多样性目标类，评估膳食解决方案的食物多样性
 */
public class DiversityObjective {
    // 目标名称
    private final String name = "diversity_objective";
    
    // 目标权重
    private double weight;
    
    // 类别多样性权重
    private double categoryWeight = 0.5;
    
    // 食物特性多样性权重
    private double attributeWeight = 0.2;
    
    // 摄入量均衡权重：摄入量暂时不需要均衡
    private double intakeWeight = 0;
    
    // 类别分布在类别多样性中的权重
    private double categoryDistributionWeight = 0.8;
    
    // 类别覆盖率在类别多样性中的权重
    private double categoryCoverageWeight = 0.2;
    
    // 理想的类别分布
    private Map<FoodCategory, Double> idealCategoryDistribution;
    
    /**
     * 构造函数
     */
    public DiversityObjective() {
        this.weight = 0.2; // 默认权重
        initializeIdealDistribution();
    }
    
    /**
     * 构造函数
     * @param weight 目标权重
     */
    public DiversityObjective(double weight) {
        this.weight = weight;
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
     * @return 目标值
     */
    public ObjectiveValue evaluate(MealSolution solution) {
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty()) {
            return new ObjectiveValue(name, 0.0, weight);
        }
        
        // 计算类别多样性得分
        double categoryScore = evaluateCategoryDiversity(genes);
        
        // 计算食物特性多样性得分
        double attributeScore = evaluateAttributeDiversity(genes);
        
        // 计算摄入量均衡得分
        double intakeScore = evaluateIntakeBalance(genes);
        
        // 计算加权总分
        double totalScore = categoryScore * categoryWeight + 
                           attributeScore * attributeWeight + 
                           intakeScore * intakeWeight;
        
        return new ObjectiveValue(name, totalScore, weight);
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
     * 评估食物摄入量均衡性
     * @param genes 食物基因列表
     * @return 摄入量均衡得分（0-1之间）
     */
    private double evaluateIntakeBalance(List<FoodGene> genes) {
        if (genes.size() <= 1) {
            return 0.5; // 单一食物，给予中等分数
        }
        
        // 计算每种食物摄入量占总摄入量的比例
        double totalIntake = 0;
        for (FoodGene gene : genes) {
            totalIntake += gene.getIntake();
        }
        
        if (totalIntake == 0) {
            return 0;
        }
        
        List<Double> intakeRatios = new ArrayList<>();
        for (FoodGene gene : genes) {
            intakeRatios.add(gene.getIntake() / totalIntake);
        }
        
        // 计算摄入量比例的标准差
        double mean = 1.0 / genes.size(); // 理想的平均分布
        double sumSquaredDiff = 0;
        
        for (double ratio : intakeRatios) {
            sumSquaredDiff += Math.pow(ratio - mean, 2);
        }
        
        double standardDeviation = Math.sqrt(sumSquaredDiff / genes.size());
        
        // 标准差越小，均衡性越好
        double balanceScore = 1.0 - Math.min(1, standardDeviation * 3);
        
        return balanceScore;
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
    }
    
    /**
     * 获取摄入量均衡权重
     * @return 摄入量均衡权重
     */
    public double getIntakeWeight() {
        return intakeWeight;
    }
    
    /**
     * 设置摄入量均衡权重
     * @param intakeWeight 摄入量均衡权重
     */
    public void setIntakeWeight(double intakeWeight) {
        this.intakeWeight = intakeWeight;
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
        this.categoryWeight = categoryWeight;
        
        // 确保所有权重总和不变
        double remainingWeight = 1 - categoryWeight - intakeWeight;
        if (remainingWeight < 0) {
            throw new IllegalArgumentException("权重总和不能超过1");
        }
        this.attributeWeight = remainingWeight;
        
        System.out.println("已调整类别分布权重：");
        System.out.println("- 类别分布在类别多样性中的权重: " + categoryDistributionWeight);
        System.out.println("- 类别覆盖率在类别多样性中的权重: " + coverageWeight);
        System.out.println("- 类别多样性在总体多样性中的权重: " + categoryWeight);
        System.out.println("- 食物特性多样性在总体多样性中的权重: " + attributeWeight);
        System.out.println("- 摄入量均衡在总体多样性中的权重: " + intakeWeight);
    }
} 