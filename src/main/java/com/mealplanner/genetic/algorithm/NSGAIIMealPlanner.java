package com.mealplanner.genetic.algorithm;

import com.mealplanner.Food;
import com.mealplanner.MealNutrients;
import com.mealplanner.UserProfile;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.objectives.ObjectiveEvaluator;
import com.mealplanner.genetic.operators.MealCrossover;
import com.mealplanner.genetic.operators.MealMutation;
import com.mealplanner.genetic.operators.MealSelection;
import com.mealplanner.genetic.util.NSGAIIConfiguration;
import com.mealplanner.genetic.util.NSGAIILogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * NSGA-II多目标遗传算法膳食规划器
 * 实现基于非支配排序和拥挤距离的多目标优化
 */
public class NSGAIIMealPlanner {
    private NSGAIIConfiguration config;
    private List<Food> foodDatabase;
    private UserProfile userProfile;
    private ObjectiveEvaluator objectiveEvaluator;
    private MealCrossover crossover;
    private MealMutation mutation;
    private MealSelection selection;
    private NSGAIILogger logger;
    
    // 最低营养素达成率阈值，默认为0.7（70%）
    private double minNutrientAchievementRate = 0.8;
    
    // 最高营养素达成率阈值，默认为1.3（130%）
    private double maxNutrientAchievementRate = 1.1;
    
    // 目标营养素，用于计算达成率
    private MealNutrients targetNutrients;
    
    /**
     * 构造函数
     * @param config 算法配置
     * @param foodDatabase 食物数据库
     * @param userProfile 用户配置文件
     */
    public NSGAIIMealPlanner(NSGAIIConfiguration config, List<Food> foodDatabase, UserProfile userProfile) {
        this.config = config;
        this.foodDatabase = foodDatabase;
        this.userProfile = userProfile;
        this.objectiveEvaluator = new ObjectiveEvaluator(userProfile);
        this.crossover = new MealCrossover(config.getCrossoverRate());
        this.mutation = new MealMutation(config.getMutationRate(), foodDatabase);
        this.selection = new MealSelection();
        this.logger = new NSGAIILogger();
    }
    
    /**
     * 构造函数（带营养素达成率范围）
     * @param config 算法配置
     * @param foodDatabase 食物数据库
     * @param userProfile 用户配置文件
     * @param minNutrientAchievementRate 最低营养素达成率（0-1之间）
     * @param maxNutrientAchievementRate 最高营养素达成率（大于1）
     */
    public NSGAIIMealPlanner(NSGAIIConfiguration config, List<Food> foodDatabase, UserProfile userProfile,
                           double minNutrientAchievementRate, double maxNutrientAchievementRate) {
        this(config, foodDatabase, userProfile);
        this.minNutrientAchievementRate = minNutrientAchievementRate;
        this.maxNutrientAchievementRate = maxNutrientAchievementRate;
    }
    
