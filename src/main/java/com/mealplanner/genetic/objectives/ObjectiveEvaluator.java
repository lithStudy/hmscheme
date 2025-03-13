package com.mealplanner.genetic.objectives;

import com.mealplanner.model.UserProfile;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.Nutrition;
import java.util.*;

/**
 * 多目标评价器，评估膳食解决方案在多个目标上的表现
 */
public class ObjectiveEvaluator {
    // 各个目标
    private List<NutrientObjective> nutrientObjectives=new ArrayList<>();
    private PreferenceObjective preferenceObjective;
    private DiversityObjective diversityObjective;
    private BalanceObjective balanceObjective;
    
    
    // 其他目标权重
    private double preferenceWeight = 0.2;
    private double diversityWeight = 0.2;
    private double balanceWeight = 0.2;
    
    // 评分阈值
    private double goodEnoughThreshold = 0.8;
    
    /**
     * 构造函数
     * @param userProfile 用户档案
     */
    public ObjectiveEvaluator(UserProfile userProfile) {
        // 初始化偏好目标评估器,用于评估食物是否符合用户偏好
        this.preferenceObjective = new PreferenceObjective(userProfile);
        // 初始化多样性目标评估器,用于评估食物种类的多样性
        this.diversityObjective = new DiversityObjective();
        // 初始化平衡性目标评估器,用于评估营养素的平衡性
        this.balanceObjective = new BalanceObjective();
        // 初始化营养元素评估器，用于评估每个营养元素成分的合理性
        createStandardNutrientObjectives(userProfile);
    }
    
    
    
    /**
     * 创建标准营养素目标
     */
    private void createStandardNutrientObjectives(UserProfile userProfile) {
        // 初始化默认的营养素权重
        Map<String, Double> nutrientWeights = initDefaultNutrientWeights();
        // 根据用户健康状况调整权重
        if (userProfile != null) {
            changeNutrientWeightsByHealthConditions(userProfile.getHealthConditions(),nutrientWeights);
        }
        /**
         * 初始化营养素目标
         **/
        // 热量目标，使用更严格的参数
        nutrientObjectives.add(new NutrientObjective("calories_objective", "calories", 
        nutrientWeights.get("calories"), true, 0.05, 0.9));
        
        nutrientObjectives.add(new NutrientObjective("carbohydrates_objective", "carbohydrates", 
                nutrientWeights.get("carbohydrates"), true));
        
        nutrientObjectives.add(new NutrientObjective("protein_objective", "protein", 
                nutrientWeights.get("protein"), true));
        
        nutrientObjectives.add(new NutrientObjective("fat_objective", "fat", 
                nutrientWeights.get("fat"), true));
        
        nutrientObjectives.add(new NutrientObjective("calcium_objective", "calcium", 
                nutrientWeights.get("calcium"), false));
        
        nutrientObjectives.add(new NutrientObjective("potassium_objective", "potassium", 
                nutrientWeights.get("potassium"), false));
        
        nutrientObjectives.add(new NutrientObjective("sodium_objective", "sodium", 
                nutrientWeights.get("sodium"), true));
        
        nutrientObjectives.add(new NutrientObjective("magnesium_objective", "magnesium", 
                nutrientWeights.get("magnesium"), false));
    }
    /**
     * 初始化营养素权重
     */
    private Map<String, Double> initDefaultNutrientWeights() {
        Map<String, Double> nutrientWeights = new HashMap<>();
        // 热量 - 主要营养素,提高权重，增强达成率
        nutrientWeights.put("calories", 3.0);
        // 碳水化合物 - 主要营养素,标准权重
        nutrientWeights.put("carbohydrates", 1.0);
        // 蛋白质 - 主要营养素,标准权重
        nutrientWeights.put("protein", 1.0);
        // 脂肪 - 主要营养素,标准权重
        nutrientWeights.put("fat", 0.8);
        // 钙质 - 次要营养素,较低权重
        nutrientWeights.put("calcium", 0.7);
        // 钾 - 次要营养素,较低权重
        nutrientWeights.put("potassium", 0.7);
        // 钠 - 需要适度控制,中等权重
        nutrientWeights.put("sodium", 0.8);
        // 镁 - 次要营养素,较低权重
        nutrientWeights.put("magnesium", 0.7);
        return nutrientWeights;
    }
    
