package com.mealplanner.genetic.objectives;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.objectives.NutrientScoring.CalorieScoringStrategy;
import com.mealplanner.genetic.objectives.NutrientScoring.DefaultNutrientScoringStrategy;
import com.mealplanner.genetic.objectives.NutrientScoring.NutrientScoringStrategy;
import com.mealplanner.genetic.objectives.NutrientScoring.StrictExcessPenaltyScoringStrategy;
import com.mealplanner.model.NutrientType;
import com.mealplanner.model.UserProfile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 营养素目标类，评估解决方案在特定营养素上的表现
 */
public class NutrientObjective extends AbstractObjectiveEvaluator {
    // 对应的营养素类型
    private NutrientType nutrientType;
    
    // 营养素硬性约束阈值，提高营养素目标的硬性约束阈值，确保所有解决方案必须满足更高的营养要求：
    private double hardConstraintThreshold = 0.8; // 默认80%的匹配度
    
    private Map<NutrientType, double[]> nutrientRates; // 添加营养素达成率范围字段
    
    private NutrientScoringStrategy scoringStrategy;
    
    /**
     * 构造函数
     * @param name 目标名称
     * @param nutrientType 营养素类型
     * @param weight 目标权重
     * @param penalizeExcess 是否惩罚过量
     * @param nutrientRates 营养素达成率范围
     */
    public NutrientObjective(NutrientType nutrientType, double weight, NutrientScoringStrategy scoringStrategy, Map<NutrientType, double[]> nutrientRates) {
        super(nutrientType.getName(), weight);
        this.nutrientType = nutrientType;
        this.nutrientRates = nutrientRates;
        this.scoringStrategy = scoringStrategy;
    }

    public NutrientObjective(NutrientType nutrientType, double weight, NutrientScoringStrategy scoringStrategy, Map<NutrientType, double[]> nutrientRates, double hardConstraintThreshold) {
        super(nutrientType.getName(), weight);
        this.nutrientType = nutrientType;
        this.nutrientRates = nutrientRates;
        this.scoringStrategy = scoringStrategy;
        this.hardConstraintThreshold = hardConstraintThreshold;
        
    }
    
    /**
     * 创建标准营养素目标列表
     * @param userProfile 用户档案
     * @return 营养素目标列表
     */
    public static List<NutrientObjective> createStandardNutrientObjectives(UserProfile userProfile) {
        List<NutrientObjective> nutrientObjectives = new ArrayList<>();
        
        // 获取营养素达成率范围
        Map<NutrientType, double[]> nutrientRates = NutrientType.getNutrientRates(userProfile);
        // 获取营养素权重
        Map<NutrientType, Double> nutrientWeights = NutrientType.getNutrientWeights(userProfile);
        // 自定义评估器的营养素类型
        Set<NutrientType> customNutrientTypes = new HashSet<>();

        // 初始化热量目标评估器
        nutrientObjectives.add(new NutrientObjective(
            NutrientType.CALORIES,
            nutrientWeights.get(NutrientType.CALORIES),
            new CalorieScoringStrategy(),
            nutrientRates));
        customNutrientTypes.add(NutrientType.CALCIUM);
        // 初始化钠目标评估器
        nutrientObjectives.add(new NutrientObjective(
            NutrientType.SODIUM,
            nutrientWeights.get(NutrientType.SODIUM),
            new StrictExcessPenaltyScoringStrategy(),
            nutrientRates));
        customNutrientTypes.add(NutrientType.SODIUM);
        // 添加其他营养素目标
        for (NutrientType type : NutrientType.values()) {
            if (!customNutrientTypes.contains(type)) { 
                nutrientObjectives.add(new NutrientObjective(
                        type,
                        nutrientWeights.get(type),
                        new DefaultNutrientScoringStrategy(),
                        nutrientRates));
            }
        }
        
        return nutrientObjectives;
    }
    
    /**
     * 实现AbstractObjectiveEvaluator抽象类的evaluate方法
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 目标值
     */
    @Override
    public ObjectiveValue evaluate(MealSolution solution, Map<NutrientType, Double> targetNutrients) {
        Map<NutrientType, Double> actualNutrients = solution.calculateTotalNutrients();
        return evaluate(solution, actualNutrients, targetNutrients);
    }
    
    /**
     * 评估解决方案在特定营养素上的表现
     * @param solution 解决方案
     * @param actualNutrients 实际营养素
     * @param targetNutrients 目标营养素
     * @return 目标值
     */
    public ObjectiveValue evaluate(MealSolution solution, Map<NutrientType, Double> actualNutrients, Map<NutrientType, Double> targetNutrients) {
        // 获取实际和目标营养素值
        double actual = actualNutrients.get(nutrientType);
        double target = targetNutrients.get(nutrientType);
        
        // 计算营养素得分
        double score = calculateNutrientScore(actual, target);
        
        // 创建目标值对象，使用带硬性约束的构造函数
        return new ObjectiveValue(getName(), score, getWeight(), true, hardConstraintThreshold);
    }
    
    /**
     * 获取特定营养素的值
     * @param nutrients 营养素对象
     * @return 营养素值
     */
    private double getNutrientValue(Map<NutrientType, Double> nutrients) {
        if (nutrientType == null) {
            return 0;
        }
        return nutrients.get(nutrientType);
        
    }
    
    /**
     * 计算营养素匹配度评分
     * @param actual 实际值
     * @param target 目标值
     * @return 评分（0-1之间）
     */
    private double calculateNutrientScore(double actual, double target) {
        if (target == 0) {
            return actual == 0 ? 1.0 : 0.0;
        }
        
        double[] rates = nutrientRates.getOrDefault(nutrientType, new double[]{0.8, 1.2});
        double ratio = actual / target;
        
        return scoringStrategy.calculateScore(ratio, rates[0], rates[1]);
    }
    
    /**
     * 获取营养素类型
     * @return 营养素类型
     */
    public NutrientType getNutrientType() {
        return nutrientType;
    }


    
}