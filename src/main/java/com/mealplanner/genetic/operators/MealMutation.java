package com.mealplanner.genetic.operators;

import com.mealplanner.Food;
import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实现膳食解决方案的变异操作
 */
public class MealMutation {
    // 变异概率
    private double mutationRate;
    
    // 食物数据库
    private List<Food> foodDatabase;
    
    // 变异类型枚举
    public enum MutationType {
        INTAKE_ADJUSTMENT,    // 摄入量调整
        FOOD_REPLACEMENT,     // 食物替换
        FOOD_ADDITION,        // 添加食物
        FOOD_REMOVAL,         // 移除食物
        CALORIES_OPTIMIZATION, // 热量优化
        NUTRIENT_SENSITIVITY, // 基于营养素敏感度分析的精准调整
        COMPREHENSIVE         // 综合变异（包含以上所有类型）
    }
    
    // 默认变异类型
    private MutationType mutationType = MutationType.COMPREHENSIVE;
    
    // 摄入量变异强度(0-1)
    private double intakeMutationStrength = 0.2;
    
    // 目标营养素达成率范围
    private double minNutrientAchievementRate = 0.8;
    private double maxNutrientAchievementRate = 1.1;
    
    // 营养素权重，用于敏感度分析，默认都是1.0
    private Map<String, Double> nutrientWeights = new HashMap<>();
    
    // 当前目标营养素（可选）
    private com.mealplanner.MealNutrients targetNutrients;
    
    /**
     * 构造函数
     * @param mutationRate 变异概率
     * @param foodDatabase 食物数据库
     */
    public MealMutation(double mutationRate, List<Food> foodDatabase) {
        this.mutationRate = mutationRate;
        this.foodDatabase = foodDatabase;
    }
    
    /**
     * 构造函数
     * @param mutationRate 变异概率
     * @param foodDatabase 食物数据库
     * @param mutationType 变异类型
     */
    public MealMutation(double mutationRate, List<Food> foodDatabase, MutationType mutationType) {
        this.mutationRate = mutationRate;
        this.foodDatabase = foodDatabase;
        this.mutationType = mutationType;
    }
    
    /**
     * 对解决方案应用变异操作
     * @param solution 待变异的解决方案
     * @param requireStaple 是否需要保留主食
     * @param targetCalories 目标热量（用于热量优化）
     * @return 是否发生了变异
     */
    public boolean apply(MealSolution solution, boolean requireStaple, double targetCalories) {
        // 如果随机值大于变异率，不执行变异
        if (Math.random() > mutationRate) {
            return false;
        }
        
        // 根据变异类型执行不同的变异操作
        switch (mutationType) {
            case INTAKE_ADJUSTMENT:
                return mutateIntake(solution);
            case FOOD_REPLACEMENT:
                return mutateReplaceFood(solution, requireStaple);
            case FOOD_ADDITION:
                return mutateAddFood(solution);
            case FOOD_REMOVAL:
                return mutateRemoveFood(solution, requireStaple);
            case CALORIES_OPTIMIZATION:
                return mutateOptimizeCalories(solution, targetCalories);
            case NUTRIENT_SENSITIVITY:
                return mutateByNutrientSensitivity(solution, requireStaple);
            case COMPREHENSIVE:
            default:
                // 随机选择一种变异类型，但给敏感度分析更高的概率
                Random random = new Random();
                double r = random.nextDouble();
                
                // 给敏感度分析和热量优化更高的概率（各30%），其他类型各10%
                if (r < 0.3) {
                    return mutateByNutrientSensitivity(solution, requireStaple);
                } else if (r < 0.6) {
                    return mutateOptimizeCalories(solution, targetCalories);
                } else if (r < 0.7) {
                    return mutateIntake(solution);
                } else if (r < 0.8) {
                    return mutateReplaceFood(solution, requireStaple);
                } else if (r < 0.9) {
                    return mutateAddFood(solution);
                } else {
                    return mutateRemoveFood(solution, requireStaple);
                }
        }
    }
    
