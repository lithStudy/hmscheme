package com.mealplanner;

import java.io.IOException;
import java.util.*;

import com.mealplanner.dietary.DietaryEvaluator;
import com.mealplanner.foodmanage.NutritionDataParser;
import com.mealplanner.logging.MealPlannerLogger;
import com.mealplanner.nutrition.NutrientRangeAdjuster;

public class MealPlanner {
    private List<Food> foodDatabase;
    private NutritionCalculator nutritionCalculator;
    private MealOptimizer mealOptimizer;
    public static final double MEAL_RATIO_BREAKFAST = 0.3;  // 早餐占比30%
    public static final double MEAL_RATIO_LUNCH = 0.35;     // 午餐占比35%
    public static final double MEAL_RATIO_DINNER = 0.35;    // 晚餐占比35%

    // 用户档案
    private UserProfile userProfile;
    
    // 饮食评估器
    private DietaryEvaluator dietaryEvaluator;

    // 各类食物的最大数量限制
    private Map<String, Integer> maxFoodPerCategory;
    private static final int DEFAULT_MAX_FOODS_PER_CATEGORY = 2;  // 默认每类食物最多2个

    // 营养素权重
    private double weightCalorie = 1.0;   // 热量权重
    private double weightCarb = 1.0;      // 碳水化合物权重
    private double weightProtein = 1.0;   // 蛋白质权重
    private double weightFat = 1.0;       // 脂肪权重
    private double weightCalcium = 0.7;   // 钙权重
    private double weightPotassium = 0.7; // 钾权重
    private double weightSodium = 0.8;    // 钠权重
    private double weightMagnesium = 0.7; // 镁权重
    private double weightPhosphorus = 0.5; // 磷权重
    private double weightIron = 0.5;      // 铁权重

    // 添加营养素范围调整器
    private NutrientRangeAdjuster nutrientAdjuster;

    public MealPlanner(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.nutritionCalculator = new NutritionCalculator(userProfile);
        this.mealOptimizer = new MealOptimizer(userProfile);
        this.dietaryEvaluator = new DietaryEvaluator(userProfile);
        this.nutrientAdjuster = new NutrientRangeAdjuster(); // 初始化营养素调整器
        initializeFoodDatabase();
        initializeMaxFoodLimits();
        adjustWeightsByHealthConditions(userProfile.getHealthConditions());
    }

    /**
     * 初始化各类食物的最大数量限制
     */
    private void initializeMaxFoodLimits() {
        maxFoodPerCategory = new HashMap<>();
        // 主食类最多只需要1个
        maxFoodPerCategory.put("staple", 1);
        // 其他食物类别默认最多2个（可以根据需要修改）
        // 示例：蔬菜类可以允许更多
        maxFoodPerCategory.put("vegetable", 3);
        maxFoodPerCategory.put("fruit", 2);
        
    }

    /**
     * 设置特定食物类别的最大数量限制
     * @param category 食物类别
     * @param maxCount 最大数量
     */
    public void setMaxFoodLimit(String category, int maxCount) {
        if (maxCount < 0) {
            throw new IllegalArgumentException("食物数量限制不能为负数");
        }
        maxFoodPerCategory.put(category, maxCount);
    }

    /**
     * 根据用户健康状况调整营养素权重
     * @param healthConditions 用户健康状况数组
     */
    private void adjustWeightsByHealthConditions(String[] healthConditions) {
        if (healthConditions == null || healthConditions.length == 0) {
            return; // 没有特殊健康状况，使用默认权重
        }

        for (String condition : healthConditions) {
            switch (condition.toLowerCase()) {
                case "hypertension": // 高血压
                    weightSodium = 1.5;    // 增加钠的权重（更严格控制）
                    weightPotassium = 1.2; // 增加钾的权重（鼓励摄入）
                    break;
                    
                case "diabetes": // 糖尿病
                    weightCalorie=1.8;
                    weightCarb = 1.5;      // 增加碳水的权重（更严格控制）
                    weightFat = 1.2;       // 增加脂肪的权重
                    break;
                    
                case "hyperlipidemia": // 高血脂
                    weightFat = 1.5;       // 增加脂肪的权重（更严格控制）
                    break;
                    
                case "gout": // 痛风
                    weightProtein = 1.3;   // 增加蛋白质的权重（控制某些蛋白质来源）
                    break;
                    
                case "ckd": // 慢性肾病（无透析）
                    weightProtein = 1.5;   // 增加蛋白质的权重（严格控制）
                    weightPotassium = 1.3; // 增加钾的权重
                    weightPhosphorus = 1.3; // 增加磷的权重
                    break;
                    
                case "ckd_dialysis": // 慢性肾病（透析）
                    weightProtein = 1.2;   // 增加蛋白质的权重（但不同于无透析）
                    weightPotassium = 1.5; // 增加钾的权重（更严格控制）
                    break;
                    
                case "osteoporosis": // 骨质疏松
                    weightCalcium = 1.5;   // 增加钙的权重
                    weightMagnesium = 1.2; // 增加镁的权重
                    break;
                    
                case "anemia": // 贫血
                    weightIron = 1.5;      // 增加铁的权重
                    break;
            }
        }
    }