    /**
     * 根据用户健康状况调整权重
     * @param healthConditions 健康状况数组
     */
    private void changeNutrientWeightsByHealthConditions(String[] healthConditions, Map<String, Double> nutrientWeights) {
        if (healthConditions == null || healthConditions.length == 0) {
            return; // 没有特殊健康状况，使用默认权重
        }
        
        for (String condition : healthConditions) {
            switch (condition.toLowerCase()) {
                case "hypertension": // 高血压
                    nutrientWeights.put("sodium", 1.5);    // 增加钠的权重（更严格控制）
                    nutrientWeights.put("potassium", 1.2); // 增加钾的权重（鼓励摄入）
                    break;
                    
                case "diabetes": // 糖尿病
                    nutrientWeights.put("calories", 1.3);   // 增加热量的权重
                    nutrientWeights.put("carbohydrates", 1.5); // 增加碳水的权重（更严格控制）
                    nutrientWeights.put("fat", 1.2);       // 增加脂肪的权重
                    break;
                    
                case "hyperlipidemia": // 高血脂
                    nutrientWeights.put("fat", 1.5);       // 增加脂肪的权重（更严格控制）
                    break;
                    
                case "gout": // 痛风
                    nutrientWeights.put("protein", 1.3);   // 增加蛋白质的权重（控制某些蛋白质来源）
                    break;
                    
                case "ckd": // 慢性肾病（无透析）
                    nutrientWeights.put("protein", 1.5);   // 增加蛋白质的权重（严格控制）
                    nutrientWeights.put("potassium", 1.3); // 增加钾的权重
                    nutrientWeights.put("phosphorus", 1.3); // 增加磷的权重
                    break;
                    
                case "ckd_dialysis": // 慢性肾病（透析）
                    nutrientWeights.put("protein", 1.2);   // 增加蛋白质的权重（但不同于无透析）
                    nutrientWeights.put("potassium", 1.5); // 增加钾的权重（更严格控制）
                    break;
                    
                case "osteoporosis": // 骨质疏松
                    nutrientWeights.put("calcium", 1.5);   // 增加钙的权重
                    nutrientWeights.put("magnesium", 1.2); // 增加镁的权重
                    break;
                    
                case "anemia": // 贫血
                    nutrientWeights.put("iron", 1.5);      // 增加铁的权重
                    break;
            }
        }
        
        // // 更新现有营养素目标的权重
        // for (NutrientObjective objective : nutrientObjectives) {
        //     String nutrient = objective.getNutrientName();
        //     if (nutrientWeights.containsKey(nutrient)) {
        //         objective.setWeight(nutrientWeights.get(nutrient));
        //     }
        // }
    }
    
    /**
     * 评估解决方案在所有目标上的表现
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 目标值列表
     */
    public List<ObjectiveValue> evaluate(MealSolution solution, Nutrition targetNutrients) {
        List<ObjectiveValue> objectiveValues = new ArrayList<>();
        
        // 计算当前膳食的总营养素
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        
        // 评估营养素目标
        for (NutrientObjective objective : nutrientObjectives) {
            ObjectiveValue value = objective.evaluate(solution, actualNutrients, targetNutrients);
            objectiveValues.add(value);
        }
        
        // 评估用户偏好目标
        ObjectiveValue preferenceValue = preferenceObjective.evaluate(solution);
        preferenceValue.setWeight(preferenceWeight);
        objectiveValues.add(preferenceValue);
        
        // 评估多样性目标
        ObjectiveValue diversityValue = diversityObjective.evaluate(solution);
        diversityValue.setWeight(diversityWeight);
        objectiveValues.add(diversityValue);
        
        // 评估平衡目标
        ObjectiveValue balanceValue = balanceObjective.evaluate(solution,targetNutrients);
        balanceValue.setWeight(balanceWeight);
        objectiveValues.add(balanceValue);
        
        return objectiveValues;
    }
    
    /**
     * 计算解决方案的总体加权评分
     * @param solution 解决方案
     * @return 总体评分（0-1之间）
     */
    public double calculateOverallScore(MealSolution solution) {
        List<ObjectiveValue> objectiveValues = solution.getObjectiveValues();
        
        if (objectiveValues == null || objectiveValues.isEmpty()) {
            return 0;
        }
        
        double totalWeightedScore = 0;
        double totalWeight = 0;
        
        for (ObjectiveValue value : objectiveValues) {
            totalWeightedScore += value.getWeightedValue();
            totalWeight += value.getWeight();
        }
        
        return totalWeight > 0 ? totalWeightedScore / totalWeight : 0;
    }
    
    /**
     * 检查解决方案是否足够好
     * @param solution 解决方案
     * @return 是否足够好
     */
    public boolean isSolutionGoodEnough(MealSolution solution) {
        // 检查所有硬性约束是否满足
        for (ObjectiveValue value : solution.getObjectiveValues()) {
            if (value.isHardConstraint() && !value.isHardConstraintSatisfied()) {
                return false;
            }
        }
        
        // 检查总体评分是否达到阈值
        double overallScore = calculateOverallScore(solution);
        return overallScore >= goodEnoughThreshold;
    }
    
    /**
     * 获取营养素目标
     * @return 营养素目标列表
     */
    public List<NutrientObjective> getNutrientObjectives() {
        return nutrientObjectives;
    }
    
    /**
     * 获取用户偏好目标
     * @return 用户偏好目标
     */
    public PreferenceObjective getPreferenceObjective() {
        return preferenceObjective;
    }
    
    /**
     * 获取多样性目标
     * @return 多样性目标
     */
    public DiversityObjective getDiversityObjective() {
        return diversityObjective;
    }
    
    /**
     * 获取平衡目标
     * @return 平衡目标
     */
    public BalanceObjective getBalanceObjective() {
        return balanceObjective;
    }
    
    
    /**
     * 设置用户偏好权重
     * @param weight 权重
     */
    public void setPreferenceWeight(double weight) {
        this.preferenceWeight = weight;
        if (preferenceObjective != null) {
            preferenceObjective.setWeight(weight);
        }
    }
    
    /**
     * 设置多样性权重
     * @param weight 权重
     */
    public void setDiversityWeight(double weight) {
        this.diversityWeight = weight;
        if (diversityObjective != null) {
            diversityObjective.setWeight(weight);
        }
    }
    
    /**
     * 设置平衡权重
     * @param weight 权重
     */
    public void setBalanceWeight(double weight) {
        this.balanceWeight = weight;
        if (balanceObjective != null) {
            balanceObjective.setWeight(weight);
        }
    }
    
    /**
     * 设置"足够好"的阈值
     * @param threshold 阈值
     */
    public void setGoodEnoughThreshold(double threshold) {
        this.goodEnoughThreshold = threshold;
    }
    
    /**
     * 获取"足够好"的阈值
     * @return 阈值
     */
    public double getGoodEnoughThreshold() {
        return goodEnoughThreshold;
    }
}