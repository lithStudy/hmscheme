package com.mealplanner.genetic.objectives;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.Nutrition;
import com.mealplanner.model.NutrientType;
import com.mealplanner.model.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 营养素目标类，评估解决方案在特定营养素上的表现
 */
public class NutrientObjective extends AbstractObjectiveEvaluator {
    // 对应的营养素类型
    private NutrientType nutrientType;
    
    // 营养素评分方式（是否惩罚过量）
    private boolean penalizeExcess; 
    
    // 营养素偏差容忍度，降低偏差容忍度，使系统对营养素目标更加严格
    private double deviationTolerance = 0.1; // 默认允许10%的偏差
    
    // 营养素硬性约束阈值，提高营养素目标的硬性约束阈值，确保所有解决方案必须满足更高的营养要求：
    private double hardConstraintThreshold = 0.8; // 默认80%的匹配度
    
    /**
     * 构造函数
     * @param name 目标名称
     * @param nutrientType 营养素类型
     * @param weight 目标权重
     * @param penalizeExcess 是否惩罚过量
     */
    public NutrientObjective(String name, NutrientType nutrientType, double weight, boolean penalizeExcess) {
        super(name, weight);
        this.nutrientType = nutrientType;
        this.penalizeExcess = penalizeExcess;
    }

    /**
     * 构造函数
     * @param name 目标名称
     * @param nutrientType 营养素类型
     * @param weight 目标权重
     * @param penalizeExcess 是否惩罚过量
     * @param deviationTolerance 偏差容忍度
     * @param hardConstraintThreshold 硬性约束阈值
     */
    public NutrientObjective(String name, NutrientType nutrientType, double weight, boolean penalizeExcess, double deviationTolerance, double hardConstraintThreshold) {
        super(name, weight);
        this.nutrientType = nutrientType;
        this.penalizeExcess = penalizeExcess;
        this.deviationTolerance = deviationTolerance;
        this.hardConstraintThreshold = hardConstraintThreshold;
    }
    