    /**
     * 原有的apply方法重载，保持向后兼容
     */
    public boolean apply(MealSolution solution, boolean requireStaple) {
        // 使用默认目标热量600（作为示例，实际应从解决方案中获取）
        return apply(solution, requireStaple, 600);
    }
    
    /**
     * 变异：调整食物摄入量
     * @param solution 待变异的解决方案
     * @return 是否成功变异
     */
    private boolean mutateIntake(MealSolution solution) {
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty()) {
            return false;
        }
        
        // 随机选择一个食物基因
        Random random = new Random();
        int index = random.nextInt(genes.size());
        FoodGene gene = genes.get(index);
        
        // 获取该食物的推荐摄入量范围
        Food food = gene.getFood();
        double minIntake = food.getRecommendedIntakeRange().getMinIntake();
        double maxIntake = food.getRecommendedIntakeRange().getMaxIntake();
        
        // 应用摄入量变异
        gene.mutateIntake(minIntake, maxIntake, intakeMutationStrength);
        
        return true;
    }
    
    /**
     * 变异：替换食物
     * @param solution 待变异的解决方案
     * @param requireStaple 是否需要保留主食
     * @return 是否成功变异
     */
    private boolean mutateReplaceFood(MealSolution solution, boolean requireStaple) {
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty() || foodDatabase.isEmpty()) {
            return false;
        }
        
        Random random = new Random();
        
        // 随机选择要替换的食物
        int index = random.nextInt(genes.size());
        FoodGene geneToReplace = genes.get(index);
        
        // 如果需要主食且选中的是唯一的主食，则不替换
        if (requireStaple && "staple".equals(geneToReplace.getFood().getCategory())) {
            boolean isOnlyStaple = genes.stream()
                    .filter(g -> "staple".equals(g.getFood().getCategory()))
                    .count() <= 1;
            
            if (isOnlyStaple) {
                // 如果是唯一的主食，尝试选择另一个非主食食物
                List<FoodGene> nonStapleGenes = genes.stream()
                        .filter(g -> !"staple".equals(g.getFood().getCategory()))
                        .collect(Collectors.toList());
                
                if (nonStapleGenes.isEmpty()) {
                    return false; // 没有可替换的非主食
                }
                
                // 随机选择一个非主食替换
                index = genes.indexOf(nonStapleGenes.get(random.nextInt(nonStapleGenes.size())));
                geneToReplace = genes.get(index);
            }
        }
        
        // 获取当前食物的类别
        String category = geneToReplace.getFood().getCategory();
        
        // 筛选同类别的食物，排除已在解决方案中的食物
        List<Food> candidateFoods = foodDatabase.stream()
                .filter(f -> f.getCategory().equals(category))
                .filter(f -> !genes.stream().anyMatch(g -> g.getFood().getName().equals(f.getName())))
                .collect(Collectors.toList());
        
        if (candidateFoods.isEmpty()) {
            return false; // 没有可替换的同类别食物
        }
        
        // 随机选择一个替换食物
        Food replacementFood = candidateFoods.get(random.nextInt(candidateFoods.size()));
        
        // 为新食物生成一个在推荐范围内的摄入量
        double minIntake = replacementFood.getRecommendedIntakeRange().getMinIntake();
        double maxIntake = replacementFood.getRecommendedIntakeRange().getMaxIntake();
        double intake = minIntake + random.nextDouble() * (maxIntake - minIntake);
        
        // 将摄入量四舍五入为整数
        intake = Math.round(intake);
        
        // 替换食物
        genes.set(index, new FoodGene(replacementFood, intake));
        
        return true;
    }
    
    /**
     * 变异：添加新食物
     * @param solution 待变异的解决方案
     * @return 是否成功变异
     */
    private boolean mutateAddFood(MealSolution solution) {
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (foodDatabase.isEmpty() || genes.size() >= 8) { // 限制最多8种食物
            return false;
        }
        
        // 获取当前解决方案中的食物名称
        Set<String> existingFoodNames = genes.stream()
                .map(g -> g.getFood().getName())
                .collect(Collectors.toSet());
        
        // 筛选可添加的食物
        List<Food> candidateFoods = foodDatabase.stream()
                .filter(f -> !existingFoodNames.contains(f.getName()))
                .collect(Collectors.toList());
        
        if (candidateFoods.isEmpty()) {
            return false; // 没有可添加的食物
        }
        
        // 随机选择一个食物添加
        Random random = new Random();
        Food newFood = candidateFoods.get(random.nextInt(candidateFoods.size()));
        
        // 为新食物生成一个在推荐范围内的摄入量
        double minIntake = newFood.getRecommendedIntakeRange().getMinIntake();
        double maxIntake = newFood.getRecommendedIntakeRange().getMaxIntake();
        double intake = minIntake + random.nextDouble() * (maxIntake - minIntake);
        
        // 将摄入量四舍五入为整数
        intake = Math.round(intake);
        
        // 添加新食物
        solution.addFood(new FoodGene(newFood, intake));
        
        return true;
    }
    
    /**
     * 变异：移除食物
     * @param solution 待变异的解决方案
     * @param requireStaple 是否需要保留主食
     * @return 是否成功变异
     */
    private boolean mutateRemoveFood(MealSolution solution, boolean requireStaple) {
        List<FoodGene> genes = solution.getFoodGenes();
        
        // 至少保留一种食物
        if (genes.size() <= 1) {
            return false;
        }
        
        Random random = new Random();
        
        // 可移除的食物索引列表
        List<Integer> removableFoodIndices = new ArrayList<>();
        
        // 检查每种食物是否可移除
        for (int i = 0; i < genes.size(); i++) {
            FoodGene gene = genes.get(i);
            
            // 如果需要主食且该食物是唯一的主食，则不能移除
            if (requireStaple && "staple".equals(gene.getFood().getCategory())) {
                boolean isOnlyStaple = genes.stream()
                        .filter(g -> "staple".equals(g.getFood().getCategory()))
                        .count() <= 1;
                
                if (isOnlyStaple) {
                    continue; // 不能移除唯一的主食
                }
            }
            
            removableFoodIndices.add(i);
        }
        
        if (removableFoodIndices.isEmpty()) {
            return false; // 没有可移除的食物
        }
        
        // 随机选择一个食物移除
        int indexToRemove = removableFoodIndices.get(random.nextInt(removableFoodIndices.size()));
        solution.removeFood(indexToRemove);
        
        return true;
    }
    
    /**
     * 变异：优化热量摄入
     * 根据目标热量和当前热量的差异，调整所选食物的摄入量
     * @param solution 待变异的解决方案
     * @param targetCalories 目标热量
     * @return 是否成功变异
     */
    public boolean mutateOptimizeCalories(MealSolution solution, double targetCalories) {
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty()) {
            return false;
        }
        
        // 计算当前膳食的总热量
        double currentCalories = solution.calculateTotalNutrients().calories;
        
        // 计算热量差额（正值表示需要增加热量，负值表示需要减少热量）
        double caloriesDifference = targetCalories - currentCalories;
        
        // 如果热量差额很小，则不需要调整
        if (Math.abs(caloriesDifference) < 20) {
            return false; // 热量已经足够接近目标，不需要变异
        }
        
        // 选择一个合适的食物进行调整
        Random random = new Random();
        
        // 根据热量差额的方向，选择高热量或低热量的食物进行调整
        List<FoodGene> candidateGenes;
        
        if (caloriesDifference > 0) {
            // 需要增加热量，选择高热量密度的食物
            candidateGenes = genes.stream()
                    .filter(gene -> {
                        double caloriesPer100g = gene.getFood().getNutrition().getCalories();
                        return caloriesPer100g > 100; // 选择热量密度较高的食物
                    })
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // 需要减少热量，选择高热量密度的食物减少摄入量
            candidateGenes = genes.stream()
                    .filter(gene -> {
                        double caloriesPer100g = gene.getFood().getNutrition().getCalories();
                        return caloriesPer100g > 100; // 选择热量密度较高的食物
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 如果没有合适的候选食物，则从所有食物中随机选择
        if (candidateGenes.isEmpty()) {
            candidateGenes = genes;
        }
        
        // 随机选择一个食物进行调整
        FoodGene selectedGene = candidateGenes.get(random.nextInt(candidateGenes.size()));
        
        // 获取该食物的推荐摄入量范围
        Food food = selectedGene.getFood();
        double minIntake = food.getRecommendedIntakeRange().getMinIntake();
        double maxIntake = food.getRecommendedIntakeRange().getMaxIntake();
        
        // 计算当前食物的热量密度（每克卡路里）
        double caloriesPerGram = food.getNutrition().getCalories() / 100.0;
        
        // 计算需要调整的克数
        double gramsToAdjust = caloriesDifference / caloriesPerGram;
        
        // 计算新的摄入量
        double currentIntake = selectedGene.getIntake();
        double newIntake = currentIntake + gramsToAdjust;
        
        // 确保新的摄入量在推荐范围内并为整数
        newIntake = Math.round(Math.max(minIntake, Math.min(maxIntake, newIntake)));
        
        // 如果新摄入量与当前摄入量相同，则随机增减10-30克
        if (newIntake == currentIntake) {
            int adjustment = random.nextInt(21) + 10; // 10到30之间的随机数
            if (caloriesDifference > 0) {
                newIntake = Math.round(Math.min(maxIntake, currentIntake + adjustment));
            } else {
                newIntake = Math.round(Math.max(minIntake, currentIntake - adjustment));
            }
        }
        
        // 设置新的摄入量
        selectedGene.setIntake(newIntake);
        
        return true;
    }
    
    /**
     * 基于营养素敏感度分析的食材克重精准调整
     * @param solution 待变异的解决方案
     * @param requireStaple 是否需要保留主食
     * @return 是否成功变异
     */
    public boolean mutateByNutrientSensitivity(MealSolution solution, boolean requireStaple) {
        // 获取当前的食物基因列表
        List<FoodGene> genes = solution.getFoodGenes();
        
        if (genes.isEmpty()) {
            return false;
        }
        
        // 计算当前的营养素水平和目标达成率
        // com.mealplanner.MealNutrients currentNutrients = solution.calculateTotalNutrients();
        Map<String, Double> nutrientRatios = calculateNutrientAchievementRatios(solution);
        
        // 找出不达标的营养素（低于下限或高于上限）
        Map<String, Double> problematicNutrients = findProblematicNutrients(nutrientRatios);
        
        if (problematicNutrients.isEmpty()) {
            // 所有营养素都在达成率范围内，随机选择一种变异
            Random random = new Random();
            int choice = random.nextInt(3);
            switch (choice) {
                case 0:
                    return mutateIntake(solution);
                case 1:
                    return mutateAddFood(solution);
                default:
                    return mutateReplaceFood(solution, requireStaple);
            }
        }
        
        // 计算食材对各营养素的贡献度
        Map<FoodGene, Map<String, Double>> foodNutrientContributions = calculateFoodNutrientContributions(solution);
        
        // 选择最适合调整的食材
        List<AdjustmentAction> adjustmentActions = determineAdjustmentActions(
            genes, problematicNutrients, foodNutrientContributions);
        
        if (adjustmentActions.isEmpty()) {
            // 如果找不到合适的调整行动，尝试添加新食物
            return mutateAddFood(solution);
        }
        
        // 执行调整行动
        boolean adjustmentMade = false;
        for (AdjustmentAction action : adjustmentActions) {
            if (applyAdjustmentAction(solution, action, requireStaple)) {
                adjustmentMade = true;
            }
        }
        
        return adjustmentMade;
    }
    
    /**
     * 计算当前膳食方案中各营养素的达成率
     * @param solution 膳食方案
     * @return 营养素达成率映射
     */
    private Map<String, Double> calculateNutrientAchievementRatios(MealSolution solution) {
        // 计算实际营养素
        com.mealplanner.MealNutrients actualNutrients = solution.calculateTotalNutrients();
        
        // 使用已设置的目标营养素，或者估计值
        com.mealplanner.MealNutrients targetsToUse = targetNutrients != null ? 
                                                  targetNutrients : 
                                                  estimateTargetNutrients(actualNutrients);
        
        Map<String, Double> ratios = new HashMap<>();
        
        // 计算主要营养素的达成率
        if (targetsToUse.calories > 0) {
            ratios.put("calories", actualNutrients.calories / targetsToUse.calories);
        }
        
        if (targetsToUse.carbohydrates > 0) {
            ratios.put("carbohydrates", actualNutrients.carbohydrates / targetsToUse.carbohydrates);
        }
        
        if (targetsToUse.protein > 0) {
            ratios.put("protein", actualNutrients.protein / targetsToUse.protein);
        }
        
        if (targetsToUse.fat > 0) {
            ratios.put("fat", actualNutrients.fat / targetsToUse.fat);
        }
        
        // 计算微量元素的达成率
        if (targetsToUse.calcium > 0) {
            ratios.put("calcium", actualNutrients.calcium / targetsToUse.calcium);
        }
        
        if (targetsToUse.potassium > 0) {
            ratios.put("potassium", actualNutrients.potassium / targetsToUse.potassium);
        }
        
        if (targetsToUse.sodium > 0) {
            ratios.put("sodium", actualNutrients.sodium / targetsToUse.sodium);
        }
        
        if (targetsToUse.magnesium > 0) {
            ratios.put("magnesium", actualNutrients.magnesium / targetsToUse.magnesium);
        }
        
        return ratios;
    }
    
    /**
     * 估计目标营养素（当无法直接获取时）
     * @param actualNutrients 当前营养素
     * @return 估计的目标营养素
     */
    private com.mealplanner.MealNutrients estimateTargetNutrients(com.mealplanner.MealNutrients actualNutrients) {
        // 这是一个简化的估计方法，实际情况应基于用户特征进行更复杂的计算
        // 假设当前营养素是目标的一个比例
        return new com.mealplanner.MealNutrients(
            actualNutrients.calories * 1.25,       // 假设目标热量是当前的1.25倍
            actualNutrients.carbohydrates * 1.2,   // 碳水
            actualNutrients.protein * 1.3,         // 蛋白质
            actualNutrients.fat * 1.15,            // 脂肪
            actualNutrients.calcium * 1.4,         // 钙
            actualNutrients.potassium * 1.5,       // 钾
            actualNutrients.sodium * 0.9,          // 钠（可能需要减少）
            actualNutrients.magnesium * 1.4        // 镁
        );
    }
    
    /**
     * 找出不在达成率范围内的营养素
     * @param nutrientRatios 营养素达成率映射
     * @return 不达标的营养素及其达成率
     */
    private Map<String, Double> findProblematicNutrients(Map<String, Double> nutrientRatios) {
        Map<String, Double> problematic = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : nutrientRatios.entrySet()) {
            String nutrient = entry.getKey();
            double ratio = entry.getValue();
            
            // 检查是否在达成率范围内
            if (ratio < minNutrientAchievementRate || ratio > maxNutrientAchievementRate) {
                problematic.put(nutrient, ratio);
            }
        }
        
        return problematic;
    }
    
    /**
     * 计算每种食材对各营养素的贡献度
     * @param solution 膳食方案
     * @return 食材-营养素贡献度映射
     */
    private Map<FoodGene, Map<String, Double>> calculateFoodNutrientContributions(MealSolution solution) {
        List<FoodGene> genes = solution.getFoodGenes();
        com.mealplanner.MealNutrients totalNutrients = solution.calculateTotalNutrients();
        Map<FoodGene, Map<String, Double>> contributions = new HashMap<>();
        
        for (FoodGene gene : genes) {
            Map<String, Double> nutrientContributions = new HashMap<>();
            Food food = gene.getFood();
            double intake = gene.getIntake();
            
            // 计算该食材对各营养素的贡献比例
            // 热量贡献
            double caloriesContrib = (food.getNutrition().getCalories() * intake / 100) / totalNutrients.calories;
            nutrientContributions.put("calories", caloriesContrib);
            
            // 碳水贡献
            double carbsContrib = (food.getNutrition().getCarbohydrates() * intake / 100) / totalNutrients.carbohydrates;
            nutrientContributions.put("carbohydrates", carbsContrib);
            
            // 蛋白质贡献
            double proteinContrib = (food.getNutrition().getProtein() * intake / 100) / totalNutrients.protein;
            nutrientContributions.put("protein", proteinContrib);
            
            // 脂肪贡献
            double fatContrib = (food.getNutrition().getFat() * intake / 100) / totalNutrients.fat;
            nutrientContributions.put("fat", fatContrib);
            
            // 钙贡献
            if (totalNutrients.calcium > 0) {
                double calciumContrib = (food.getNutrition().getCalcium() * intake / 100) / totalNutrients.calcium;
                nutrientContributions.put("calcium", calciumContrib);
            }
            
            // 钾贡献
            if (totalNutrients.potassium > 0) {
                double potassiumContrib = (food.getNutrition().getPotassium() * intake / 100) / totalNutrients.potassium;
                nutrientContributions.put("potassium", potassiumContrib);
            }
            
            // 钠贡献
            if (totalNutrients.sodium > 0) {
                double sodiumContrib = (food.getNutrition().getSodium() * intake / 100) / totalNutrients.sodium;
                nutrientContributions.put("sodium", sodiumContrib);
            }
            
            // 镁贡献
            if (totalNutrients.magnesium > 0) {
                double magnesiumContrib = (food.getNutrition().getMagnesium() * intake / 100) / totalNutrients.magnesium;
                nutrientContributions.put("magnesium", magnesiumContrib);
            }
            
            contributions.put(gene, nutrientContributions);
        }
        
        return contributions;
    }
    
    /**
     * 确定需要执行的调整行动
     * @param genes 食物基因列表
     * @param problematicNutrients 不达标的营养素
     * @param foodContributions 食材营养素贡献度
     * @return 调整行动列表
     */
    private List<AdjustmentAction> determineAdjustmentActions(
            List<FoodGene> genes,
            Map<String, Double> problematicNutrients,
            Map<FoodGene, Map<String, Double>> foodContributions) {
        
        List<AdjustmentAction> actions = new ArrayList<>();
        
        // 对每个问题营养素，找出最适合调整的食材
        for (Map.Entry<String, Double> entry : problematicNutrients.entrySet()) {
            String nutrient = entry.getKey();
            double currentRatio = entry.getValue();
            boolean needIncrease = currentRatio < minNutrientAchievementRate;
            
            // 根据食材对该营养素的贡献度排序，选择贡献最大的食材
            List<FoodGene> rankedGenes = rankGenesByNutrientContribution(genes, nutrient, foodContributions);
            
            if (!rankedGenes.isEmpty()) {
                // 选择前三个最相关的食材（如果有的话）
                int adjustCount = Math.min(3, rankedGenes.size());
                
                for (int i = 0; i < adjustCount; i++) {
                    FoodGene gene = rankedGenes.get(i);
                    
                    // 计算调整方向和幅度
                    double currentIntake = gene.getIntake();
                    double minIntake = gene.getFood().getRecommendedIntakeRange().getMinIntake();
                    double maxIntake = gene.getFood().getRecommendedIntakeRange().getMaxIntake();
                    
                    // 计算调整因子，基于达成率差距
                    double targetRatio = needIncrease ? minNutrientAchievementRate : maxNutrientAchievementRate;
                    double gap = Math.abs(currentRatio - targetRatio);
                    double adjustmentFactor = calculateAdjustmentFactor(gap);
                    
                    // 根据营养素贡献度缩放调整因子
                    double contribution = foodContributions.get(gene).getOrDefault(nutrient, 0.0);
                    adjustmentFactor *= Math.min(1.0, contribution * 2); // 增强高贡献食材的调整效果
                    
                    // 计算新的摄入量
                    double newIntake;
                    if (needIncrease) {
                        newIntake = currentIntake * (1 + adjustmentFactor);
                        newIntake = Math.min(maxIntake, newIntake); // 不超过最大推荐量
                    } else {
                        newIntake = currentIntake * (1 - adjustmentFactor);
                        newIntake = Math.max(minIntake, newIntake); // 不低于最小推荐量
                    }
                    
                    // 四舍五入到整数
                    newIntake = Math.round(newIntake);
                    
                    // 只有当调整幅度超过一定阈值时才执行调整
                    if (Math.abs(newIntake - currentIntake) >= 5) {
                        actions.add(new AdjustmentAction(
                            gene, 
                            needIncrease ? AdjustmentType.INCREASE : AdjustmentType.DECREASE,
                            newIntake,
                            nutrient
                        ));
                    }
                }
            }
        }
        
        // 对调整行动进行排序，优先处理影响大的营养素
        actions.sort((a1, a2) -> {
            // 优先级计算考虑营养素的重要性权重
            double weight1 = nutrientWeights.getOrDefault(a1.getTargetNutrient(), 1.0);
            double weight2 = nutrientWeights.getOrDefault(a2.getTargetNutrient(), 1.0);
            return Double.compare(weight2, weight1); // 降序
        });
        
        // 限制最大调整行动数量
        int maxActions = Math.min(3, actions.size());
        return actions.subList(0, maxActions);
    }
    
    /**
     * 根据对特定营养素的贡献度对食材进行排序
     * @param genes 食物基因列表
     * @param nutrient 目标营养素
     * @param foodContributions 食材营养素贡献度
     * @return 排序后的食物基因列表
     */
    private List<FoodGene> rankGenesByNutrientContribution(
            List<FoodGene> genes,
            String nutrient,
            Map<FoodGene, Map<String, Double>> foodContributions) {
        
        // 复制一份基因列表，避免修改原列表
        List<FoodGene> rankedGenes = new ArrayList<>(genes);
        
        // 按照食材对该营养素的贡献度降序排序
        rankedGenes.sort((g1, g2) -> {
            double contrib1 = foodContributions.get(g1).getOrDefault(nutrient, 0.0);
            double contrib2 = foodContributions.get(g2).getOrDefault(nutrient, 0.0);
            return Double.compare(contrib2, contrib1); // 降序排序
        });
        
        return rankedGenes;
    }
    
    /**
     * 根据达成率差距计算调整因子
     * @param gap 达成率与目标值的差距
     * @return 调整因子
     */
    private double calculateAdjustmentFactor(double gap) {
        // 非线性函数：差距越大，调整越强
        // 调整范围：0.05 (5%) 到 0.3 (30%)
        return Math.min(0.3, Math.max(0.05, gap * 0.5));
    }
    
    /**
     * 应用调整行动
     * @param solution 膳食方案
     * @param action 调整行动
     * @param requireStaple 是否需要保留主食
     * @return 是否成功应用
     */
    private boolean applyAdjustmentAction(MealSolution solution, AdjustmentAction action, boolean requireStaple) {
        FoodGene gene = action.getGene();
        
        // 如果是减少操作且涉及唯一主食，需要检查
        if (action.getType() == AdjustmentType.DECREASE && 
            requireStaple && 
            "staple".equals(gene.getFood().getCategory())) {
            
            boolean isOnlyStaple = solution.getFoodGenes().stream()
                    .filter(g -> "staple".equals(g.getFood().getCategory()))
                    .count() <= 1;
            
            if (isOnlyStaple) {
                // 不减少唯一的主食，改为添加新食材
                return mutateAddFood(solution);
            }
        }
        
        // 设置新的摄入量
        gene.setIntake(action.getNewIntake());
        return true;
    }
    
    /**
     * 调整类型枚举
     */
    private enum AdjustmentType {
        INCREASE,   // 增加摄入量
        DECREASE    // 减少摄入量
    }
    
    /**
     * 调整行动类，记录对特定食材的调整信息
     */
    private class AdjustmentAction {
        private FoodGene gene;              // 要调整的食材
        private AdjustmentType type;        // 调整类型
        private double newIntake;           // 调整后的摄入量
        private String targetNutrient;      // 目标调整的营养素
        
        public AdjustmentAction(FoodGene gene, AdjustmentType type, double newIntake, String targetNutrient) {
            this.gene = gene;
            this.type = type;
            this.newIntake = newIntake;
            this.targetNutrient = targetNutrient;
        }
        
        public FoodGene getGene() {
            return gene;
        }
        
        public AdjustmentType getType() {
            return type;
        }
        
        public double getNewIntake() {
            return newIntake;
        }
        
        public String getTargetNutrient() {
            return targetNutrient;
        }
    }
    
    /**
     * 设置营养素达成率范围
     * @param minRate 最低达成率（0-1之间）
     * @param maxRate 最高达成率（大于1）
     */
    public void setNutrientAchievementRateRange(double minRate, double maxRate) {
        if (minRate < 0 || minRate > 1) {
            throw new IllegalArgumentException("最低营养素达成率必须在0到1之间");
        }
        if (maxRate <= 1) {
            throw new IllegalArgumentException("最高营养素达成率必须大于1");
        }
        this.minNutrientAchievementRate = minRate;
        this.maxNutrientAchievementRate = maxRate;
    }
    
    /**
     * 设置某个营养素的权重
     * @param nutrient 营养素名称
     * @param weight 权重值
     */
    public void setNutrientWeight(String nutrient, double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("营养素权重不能为负数");
        }
        nutrientWeights.put(nutrient, weight);
    }
    
    /**
     * 批量设置营养素权重
     * @param weights 营养素权重映射
     */
    public void setNutrientWeights(Map<String, Double> weights) {
        nutrientWeights.clear();
        nutrientWeights.putAll(weights);
    }
    
    /**
     * 获取变异率
     * @return 变异率
     */
    public double getMutationRate() {
        return mutationRate;
    }
    
    /**
     * 设置变异率
     * @param mutationRate 变异率
     */
    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }
    
    /**
     * 获取变异类型
     * @return 变异类型
     */
    public MutationType getMutationType() {
        return mutationType;
    }
    
    /**
     * 设置变异类型
     * @param mutationType 变异类型
     */
    public void setMutationType(MutationType mutationType) {
        this.mutationType = mutationType;
    }
    
    /**
     * 获取摄入量变异强度
     * @return 摄入量变异强度
     */
    public double getIntakeMutationStrength() {
        return intakeMutationStrength;
    }
    
    /**
     * 设置摄入量变异强度
     * @param intakeMutationStrength 摄入量变异强度
     */
    public void setIntakeMutationStrength(double intakeMutationStrength) {
        this.intakeMutationStrength = intakeMutationStrength;
    }
    
    /**
     * 设置目标营养素，用于更精确地计算达成率
     * @param targetNutrients 目标营养素
     */
    public void setTargetNutrients(com.mealplanner.MealNutrients targetNutrients) {
        this.targetNutrients = targetNutrients;
    }
    
    /**
     * 获取当前设置的目标营养素
     * @return 目标营养素，如果未设置则返回null
     */
    public com.mealplanner.MealNutrients getTargetNutrients() {
        return this.targetNutrients;
    }
}