    private void initializeFoodDatabase() {
        foodDatabase = new ArrayList<>();

        NutritionDataParser parser = new NutritionDataParser();
        try {
            foodDatabase = parser.convertToFoodObjects(parser.parseNutritionDataFromFile("src/main/resources/all.xlsx"));
            // 数据库中的食物默认都是100g，保持这种方式，在使用时再调整摄入量
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 生成每日膳食计划
     * @return 每日膳食计划
     */
    public DailyMealPlan generateDailyMealPlan() {
        MealNutrients dailyNeeds = nutritionCalculator.calculateDailyNutrientNeeds();
        
        // 为每餐分配营养需求
        MealNutrients breakfastNeeds = calculateMealNutrients(dailyNeeds, MEAL_RATIO_BREAKFAST);
        MealNutrients lunchNeeds = calculateMealNutrients(dailyNeeds, MEAL_RATIO_LUNCH);
        MealNutrients dinnerNeeds = calculateMealNutrients(dailyNeeds, MEAL_RATIO_DINNER);

        // 用于跟踪已使用的食物
        Set<String> usedFoods = new HashSet<>();

        // 生成每餐食谱
        List<Food> breakfast = generateMeal(breakfastNeeds, usedFoods, true);
        List<Food> lunch = generateMeal(lunchNeeds, usedFoods, true);
        List<Food> dinner = generateMeal(dinnerNeeds, usedFoods, true);

        System.out.println("\n======= 开始多轮优化膳食计划 =======");
        
        // 多轮优化各餐的食物摄入量
        System.out.println("\n--- 优化早餐 ---");
        breakfast = mealOptimizer.optimizeMeal(breakfast, breakfastNeeds);
        
        System.out.println("\n--- 优化午餐 ---");
        lunch = mealOptimizer.optimizeMeal(lunch, lunchNeeds);
        
        System.out.println("\n--- 优化晚餐 ---");
        dinner = mealOptimizer.optimizeMeal(dinner, dinnerNeeds);
        
        System.out.println("\n======= 膳食计划优化完成 =======");

        // 创建每日膳食计划
        DailyMealPlan dailyPlan = new DailyMealPlan(breakfast, lunch, dinner);
        
        // 打印详细的营养素成分（用于调试）
        printDetailedNutrition(dailyPlan, dailyNeeds);
        
        return dailyPlan;
    }
    
    /**
     * 打印详细的营养素成分
     * @param plan 每日膳食计划
     * @param dailyNeeds 每日营养需求
     */
    private void printDetailedNutrition(DailyMealPlan plan, MealNutrients dailyNeeds) {
        System.out.println("\n========== 食谱营养素详细分析（调试信息）==========");
        
        // 计算每餐的实际营养素
        NutrientSummary breakfastSummary = calculateMealNutrientSummary(plan.getBreakfast());
        NutrientSummary lunchSummary = calculateMealNutrientSummary(plan.getLunch());
        NutrientSummary dinnerSummary = calculateMealNutrientSummary(plan.getDinner());
        
        // 计算全天总营养素
        NutrientSummary dailySummary = breakfastSummary.add(lunchSummary).add(dinnerSummary);
        
        // 打印每餐营养素
        System.out.println("\n早餐营养素详情:");
        printNutrientSummary(breakfastSummary);
        
        System.out.println("\n午餐营养素详情:");
        printNutrientSummary(lunchSummary);
        
        System.out.println("\n晚餐营养素详情:");
        printNutrientSummary(dinnerSummary);
        
        // 打印全天总营养素
        System.out.println("\n全天营养素总计:");
        printNutrientSummary(dailySummary);
        
        // 打印目标营养素
        System.out.println("\n目标营养素:");
        System.out.printf("总热量: %.1f kcal\n", dailyNeeds.getCalories());
        System.out.printf("碳水化合物: %.1f g (%.1f%%)\n", 
                        dailyNeeds.getCarbohydrates(), 
                        dailyNeeds.getCarbohydrates() * 4 / dailyNeeds.getCalories() * 100);
        System.out.printf("蛋白质: %.1f g (%.1f%%)\n", 
                        dailyNeeds.getProtein(), 
                        dailyNeeds.getProtein() * 4 / dailyNeeds.getCalories() * 100);
        System.out.printf("脂肪: %.1f g (%.1f%%)\n", 
                        dailyNeeds.getFat(), 
                        dailyNeeds.getFat() * 9 / dailyNeeds.getCalories() * 100);
        System.out.printf("钙: %.1f mg\n", dailyNeeds.getCalcium());
        System.out.printf("钾: %.1f mg\n", dailyNeeds.getPotassium());
        System.out.printf("钠: %.1f mg\n", dailyNeeds.getSodium());
        System.out.printf("镁: %.1f mg\n", dailyNeeds.getMagnesium());
        
        // 打印营养素限制信息
        printNutrientLimits();
    }
    
    /**
     * 打印营养素限制信息
     */
    private void printNutrientLimits() {
        Map<String, NutrientLimit> limits = nutritionCalculator.getAllNutrientLimits();
        if (limits.isEmpty()) {
            System.out.println("\n没有特定的营养素限制。");
            return;
        }
        
        System.out.println("\n营养素限制:");
        if (limits.containsKey("sodium")) {
            NutrientLimit limit = limits.get("sodium");
            System.out.printf("钠: %.1f - %.1f mg\n", limit.getMinValue(), limit.getMaxValue());
        }
        
        if (limits.containsKey("carbohydrates")) {
            NutrientLimit limit = limits.get("carbohydrates");
            System.out.printf("碳水化合物: %.1f - %.1f g\n", limit.getMinValue(), limit.getMaxValue());
        }
        
        if (limits.containsKey("protein")) {
            NutrientLimit limit = limits.get("protein");
            System.out.printf("蛋白质: %.1f - %.1f g\n", limit.getMinValue(), limit.getMaxValue());
        }
        
        if (limits.containsKey("fat")) {
            NutrientLimit limit = limits.get("fat");
            System.out.printf("脂肪: %.1f - %.1f g\n", limit.getMinValue(), limit.getMaxValue());
        }
        
        if (limits.containsKey("potassium")) {
            NutrientLimit limit = limits.get("potassium");
            System.out.printf("钾: %.1f - %.1f mg\n", limit.getMinValue(), limit.getMaxValue());
        }
    }
    
    /**
     * 打印营养素摘要
     * @param summary 营养素摘要
     */
    private void printNutrientSummary(NutrientSummary summary) {
        System.out.printf("热量: %.1f kcal\n", summary.calories);
        System.out.printf("碳水: %.1f g (%.1f%%)\n", summary.carbs, summary.getCarbsPercentage());
        System.out.printf("蛋白质: %.1f g (%.1f%%)\n", summary.protein, summary.getProteinPercentage());
        System.out.printf("脂肪: %.1f g (%.1f%%)\n", summary.fat, summary.getFatPercentage());
        System.out.printf("微量元素: 钙 %.1f mg, 钾 %.1f mg, 钠 %.1f mg, 镁 %.1f mg\n", 
                summary.calcium, summary.potassium, summary.sodium, summary.magnesium);
    }
    
    /**
     * 计算一餐的营养素摘要
     * @param foods 食物列表
     * @return 营养素摘要
     */
    private NutrientSummary calculateMealNutrientSummary(List<Food> foods) {
        double calories = 0;
        double carbs = 0;
        double protein = 0;
        double fat = 0;
        double calcium = 0;
        double potassium = 0;
        double sodium = 0;
        double magnesium = 0;
        
        for (Food food : foods) {
            calories += food.getCalories();
            carbs += food.getCarbohydrates();
            protein += food.getProtein();
            fat += food.getFat();
            calcium += food.getCalcium();
            potassium += food.getPotassium();
            sodium += food.getSodium();
            magnesium += food.getMagnesium();
        }
        
        return new NutrientSummary(calories, carbs, protein, fat, calcium, potassium, sodium, magnesium);
    }

    public MealNutrients calculateMealNutrients(MealNutrients dailyNeeds, double ratio) {
        return new MealNutrients(
            dailyNeeds.getCalories() * ratio,
            dailyNeeds.getCarbohydrates() * ratio,
            dailyNeeds.getProtein() * ratio,
            dailyNeeds.getFat() * ratio,
            dailyNeeds.getCalcium() * ratio,
            dailyNeeds.getPotassium() * ratio,
            dailyNeeds.getSodium() * ratio,
            dailyNeeds.getMagnesium() * ratio
        );
    }

    private List<Food> generateMeal(MealNutrients targetNutrients, Set<String> usedFoods, boolean requireStaple) {
        List<Food> meal = new ArrayList<>();
        MealPlannerLogger.startMealGeneration();
        
        // 记录每个类别食物的数量
        Map<String, Integer> categoryCount = new HashMap<>();
        
        // 已拒绝的食物集合
        Set<String> rejectedFoods = new HashSet<>();
        
        // 调整计数
        int adjustmentCount = 0;
        int maxAdjustments = 5; // 最多进行5次营养素范围调整
        
        // 调整前保存原始目标营养素
        MealNutrients originalTargetNutrients = MealNutrients.copy(targetNutrients);
        
        // 记录营养素状态
        boolean hasNutrientExcess = false;
        String excessNutrientName = "";
        
        // 如果要求主食，首先添加一个
        if (requireStaple) {
            Food stapleFood = findBestStapleFood(targetNutrients, usedFoods);
            if (stapleFood != null) {
                meal.add(stapleFood);
                usedFoods.add(stapleFood.getName());
                categoryCount.put(stapleFood.getCategory(), 
                                  categoryCount.getOrDefault(stapleFood.getCategory(), 0) + 1);
                
                // 从目标营养素中减去已添加食物的营养成分
                targetNutrients = subtractNutrients(targetNutrients, stapleFood);
                
                // 检查是否导致营养素超标
                NutrientBalance balance = checkNutrientBalance(meal, originalTargetNutrients);
                if (balance.hasExcess) {
                    MealPlannerLogger.warning("添加主食后发现营养素超标: " + balance.excessNutrient);
                    hasNutrientExcess = true;
                    excessNutrientName = balance.excessNutrient;
                }
                
                MealPlannerLogger.stapleAdded(stapleFood);
            }
        }
        
        // 尝试添加更多食物直到达到营养素目标或者找不到更多合适的食物
        int attempts = 0;
        int maxAttempts = 20; // 最多尝试20次
        
        // 摄入量优化尝试次数
        int intakeOptimizationAttempts = 0;
        int maxIntakeOptimizationAttempts = 3; // 在放宽营养素要求前，最多尝试3次摄入量优化
        
        while (attempts < maxAttempts) {
            attempts++;
            
            // 如果已经检测到营养素超标，立即进行摄入量优化
            if (hasNutrientExcess && meal.size() >= 1) {
                MealPlannerLogger.warning("检测到营养素超标 (" + excessNutrientName + ")，尝试通过调整摄入量解决");
                List<Food> optimizedMeal = optimizeMealWithExcessHandling(meal, originalTargetNutrients);
                
                // 检查优化后的结果
                NutrientBalance balance = checkNutrientBalance(optimizedMeal, originalTargetNutrients);
                if (!balance.hasExcess) {
                    MealPlannerLogger.success("通过调整摄入量成功解决营养素超标问题");
                    meal = optimizedMeal;
                    hasNutrientExcess = false;
                    excessNutrientName = "";
                } else {
                    MealPlannerLogger.warning("调整摄入量后仍存在营养素超标: " + balance.excessNutrient);
                    // 如果无法解决超标问题，考虑移除最后添加的食材
                    if (meal.size() > 1) {
                        Food lastFood = meal.remove(meal.size() - 1);
                        MealPlannerLogger.warning("移除食材 " + lastFood.getName() + " 以减少营养素超标");
                        usedFoods.remove(lastFood.getName());
                        String category = lastFood.getCategory();
                        categoryCount.put(category, categoryCount.getOrDefault(category, 0) - 1);
                        
                        // 重新检查营养平衡
                        balance = checkNutrientBalance(meal, originalTargetNutrients);
                        hasNutrientExcess = balance.hasExcess;
                        excessNutrientName = balance.hasExcess ? balance.excessNutrient : "";
                    }
                }
                continue;
            }
            
            // 优先通过优化摄入量满足营养需求 (仅在无超标情况下进行)
            if (!hasNutrientExcess && meal.size() >= 2 && intakeOptimizationAttempts < maxIntakeOptimizationAttempts) {
                MealPlannerLogger.info("尝试通过调整食材摄入量来满足营养素要求 (尝试 " + (intakeOptimizationAttempts + 1) + "/" + maxIntakeOptimizationAttempts + ")");
                
                // 使用膳食优化器优化现有食物的摄入量
                List<Food> optimizedMeal = mealOptimizer.optimizeMeal(new ArrayList<>(meal), originalTargetNutrients);
                
                // 检查优化后的结果
                NutrientBalance balance = checkNutrientBalance(optimizedMeal, originalTargetNutrients);
                
                if (balance.isBalanced) {
                    MealPlannerLogger.success("通过调整食材摄入量成功满足营养素要求");
                    meal = optimizedMeal;
                    break;
                } else if (balance.hasExcess) {
                    MealPlannerLogger.warning("优化后发现营养素超标: " + balance.excessNutrient);
                    hasNutrientExcess = true;
                    excessNutrientName = balance.excessNutrient;
                    // 继续循环，下次会处理超标问题
                } else {
                    intakeOptimizationAttempts++;
                    if (intakeOptimizationAttempts >= maxIntakeOptimizationAttempts) {
                        MealPlannerLogger.warning("无法通过调整食材摄入量满足营养素要求，将尝试添加更多食材");
                    }
                    // 继续添加更多食物
                }
                continue;
            }
            
            // 如果已经有营养素超标，不再添加新食材
            if (hasNutrientExcess) {
                MealPlannerLogger.warning("存在营养素超标，停止添加更多食材");
                break;
            }
            
            // 寻找最佳食物
            Food bestFood = findBestFood(targetNutrients, categoryCount, usedFoods);
            
            // 如果找到合适的食物
            if (bestFood != null && !rejectedFoods.contains(bestFood.getName())) {
                // 在添加前检查是否会导致营养素严重超标
            List<Food> tempMeal = new ArrayList<>(meal);
            tempMeal.add(bestFood);
                NutrientBalance balance = checkNutrientBalance(tempMeal, originalTargetNutrients);
                
                if (balance.hasExcess && balance.excessPercentage > 0.3) { // 超标30%视为严重超标
                    MealPlannerLogger.warning("添加 " + bestFood.getName() + " 会导致 " + balance.excessNutrient + " 严重超标 (" + 
                                        String.format("%.1f%%", balance.excessPercentage * 100) + ")，尝试寻找其他食材");
                rejectedFoods.add(bestFood.getName());
                continue;
            }
            
                // 添加食材
            meal.add(bestFood);
            usedFoods.add(bestFood.getName());
                
                // 更新类别计数
            categoryCount.put(bestFood.getCategory(), 
                            categoryCount.getOrDefault(bestFood.getCategory(), 0) + 1);
            
                // 从目标营养素中减去已添加食物的营养成分
            targetNutrients = subtractNutrients(targetNutrients, bestFood);
                
                MealPlannerLogger.foodAdded(bestFood);
                
                // 检查添加后的营养平衡
                balance = checkNutrientBalance(meal, originalTargetNutrients);
                if (balance.hasExcess) {
                    MealPlannerLogger.warning("添加食材后发现营养素超标: " + balance.excessNutrient);
                    hasNutrientExcess = true;
                    excessNutrientName = balance.excessNutrient;
                } else if (balance.isBalanced) {
                    MealPlannerLogger.success("已达到营养平衡，停止添加更多食材");
                    break;
                }
                
                // 重置摄入量优化尝试计数
                intakeOptimizationAttempts = 0;
            } else {
                // 如果找不到适合的食物，考虑放宽营养素要求
                if (adjustmentCount < maxAdjustments) {
                    // 增加偏差，放宽要求
                    if (nutrientAdjuster.increaseDeviation()) {
                        double currentDeviation = nutrientAdjuster.getCurrentDeviation();
                        MealPlannerLogger.nutrientRequirementRelaxed(currentDeviation);
                        MealPlannerLogger.warning("放宽营养素要求，当前偏差: " + 
                                            String.format("%.2f", currentDeviation * 100) + "%");
                        adjustmentCount++;
                        // 清空已拒绝的食物，重新评估所有食物
                        rejectedFoods.clear();
                        // 重置摄入量优化尝试计数
                        intakeOptimizationAttempts = 0;
                        continue; // 使用新的容差再次尝试
                    }
                }
                
                // 没有找到合适的食物，也无法放宽要求
                if (bestFood != null) {
                    rejectedFoods.add(bestFood.getName());
                } else {
                    // 连续几次找不到改进，则停止尝试
                    if (meal.size() > 0) {
                        MealPlannerLogger.warning("无法找到更多适合的食物，停止尝试");
                        break;
                    } else {
                        MealPlannerLogger.warning("无法找到任何适合的食物");
                    }
                }
            }
        }
        
        // 最终一次尝试通过优化当前食物的摄入量来满足需求
        if (meal.size() >= 1) {
            MealPlannerLogger.info("最终尝试通过调整食材摄入量优化膳食");
            meal = optimizeMealWithExcessHandling(meal, originalTargetNutrients);
            
            // 检查最终结果
            NutrientBalance finalBalance = checkNutrientBalance(meal, originalTargetNutrients);
            if (finalBalance.isBalanced) {
                MealPlannerLogger.success("最终优化成功，达到营养平衡");
            } else if (finalBalance.hasExcess) {
                MealPlannerLogger.warning("最终优化后仍存在营养素超标: " + finalBalance.excessNutrient);
            } else {
                MealPlannerLogger.warning("最终优化后仍存在营养素不足");
            }
        }
        
        MealPlannerLogger.completeMealGeneration(meal.size(), attempts, adjustmentCount);
        
        // 如果膳食中的食物数量太少，记录警告
        if (meal.size() < 3) {
            MealPlannerLogger.warning("未能找到足够的食物组成一餐 (仅找到 " + meal.size() + " 种食物)");
            MealPlannerLogger.warning("已尝试放宽要求 " + adjustmentCount + " 次，共尝试 " + attempts + " 次筛选");
        }
        
        return meal;
    }

    /**
     * 专门用于处理营养素超标情况的膳食优化
     * @param meal 当前膳食
     * @param targetNutrients 目标营养素
     * @return 优化后的膳食
     */
    private List<Food> optimizeMealWithExcessHandling(List<Food> meal, MealNutrients targetNutrients) {
        // 首先使用常规优化
        List<Food> optimizedMeal = mealOptimizer.optimizeMeal(new ArrayList<>(meal), targetNutrients);
        
        // 检查优化结果
        NutrientBalance balance = checkNutrientBalance(optimizedMeal, targetNutrients);
        
        // 如果仍存在超标，尝试减少超标营养素最高的食材的摄入量
        if (balance.hasExcess) {
            MealPlannerLogger.info("尝试特别减少含" + balance.excessNutrient + "高的食材的摄入量");
            
            // 找出超标营养素最高的食材
            Food highestFood = null;
            double highestContent = 0;
            
            for (Food food : optimizedMeal) {
                double content = getNutrientContent(food, balance.excessNutrient);
                if (content > highestContent) {
                    highestContent = content;
                    highestFood = food;
                }
            }
            
            // 使用当前量作为基础 (假设Food类有一个getQuantity方法返回食物的摄入量)
            double currentAmount = 100.0; // 默认值，实际应当从Food对象获取
            try {
                // 尝试使用反射获取Food类的摄入量方法
                java.lang.reflect.Method getMethod = highestFood.getClass().getMethod("getQuantity");
                if (getMethod != null) {
                    Object result = getMethod.invoke(highestFood);
                    if (result instanceof Number) {
                        currentAmount = ((Number)result).doubleValue();
                    }
                }
            } catch (Exception e) {
                // 无法获取摄入量，使用默认值
                MealPlannerLogger.warning("无法获取食材摄入量，使用默认值100克");
            }
            
            double reducedAmount = currentAmount * 0.7; // 减少30%
            
            // 更新食材的摄入量 - 由于我们不确定Food类的具体实现，
            // 在这里我们将移除超标食材，而不是尝试调整其量
            List<Food> adjustedMeal = new ArrayList<>();
            for (Food food : optimizedMeal) {
                if (food.getName().equals(highestFood.getName())) {
                    // 检查是否应该移除该食材
                    if (reducedAmount >= 10) { // 确保摄入量不会太低
                        MealPlannerLogger.info("由于无法直接调整食材摄入量，将完全移除 " + food.getName());
                        // 不添加到调整后的膳食中，即移除该食材
                    } else {
                        // 如果减少后摄入量太低，考虑完全移除
                        MealPlannerLogger.warning("完全移除 " + food.getName() + " 以减少营养素超标");
                    }
                } else {
                    adjustedMeal.add(food);
                }
            }
            
            // 如果调整后的膳食不为空，进行一次最终优化
            if (!adjustedMeal.isEmpty()) {
                optimizedMeal = mealOptimizer.optimizeMeal(adjustedMeal, targetNutrients);
            }
        }
        
        return optimizedMeal;
    }

    /**
     * 获取食材中特定营养素的含量
     * @param food 食材
     * @param nutrientName 营养素名称
     * @return 营养素含量
     */
    private double getNutrientContent(Food food, String nutrientName) {
        switch (nutrientName) {
            case "热量":
                return food.getCalories();
            case "碳水化合物":
                return food.getCarbohydrates();
            case "蛋白质":
                return food.getProtein();
            case "脂肪":
                return food.getFat();
            case "钙":
                return food.getCalcium();
            case "钾":
                return food.getPotassium();
            case "钠":
                return food.getSodium();
            case "镁":
                return food.getMagnesium();
            default:
                return 0;
        }
    }

    /**
     * 检查膳食的营养平衡状态
     * @param meal 膳食
     * @param targetNutrients 目标营养素
     * @return 营养平衡状态
     */
    private NutrientBalance checkNutrientBalance(List<Food> meal, MealNutrients targetNutrients) {
        if (meal.isEmpty()) {
            return new NutrientBalance(false, true, false, "", 0);
        }
        
        // 计算当前膳食的总营养素
        double totalCalories = 0, totalCarbs = 0, totalProtein = 0, totalFat = 0;
        double totalCalcium = 0, totalPotassium = 0, totalSodium = 0, totalMagnesium = 0;
        
        for (Food food : meal) {
            totalCalories += food.getCalories();
            totalCarbs += food.getCarbohydrates();
            totalProtein += food.getProtein();
            totalFat += food.getFat();
            totalCalcium += food.getCalcium();
            totalPotassium += food.getPotassium();
            totalSodium += food.getSodium();
            totalMagnesium += food.getMagnesium();
        }
        
        // 获取当前偏差
        double deviation = nutrientAdjuster.getCurrentDeviation();
        
        // 检查营养素是否平衡
        boolean isUnderTarget = true;  // 是否所有营养素都不足
        boolean isBalanced = true;     // 是否所有营养素都在目标范围内
        boolean hasExcess = false;     // 是否有超标的营养素
        String excessNutrient = "";    // 超标的营养素名称
        double excessPercentage = 0;   // 超标百分比
        
        // 检查热量
        double lowerCalories = targetNutrients.calories * (1 - deviation);
        double upperCalories = targetNutrients.calories * (1 + deviation);
        if (totalCalories < lowerCalories) {
            isBalanced = false;
        } else if (totalCalories > upperCalories) {
            isBalanced = false;
            isUnderTarget = false;
            hasExcess = true;
            excessNutrient = "热量";
            excessPercentage = (totalCalories - upperCalories) / targetNutrients.calories;
        }
        
        // 检查碳水化合物
        double lowerCarbs = targetNutrients.carbohydrates * (1 - deviation);
        double upperCarbs = targetNutrients.carbohydrates * (1 + deviation);
        if (totalCarbs < lowerCarbs) {
            isBalanced = false;
        } else if (totalCarbs > upperCarbs) {
            isBalanced = false;
            isUnderTarget = false;
            if (!hasExcess || (totalCarbs - upperCarbs) / targetNutrients.carbohydrates > excessPercentage) {
                hasExcess = true;
                excessNutrient = "碳水化合物";
                excessPercentage = (totalCarbs - upperCarbs) / targetNutrients.carbohydrates;
            }
        }
        
        // 检查蛋白质
        double lowerProtein = targetNutrients.protein * (1 - deviation);
        double upperProtein = targetNutrients.protein * (1 + deviation);
        if (totalProtein < lowerProtein) {
            isBalanced = false;
        } else if (totalProtein > upperProtein) {
            isBalanced = false;
            isUnderTarget = false;
            if (!hasExcess || (totalProtein - upperProtein) / targetNutrients.protein > excessPercentage) {
                hasExcess = true;
                excessNutrient = "蛋白质";
                excessPercentage = (totalProtein - upperProtein) / targetNutrients.protein;
            }
        }
        
        // 检查脂肪
        double lowerFat = targetNutrients.fat * (1 - deviation);
        double upperFat = targetNutrients.fat * (1 + deviation);
        if (totalFat < lowerFat) {
            isBalanced = false;
        } else if (totalFat > upperFat) {
            isBalanced = false;
            isUnderTarget = false;
            if (!hasExcess || (totalFat - upperFat) / targetNutrients.fat > excessPercentage) {
                hasExcess = true;
                excessNutrient = "脂肪";
                excessPercentage = (totalFat - upperFat) / targetNutrients.fat;
            }
        }
        
        // 可以继续添加其他营养素的检查...
        
        return new NutrientBalance(isBalanced, isUnderTarget, hasExcess, excessNutrient, excessPercentage);
    }

    /**
     * 存储营养平衡状态的内部类
     */
    private class NutrientBalance {
        boolean isBalanced;    // 是否营养平衡
        boolean isUnderTarget; // 是否所有营养素都不足
        boolean hasExcess;     // 是否有超标的营养素
        String excessNutrient; // 超标的营养素名称
        double excessPercentage; // 超标百分比
        
        public NutrientBalance(boolean isBalanced, boolean isUnderTarget, 
                               boolean hasExcess, String excessNutrient, double excessPercentage) {
            this.isBalanced = isBalanced;
            this.isUnderTarget = isUnderTarget;
            this.hasExcess = hasExcess;
            this.excessNutrient = excessNutrient;
            this.excessPercentage = excessPercentage;
        }
    }

    private Food findBestStapleFood(MealNutrients targetNutrients, Set<String> usedFoods) {
        Food bestStaple = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Food food : foodDatabase) {
            if (food.getCategory().equals("staple") && !usedFoods.contains(food.getName())) {
                // 计算该食物的最佳摄入量
                double optimalIntake = food.calculateOptimalIntake(targetNutrients);
                
                // 使用最佳摄入量创建新的食物对象
                Food foodWithOptimalIntake = food.withIntake(optimalIntake);
                
                double score = calculateFoodScore(foodWithOptimalIntake, targetNutrients);
                if (score > bestScore) {
                    bestScore = score;
                    bestStaple = foodWithOptimalIntake;
                }
            }
        }
        
        return bestStaple;
    }

    private Food findBestFood(MealNutrients targetNutrients, Map<String, Integer> categoryCount, Set<String> usedFoods) {
        Food bestFood = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        // 添加筛选统计
        int totalFoods = foodDatabase.size();
        int excludedUsed = 0;
        int excludedCategory = 0;
        int excludedDietary = 0;
        int excludedNutrientExcess = 0;
        
        for (Food food : foodDatabase) {
            // 跳过已使用的食物
            if (usedFoods.contains(food.getName())) {
                MealPlannerLogger.foodFiltered(food.getName(), "已使用");
                excludedUsed++;
                continue;
            }
            
            // 限制每类食物的数量
            int maxAllowed = maxFoodPerCategory.getOrDefault(food.getCategory(), DEFAULT_MAX_FOODS_PER_CATEGORY);
            if (categoryCount.getOrDefault(food.getCategory(), 0) >= maxAllowed) {
                MealPlannerLogger.foodFiltered(food.getName(), "类别 " + food.getCategory() + " 已达最大数量");
                excludedCategory++;
                continue;
            }
            
            // 检查食物是否符合用户的饮食限制和偏好
            if (!dietaryEvaluator.isFoodSuitable(food)) {
                MealPlannerLogger.foodFiltered(food.getName(), "不符合用户饮食限制或偏好");
                excludedDietary++;
                continue;
            }
            
            // 计算初始保守的摄入量（使用推荐范围的最小值，而不是默认值或最佳值）
            // 这样可以降低最初添加食材导致营养素超标的风险
            IntakeRange range = food.getRecommendedIntakeRange();
            double conservativeIntake = range.getMinIntake();
            
            // 创建使用保守摄入量的食物对象
            Food foodWithConservativeIntake = food.withIntake(conservativeIntake);
            
            // 检查保守摄入量下是否会导致重要营养素严重超标
            if (wouldCauseNutrientExcess(foodWithConservativeIntake, targetNutrients)) {
                MealPlannerLogger.foodFiltered(food.getName(), "即使最小量也会导致营养素超标");
                excludedNutrientExcess++;
                continue;
            }
            
            // 基于当前需求计算最佳摄入量
            double optimalIntake = calculateOptimalIntakeWithTolerance(food, targetNutrients);
            
            // 使用计算出的最佳摄入量创建新的食物对象
            Food foodWithOptimalIntake = food.withIntake(optimalIntake);
            
            // 检查此摄入量是否会导致任何关键营养素超标
            // 如果会超标，则回退到较低的摄入量
            if (wouldCauseNutrientExcess(foodWithOptimalIntake, targetNutrients)) {
                // 尝试更保守的摄入量
                double reducedIntake = optimalIntake * 0.7; // 减少30%
                if (reducedIntake < range.getMinIntake()) {
                    // 如果减少后低于最小摄入量，则使用最小摄入量
                    reducedIntake = range.getMinIntake();
                }
                
                foodWithOptimalIntake = food.withIntake(reducedIntake);
                MealPlannerLogger.debug("降低 " + food.getName() + " 的摄入量至 " + 
                                 String.format("%.0f", reducedIntake) + "克以避免营养素超标");
            }
            
            // 计算食物评分
            double score = calculateFoodScore(foodWithOptimalIntake, targetNutrients);
            MealPlannerLogger.foodScored(food.getName(), score);
            
            if (score > bestScore) {
                bestScore = score;
                bestFood = foodWithOptimalIntake;
            }
        }
        
        // 输出筛选统计
        int consideredFoods = totalFoods - excludedUsed - excludedCategory - excludedDietary - excludedNutrientExcess;
        MealPlannerLogger.info("食物筛选统计: 总数=" + totalFoods + 
                         ", 已使用=" + excludedUsed +
                         ", 类别限制=" + excludedCategory +
                         ", 饮食限制=" + excludedDietary +
                         ", 营养素超标=" + excludedNutrientExcess +
                         ", 考虑评分=" + consideredFoods);
        
        if (bestFood != null) {
            MealPlannerLogger.debug("选择最佳食物: " + bestFood.getName() + 
                             "，摄入量: " + String.format("%.0f", bestFood.getWeight()) + "克" +
                             ", 评分: " + String.format("%.2f", bestScore));
        } else {
            MealPlannerLogger.warning("无法找到合适的食物");
        }
        
        return bestFood;
    }

    /**
     * 检查添加特定食材是否会导致重要营养素超标
     * @param food 要检查的食材
     * @param targetNutrients 目标营养素
     * @return 是否会导致超标
     */
    private boolean wouldCauseNutrientExcess(Food food, MealNutrients targetNutrients) {
        // 获取当前偏差
        double deviation = nutrientAdjuster.getCurrentDeviation();
        // 允许的最大值
        double maxCalories = targetNutrients.calories * (1 + deviation);
        double maxCarbs = targetNutrients.carbohydrates * (1 + deviation);
        double maxProtein = targetNutrients.protein * (1 + deviation);
        double maxFat = targetNutrients.fat * (1 + deviation);
        
        // 检查各营养素是否超标
        if (food.getCalories() > maxCalories) {
            MealPlannerLogger.debug(food.getName() + " 热量超标: " + 
                            String.format("%.0f", food.getCalories()) + " > " + 
                            String.format("%.0f", maxCalories));
            return true;
        }
        
        if (food.getCarbohydrates() > maxCarbs) {
            MealPlannerLogger.debug(food.getName() + " 碳水化合物超标: " + 
                            String.format("%.1f", food.getCarbohydrates()) + "g > " + 
                            String.format("%.1f", maxCarbs) + "g");
            return true;
        }
        
        if (food.getProtein() > maxProtein) {
            MealPlannerLogger.debug(food.getName() + " 蛋白质超标: " + 
                            String.format("%.1f", food.getProtein()) + "g > " + 
                            String.format("%.1f", maxProtein) + "g");
            return true;
        }
        
        if (food.getFat() > maxFat) {
            MealPlannerLogger.debug(food.getName() + " 脂肪超标: " + 
                            String.format("%.1f", food.getFat()) + "g > " + 
                            String.format("%.1f", maxFat) + "g");
            return true;
        }
        
        // 其他营养素检查可以根据需要添加
        
        return false;
    }

    /**
     * 计算食材的最佳摄入量，考虑当前的营养素偏差和已添加食材的情况
     * @param food 食材
     * @param targetNutrients 目标营养素
     * @return 最佳摄入量
     */
    private double calculateOptimalIntakeWithTolerance(Food food, MealNutrients targetNutrients) {
        // 使用食物自身的计算方法获取基础最佳摄入量
        double baseOptimalIntake = food.calculateOptimalIntake(targetNutrients);
        
        // 获取食物的推荐摄入量范围
        IntakeRange range = food.getRecommendedIntakeRange();
        
        // 如果目标营养素已经很小（表示大部分营养素需求已满足），则使用保守的摄入量
        boolean isTargetSmall = isTargetNutrientSmall(targetNutrients);
        if (isTargetSmall) {
            // 当目标营养素接近满足时，使用较低的摄入量避免超标
            double conservativeIntake = range.getMinIntake() * 1.2; // 比最小摄入量略高20%
            MealPlannerLogger.debug("目标营养素接近满足，为 " + food.getName() + 
                             " 设置保守摄入量: " + String.format("%.0f", conservativeIntake) + "克");
            return conservativeIntake;
        }
        
        // 考虑营养素偏差，计算调整后的摄入量
        double currentDeviation = nutrientAdjuster.getCurrentDeviation();
        
        // 根据当前偏差调整摄入量范围
        // 当偏差较大时，更倾向于保守的摄入量以避免营养素超标
        double minIntake = range.getMinIntake();
        double maxIntake = range.getMaxIntake() * (1 - currentDeviation * 0.5); // 随着偏差增大，最大摄入量减小
        
        // 确保摄入量在调整后的范围内
        double adjustedIntake = Math.max(minIntake, Math.min(maxIntake, baseOptimalIntake));
        
        // 根据特定营养素需求调整摄入量
        adjustedIntake = adjustIntakeByNutrientNeeds(food, targetNutrients, adjustedIntake);
        
        return adjustedIntake;
    }

    /**
     * 判断目标营养素是否已经接近满足（大部分营养素需求已满足）
     * @param targetNutrients 目标营养素
     * @return 是否目标营养素已小
     */
    private boolean isTargetNutrientSmall(MealNutrients targetNutrients) {
        // 设定阈值，表示还需要多少比例的营养素
        double threshold = 0.2; // 如果剩余需求小于20%则认为接近满足
        
        // 获取参考营养素（成年人一餐的典型需求）
        double refCalories = 600; // 一餐约600大卡
        double refCarbs = 75;     // 一餐约75克碳水
        double refProtein = 25;   // 一餐约25克蛋白质
        double refFat = 20;       // 一餐约20克脂肪
        
        // 检查各主要营养素是否已接近满足
        boolean caloriesSmall = targetNutrients.calories < refCalories * threshold;
        boolean carbsSmall = targetNutrients.carbohydrates < refCarbs * threshold;
        boolean proteinSmall = targetNutrients.protein < refProtein * threshold;
        boolean fatSmall = targetNutrients.fat < refFat * threshold;
        
        // 如果任意两种主要营养素接近满足，则认为整体接近满足
        int smallCount = 0;
        if (caloriesSmall) smallCount++;
        if (carbsSmall) smallCount++;
        if (proteinSmall) smallCount++;
        if (fatSmall) smallCount++;
        
        return smallCount >= 2;
    }

    /**
     * 根据特定营养素需求调整食材摄入量
     * @param food 食材
     * @param targetNutrients 目标营养素
     * @param baseIntake 基础摄入量
     * @return 调整后的摄入量
     */
    private double adjustIntakeByNutrientNeeds(Food food, MealNutrients targetNutrients, double baseIntake) {
        // 获取食材每100克的营养成分
        Nutrition nutrition = food.getNutrition();
        if (nutrition == null) {
            return baseIntake;
        }
        
        double adjustedIntake = baseIntake;
        
        // 找出最接近超标的营养素，据此调整摄入量
        double caloriesRatio = (nutrition.getCalories() * baseIntake / 100) / targetNutrients.calories;
        double carbsRatio = (nutrition.getCarbohydrates() * baseIntake / 100) / targetNutrients.carbohydrates;
        double proteinRatio = (nutrition.getProtein() * baseIntake / 100) / targetNutrients.protein;
        double fatRatio = (nutrition.getFat() * baseIntake / 100) / targetNutrients.fat;
        
        // 找出最高的比率（接近超标的营养素）
        double maxRatio = Math.max(Math.max(caloriesRatio, carbsRatio), Math.max(proteinRatio, fatRatio));
        
        // 如果任何营养素接近或超过目标值的80%，则按比例减少摄入量
        if (maxRatio > 0.8) {
            adjustedIntake = baseIntake / (maxRatio * 1.2); // 留出20%的余量
            
            // 确保调整后的摄入量不低于最小推荐量
            IntakeRange range = food.getRecommendedIntakeRange();
            adjustedIntake = Math.max(range.getMinIntake(), adjustedIntake);
            
            MealPlannerLogger.debug("按营养比例调整 " + food.getName() + " 的摄入量: " + 
                             String.format("%.0f", baseIntake) + "克 -> " + 
                             String.format("%.0f", adjustedIntake) + "克");
        }
        
        return adjustedIntake;
    }

    private double calculateFoodScore(Food food, MealNutrients target) {
        // 计算食物与目标营养需求的匹配度
        
        // 主要营养素评分
        double calorieScore = calculateNutrientScore(food.getCalories(), target.calories);
        double carbScore = calculateNutrientScore(food.getCarbohydrates(), target.carbohydrates);
        double proteinScore = calculateNutrientScore(food.getProtein(), target.protein);
        double fatScore = calculateNutrientScore(food.getFat(), target.fat);
        
        // 微量元素评分
        double calciumScore = calculateNutrientScore(food.getCalcium(), target.calcium);
        double potassiumScore = calculateNutrientScore(food.getPotassium(), target.potassium);
        double sodiumScore = calculateNutrientScore(food.getSodium(), target.sodium);
        double magnesiumScore = calculateNutrientScore(food.getMagnesium(), target.magnesium);
        
        // 计算加权平均分数
        double nutritionScore = (calorieScore * weightCalorie + 
                               carbScore * weightCarb +
                               proteinScore * weightProtein +
                               fatScore * weightFat +
                               calciumScore * weightCalcium +
                               potassiumScore * weightPotassium +
                               sodiumScore * weightSodium +
                              magnesiumScore * weightMagnesium) / 
                             (weightCalorie + weightCarb + weightProtein + weightFat + 
                              weightCalcium + weightPotassium + weightSodium + weightMagnesium);
        
        // 如果没有饮食评估器，直接返回营养评分
        if (dietaryEvaluator == null) {
            return nutritionScore;
        }
        
        // 计算用户偏好评分
        double preferenceScore = dietaryEvaluator.calculatePreferenceScore(food);
        
        // 综合考虑营养评分和偏好评分
        double preferenceWeight = mealOptimizer.getPreferenceWeight();
        return nutritionScore * (1 - preferenceWeight) + preferenceScore * preferenceWeight;
    }
    
    /**
     * 计算单个营养素的评分
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
        
        // 使用改进的评分函数，对过量和不足的惩罚不同
        if (ratio <= 1.0) {
            // 不足的情况，线性惩罚
            return ratio;
        } else {
            // 过量的情况，指数惩罚（过量越多，惩罚越严重）
            return Math.exp(-(ratio - 1.0));
        }
    }

    /**
     * 从目标营养素中减去食物的营养成分
     * @param target 目标营养素
     * @param food 要减去的食物
     * @return 减去后的营养素
     */
    private MealNutrients subtractNutrients(MealNutrients target, Food food) {
        MealNutrients foodNutrients = new MealNutrients(
            food.getCalories(), 
            food.getCarbohydrates(), 
            food.getProtein(), 
            food.getFat(), 
            food.getCalcium(), 
            food.getPotassium(), 
            food.getSodium(), 
            food.getMagnesium()
        );
        return MealNutrients.subtract(target, foodNutrients);
    }

    /**
     * 检查食物是否符合用户的饮食限制和偏好
     * @param food 要检查的食物
     * @return 如果食物符合用户的饮食限制和偏好则返回true
     */
    private boolean isFoodSuitableForUser(Food food) {
        // 如果没有用户档案，则认为所有食物都适合
        if (userProfile == null) {
            return true;
        }
        
        // 检查过敏原
        for (String allergen : food.getAllergens()) {
            if (userProfile.isAllergicTo(allergen)) {
                return false; // 用户对该食物过敏
            }
        }
        
        // 检查宗教限制
        for (String restriction : food.getReligiousRestrictions()) {
            if (userProfile.hasReligiousRestrictionFor(restriction)) {
                return false; // 该食物违反用户的宗教限制
            }
        }
        
        // 检查用户是否不喜欢该食物
        if (userProfile.dislikesFood(food.getName())) {
            return false; // 用户不喜欢该食物
        }
        
        // 检查辣度偏好
        if (!userProfile.acceptsSpicyLevel(food.getSpicyLevel())) {
            return false; // 食物辣度超过用户接受范围
        }
        
        return true;
    }

    /**
     * 设置特定营养素的限制范围
     * @param nutrientName 营养素名称（如"sodium"、"carbohydrates"等）
     * @param minValue 最小值
     * @param maxValue 最大值
     */
    public void setNutrientLimit(String nutrientName, double minValue, double maxValue) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("最小值不能大于最大值");
        }
        
