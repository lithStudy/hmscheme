package com.mealplanner;

import java.util.HashMap;
import java.util.Map;

/**
 * 营养素目标管理器，管理所有营养素目标
 */
public class NutrientTargetManager {
    private Map<String, NutrientTarget> nutrientTargets;
    private UserProfile userProfile;
    
    /**
     * 创建一个营养素目标管理器
     * @param userProfile 用户档案
     */
    public NutrientTargetManager(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.nutrientTargets = new HashMap<>();
        initializeTargets();
        adjustTargetsByHealthConditions();
    }
    
    /**
     * 初始化默认营养素目标
     */
    private void initializeTargets() {
        // 计算基本营养需求
        double tdee = userProfile.calculateTDEE();
        
        // 主要营养素（使用DASH饮食模式的比例）
        double carbsTarget = tdee * 0.525 / 4.0;  // 碳水占52.5%，4 kcal/g
        double proteinTarget = tdee * 0.175 / 4.0;  // 蛋白质占17.5%，4 kcal/g
        double fatTarget = tdee * 0.275 / 9.0;  // 脂肪占27.5%，9 kcal/g
        
        // 添加主要营养素目标
        nutrientTargets.put("calories", new NutrientTarget("热量", tdee * 0.9, tdee, tdee * 1.1, 1.0, false));
        nutrientTargets.put("carbs", new NutrientTarget("碳水化合物", carbsTarget, 1.0, false));
        nutrientTargets.put("protein", new NutrientTarget("蛋白质", proteinTarget, 1.0, false));
        nutrientTargets.put("fat", new NutrientTarget("脂肪", fatTarget, 1.0, false));
        
        // 微量元素目标
        double calciumTarget = userProfile.getGender().equalsIgnoreCase("male") ? 1000.0 : 1000.0;  // 钙(mg)
        double potassiumTarget = 3500.0;  // 钾(mg)
        double sodiumTarget = 2000.0;  // 钠(mg)，限制性营养素
        double magnesiumTarget = userProfile.getGender().equalsIgnoreCase("male") ? 400.0 : 310.0;  // 镁(mg)
        double phosphorusTarget = 700.0;  // 磷(mg)
        double ironTarget = userProfile.getGender().equalsIgnoreCase("male") ? 8.0 : 18.0;  // 铁(mg)
        
        // 添加微量元素目标
        nutrientTargets.put("calcium", new NutrientTarget("钙", calciumTarget, 0.7, false));
        nutrientTargets.put("potassium", new NutrientTarget("钾", potassiumTarget, 0.7, false));
        nutrientTargets.put("sodium", new NutrientTarget("钠", sodiumTarget, 0.8, true));  // 钠是限制性营养素
        nutrientTargets.put("magnesium", new NutrientTarget("镁", magnesiumTarget, 0.7, false));
        nutrientTargets.put("phosphorus", new NutrientTarget("磷", phosphorusTarget, 0.5, false));
        nutrientTargets.put("iron", new NutrientTarget("铁", ironTarget, 0.5, false));
    }
    
