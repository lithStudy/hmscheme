package com.mealplanner;

import java.io.IOException;
import java.util.*;

import com.mealplanner.foodmanage.NutritionDataParser;

public class MealPlanner {
    private List<Food> foodDatabase;
    private NutritionCalculator nutritionCalculator;
    private static final double MEAL_RATIO_BREAKFAST = 0.3;  // 早餐占比30%
    private static final double MEAL_RATIO_LUNCH = 0.35;     // 午餐占比35%
    private static final double MEAL_RATIO_DINNER = 0.35;    // 晚餐占比35%

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

    public MealPlanner(UserProfile userProfile) {
        this.nutritionCalculator = new NutritionCalculator(userProfile);
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
        DailyNutrientNeeds dailyNeeds = nutritionCalculator.calculateDailyNutrientNeeds();
        
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
    private void printDetailedNutrition(DailyMealPlan plan, DailyNutrientNeeds dailyNeeds) {
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
        System.out.printf("总热量: %.1f kcal\n", dailyNeeds.getTotalCalories());
        System.out.printf("碳水化合物: %.1f g (%.1f%%)\n", 
                        dailyNeeds.getCarbohydrates(), 
                        dailyNeeds.getCarbohydrates() * 4 / dailyNeeds.getTotalCalories() * 100);
        System.out.printf("蛋白质: %.1f g (%.1f%%)\n", 
                        dailyNeeds.getProtein(), 
                        dailyNeeds.getProtein() * 4 / dailyNeeds.getTotalCalories() * 100);
        System.out.printf("脂肪: %.1f g (%.1f%%)\n", 
                        dailyNeeds.getFat(), 
                        dailyNeeds.getFat() * 9 / dailyNeeds.getTotalCalories() * 100);
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

    private MealNutrients calculateMealNutrients(DailyNutrientNeeds dailyNeeds, double ratio) {
        return new MealNutrients(
            dailyNeeds.getTotalCalories() * ratio,
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
        // 用于存储一餐的食物列表
        List<Food> meal = new ArrayList<>();
        // 用于记录每个食物类别的数量
        Map<String, Integer> categoryCount = new HashMap<>();
        // 剩余热量
        double remainingCalories = targetNutrients.calories;
        // 用于跟踪尝试过但被限制规则拒绝的食物
        Set<String> rejectedFoods = new HashSet<>();
        
        // 如果需要主食，先选择主食
        if (requireStaple) {
            Food staple = findBestStapleFood(targetNutrients, usedFoods);
            if (staple != null) {
                meal.add(staple);
                remainingCalories -= staple.getCalories();
                usedFoods.add(staple.getName());
                categoryCount.put("staple", 1);
                targetNutrients = subtractNutrients(targetNutrients, staple);
            }
        }
        
        // 选择其他食物
        int attemptCount = 0;
        int maxAttempts = 20; // 设置最大尝试次数，防止无限循环
        
        while (remainingCalories > 0 && meal.size() < 5 && attemptCount < maxAttempts) {
            attemptCount++;
            
            // 合并已使用和已拒绝的食物集合，用于查找下一个最佳食物
            Set<String> excludedFoods = new HashSet<>(usedFoods);
            excludedFoods.addAll(rejectedFoods);
            
            Food bestFood = findBestFood(targetNutrients, categoryCount, excludedFoods);
            if (bestFood == null) break;
            
            // 检查添加这个食物后是否会超出营养素限制
            List<Food> tempMeal = new ArrayList<>(meal);
            tempMeal.add(bestFood);
            if (!checkMealNutrientLimits(tempMeal)) {
                // 如果超出限制，将食物添加到拒绝列表
                rejectedFoods.add(bestFood.getName());
                continue;
            }
            
            meal.add(bestFood);
            remainingCalories -= bestFood.getCalories();
            usedFoods.add(bestFood.getName());
            categoryCount.put(bestFood.getCategory(), 
                            categoryCount.getOrDefault(bestFood.getCategory(), 0) + 1);
            
            targetNutrients = subtractNutrients(targetNutrients, bestFood);
        }
        
        // 如果因为营养素限制导致无法添加足够的食物，记录警告信息
        if (attemptCount >= maxAttempts) {
            System.out.println("警告：由于营养素限制，无法找到足够的食物组成一餐。");
        }
        
        return meal;
    }

    private Food findBestStapleFood(MealNutrients targetNutrients, Set<String> usedFoods) {
        Food bestStaple = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Food food : foodDatabase) {
            if (food.getCategory().equals("staple") && !usedFoods.contains(food.getName())) {
                double score = calculateFoodScore(food, targetNutrients);
                if (score > bestScore) {
                    bestScore = score;
                    bestStaple = food;
                }
            }
        }
        
        return bestStaple;
    }

    private Food findBestFood(MealNutrients targetNutrients, Map<String, Integer> categoryCount, Set<String> usedFoods) {
        Food bestFood = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Food food : foodDatabase) {
            // 跳过已使用的食物
            if (usedFoods.contains(food.getName())) {
                continue;
            }
            
            // 限制每类食物的数量
            int maxAllowed = maxFoodPerCategory.getOrDefault(food.getCategory(), DEFAULT_MAX_FOODS_PER_CATEGORY);
            if (categoryCount.getOrDefault(food.getCategory(), 0) >= maxAllowed) {
                continue;
            }
            
            // 计算食物评分
            double score = calculateFoodScore(food, targetNutrients);
            if (score > bestScore) {
                bestScore = score;
                bestFood = food;
            }
        }
        
        return bestFood;
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
        
        // 检查是否有营养素超出限制
        Map<String, NutrientLimit> nutrientLimits = nutritionCalculator.getAllNutrientLimits();
        
        // 应用营养素限制的惩罚
        if (nutrientLimits.containsKey("sodium")) {
            NutrientLimit sodiumLimit = nutrientLimits.get("sodium");
            double sodiumDeviationScore = sodiumLimit.calculateDeviationScore(food.getSodium());
            if (sodiumDeviationScore < 0) {
                // 如果钠超出限制，降低评分，但不要过于严厉
                sodiumScore = sodiumScore * (1.0 + sodiumDeviationScore * 2.0); // 降低惩罚系数
            }
        }
        
        if (nutrientLimits.containsKey("carbohydrates")) {
            NutrientLimit carbLimit = nutrientLimits.get("carbohydrates");
            double carbDeviationScore = carbLimit.calculateDeviationScore(food.getCarbohydrates());
            if (carbDeviationScore < 0) {
                carbScore = carbScore * (1.0 + carbDeviationScore * 2.0);
            }
        }
        
        if (nutrientLimits.containsKey("protein")) {
            NutrientLimit proteinLimit = nutrientLimits.get("protein");
            double proteinDeviationScore = proteinLimit.calculateDeviationScore(food.getProtein());
            if (proteinDeviationScore < 0) {
                proteinScore = proteinScore * (1.0 + proteinDeviationScore * 2.0);
            }
        }
        
        if (nutrientLimits.containsKey("fat")) {
            NutrientLimit fatLimit = nutrientLimits.get("fat");
            double fatDeviationScore = fatLimit.calculateDeviationScore(food.getFat());
            if (fatDeviationScore < 0) {
                fatScore = fatScore * (1.0 + fatDeviationScore * 2.0);
            }
        }
        
        if (nutrientLimits.containsKey("potassium")) {
            NutrientLimit potassiumLimit = nutrientLimits.get("potassium");
            double potassiumDeviationScore = potassiumLimit.calculateDeviationScore(food.getPotassium());
            if (potassiumDeviationScore < 0) {
                potassiumScore = potassiumScore * (1.0 + potassiumDeviationScore * 2.0);
            }
        }
        
        // 计算总权重
        double totalWeight = weightCalorie + weightCarb + weightProtein + weightFat +
                            weightCalcium + weightPotassium + weightSodium + weightMagnesium;
        
        // 加权平均评分
        double weightedScore = (calorieScore * weightCalorie +
                               carbScore * weightCarb +
                               proteinScore * weightProtein +
                               fatScore * weightFat +
                               calciumScore * weightCalcium +
                               potassiumScore * weightPotassium +
                               sodiumScore * weightSodium +
                               magnesiumScore * weightMagnesium) / totalWeight;
        
        return weightedScore;
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

    private MealNutrients subtractNutrients(MealNutrients target, Food food) {
        return new MealNutrients(
            target.calories - food.getCalories(),
            target.carbohydrates - food.getCarbohydrates(),
            target.protein - food.getProtein(),
            target.fat - food.getFat(),
            target.calcium - food.getCalcium(),
            target.potassium - food.getPotassium(),
            target.sodium - food.getSodium(),
            target.magnesium - food.getMagnesium()
        );
    }

    /**
     * 检查一餐的总营养素是否符合限制
     * @param foods 食物列表
     * @return 是否符合所有限制
     */
    private boolean checkMealNutrientLimits(List<Food> foods) {
        // 获取所有营养素限制
        Map<String, NutrientLimit> nutrientLimits = nutritionCalculator.getAllNutrientLimits();
        if (nutrientLimits.isEmpty()) {
            return true; // 没有限制，直接返回true
        }
        
        // 计算这餐的总营养素
        double totalSodium = 0;
        double totalCarbs = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalPotassium = 0;
        
        for (Food food : foods) {
            totalSodium += food.getSodium();
            totalCarbs += food.getCarbohydrates();
            totalProtein += food.getProtein();
            totalFat += food.getFat();
            totalPotassium += food.getPotassium();
        }
        
        // 检查是否超出限制
        if (nutrientLimits.containsKey("sodium")) {
            NutrientLimit sodiumLimit = nutrientLimits.get("sodium");
            if (!sodiumLimit.isWithinLimit(totalSodium)) {
                return false;
            }
        }
        
        if (nutrientLimits.containsKey("carbohydrates")) {
            NutrientLimit carbLimit = nutrientLimits.get("carbohydrates");
            if (!carbLimit.isWithinLimit(totalCarbs)) {
                return false;
            }
        }
        
        if (nutrientLimits.containsKey("protein")) {
            NutrientLimit proteinLimit = nutrientLimits.get("protein");
            if (!proteinLimit.isWithinLimit(totalProtein)) {
                return false;
            }
        }
        
        if (nutrientLimits.containsKey("fat")) {
            NutrientLimit fatLimit = nutrientLimits.get("fat");
            if (!fatLimit.isWithinLimit(totalFat)) {
                return false;
            }
        }
        
        if (nutrientLimits.containsKey("potassium")) {
            NutrientLimit potassiumLimit = nutrientLimits.get("potassium");
            if (!potassiumLimit.isWithinLimit(totalPotassium)) {
                return false;
            }
        }
        
        return true; // 所有检查都通过
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
}

class MealNutrients {
    double calories;      // 卡路里(kcal)
    double carbohydrates; // 碳水化合物(g)
    double protein;       // 蛋白质(g)
    double fat;           // 脂肪(g)
    double calcium;       // 钙(mg)
    double potassium;     // 钾(mg)
    double sodium;        // 钠(mg)
    double magnesium;     // 镁(mg)

    public MealNutrients(double calories, double carbohydrates, double protein, 
                        double fat, double calcium, double potassium, 
                        double sodium, double magnesium) {
        this.calories = calories;
        this.carbohydrates = carbohydrates;
        this.protein = protein;
        this.fat = fat;
        this.calcium = calcium;
        this.potassium = potassium;
        this.sodium = sodium;
        this.magnesium = magnesium;
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