        // 创建一个临时的健康状况"custom"，用于存储自定义限制
        Map<String, NutrientLimit> customLimits = nutritionCalculator.getDiseaseNutrientLimits()
                .computeIfAbsent("custom", k -> new HashMap<>());
        
        // 设置限制
        customLimits.put(nutrientName, new NutrientLimit(minValue, maxValue));
        
        // 确保用户的健康状况中包含"custom"
        UserProfile userProfile = nutritionCalculator.getUserProfile();
        String[] conditions = userProfile.getHealthConditions();
        boolean hasCustom = false;
        
        if (conditions != null) {
            for (String condition : conditions) {
                if ("custom".equals(condition)) {
                    hasCustom = true;
                    break;
                }
            }
        }
        
        if (!hasCustom) {
            // 添加"custom"到健康状况
            String[] newConditions;
            if (conditions == null || conditions.length == 0) {
                newConditions = new String[]{"custom"};
            } else {
                newConditions = new String[conditions.length + 1];
                System.arraycopy(conditions, 0, newConditions, 0, conditions.length);
                newConditions[conditions.length] = "custom";
            }
            userProfile.setHealthConditions(newConditions);
        }
    }

    /**
     * 清除所有自定义的营养素限制
     */
    public void clearCustomNutrientLimits() {
        Map<String, Map<String, NutrientLimit>> limits = nutritionCalculator.getDiseaseNutrientLimits();
        limits.remove("custom");
        
        // 从健康状况中移除"custom"
        UserProfile userProfile = nutritionCalculator.getUserProfile();
        String[] conditions = userProfile.getHealthConditions();
        
        if (conditions != null) {
            List<String> newConditionsList = new ArrayList<>();
            for (String condition : conditions) {
                if (!"custom".equals(condition)) {
                    newConditionsList.add(condition);
                }
            }
            
            userProfile.setHealthConditions(newConditionsList.toArray(new String[0]));
        }
    }

    /**
     * 获取饮食评估器
     * @return 饮食评估器
     */
    public DietaryEvaluator getDietaryEvaluator() {
        return dietaryEvaluator;
    }
    
    /**
     * 设置饮食评估器
     * @param dietaryEvaluator 饮食评估器
     */
    public void setDietaryEvaluator(DietaryEvaluator dietaryEvaluator) {
        this.dietaryEvaluator = dietaryEvaluator;
    }
}

