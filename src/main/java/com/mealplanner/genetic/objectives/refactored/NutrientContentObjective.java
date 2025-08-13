package com.mealplanner.genetic.objectives.refactored;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.objectives.core.BaseObjectiveEvaluator;
import com.mealplanner.model.NutrientType;
import com.mealplanner.model.UserProfile;

import java.util.Map;

/**
 * 营养素含量目标，直接评估单个营养素含量的达成情况
 * 职责：
 * 1. 评估单个营养素的达成情况
 */
public class NutrientContentObjective extends BaseObjectiveEvaluator {
    
    private final NutrientType nutrientType;
    private final UserProfile userProfile;

    // 主要营养素
    public static final NutrientType[] mainNutrients = {
            NutrientType.CALORIES, NutrientType.PROTEIN, NutrientType.FAT,
            NutrientType.CARBOHYDRATES, NutrientType.FIBER
    };

    // 微量营养素
    public static final NutrientType[] microNutrients = {
            NutrientType.CALCIUM, NutrientType.IRON, NutrientType.VITAMIN_A,
            NutrientType.VITAMIN_C, NutrientType.SODIUM
    };

    public static final String nutrientNameSuffix = "营养素目标_";
    
    public NutrientContentObjective(NutrientType nutrientType, UserProfile userProfile, double weight) {
        super(nutrientNameSuffix + nutrientType.getDisplayName(), weight, false, 0.0);
        this.nutrientType = nutrientType;
        this.userProfile = userProfile;
    }
    
    @Override
    public ObjectiveValue evaluate(MealSolution solution, Map<NutrientType, Double> targetNutrients) {
        Map<NutrientType, Double> actualNutrients = solution.calculateTotalNutrients();
        
        double actualValue = actualNutrients.get(nutrientType);
        double targetValue = targetNutrients.get(nutrientType);
        
        double score = 0.0;
        if (targetValue > 0) {
            double ratio = actualValue / targetValue;
            
            // 获取营养素的合理范围
            Map<NutrientType, double[]> nutrientRates = NutrientType.getNutrientRates(userProfile);
            double[] range = nutrientRates.get(nutrientType);
            double minRate = range[0];
            double maxRate = range[1];
            
            if (ratio >= minRate && ratio <= maxRate) {
                // 在合理范围内，得分较高
                score = 1.0 - Math.abs(ratio - 1.0) * 0.5;
            } else if (ratio < minRate) {
                // 不足，按比例扣分
                score = ratio / minRate * 0.5;
            } else {
                // 过量，按比例扣分
                double excess = (ratio - maxRate) / maxRate;
                score = Math.max(0.0, 0.5 - excess * 0.3);
            }
        }
        
        score = Math.max(0.0, Math.min(1.0, score));
        return createObjectiveValue(score);
    }
    
    public NutrientType getNutrientType() {
        return nutrientType;
    }
}
