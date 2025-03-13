package com.mealplanner.genetic.util;

import java.util.HashMap;
import java.util.Map;

import com.mealplanner.genetic.algorithm.NSGAIIMealPlanner;
import com.mealplanner.model.UserProfile;

public class NutrientObjectiveConfig {
    // 默认营养素达成率范围
    private static final double DEFAULT_MIN_RATE = 0.9;
    private static final double DEFAULT_MAX_RATE = 1.1;
    
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
    public static void configureNutrientAchievementRates(NSGAIIMealPlanner planner, UserProfile userProfile) {
        // 创建营养素达成率映射
        Map<String, double[]> nutrientRates = new HashMap<>(DEFAULT_NUTRIENT_RATES);
        
        // 根据用户健康状况调整营养素达成率
        if (userProfile != null && userProfile.getHealthConditions() != null) {
            for (String condition : userProfile.getHealthConditions()) {
                Map<String, double[]> diseaseRates = DISEASE_NUTRIENT_RATES.get(condition.toLowerCase());
                if (diseaseRates != null) {
                    // 将疾病特定的营养素达成率应用到总映射中
                    nutrientRates.putAll(diseaseRates);
                }
            }
        }
        
        // 设置每个营养素的达成率范围
        planner.setNutrientAchievementRates(nutrientRates);
        
        // 为了向后兼容，也设置全局的最小和最大达成率
        // 这些值将作为默认值，当特定营养素没有设置时使用
        // planner.setMinNutrientAchievementRate(DEFAULT_MIN_RATE);
        // planner.setMaxNutrientAchievementRate(DEFAULT_MAX_RATE);
    }
    
    /**
     * 配置默认的营养素达成率范围（不考虑用户健康状况）
     * @param planner NSGA-II膳食规划器
     */
    public static void configureNutrientAchievementRates(NSGAIIMealPlanner planner) {
        // 设置默认的营养素达成率范围
        planner.setNutrientAchievementRates(DEFAULT_NUTRIENT_RATES);
        
        // 为了向后兼容，也设置全局的最小和最大达成率
        // planner.setMinNutrientAchievementRate(DEFAULT_MIN_RATE);
        // planner.setMaxNutrientAchievementRate(DEFAULT_MAX_RATE);
    }
}