    /**
     * 生成一餐的膳食方案
     * @param targetNutrients 目标营养素需求
     * @param requireStaple 是否要求包含主食
     * @return 最优的膳食方案列表（帕累托前沿）
     */
    public List<MealSolution> generateMeal(MealNutrients targetNutrients, boolean requireStaple) {
        // 保存目标营养素，用于计算达成率
        this.targetNutrients = targetNutrients;
        
        // 设置变异器的营养素达成率范围，确保一致性
        mutation.setNutrientAchievementRateRange(minNutrientAchievementRate, maxNutrientAchievementRate);
        
        // 将目标营养素传递给变异器，以便精确计算营养素达成率
        mutation.setTargetNutrients(targetNutrients);
        
        // 优先使用营养素敏感度分析变异策略
        mutation.setMutationType(com.mealplanner.genetic.operators.MealMutation.MutationType.NUTRIENT_SENSITIVITY);
        
        // 设置营养素权重，让主要营养素（热量、蛋白质等）有更高的优先级
        Map<String, Double> nutrientWeights = new HashMap<>();
        nutrientWeights.put("calories", 2.0);    // 热量权重高
        nutrientWeights.put("protein", 1.8);     // 蛋白质权重高
        nutrientWeights.put("carbohydrates", 1.5); // 碳水权重中高
        nutrientWeights.put("fat", 1.5);         // 脂肪权重中高
        // 微量营养素权重正常
        nutrientWeights.put("calcium", 1.2);
        nutrientWeights.put("potassium", 1.2);
        nutrientWeights.put("sodium", 1.0);
        nutrientWeights.put("magnesium", 1.0);
        
        mutation.setNutrientWeights(nutrientWeights);
        
        logger.startAlgorithm(config);
        
        // 初始化种群
        Population population = initializePopulation(targetNutrients, requireStaple);
        
        // 评估初始种群的目标值
        evaluatePopulation(population, targetNutrients);
        
        // 对初始种群进行非支配排序和拥挤度计算
        NonDominatedSorting.sort(population);
        CrowdingDistanceCalculator.calculate(population);
        
        logger.logInitialPopulation(population);
        
        // 主循环：进化过程
        for (int generation = 0; generation < config.getMaxGenerations(); generation++) {
            logger.startGeneration(generation);
            
            // 1. 创建子代种群
            Population offspringPopulation = createOffspringPopulation(population, targetNutrients, requireStaple);
            
            // 2. 合并父代和子代
            Population combinedPopulation = Population.merge(population, offspringPopulation);
            
            // 3. 对合并后的种群进行非支配排序
            NonDominatedSorting.sort(combinedPopulation);
            
            // 4. 计算拥挤度
            CrowdingDistanceCalculator.calculate(combinedPopulation);
            
            // 5. 选择下一代种群
            population = selectNextGeneration(combinedPopulation);
            
            // 记录当前代的信息
            logGenerationInfo(generation, population);
            
            // 检查是否满足提前终止条件
            if (checkTerminationCriteria(population, generation)) {
                logger.logEarlyTermination(generation);
                break;
            }
        }
        
        // 获取帕累托最优前沿
        List<MealSolution> paretoFront = getParetoFront(population);
        logger.logFinalSolutions(paretoFront);
        
        return paretoFront;
    }
    
    /**
     * 初始化种群
     */
    private Population initializePopulation(MealNutrients targetNutrients, boolean requireStaple) {
        List<MealSolution> solutions = new ArrayList<>();
        
        for (int i = 0; i < config.getPopulationSize(); i++) {
            MealSolution solution = MealSolution.createRandom(
                    foodDatabase, 
                    config.getMinFoodsPerMeal(), 
                    config.getMaxFoodsPerMeal(),
                    requireStaple
            );
            solutions.add(solution);
        }
        
        return new Population(solutions);
    }
    
    /**
     * 评估种群中所有解决方案的目标值
     */
    private void evaluatePopulation(Population population, MealNutrients targetNutrients) {
        for (MealSolution solution : population.getSolutions()) {
            List<ObjectiveValue> objectiveValues = objectiveEvaluator.evaluate(solution, targetNutrients);
            solution.setObjectiveValues(objectiveValues);
        }
    }
    
    /**
     * 创建子代种群
     */
    private Population createOffspringPopulation(Population parentPopulation, MealNutrients targetNutrients, boolean requireStaple) {
        List<MealSolution> offspring = new ArrayList<>();
        
        while (offspring.size() < config.getPopulationSize()) {
            // 基于锦标赛选择父代
            List<MealSolution> parents = selection.select(parentPopulation, 2);
            
            // 应用交叉
            List<MealSolution> children = crossover.apply(parents.get(0), parents.get(1));
            
            // 应用变异
            for (MealSolution child : children) {
                // 使用目标热量进行变异
                mutation.apply(child, requireStaple, targetNutrients.calories);
                
                // 确保解的有效性
                while (!child.isValid(requireStaple)) {
                    child = MealSolution.createRandom(
                            foodDatabase,
                            config.getMinFoodsPerMeal(),
                            config.getMaxFoodsPerMeal(),
                            requireStaple
                    );
                }
                
                // 评估子代的目标值
                List<ObjectiveValue> objectiveValues = objectiveEvaluator.evaluate(child, targetNutrients);
                child.setObjectiveValues(objectiveValues);
                
                offspring.add(child);
                
                if (offspring.size() >= config.getPopulationSize()) {
                    break;
                }
            }
        }
        
        return new Population(offspring);
    }
    