    /**
     * 根据健康状况调整营养素目标
     */
    private void adjustTargetsByHealthConditions() {
        String[] conditions = userProfile.getHealthConditions();
        if (conditions == null || conditions.length == 0) {
            return;
        }
        
        // 创建临时存储，用于记录每种营养素的最严格限制
        Map<String, Double> minTargetValues = new HashMap<>();
        Map<String, Double> maxTargetValues = new HashMap<>();
        Map<String, Double> maxWeights = new HashMap<>();
        
        // 第一步：收集所有疾病对各营养素的要求
        for (String condition : conditions) {
            switch (condition.toLowerCase()) {
                case "hypertension": // 高血压
                    // 钠：降低目标值至1500mg，权重1.5
                    updateNutrientConstraint("sodium", 1500.0, null, 1.5, minTargetValues, maxTargetValues, maxWeights);
                    // 钾：提高目标值至4700mg，权重1.2
                    updateNutrientConstraint("potassium", null, 4700.0, 1.2, minTargetValues, maxTargetValues, maxWeights);
                    break;
                    
                case "diabetes": // 糖尿病
                    // 碳水：降低目标值15%，权重1.5
                    double diabetesCarbTarget = getNutrientTarget("carbs").getTargetValue() * 0.85;
                    updateNutrientConstraint("carbs", diabetesCarbTarget, null, 1.5, minTargetValues, maxTargetValues, maxWeights);
                    // 脂肪：权重1.2
                    updateNutrientConstraint("fat", null, null, 1.2, minTargetValues, maxTargetValues, maxWeights);
                    break;
                    
                case "hyperlipidemia": // 高血脂
                    // 脂肪：降低目标值20%，权重1.5
                    double lipidFatTarget = getNutrientTarget("fat").getTargetValue() * 0.8;
                    updateNutrientConstraint("fat", lipidFatTarget, null, 1.5, minTargetValues, maxTargetValues, maxWeights);
                    break;
                    
                case "gout": // 痛风
                    // 蛋白质：降低目标值10%，权重1.3
                    double goutProteinTarget = getNutrientTarget("protein").getTargetValue() * 0.9;
                    updateNutrientConstraint("protein", goutProteinTarget, null, 1.3, minTargetValues, maxTargetValues, maxWeights);
                    break;
                    
                case "ckd": // 慢性肾病（无透析）
                    // 蛋白质：降低目标值30%，权重1.5
                    double ckdProteinTarget = getNutrientTarget("protein").getTargetValue() * 0.7;
                    updateNutrientConstraint("protein", ckdProteinTarget, null, 1.5, minTargetValues, maxTargetValues, maxWeights);
                    // 钾：降低目标值至2000mg，权重1.3
                    updateNutrientConstraint("potassium", 2000.0, null, 1.3, minTargetValues, maxTargetValues, maxWeights);
                    // 磷：降低目标值至800mg，权重1.3
                    updateNutrientConstraint("phosphorus", 800.0, null, 1.3, minTargetValues, maxTargetValues, maxWeights);
                    break;
                    
                case "ckd_dialysis": // 慢性肾病（透析）
                    // 蛋白质：提高目标值20%，权重1.2
                    double dialysisProteinTarget = getNutrientTarget("protein").getTargetValue() * 1.2;
                    updateNutrientConstraint("protein", null, dialysisProteinTarget, 1.2, minTargetValues, maxTargetValues, maxWeights);
                    // 钾：降低目标值至1500mg，权重1.5
                    updateNutrientConstraint("potassium", 1500.0, null, 1.5, minTargetValues, maxTargetValues, maxWeights);
                    break;
                    
                case "osteoporosis": // 骨质疏松
                    // 钙：提高目标值至1200mg，权重1.5
                    updateNutrientConstraint("calcium", null, 1200.0, 1.5, minTargetValues, maxTargetValues, maxWeights);
                    // 镁：权重1.2
                    updateNutrientConstraint("magnesium", null, null, 1.2, minTargetValues, maxTargetValues, maxWeights);
                    break;
                    
                case "anemia": // 贫血
                    // 铁：提高目标值50%，权重1.5
                    double anemiaIronTarget = getNutrientTarget("iron").getTargetValue() * 1.5;
                    updateNutrientConstraint("iron", null, anemiaIronTarget, 1.5, minTargetValues, maxTargetValues, maxWeights);
                    break;
            }
        }
        
        // 第二步：应用最终的调整
        for (String nutrientName : nutrientTargets.keySet()) {
            NutrientTarget target = getNutrientTarget(nutrientName);
            
            // 应用权重调整（取最大值）
            if (maxWeights.containsKey(nutrientName)) {
                target.setWeight(maxWeights.get(nutrientName));
            }
            
            // 应用目标值调整（取最小值或最大值，取决于营养素类型）
            if (target.isLimitingNutrient()) {
                // 对于限制性营养素（如钠），取最小的目标值
                if (minTargetValues.containsKey(nutrientName)) {
                    target.adjustTargetValue(minTargetValues.get(nutrientName));
                }
            } else {
                // 对于非限制性营养素，需要平衡考虑
                if (minTargetValues.containsKey(nutrientName) && maxTargetValues.containsKey(nutrientName)) {
                    // 如果既有最小值又有最大值，取中间值
                    double minValue = minTargetValues.get(nutrientName);
                    double maxValue = maxTargetValues.get(nutrientName);
                    // 确保最小值不大于最大值
                    if (minValue <= maxValue) {
                        target.adjustTargetValue((minValue + maxValue) / 2);
                    } else {
                        // 如果有冲突，优先考虑限制
                        target.adjustTargetValue(minValue);
                    }
                } else if (minTargetValues.containsKey(nutrientName)) {
                    // 只有最小值，直接应用
                    target.adjustTargetValue(minTargetValues.get(nutrientName));
                } else if (maxTargetValues.containsKey(nutrientName)) {
                    // 只有最大值，直接应用
                    target.adjustTargetValue(maxTargetValues.get(nutrientName));
                }
            }
        }
        
        // 打印调整后的目标值（用于调试）
        logAdjustedTargets();
    }
    