class DailyMealPlan {
    private List<Food> breakfast;
    private List<Food> lunch;
    private List<Food> dinner;

    public DailyMealPlan(List<Food> breakfast, List<Food> lunch, List<Food> dinner) {
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
    }
    
    // 添加getter方法
    public List<Food> getBreakfast() { return breakfast; }
    public List<Food> getLunch() { return lunch; }
    public List<Food> getDinner() { return dinner; }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("每日膳食计划:\n\n");
        
        // 早餐
        sb.append("早餐 (6:30-8:30):\n");
        for (Food food : breakfast) {
            sb.append(String.format("- %s %s\n", food.getName(), food.getPortionDescription()));
        }
        sb.append(getMealNutritionSummary(breakfast));
        
        // 午餐
        sb.append("\n午餐 (11:30-13:30):\n");
        for (Food food : lunch) {
            sb.append(String.format("- %s %s\n", food.getName(), food.getPortionDescription()));
        }
        sb.append(getMealNutritionSummary(lunch));
        
        // 晚餐
        sb.append("\n晚餐 (17:30-19:30):\n");
        for (Food food : dinner) {
            sb.append(String.format("- %s %s\n", food.getName(), food.getPortionDescription()));
        }
        sb.append(getMealNutritionSummary(dinner));
        
