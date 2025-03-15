package com.mealplanner.genetic.operators;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.model.Food;
import com.mealplanner.model.FoodCategory;
import com.mealplanner.model.Nutrition;

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
    
    // 营养素达成率范围
    private double minNutrientAchievementRate = 0.8;
    private double maxNutrientAchievementRate = 1.2;
    
    // 不同营养素的达成率范围
    private Map<String, double[]> nutrientAchievementRates = new HashMap<>();
    
    // 营养素权重，用于敏感度分析，默认都是1.0
    private Map<String, Double> nutrientWeights = new HashMap<>();
    
    // 当前目标营养素（可选）
    private Nutrition targetNutrients;
    
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
     * 应用变异操作
     * @param solution 解决方案
     * @param requireStaple 是否需要主食
     * @param targetCalories 目标热量
     * @return 是否成功应用变异
     */
    public boolean apply(MealSolution solution, boolean requireStaple, double targetCalories) {
        if (solution == null) {
            return false;
        }
        
        Random random = new Random();
        boolean mutated = false;
        
        // 根据变异类型应用不同的变异策略
        switch (mutationType) {
            case INTAKE_ADJUSTMENT:
                if (random.nextDouble() < mutationRate) {
                    mutated = mutateIntake(solution);
                }
                break;
                
            case FOOD_REPLACEMENT:
                if (random.nextDouble() < mutationRate) {
                    mutated = mutateReplaceFood(solution, requireStaple);
                }
                break;
                
            case FOOD_ADDITION:
                if (random.nextDouble() < mutationRate) {
                    mutated = mutateAddFood(solution, requireStaple);
                }
                break;
                
            case FOOD_REMOVAL:
                if (random.nextDouble() < mutationRate) {
                    mutated = mutateRemoveFood(solution, requireStaple);
                }
                break;
                
            case CALORIES_OPTIMIZATION:
                if (random.nextDouble() < mutationRate) {
                    mutated = mutateOptimizeCalories(solution, targetCalories);
                }
                break;
                
            case NUTRIENT_SENSITIVITY:
                if (random.nextDouble() < mutationRate) {
                    mutated = mutateByNutrientSensitivity(solution, requireStaple);
                }
                break;
                
            case COMPREHENSIVE:
            default:
                // 综合变异：随机选择一种变异类型
                double r = random.nextDouble();
                if (r < mutationRate) {
                    int mutationChoice = random.nextInt(5);
                    switch (mutationChoice) {
                        case 0:
                            mutated = mutateIntake(solution);
                            break;
                        case 1:
                            mutated = mutateReplaceFood(solution, requireStaple);
                            break;
                        case 2:
                            mutated = mutateAddFood(solution, requireStaple);
                            break;
                        case 3:
                            mutated = mutateRemoveFood(solution, requireStaple);
                            break;
                        case 4:
                            mutated = mutateOptimizeCalories(solution, targetCalories);
                            break;
                    }
                }
                break;
        }
        
        // 确保解决方案有效
        if (mutated && !solution.isValid(requireStaple)) {
            // 变异导致解决方案无效，恢复到有效状态
            ensureValidSolution(solution, requireStaple);
        }
        
        return mutated;
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
        if (requireStaple && FoodCategory.STAPLE.equals(geneToReplace.getFood().getCategory())) {
            boolean isOnlyStaple = genes.stream()
                    .filter(g -> FoodCategory.STAPLE.equals(g.getFood().getCategory()))
                    .count() <= 1;
            
            if (isOnlyStaple) {
                // 如果是唯一的主食，尝试选择另一个非主食食物
                List<FoodGene> nonStapleGenes = genes.stream()
                        .filter(g -> !FoodCategory.STAPLE.equals(g.getFood().getCategory()))
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
        FoodCategory category = geneToReplace.getFood().getCategory();
        
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
     * 添加食物变异
     * @param solution 解决方案
     * @param requireStaple 是否需要主食
     * @return 是否成功变异
     */
    private boolean mutateAddFood(MealSolution solution, boolean requireStaple) {
        Random random = new Random();
        
        // 获取当前解决方案中的所有食物
        Set<String> existingFoodNames = solution.getFoodGenes().stream()
                .map(gene -> gene.getFood().getName())
                .collect(Collectors.toSet());
        
        // 获取未包含在解决方案中的候选食物
        List<Food> candidateFoods = foodDatabase.stream()
                .filter(food -> !existingFoodNames.contains(food.getName()))
                .collect(Collectors.toList());
        
        // 如果requireStaple为true，且当前已有主食，则排除所有主食
        if (requireStaple) {
            // 检查当前是否已有主食
            boolean hasStaple = solution.getFoodGenes().stream()
                    .anyMatch(gene -> FoodCategory.STAPLE.equals(gene.getFood().getCategory()));
            
            if (hasStaple) {
                // 已有主食，从候选食物中排除所有主食
                candidateFoods = candidateFoods.stream()
                        .filter(food -> !FoodCategory.STAPLE.equals(food.getCategory()))
                        .collect(Collectors.toList());
            } else {
                // 没有主食，只选择主食
                candidateFoods = candidateFoods.stream()
                        .filter(food -> FoodCategory.STAPLE.equals(food.getCategory()))
                        .collect(Collectors.toList());
            }
        }
        
        if (candidateFoods.isEmpty()) {
            return false; // 没有可添加的候选食物
        }
        
        // 随机选择一个食物添加到解决方案中
        Food selectedFood = candidateFoods.get(random.nextInt(candidateFoods.size()));
        
        // 随机生成一个在推荐范围内的摄入量
        double minIntake = selectedFood.getRecommendedIntakeRange().getMinIntake();
        double maxIntake = selectedFood.getRecommendedIntakeRange().getMaxIntake();
        double intake = minIntake + random.nextDouble() * (maxIntake - minIntake);
        
        // 将摄入量四舍五入为整数
        intake = Math.round(intake);
        
        // 添加新食物
        solution.addFood(new FoodGene(selectedFood, intake));
        
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
            if (requireStaple && FoodCategory.STAPLE.equals(gene.getFood().getCategory())) {
                boolean isOnlyStaple = genes.stream()
                        .filter(g -> FoodCategory.STAPLE.equals(g.getFood().getCategory()))
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
        double currentCalories = solution.calculateTotalNutrients().getCalories();
        
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
        // com.mealplanner.Nutrition currentNutrients = solution.calculateTotalNutrients();
        Map<String, Double> nutrientRatios = calculateNutrientAchievementRatios(solution);
        
        // 找出需要调整的营养素
        List<String> deficientNutrients = new ArrayList<>();
        List<String> excessiveNutrients = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : nutrientRatios.entrySet()) {
            String nutrient = entry.getKey();
            double ratio = entry.getValue();
            
            if (isDeficient(ratio, nutrient)) {
                deficientNutrients.add(nutrient);
            } else if (isExcessive(ratio, nutrient)) {
                excessiveNutrients.add(nutrient);
            }
        }
        
        if (deficientNutrients.isEmpty() && excessiveNutrients.isEmpty()) {
            // 所有营养素都在达成率范围内，随机选择一种变异
            Random random = new Random();
            int choice = random.nextInt(3);
            switch (choice) {
                case 0:
                    return mutateIntake(solution);
                case 1:
                    return mutateAddFood(solution, requireStaple);
                default:
                    return mutateReplaceFood(solution, requireStaple);
            }
        }
        
        // 计算食材对各营养素的贡献度
        Map<FoodGene, Map<String, Double>> foodNutrientContributions = calculateFoodNutrientContributions(solution);
        
        // 选择最适合调整的食材
        List<AdjustmentAction> adjustmentActions = determineAdjustmentActions(
            genes, deficientNutrients, foodNutrientContributions);
        
        if (adjustmentActions.isEmpty()) {
            // 如果找不到合适的调整行动，尝试添加新食物
            return mutateAddFood(solution, requireStaple);
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
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        
        // 使用已设置的目标营养素，或者估计值
        Nutrition targetsToUse = targetNutrients != null ? 
                                                  targetNutrients : 
                                                  estimateTargetNutrients(actualNutrients);
        
        Map<String, Double> ratios = new HashMap<>();
        
        // 计算主要营养素的达成率
        if (targetsToUse.getCalories() > 0) {
            ratios.put("calories", actualNutrients.getCalories() / targetsToUse.getCalories());
        }
        
        if (targetsToUse.getCarbohydrates() > 0) {
            ratios.put("carbohydrates", actualNutrients.getCarbohydrates() / targetsToUse.getCarbohydrates());
        }
        
        if (targetsToUse.getProtein() > 0) {
            ratios.put("protein", actualNutrients.getProtein() / targetsToUse.getProtein());
        }
        
        if (targetsToUse.getFat() > 0) {
            ratios.put("fat", actualNutrients.getFat() / targetsToUse.getFat());
        }
        
        // 计算微量元素的达成率
        if (targetsToUse.getCalcium() > 0) {
            ratios.put("calcium", actualNutrients.getCalcium() / targetsToUse.getCalcium());
        }
        
        if (targetsToUse.getPotassium() > 0) {
            ratios.put("potassium", actualNutrients.getPotassium() / targetsToUse.getPotassium());
        }
        
        if (targetsToUse.getSodium() > 0) {
            ratios.put("sodium", actualNutrients.getSodium() / targetsToUse.getSodium());
        }
        
        if (targetsToUse.getMagnesium() > 0) {
            ratios.put("magnesium", actualNutrients.getMagnesium() / targetsToUse.getMagnesium());
        }
        
        return ratios;
    }
    
    /**
     * 估计目标营养素（当无法直接获取时）
     * @param actualNutrients 当前营养素
     * @return 估计的目标营养素
     */
        private Nutrition estimateTargetNutrients(Nutrition actualNutrients) {
        // 这是一个简化的估计方法，实际情况应基于用户特征进行更复杂的计算
        // 假设当前营养素是目标的一个比例
        return new Nutrition(
            actualNutrients.getCalories() * 1.25,       // 假设目标热量是当前的1.25倍
            actualNutrients.getCarbohydrates() * 1.2,   // 碳水
            actualNutrients.getProtein() * 1.3,         // 蛋白质
            actualNutrients.getFat() * 1.15,            // 脂肪
            actualNutrients.getCalcium() * 1.4,         // 钙
            actualNutrients.getPotassium() * 1.5,       // 钾
            actualNutrients.getSodium() * 0.9,          // 钠（可能需要减少）
            actualNutrients.getMagnesium() * 1.4        // 镁
        );
    }
    
    /**
     * 确定营养素是否需要调整
     * @param ratio 当前达成率
     * @param nutrientName 营养素名称
     * @return 是否需要调整
     */
    private boolean needsAdjustment(double ratio, String nutrientName) {
        double[] range = getNutrientAchievementRate(nutrientName);
        return ratio < range[0] || ratio > range[1];
    }
    
    /**
     * 确定营养素是否不足
     * @param ratio 当前达成率
     * @param nutrientName 营养素名称
     * @return 是否不足
     */
    private boolean isDeficient(double ratio, String nutrientName) {
        double[] range = getNutrientAchievementRate(nutrientName);
        return ratio < range[0];
    }
    
    /**
     * 确定营养素是否过量
     * @param ratio 当前达成率
     * @param nutrientName 营养素名称
     * @return 是否过量
     */
    private boolean isExcessive(double ratio, String nutrientName) {
        double[] range = getNutrientAchievementRate(nutrientName);
        return ratio > range[1];
    }
    
    /**
     * 计算每种食材对各营养素的贡献度
     * @param solution 膳食方案
     * @return 食材-营养素贡献度映射
     */
    private Map<FoodGene, Map<String, Double>> calculateFoodNutrientContributions(MealSolution solution) {
        List<FoodGene> genes = solution.getFoodGenes();
        Nutrition totalNutrients = solution.calculateTotalNutrients();
        Map<FoodGene, Map<String, Double>> contributions = new HashMap<>();
        
        for (FoodGene gene : genes) {
            Map<String, Double> nutrientContributions = new HashMap<>();
            Food food = gene.getFood();
            double intake = gene.getIntake();
            
            // 计算该食材对各营养素的贡献比例
            // 热量贡献
            double caloriesContrib = (food.getNutrition().getCalories() * intake / 100) / totalNutrients.getCalories();
            nutrientContributions.put("calories", caloriesContrib);
            
            // 碳水贡献
            double carbsContrib = (food.getNutrition().getCarbohydrates() * intake / 100) / totalNutrients.getCarbohydrates();
            nutrientContributions.put("carbohydrates", carbsContrib);
            
            // 蛋白质贡献
            double proteinContrib = (food.getNutrition().getProtein() * intake / 100) / totalNutrients.getProtein();
            nutrientContributions.put("protein", proteinContrib);
            
            // 脂肪贡献
            double fatContrib = (food.getNutrition().getFat() * intake / 100) / totalNutrients.getFat();
            nutrientContributions.put("fat", fatContrib);
            
            // 钙贡献
            if (totalNutrients.getCalcium() > 0) {
                double calciumContrib = (food.getNutrition().getCalcium() * intake / 100) / totalNutrients.getCalcium();
                nutrientContributions.put("calcium", calciumContrib);
            }
            
            // 钾贡献
            if (totalNutrients.getPotassium() > 0) {
                double potassiumContrib = (food.getNutrition().getPotassium() * intake / 100) / totalNutrients.getPotassium();
                nutrientContributions.put("potassium", potassiumContrib);
            }
            
            // 钠贡献
            if (totalNutrients.getSodium() > 0) {
                double sodiumContrib = (food.getNutrition().getSodium() * intake / 100) / totalNutrients.getSodium();
                nutrientContributions.put("sodium", sodiumContrib);
            }
            
            // 镁贡献
            if (totalNutrients.getMagnesium() > 0) {
                double magnesiumContrib = (food.getNutrition().getMagnesium() * intake / 100) / totalNutrients.getMagnesium();
                nutrientContributions.put("magnesium", magnesiumContrib);
            }
            
            contributions.put(gene, nutrientContributions);
        }
        
        return contributions;
    }
    
    /**
     * 确定需要执行的调整行动
     * @param genes 食物基因列表
     * @param deficientNutrients 不足的营养素
     * @param foodContributions 食材营养素贡献度
     * @return 调整行动列表
     */
    private List<AdjustmentAction> determineAdjustmentActions(
            List<FoodGene> genes,
            List<String> deficientNutrients,
            Map<FoodGene, Map<String, Double>> foodContributions) {
        
        List<AdjustmentAction> actions = new ArrayList<>();
        
        // 对每个问题营养素，找出最适合调整的食材
        for (String nutrient : deficientNutrients) {
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
                    double targetRatio = minNutrientAchievementRate;
                    double gap = Math.abs(currentIntake - targetRatio);
                    double adjustmentFactor = calculateAdjustmentFactor(gap);
                    
                    // 根据营养素贡献度缩放调整因子
                    double contribution = foodContributions.get(gene).getOrDefault(nutrient, 0.0);
                    adjustmentFactor *= Math.min(1.0, contribution * 2); // 增强高贡献食材的调整效果
                    
                    // 计算新的摄入量
                    double newIntake;
                    if (currentIntake < targetRatio) {
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
     * @param requireStaple 是否要求包含主食
     * @return 是否成功应用
     */
    private boolean applyAdjustmentAction(MealSolution solution, AdjustmentAction action, boolean requireStaple) {
        FoodGene gene = action.getGene();
        double newIntake = action.getNewIntake();
        
        // 应用新的摄入量
        gene.setIntake(newIntake);
        
        // 检查调整后的解是否有效
        if (!solution.isValid(requireStaple)) {
            // 如果无效，恢复原来的摄入量
            return false;
        }
        
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
        private double newIntake;           // 调整后的摄入量
        private String targetNutrient;      // 目标调整的营养素
        
        public AdjustmentAction(FoodGene gene, double newIntake, String targetNutrient) {
            this.gene = gene;
            this.newIntake = newIntake;
            this.targetNutrient = targetNutrient;
        }
        
        public FoodGene getGene() {
            return gene;
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
     * @param minRate 最小达成率
     * @param maxRate 最大达成率
     */
    public void setNutrientAchievementRateRange(double minRate, double maxRate) {
        this.minNutrientAchievementRate = minRate;
        this.maxNutrientAchievementRate = maxRate;
    }
    
    /**
     * 设置不同营养素的达成率范围
     * @param nutrientRates 营养素达成率范围映射
     */
    public void setNutrientAchievementRates(Map<String, double[]> nutrientRates) {
        this.nutrientAchievementRates = nutrientRates;
    }
    
    /**
     * 获取特定营养素的达成率范围
     * @param nutrientName 营养素名称
     * @return 达成率范围数组 [最小达成率, 最大达成率]
     */
    public double[] getNutrientAchievementRate(String nutrientName) {
        return nutrientAchievementRates.getOrDefault(nutrientName, 
                new double[]{minNutrientAchievementRate, maxNutrientAchievementRate});
    }
    
    /**
     * 设置营养素权重
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
    public void setTargetNutrients(Nutrition targetNutrients) {
        this.targetNutrients = targetNutrients;
    }
    
    /**
     * 获取当前设置的目标营养素
     * @return 目标营养素，如果未设置则返回null
     */
    public Nutrition getTargetNutrients() {
        return this.targetNutrients;
    }
    
    /**
     * 确保解决方案有效
     * @param solution 解决方案
     * @param requireStaple 是否需要主食
     */
    private void ensureValidSolution(MealSolution solution, boolean requireStaple) {
        // 如果要求主食，确保有且只有一个主食
        if (requireStaple) {
            List<FoodGene> staples = solution.getFoodGenes().stream()
                    .filter(gene -> FoodCategory.STAPLE.equals(gene.getFood().getCategory()))
                    .collect(Collectors.toList());
            
            if (staples.isEmpty()) {
                // 没有主食，添加一个主食
                List<Food> stapleFoods = foodDatabase.stream()
                        .filter(food -> FoodCategory.STAPLE.equals(food.getCategory()))
                        .collect(Collectors.toList());
                
                if (!stapleFoods.isEmpty()) {
                    Random random = new Random();
                    Food staple = stapleFoods.get(random.nextInt(stapleFoods.size()));
                    
                    double minIntake = staple.getRecommendedIntakeRange().getMinIntake();
                    double maxIntake = staple.getRecommendedIntakeRange().getMaxIntake();
                    double intake = minIntake + random.nextDouble() * (maxIntake - minIntake);
                    intake = Math.round(intake);
                    
                    solution.addFood(new FoodGene(staple, intake));
                }
            } else if (staples.size() > 1) {
                // 有多个主食，只保留一个
                Random random = new Random();
                FoodGene keepStaple = staples.get(random.nextInt(staples.size()));
                
                for (FoodGene staple : staples) {
                    if (staple != keepStaple) {
                        // 找到该基因在解决方案中的索引并移除
                        int index = solution.getFoodGenes().indexOf(staple);
                        if (index >= 0) {
                            solution.removeFood(index);
                        }
                    }
                }
            }
        }
        
        // 移除重复食物
        Set<String> uniqueFoods = new HashSet<>();
        List<Integer> duplicateIndices = new ArrayList<>();
        
        for (int i = 0; i < solution.getFoodGenes().size(); i++) {
            FoodGene gene = solution.getFoodGenes().get(i);
            if (!uniqueFoods.add(gene.getFood().getName())) {
                duplicateIndices.add(i);
            }
        }
        
        // 从后向前移除重复食物（避免索引变化）
        for (int i = duplicateIndices.size() - 1; i >= 0; i--) {
            solution.removeFood(duplicateIndices.get(i));
        }
    }
}