    /**
     * 更新营养素约束
     * @param nutrientName 营养素名称
     * @param minValue 最小目标值（限制上限）
     * @param maxValue 最大目标值（提高下限）
     * @param weight 权重
     * @param minTargetValues 最小目标值映射
     * @param maxTargetValues 最大目标值映射
     * @param maxWeights 最大权重映射
     */
    private void updateNutrientConstraint(String nutrientName, Double minValue, Double maxValue, 
                                         Double weight, Map<String, Double> minTargetValues, 
                                         Map<String, Double> maxTargetValues, Map<String, Double> maxWeights) {
        // 更新最小目标值
        if (minValue != null) {
            if (!minTargetValues.containsKey(nutrientName) || minValue < minTargetValues.get(nutrientName)) {
                minTargetValues.put(nutrientName, minValue);
            }
        }
        
        // 更新最大目标值
        if (maxValue != null) {
            if (!maxTargetValues.containsKey(nutrientName) || maxValue > maxTargetValues.get(nutrientName)) {
                maxTargetValues.put(nutrientName, maxValue);
            }
        }
        
        // 更新权重（取最大值）
        if (weight != null) {
            if (!maxWeights.containsKey(nutrientName) || weight > maxWeights.get(nutrientName)) {
                maxWeights.put(nutrientName, weight);
            }
        }
    }
    
    /**
     * 记录调整后的目标值（用于调试）
     */
    private void logAdjustedTargets() {
        System.out.println("\n========== 调整后的营养素目标 ==========");
        for (String nutrientName : nutrientTargets.keySet()) {
            NutrientTarget target = getNutrientTarget(nutrientName);
            System.out.printf("%s: 目标值=%.1f, 范围=[%.1f-%.1f], 权重=%.1f, 限制性=%b\n",
                             target.getName(), target.getTargetValue(), target.getMinValue(),
                             target.getMaxValue(), target.getWeight(), target.isLimitingNutrient());
        }
        System.out.println("========================================\n");
    }
    
    /**
     * 获取营养素目标
     * @param nutrientName 营养素名称
     * @return 营养素目标
     */
    public NutrientTarget getNutrientTarget(String nutrientName) {
        return nutrientTargets.get(nutrientName);
    }
    
    /**
     * 计算食物的综合评分
     * @param food 食物
     * @return 评分（0-1之间）
     */
    public double calculateFoodScore(Food food) {
        double totalWeight = 0.0;
        double weightedScore = 0.0;
        
        // 计算主要营养素评分
        NutrientTarget caloriesTarget = getNutrientTarget("calories");
        NutrientTarget carbsTarget = getNutrientTarget("carbs");
        NutrientTarget proteinTarget = getNutrientTarget("protein");
        NutrientTarget fatTarget = getNutrientTarget("fat");
        
        weightedScore += caloriesTarget.getWeightedScore(food.getCalories());
        weightedScore += carbsTarget.getWeightedScore(food.getCarbohydrates());
        weightedScore += proteinTarget.getWeightedScore(food.getProtein());
        weightedScore += fatTarget.getWeightedScore(food.getFat());
        
        totalWeight += caloriesTarget.getWeight();
        totalWeight += carbsTarget.getWeight();
        totalWeight += proteinTarget.getWeight();
        totalWeight += fatTarget.getWeight();
        
        // 计算微量元素评分
        NutrientTarget calciumTarget = getNutrientTarget("calcium");
        NutrientTarget potassiumTarget = getNutrientTarget("potassium");
        NutrientTarget sodiumTarget = getNutrientTarget("sodium");
        NutrientTarget magnesiumTarget = getNutrientTarget("magnesium");
        
        weightedScore += calciumTarget.getWeightedScore(food.getCalcium());
        weightedScore += potassiumTarget.getWeightedScore(food.getPotassium());
        weightedScore += sodiumTarget.getWeightedScore(food.getSodium());
        weightedScore += magnesiumTarget.getWeightedScore(food.getMagnesium());
        
        totalWeight += calciumTarget.getWeight();
        totalWeight += potassiumTarget.getWeight();
        totalWeight += sodiumTarget.getWeight();
        totalWeight += magnesiumTarget.getWeight();
        
        // 返回加权平均分
        return weightedScore / totalWeight;
    }
    
    /**
     * 获取所有营养素目标
     * @return 营养素目标映射
     */
    public Map<String, NutrientTarget> getAllNutrientTargets() {
        return nutrientTargets;
    }
} 