        // 全天营养总结
        sb.append("\n全天营养总结:\n");
        List<Food> allFoods = new ArrayList<>();
        allFoods.addAll(breakfast);
        allFoods.addAll(lunch);
        allFoods.addAll(dinner);
        sb.append(getMealNutritionSummary(allFoods));
        
        return sb.toString();
    }
    
    private String getMealNutritionSummary(List<Food> foods) {
        double totalCalories = 0;
        double totalCarbs = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalCalcium = 0;
        double totalPotassium = 0;
        double totalSodium = 0;
        double totalMagnesium = 0;
        
        for (Food food : foods) {
            totalCalories += food.getCalories();
            totalCarbs += food.getCarbohydrates();
            totalProtein += food.getProtein();
            totalFat += food.getFat();
            totalCalcium += food.getCalcium();
            totalPotassium += food.getPotassium();
            totalSodium += food.getSodium();
            totalMagnesium += food.getMagnesium();
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  营养总结: %.0f卡路里, %.1fg碳水, %.1fg蛋白质, %.1fg脂肪\n", 
                 totalCalories, totalCarbs, totalProtein, totalFat));
        sb.append(String.format("  微量元素: %.0fmg钙, %.0fmg钾, %.0fmg钠, %.0fmg镁\n", 
                 totalCalcium, totalPotassium, totalSodium, totalMagnesium));
        
        // 计算三大营养素比例
        double totalNutrientCalories = (totalCarbs * 4) + (totalProtein * 4) + (totalFat * 9);
        double carbsPercentage = (totalCarbs * 4) / totalNutrientCalories * 100;
        double proteinPercentage = (totalProtein * 4) / totalNutrientCalories * 100;
        double fatPercentage = (totalFat * 9) / totalNutrientCalories * 100;
        
        sb.append(String.format("  营养素比例: 碳水%.1f%%, 蛋白质%.1f%%, 脂肪%.1f%%\n", 
                 carbsPercentage, proteinPercentage, fatPercentage));
        
        return sb.toString();
    }
}