    /**
     * 选择下一代种群
     */
    private Population selectNextGeneration(Population combinedPopulation) {
        List<MealSolution> nextGeneration = new ArrayList<>();
        int currentRank = 1;
        
        // 按非支配排名逐层添加解决方案
        while (nextGeneration.size() < config.getPopulationSize()) {
            // 创建currentRank的final副本
            final int rankForFilter = currentRank;
            
            // 使用副本变量在lambda表达式中
            List<MealSolution> currentFront = combinedPopulation.getSolutions().stream()
                    .filter(s -> s.getRank() == rankForFilter)
                    .collect(Collectors.toList());
            
            if (currentFront.isEmpty()) {
                break; // 没有更多的层级
            }
            
            // 如果添加当前层级的所有解决方案会超出种群大小
            if (nextGeneration.size() + currentFront.size() > config.getPopulationSize()) {
                // 按拥挤度降序排序
                currentFront.sort(Comparator.comparing(MealSolution::getCrowdingDistance).reversed());
                
                // 只添加所需数量的解决方案
                int remainingSlots = config.getPopulationSize() - nextGeneration.size();
                nextGeneration.addAll(currentFront.subList(0, remainingSlots));
            } else {
                // 添加当前层级的所有解决方案
                nextGeneration.addAll(currentFront);
            }
            
            currentRank++;
        }
        
        return new Population(nextGeneration);
    }
    
    /**
     * 检查终止条件
     */
    private boolean checkTerminationCriteria(Population population, int currentGeneration) {
        // 1. 检查是否达到最大代数
        if (currentGeneration >= config.getMaxGenerations() - 1) {
            return true;
        }
        
        // 2. 检查是否找到足够好的解决方案
        // 如果第一层非支配解的数量足够多，并且目标值足够好，可以提前结束
        List<MealSolution> firstFront = population.getSolutions().stream()
                .filter(s -> s.getRank() == 1)
                .collect(Collectors.toList());
        
        if (firstFront.size() >= config.getMinParetoSolutions()) {
            boolean allGoodEnough = firstFront.stream()
                    .allMatch(s -> objectiveEvaluator.isSolutionGoodEnough(s));
            
            if (allGoodEnough) {
                return true;
            }
        }
        
        // 3. 检查种群多样性是否降低
        // 这里可以添加更复杂的终止条件
        
        return false;
    }
    
    /**
     * 获取帕累托最优前沿
     */
    private List<MealSolution> getParetoFront(Population population) {
        // 先获取非支配排名为1的解决方案
        List<MealSolution> allParetoFront = population.getSolutions().stream()
                .filter(s -> s.getRank() == 1)
                .collect(Collectors.toList());
        
        logger.info("原始帕累托前沿解决方案数量: " + allParetoFront.size());
        
        // 筛选出满足所有营养素最低达成率要求的解决方案
        List<MealSolution> filteredSolutions = allParetoFront.stream()
                .filter(solution -> checkAllNutrientsAchievement(solution, targetNutrients))
                .collect(Collectors.toList());
        
        logger.info("筛选后的解决方案数量: " + filteredSolutions.size());
        
        // 如果没有解决方案满足要求，则放宽条件，选择最接近要求的解决方案
        if (filteredSolutions.isEmpty()) {
            logger.warning("没有解决方案满足所有营养素达成率 >= " + minNutrientAchievementRate + " 的要求");
            
            // 找出平均营养素达成率最高的解决方案
            Optional<MealSolution> bestSolution = allParetoFront.stream()
                    .max(Comparator.comparing(solution -> 
                        calculateAverageNutrientAchievement(solution, targetNutrients)));
            
            if (bestSolution.isPresent()) {
                logger.info("选择平均营养素达成率最高的解决方案作为替代");
                filteredSolutions.add(bestSolution.get());
            }
        }
        
        return filteredSolutions;
    }
    
