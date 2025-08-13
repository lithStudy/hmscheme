package com.mealplanner.genetic.objectives.refactored;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.objectives.core.BaseObjectiveEvaluator;
import com.mealplanner.model.Food;
import com.mealplanner.model.NutrientType;
import com.mealplanner.model.UserProfile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 优化后的用户偏好目标
 * 整合安全性约束和个人偏好评估
 * 
 * 职责：
 * 1. 安全性约束（过敏原、宗教禁忌）- 硬约束部分
 * 2. 个人偏好匹配（口味、辣度、不喜欢的食物）- 软约束部分
 * 3. 适度的口味多样性（避免过于单调）
 */
public class UserPreferenceObjective extends BaseObjectiveEvaluator {
    
    private UserProfile userProfile;
    
    // 各维度权重
    private double safetyWeight = 0.6;          // 安全性权重（硬约束性质）
    private double flavorMatchWeight = 0.25;    // 口味匹配权重
    private double varietyWeight = 0.15;        // 口味多样性权重
    
    /**
     * 构造函数
     * @param userProfile 用户档案
     * @param weight 目标权重
     */
    public UserPreferenceObjective(UserProfile userProfile, double weight) {
        super("user_preference", weight, true, 0.8); // 设为硬约束，因为包含安全性
        this.userProfile = userProfile;
    }
    
    @Override
    public ObjectiveValue evaluate(MealSolution solution, Map<NutrientType, Double> targetNutrients) {
        if (userProfile == null) {
            return createObjectiveValue(0.5); // 无用户档案，给予中等分数
        }
        
        List<FoodGene> genes = solution.getFoodGenes();
        if (genes.isEmpty()) {
            return createObjectiveValue(1.0);
        }
        
        // 评估安全性（硬约束）
        double safetyScore = evaluateSafety(genes);
        
        // 评估口味匹配度
        double flavorScore = evaluateFlavorMatch(genes);
        
        // 评估口味多样性
        double varietyScore = evaluateFlavorVariety(genes);
        
        // 综合得分
        double totalScore = safetyScore * safetyWeight +
                          flavorScore * flavorMatchWeight +
                          varietyScore * varietyWeight;
        
        return createObjectiveValue(totalScore);
    }
    
    /**
     * 评估安全性
     * 检查过敏原、宗教禁忌和强烈不喜欢的食物
     */
    private double evaluateSafety(List<FoodGene> genes) {
        int allergenViolations = 0;
        int religionViolations = 0;
        int strongDislikeCount = 0;
        
        for (FoodGene gene : genes) {
            Food food = gene.getFood();
            
            // 检查过敏原（完全不能接受）
            for (String allergen : food.getAllergens()) {
                if (userProfile.isAllergicTo(allergen)) {
                    allergenViolations++;
                    break; // 一个食物只计一次违规
                }
            }
            
            // 检查宗教限制（完全不能接受）
            for (String restriction : food.getReligiousRestrictions()) {
                if (userProfile.hasReligiousRestrictionFor(restriction)) {
                    religionViolations++;
                    break;
                }
            }
            
            // 检查强烈不喜欢的食物（可接受但扣分）
            if (userProfile.dislikesFood(food.getName())) {
                strongDislikeCount++;
            }
        }
        
        // 计算安全性得分
        double score = 1.0;
        
        // 过敏原和宗教限制是绝对不能接受的
        if (allergenViolations > 0 || religionViolations > 0) {
            return 0.0; // 直接返回0分，触发硬约束
        }
        
        // 不喜欢的食物适度扣分
        if (strongDislikeCount > 0) {
            double dislikeRatio = (double) strongDislikeCount / genes.size();
            score *= (1.0 - dislikeRatio * 0.5); // 最多扣50%
        }
        
        return Math.max(0.1, score); // 保证最低分数
    }
    
    /**
     * 评估口味匹配度
     */
    private double evaluateFlavorMatch(List<FoodGene> genes) {
        double totalScore = 0.0;
        int evaluatedFoods = 0;
        
        for (FoodGene gene : genes) {
            Food food = gene.getFood();
            double foodScore = 0.5; // 基础分数
            
            // 口味匹配度评估
            String[] flavors = food.getFlavorProfiles();
            if (flavors.length > 0) {
                int matchedFlavors = 0;
                for (String flavor : flavors) {
                    if (userProfile.likesFlavor(flavor)) {
                        matchedFlavors++;
                    }
                }
                double matchRatio = (double) matchedFlavors / flavors.length;
                foodScore = 0.3 + matchRatio * 0.7; // 0.3-1.0分
            }
            
            // 辣度匹配度评估
            int userSpicyPreference = userProfile.getSpicyPreference();
            int foodSpicyLevel = food.getSpicyLevel();
            int spicyDifference = Math.abs(foodSpicyLevel - userSpicyPreference);
            
            double spicyMatchScore = 1.0;
            if (spicyDifference == 1) {
                spicyMatchScore = 0.8;
            } else if (spicyDifference == 2) {
                spicyMatchScore = 0.5;
            } else if (spicyDifference > 2) {
                spicyMatchScore = 0.2;
            }
            
            // 如果用户完全不能接受该辣度
            if (!userProfile.acceptsSpicyLevel(foodSpicyLevel)) {
                spicyMatchScore *= 0.3;
            }
            
            // 综合口味和辣度评分
            double combinedScore = foodScore * 0.7 + spicyMatchScore * 0.3;
            totalScore += combinedScore;
            evaluatedFoods++;
        }
        
        return evaluatedFoods > 0 ? totalScore / evaluatedFoods : 0.5;
    }
    
    /**
     * 评估口味多样性
     * 适度的多样性是好的，但不要过于追求复杂
     */
    private double evaluateFlavorVariety(List<FoodGene> genes) {
        Set<String> allFlavors = new HashSet<>();
        
        for (FoodGene gene : genes) {
            for (String flavor : gene.getFood().getFlavorProfiles()) {
                allFlavors.add(flavor);
            }
        }
        
        int flavorCount = allFlavors.size();
        
        // 理想的口味数量：3-5种
        if (flavorCount <= 1) {
            return 0.3; // 太单调
        } else if (flavorCount <= 2) {
            return 0.5; // 稍微单调
        } else if (flavorCount <= 5) {
            return 0.7 + (flavorCount - 2) * 0.1; // 0.7-1.0，理想范围
        } else {
            // 超过5种，轻微扣分（避免过于复杂）
            return Math.max(0.8, 1.0 - (flavorCount - 5) * 0.05);
        }
    }
    
    /**
     * 检查解决方案是否满足安全性要求
     */
    public boolean isSafe(MealSolution solution) {
        if (userProfile == null) return true;
        
        for (FoodGene gene : solution.getFoodGenes()) {
            Food food = gene.getFood();
            
            // 检查过敏原
            for (String allergen : food.getAllergens()) {
                if (userProfile.isAllergicTo(allergen)) {
                    return false;
                }
            }
            
            // 检查宗教限制
            for (String restriction : food.getReligiousRestrictions()) {
                if (userProfile.hasReligiousRestrictionFor(restriction)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 设置维度权重
     */
    public void setDimensionWeights(double safetyWeight, double flavorWeight, double varietyWeight) {
        double sum = safetyWeight + flavorWeight + varietyWeight;
        if (sum > 0) {
            this.safetyWeight = safetyWeight / sum;
            this.flavorMatchWeight = flavorWeight / sum;
            this.varietyWeight = varietyWeight / sum;
        }
    }
    
    /**
     * 更新用户档案
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
    
}
