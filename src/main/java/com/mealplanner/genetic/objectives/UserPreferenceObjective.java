package com.mealplanner.genetic.objectives;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.Food;
import com.mealplanner.model.NutrientType;
import com.mealplanner.model.UserProfile;

import java.util.List;
import java.util.Map;

/**
 * 用户偏好目标类，评估解决方案与用户偏好的匹配度
 */
public class UserPreferenceObjective extends AbstractObjectiveEvaluator {
    // 用户档案
    private UserProfile userProfile;
    
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
    public UserPreferenceObjective(UserProfile userProfile) {
        super("preference_objective", 0.2);
        this.userProfile = userProfile;
    }
    
    /**
     * 构造函数
     * @param userProfile 用户档案
     * @param weight 目标权重
     */
    public UserPreferenceObjective(UserProfile userProfile, double weight) {
        super("preference_objective", weight);
        this.userProfile = userProfile;
    }
    
    /**
     * 评估解决方案与用户偏好的匹配度
     * @param solution 解决方案
     * @param targetNutrients 目标营养素（此参数在偏好评估中不使用，但需要实现接口）
     * @return 目标值
     */
    @Override
    public ObjectiveValue evaluate(MealSolution solution, Map<NutrientType, Double> targetNutrients) {
        return evaluate(solution);
    }
    
    /**
     * 评估解决方案与用户偏好的匹配度
     * @param solution 解决方案
     * @return 目标值
     */
    public ObjectiveValue evaluate(MealSolution solution) {
        // 如果没有用户档案，则默认完全匹配
        if (userProfile == null) {
            return new ObjectiveValue(getName(), 1.0, getWeight());
        }
        
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty()) {
            return new ObjectiveValue(getName(), 0.0, getWeight());
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
        
        return new ObjectiveValue(getName(), averageScore, getWeight());
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