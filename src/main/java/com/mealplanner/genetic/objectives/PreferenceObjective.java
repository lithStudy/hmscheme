package com.mealplanner.genetic.objectives;

import com.mealplanner.Food;
import com.mealplanner.UserProfile;
import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;

import java.util.List;

/**
 * 用户偏好目标类，评估解决方案与用户偏好的匹配度
 */
public class PreferenceObjective {
    // 目标名称
    private final String name = "preference_objective";
    
    // 用户档案
    private UserProfile userProfile;
    
    // 目标权重
    private double weight;
    
    // 各种偏好因素的权重
    private double flavorWeight = 0.3;
    private double allergenWeight = 1.0;
    private double religionWeight = 1.0;
    private double dislikeWeight = 0.8;
    private double spicyWeight = 0.6;
    
    /**
     * 构造函数
     * @param userProfile 用户档案
     */
    public PreferenceObjective(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.weight = 0.2; // 默认权重
    }
    
    /**
     * 构造函数
     * @param userProfile 用户档案
     * @param weight 目标权重
     */
    public PreferenceObjective(UserProfile userProfile, double weight) {
        this.userProfile = userProfile;
        this.weight = weight;
    }
    
    /**
     * 评估解决方案与用户偏好的匹配度
     * @param solution 解决方案
     * @return 目标值
     */
    public ObjectiveValue evaluate(MealSolution solution) {
        // 如果没有用户档案，则默认完全匹配
        if (userProfile == null) {
            return new ObjectiveValue(name, 1.0, weight);
        }
        
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty()) {
            return new ObjectiveValue(name, 0.0, weight);
        }
        
        double totalScore = 0;
        int violationCount = 0;
        
        // 评估每种食物
        for (FoodGene gene : genes) {
            Food food = gene.getFood();
            double foodScore = evaluateFood(food);
            
            // 如果食物完全不符合用户偏好（如过敏原），则计为违规
            if (foodScore < 0.1) {
                violationCount++;
            }
            
            totalScore += foodScore;
        }
        
        // 计算平均分数
        double averageScore = totalScore / genes.size();
        
        // 如果有严重违规（如过敏原），大幅降低总分
        if (violationCount > 0) {
            averageScore *= Math.pow(0.5, violationCount);
        }
        
        return new ObjectiveValue(name, averageScore, weight);
    }
    
    /**
     * 评估单种食物与用户偏好的匹配度
     * @param food 食物
     * @return 评分（0-1之间）
     */
    private double evaluateFood(Food food) {
        double score = 1.0;
        
        // 检查过敏原
        for (String allergen : food.getAllergens()) {
            if (userProfile.isAllergicTo(allergen)) {
                score -= allergenWeight; // 严重惩罚过敏原
            }
        }
        
        // 检查宗教限制
        for (String restriction : food.getReligiousRestrictions()) {
            if (userProfile.hasReligiousRestrictionFor(restriction)) {
                score -= religionWeight; // 严重惩罚宗教限制
            }
        }
        
        // 检查用户是否不喜欢该食物
        if (userProfile.dislikesFood(food.getName())) {
            score -= dislikeWeight; // 惩罚不喜欢的食物
        }
        
        // 检查辣度偏好
        if (!userProfile.acceptsSpicyLevel(food.getSpicyLevel())) {
            score -= spicyWeight * Math.abs(food.getSpicyLevel() - userProfile.getSpicyPreference()) / 5.0;
        }
        
        // 检查口味偏好匹配度
        double flavorMatchScore = 0;
        for (String flavor : food.getFlavorProfiles()) {
            if (userProfile.likesFlavor(flavor)) {
                flavorMatchScore += 0.2; // 每匹配一个偏好口味加分
            }
        }
        score += flavorMatchScore * flavorWeight;
        
        // 限制分数在0-1范围内
        return Math.max(0, Math.min(1, score));
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
     * @return
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
     * 获取口味因素权重
     * @return 口味因素权重
     */
    public double getFlavorWeight() {
        return flavorWeight;
    }
    
    /**
     * 设置口味因素权重
     * @param flavorWeight 口味因素权重
     */
    public void setFlavorWeight(double flavorWeight) {
        this.flavorWeight = flavorWeight;
    }
    
    /**
     * 获取过敏原因素权重
     * @return 过敏原因素权重
     */
    public double getAllergenWeight() {
        return allergenWeight;
    }
    
    /**
     * 设置过敏原因素权重
     * @param allergenWeight 过敏原因素权重
     */
    public void setAllergenWeight(double allergenWeight) {
        this.allergenWeight = allergenWeight;
    }
    
    /**
     * 获取宗教限制因素权重
     * @return 宗教限制因素权重
     */
    public double getReligionWeight() {
        return religionWeight;
    }
    
    /**
     * 设置宗教限制因素权重
     * @param religionWeight 宗教限制因素权重
     */
    public void setReligionWeight(double religionWeight) {
        this.religionWeight = religionWeight;
    }
    
    /**
     * 获取不喜欢食物因素权重
     * @return 不喜欢食物因素权重
     */
    public double getDislikeWeight() {
        return dislikeWeight;
    }
    
    /**
     * 设置不喜欢食物因素权重
     * @param dislikeWeight 不喜欢食物因素权重
     */
    public void setDislikeWeight(double dislikeWeight) {
        this.dislikeWeight = dislikeWeight;
    }
    
    /**
     * 获取辣度因素权重
     * @return 辣度因素权重
     */
    public double getSpicyWeight() {
        return spicyWeight;
    }
    
    /**
     * 设置辣度因素权重
     * @param spicyWeight 辣度因素权重
     */
    public void setSpicyWeight(double spicyWeight) {
        this.spicyWeight = spicyWeight;
    }
} 