    /**
     * 检查解决方案是否满足所有营养素的达成率要求
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 是否满足要求
     */
    private boolean checkAllNutrientsAchievement(MealSolution solution, MealNutrients targetNutrients) {
        MealNutrients actualNutrients = solution.calculateTotalNutrients();
        
        // 检查主要营养素（热量、碳水、蛋白质、脂肪）
        if (targetNutrients.calories > 0) {
            double caloriesRatio = actualNutrients.calories / targetNutrients.calories;
            if (caloriesRatio < minNutrientAchievementRate || caloriesRatio > maxNutrientAchievementRate) {
                return false;
            }
        }
        
        if (targetNutrients.carbohydrates > 0) {
            double carbsRatio = actualNutrients.carbohydrates / targetNutrients.carbohydrates;
            if (carbsRatio < minNutrientAchievementRate || carbsRatio > maxNutrientAchievementRate) {
                return false;
            }
        }
        
        if (targetNutrients.protein > 0) {
            double proteinRatio = actualNutrients.protein / targetNutrients.protein;
            if (proteinRatio < minNutrientAchievementRate || proteinRatio > maxNutrientAchievementRate) {
                return false;
            }
        }
        
        if (targetNutrients.fat > 0) {
            double fatRatio = actualNutrients.fat / targetNutrients.fat;
            if (fatRatio < minNutrientAchievementRate || fatRatio > maxNutrientAchievementRate) {
                return false;
            }
        }
        
        // 微量元素可以选择性检查，这里也一并检查
        if (targetNutrients.calcium > 0) {
            double calciumRatio = actualNutrients.calcium / targetNutrients.calcium;
            if (calciumRatio < minNutrientAchievementRate || calciumRatio > maxNutrientAchievementRate) {
                return false;
            }
        }
        
        if (targetNutrients.potassium > 0) {
            double potassiumRatio = actualNutrients.potassium / targetNutrients.potassium;
            if (potassiumRatio < minNutrientAchievementRate || potassiumRatio > maxNutrientAchievementRate) {
                return false;
            }
        }
        
        if (targetNutrients.sodium > 0) {
            double sodiumRatio = actualNutrients.sodium / targetNutrients.sodium;
            if (sodiumRatio < minNutrientAchievementRate || sodiumRatio > maxNutrientAchievementRate) {
                return false;
            }
        }
        
        if (targetNutrients.magnesium > 0) {
            double magnesiumRatio = actualNutrients.magnesium / targetNutrients.magnesium;
            if (magnesiumRatio < minNutrientAchievementRate || magnesiumRatio > maxNutrientAchievementRate) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 计算解决方案的平均营养素达成率，同时考虑过高和过低的偏差
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 平均营养素达成率得分
     */
    private double calculateAverageNutrientAchievement(MealSolution solution, MealNutrients targetNutrients) {
        MealNutrients actualNutrients = solution.calculateTotalNutrients();
        
        double[] ratios = new double[8];
        double[] scores = new double[8];
        int count = 0;
        
        // 计算各营养素的达成率
        if (targetNutrients.calories > 0) {
            ratios[count] = actualNutrients.calories / targetNutrients.calories;
            count++;
        }
        
        if (targetNutrients.carbohydrates > 0) {
            ratios[count] = actualNutrients.carbohydrates / targetNutrients.carbohydrates;
            count++;
        }
        
        if (targetNutrients.protein > 0) {
            ratios[count] = actualNutrients.protein / targetNutrients.protein;
            count++;
        }
        
        if (targetNutrients.fat > 0) {
            ratios[count] = actualNutrients.fat / targetNutrients.fat;
            count++;
        }
        
        if (targetNutrients.calcium > 0) {
            ratios[count] = actualNutrients.calcium / targetNutrients.calcium;
            count++;
        }
        
        if (targetNutrients.potassium > 0) {
            ratios[count] = actualNutrients.potassium / targetNutrients.potassium;
            count++;
        }
        
        if (targetNutrients.sodium > 0) {
            ratios[count] = actualNutrients.sodium / targetNutrients.sodium;
            count++;
        }
        
        if (targetNutrients.magnesium > 0) {
            ratios[count] = actualNutrients.magnesium / targetNutrients.magnesium;
            count++;
        }
        
        // 计算每个营养素的得分
        // 当达成率在[minRate, maxRate]范围内时，得分为1.0
        // 当达成率超出范围时，得分根据偏离程度降低
        for (int i = 0; i < count; i++) {
            if (ratios[i] >= minNutrientAchievementRate && ratios[i] <= maxNutrientAchievementRate) {
                scores[i] = 1.0;
            } else if (ratios[i] < minNutrientAchievementRate) {
                // 不足情况下，得分线性降低，0时得分为0
                scores[i] = ratios[i] / minNutrientAchievementRate;
            } else {
                // 过量情况下，得分指数降低，防止过度超标
                double excess = ratios[i] - maxNutrientAchievementRate;
                scores[i] = Math.exp(-excess);
            }
        }
        
        // 计算平均得分
        double totalScore = 0;
        for (int i = 0; i < count; i++) {
            totalScore += scores[i];
        }
        
        return count > 0 ? totalScore / count : 0;
    }
    
    /**
     * 记录当前代的信息
     */
    private void logGenerationInfo(int generation, Population population) {
        // 计算并记录当前代的统计信息
        List<MealSolution> firstFront = population.getSolutions().stream()
                .filter(s -> s.getRank() == 1)
                .collect(Collectors.toList());
        
        logger.logGeneration(generation, population, firstFront);
    }
    
    /**
     * 获取算法配置
     */
    public NSGAIIConfiguration getConfig() {
        return config;
    }
    
    /**
     * 设置算法配置
     */
    public void setConfig(NSGAIIConfiguration config) {
        this.config = config;
    }
    
    /**
     * 获取最低营养素达成率
     * @return 最低营养素达成率
     */
    public double getMinNutrientAchievementRate() {
        return minNutrientAchievementRate;
    }
    
    /**
     * 设置最低营养素达成率
     * @param minNutrientAchievementRate 最低营养素达成率（0-1之间）
     */
    public void setMinNutrientAchievementRate(double minNutrientAchievementRate) {
        if (minNutrientAchievementRate < 0 || minNutrientAchievementRate > 1) {
            throw new IllegalArgumentException("最低营养素达成率必须在0到1之间");
        }
        this.minNutrientAchievementRate = minNutrientAchievementRate;
    }
    
    /**
     * 获取最高营养素达成率
     * @return 最高营养素达成率
     */
    public double getMaxNutrientAchievementRate() {
        return maxNutrientAchievementRate;
    }
    
    /**
     * 设置最高营养素达成率
     * @param maxNutrientAchievementRate 最高营养素达成率（大于1）
     */
    public void setMaxNutrientAchievementRate(double maxNutrientAchievementRate) {
        if (maxNutrientAchievementRate <= 1) {
            throw new IllegalArgumentException("最高营养素达成率必须大于1");
        }
        this.maxNutrientAchievementRate = maxNutrientAchievementRate;
    }
} 