package com.mealplanner.genetic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mealplanner.genetic.algorithm.NSGAIIMealPlanner;
import com.mealplanner.model.UserProfile;

public class NutrientObjectiveConfig {
    // 不同营养素的默认达成率范围
    private static final Map<String, double[]> DEFAULT_NUTRIENT_RATES = new HashMap<>();
    
    // 不同疾病的营养素达成率调整
    private static final Map<String, Map<String, double[]>> DISEASE_NUTRIENT_RATES = new HashMap<>();
    
    static {
        // 初始化默认营养素达成率范围
        // 格式: [最小达成率, 最大达成率]
        DEFAULT_NUTRIENT_RATES.put("calories", new double[]{0.9, 1.1});      // 热量
        DEFAULT_NUTRIENT_RATES.put("carbohydrates", new double[]{0.85, 1.15}); // 碳水化合物
        DEFAULT_NUTRIENT_RATES.put("protein", new double[]{0.9, 1.2});       // 蛋白质
        DEFAULT_NUTRIENT_RATES.put("fat", new double[]{0.7, 1.1});           // 脂肪
        DEFAULT_NUTRIENT_RATES.put("calcium", new double[]{0.8, 1.5});       // 钙
        DEFAULT_NUTRIENT_RATES.put("potassium", new double[]{0.8, 1.5});     // 钾
        DEFAULT_NUTRIENT_RATES.put("sodium", new double[]{0.5, 1.0});        // 钠（限制上限）
        DEFAULT_NUTRIENT_RATES.put("magnesium", new double[]{0.8, 1.5});     // 镁
        
        // 初始化疾病特定的营养素达成率调整
        
        // 糖尿病
        Map<String, double[]> diabetesRates = new HashMap<>();
        diabetesRates.put("calories", new double[]{0.9, 1.0});       // 控制热量
        diabetesRates.put("carbohydrates", new double[]{0.7, 0.9});  // 严格限制碳水
        diabetesRates.put("protein", new double[]{1.0, 1.2});        // 适当增加蛋白质
        diabetesRates.put("fat", new double[]{0.8, 1.0});            // 控制脂肪
        diabetesRates.put("sodium", new double[]{0.5, 0.9});         // 限制钠
        DISEASE_NUTRIENT_RATES.put("diabetes", diabetesRates);
        
        // 高血压
        Map<String, double[]> hypertensionRates = new HashMap<>();
        hypertensionRates.put("sodium", new double[]{0.3, 0.7});     // 严格限制钠
        hypertensionRates.put("potassium", new double[]{1.0, 1.5});  // 增加钾
        hypertensionRates.put("fat", new double[]{0.7, 0.9});        // 限制脂肪
        DISEASE_NUTRIENT_RATES.put("hypertension", hypertensionRates);
        
        // 肾病
        Map<String, double[]> kidneyDiseaseRates = new HashMap<>();
        kidneyDiseaseRates.put("protein", new double[]{0.6, 0.8});   // 限制蛋白质
        kidneyDiseaseRates.put("sodium", new double[]{0.4, 0.8});    // 限制钠
        kidneyDiseaseRates.put("potassium", new double[]{0.6, 0.9}); // 限制钾
        kidneyDiseaseRates.put("phosphorus", new double[]{0.6, 0.9});// 限制磷
        DISEASE_NUTRIENT_RATES.put("kidney_disease", kidneyDiseaseRates);
        
        // 骨质疏松
        Map<String, double[]> osteoporosisRates = new HashMap<>();
        osteoporosisRates.put("calcium", new double[]{1.2, 1.8});    // 增加钙
        osteoporosisRates.put("vitamin_d", new double[]{1.2, 1.8});  // 增加维生素D
        DISEASE_NUTRIENT_RATES.put("osteoporosis", osteoporosisRates);
        
        // 心脏病
        Map<String, double[]> heartDiseaseRates = new HashMap<>();
        heartDiseaseRates.put("fat", new double[]{0.6, 0.8});        // 严格限制脂肪
        heartDiseaseRates.put("sodium", new double[]{0.4, 0.7});     // 限制钠
        heartDiseaseRates.put("fiber", new double[]{1.2, 1.5});      // 增加纤维
        DISEASE_NUTRIENT_RATES.put("heart_disease", heartDiseaseRates);
    }
    
    /**
     * 配置营养素达成率范围
     * @param planner NSGA-II膳食规划器
     * @param userProfile 用户档案（包含健康状况）
     */
    public static Map<String, double[]> configureNutrientAchievementRates( UserProfile userProfile) {
        // 创建营养素达成率映射
        Map<String, double[]> nutrientRates = new HashMap<>(DEFAULT_NUTRIENT_RATES);
        
        // 根据用户健康状况调整营养素达成率
        if (userProfile != null && userProfile.getHealthConditions() != null) {
            // 创建一个临时映射，用于存储所有疾病对每个营养素的要求
            Map<String, List<double[]>> allDiseaseRequirements = new HashMap<>();
            
            // 收集所有疾病对每个营养素的要求
            for (String condition : userProfile.getHealthConditions()) {
                Map<String, double[]> diseaseRates = DISEASE_NUTRIENT_RATES.get(condition.toLowerCase());
                if (diseaseRates != null) {
                    for (Map.Entry<String, double[]> entry : diseaseRates.entrySet()) {
                        String nutrient = entry.getKey();
                        double[] range = entry.getValue();
                        
                        if (!allDiseaseRequirements.containsKey(nutrient)) {
                            allDiseaseRequirements.put(nutrient, new ArrayList<>());
                        }
                        allDiseaseRequirements.get(nutrient).add(range);
                    }
                }
            }
            
            // 对于每个营养素，综合考虑所有疾病的要求，取最严格的限制
            for (Map.Entry<String, List<double[]>> entry : allDiseaseRequirements.entrySet()) {
                String nutrient = entry.getKey();
                List<double[]> ranges = entry.getValue();
                
                if (!ranges.isEmpty()) {
                    // 初始化为第一个范围
                    double minRate = ranges.get(0)[0];
                    double maxRate = ranges.get(0)[1];
                    
                    // 比较所有范围，取最严格的限制
                    for (int i = 1; i < ranges.size(); i++) {
                        double[] range = ranges.get(i);
                        minRate = Math.max(minRate, range[0]); // 对于下限，取最大值
                        maxRate = Math.min(maxRate, range[1]); // 对于上限，取最小值
                    }
                    
                    // 确保下限不大于上限
                    if (minRate <= maxRate) {
                        nutrientRates.put(nutrient, new double[]{minRate, maxRate});
                    } else {
                        // 如果出现下限大于上限的情况，取中间值
                        double middleValue = (minRate + maxRate) / 2;
                        nutrientRates.put(nutrient, new double[]{middleValue, middleValue});
                        System.out.println("警告：营养素 " + nutrient + " 的多种疾病要求冲突，取中间值 " + middleValue);
                    }
                }
            }
        }
        return nutrientRates;
    }
}