    /**
     * 创建标准营养素目标列表
     * @param userProfile 用户档案
     * @return 营养素目标列表
     */
    public static List<NutrientObjective> createStandardNutrientObjectives(UserProfile userProfile) {
        List<NutrientObjective> nutrientObjectives = new ArrayList<>();
        
        // 获取营养素权重
        Map<NutrientType, Double> nutrientWeights = NutrientType.getNutrientWeights(userProfile);
        
        // 初始化营养素目标
        // 热量目标，使用更严格的参数
        nutrientObjectives.add(new NutrientObjective(
                NutrientType.CALORIES.getName() + "_objective", 
                NutrientType.CALORIES, 
                nutrientWeights.get(NutrientType.CALORIES), 
                NutrientType.CALORIES.isDefaultPenalizeExcess(), 
                0.05, 0.9));
        
        // 添加其他营养素目标
        for (NutrientType type : NutrientType.values()) {
            if (type != NutrientType.CALORIES) { // 热量已经单独处理
                nutrientObjectives.add(new NutrientObjective(
                        type.getName() + "_objective",
                        type,
                        nutrientWeights.get(type),
                        type.isDefaultPenalizeExcess()));
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
    public ObjectiveValue evaluate(MealSolution solution, Nutrition targetNutrients) {
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        return evaluate(solution, actualNutrients, targetNutrients);
    }
    
    /**
     * 评估解决方案在特定营养素上的表现
     * @param solution 解决方案
     * @param actualNutrients 实际营养素
     * @param targetNutrients 目标营养素
     * @return 目标值
     */
    public ObjectiveValue evaluate(MealSolution solution, Nutrition actualNutrients, Nutrition targetNutrients) {
        // 获取实际和目标营养素值
        double actual = getNutrientValue(actualNutrients);
        double target = getNutrientValue(targetNutrients);
        
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
    private double getNutrientValue(Nutrition nutrients) {
        if (nutrientType == null) {
            return 0;
        }
        
        switch (nutrientType) {
            case CALORIES:
                return nutrients.getCalories();
            case CARBOHYDRATES:
                return nutrients.getCarbohydrates();
            case PROTEIN:
                return nutrients.getProtein();
            case FAT:
                return nutrients.getFat();
            case CALCIUM:
                return nutrients.getCalcium();
            case POTASSIUM:
                return nutrients.getPotassium();
            case SODIUM:
                return nutrients.getSodium();
            case MAGNESIUM:
                return nutrients.getMagnesium();
            case IRON:
                return nutrients.getIron();
            case PHOSPHORUS:
                return nutrients.getPhosphorus();
            default:
                return 0;
        }
    }
    
    /**
     * 计算营养素匹配度评分
     * @param actual 实际值
     * @param target 目标值
     * @return 评分（0-1之间）
     */
    private double calculateNutrientScore(double actual, double target) {
        // 避免除以零
        if (target == 0) {
            return actual == 0 ? 1.0 : 0.0;
        }
        
        // 计算实际值与目标值的比率
        double ratio = actual / target;
        
        // 计算比率与理想比率1.0的偏差
        double deviation = Math.abs(ratio - 1.0);
        
        // 如果偏差在容忍范围内，给予高分
        if (deviation <= deviationTolerance) {
            // 根据偏差程度给予分数，偏差越小分数越高
            return 1.0 - (deviation / deviationTolerance) * 0.2;
        }
        
        // 对热量不足的惩罚更严厉
        if (ratio < 1.0) {
            if (nutrientType == NutrientType.CALORIES) {
                // 热量不足：更快速地减少分数
                return Math.max(0, 0.8 - (1.0 - ratio - deviationTolerance) * 3);
            } else {
                // 其他营养素不足：标准线性减少分数
                return Math.max(0, 0.8 - (1.0 - ratio - deviationTolerance) * 2);
            }
        } else if (penalizeExcess) {
            // 营养素过量且需要惩罚：指数减少分数
            return Math.max(0, 0.8 * Math.exp(-(ratio - 1.0 - deviationTolerance)));
        } else {
            // 营养素过量但不严格惩罚：缓慢减少分数
            return Math.max(0, 0.8 - (ratio - 1.0 - deviationTolerance) * 0.5);
        }
    }
    
    /**
     * 获取营养素类型
     * @return 营养素类型
     */
    public NutrientType getNutrientType() {
        return nutrientType;
    }
    
    /**
     * 设置营养素类型
     * @param nutrientType 营养素类型
     */
    public void setNutrientType(NutrientType nutrientType) {
        this.nutrientType = nutrientType;
    }
    
    /**
     * 获取营养素名称
     * @return 营养素名称
     */
    public String getNutrientName() {
        return nutrientType != null ? nutrientType.getName() : null;
    }
    
    /**
     * 是否惩罚过量
     * @return 是否惩罚过量
     */
    public boolean isPenalizeExcess() {
        return penalizeExcess;
    }
    
    /**
     * 设置是否惩罚过量
     * @param penalizeExcess 是否惩罚过量
     */
    public void setPenalizeExcess(boolean penalizeExcess) {
        this.penalizeExcess = penalizeExcess;
    }
    
    /**
     * 获取偏差容忍度
     * @return 偏差容忍度
     */
    public double getDeviationTolerance() {
        return deviationTolerance;
    }
    
    /**
     * 设置偏差容忍度
     * @param deviationTolerance 偏差容忍度
     */
    public void setDeviationTolerance(double deviationTolerance) {
        this.deviationTolerance = deviationTolerance;
    }
    
    /**
     * 获取硬性约束阈值
     * @return 硬性约束阈值
     */
    public double getHardConstraintThreshold() {
        return hardConstraintThreshold;
    }
    
    /**
     * 设置硬性约束阈值
     * @param hardConstraintThreshold 硬性约束阈值
     */
    public void setHardConstraintThreshold(double hardConstraintThreshold) {
        this.hardConstraintThreshold = hardConstraintThreshold;
    }
}