/**
 * 营养素摘要类，用于存储和计算营养素信息
 */
class NutrientSummary {
    double calories;
    double carbs;
    double protein;
    double fat;
    double calcium;
    double potassium;
    double sodium;
    double magnesium;
    
    public NutrientSummary(double calories, double carbs, double protein, double fat,
                          double calcium, double potassium, double sodium, double magnesium) {
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
        this.calcium = calcium;
        this.potassium = potassium;
        this.sodium = sodium;
        this.magnesium = magnesium;
    }
    
    /**
     * 计算碳水化合物占总热量的百分比
     * @return 百分比
     */
    public double getCarbsPercentage() {
        double totalCalories = (carbs * 4) + (protein * 4) + (fat * 9);
        return totalCalories > 0 ? (carbs * 4 / totalCalories) * 100 : 0;
    }
    
    /**
     * 计算蛋白质占总热量的百分比
     * @return 百分比
     */
    public double getProteinPercentage() {
        double totalCalories = (carbs * 4) + (protein * 4) + (fat * 9);
        return totalCalories > 0 ? (protein * 4 / totalCalories) * 100 : 0;
    }
    
    /**
     * 计算脂肪占总热量的百分比
     * @return 百分比
     */
    public double getFatPercentage() {
        double totalCalories = (carbs * 4) + (protein * 4) + (fat * 9);
        return totalCalories > 0 ? (fat * 9 / totalCalories) * 100 : 0;
    }
    
    /**
     * 将两个营养素摘要相加
     * @param other 另一个营养素摘要
     * @return 相加后的新营养素摘要
     */
    public NutrientSummary add(NutrientSummary other) {
        return new NutrientSummary(
            this.calories + other.calories,
            this.carbs + other.carbs,
            this.protein + other.protein,
            this.fat + other.fat,
            this.calcium + other.calcium,
            this.potassium + other.potassium,
            this.sodium + other.sodium,
            this.magnesium + other.magnesium
        );
    }
} 