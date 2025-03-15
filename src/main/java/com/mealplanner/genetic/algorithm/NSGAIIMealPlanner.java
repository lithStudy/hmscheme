package com.mealplanner.genetic.algorithm;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.genetic.objectives.MultiObjectiveEvaluator;
import com.mealplanner.genetic.operators.MealCrossover;
import com.mealplanner.genetic.operators.MealMutation;
import com.mealplanner.genetic.operators.MealSelection;
import com.mealplanner.genetic.util.NSGAIIConfiguration;
import com.mealplanner.genetic.util.NSGAIILogger;
import com.mealplanner.model.Food;
import com.mealplanner.model.Nutrition;
import com.mealplanner.model.UserProfile;
import com.mealplanner.export.MealSolutionExcelExporter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.File;

/**
 * NSGA-II多目标遗传算法膳食规划器
 * 实现基于非支配排序和拥挤距离的多目标优化
 */
public class NSGAIIMealPlanner {
    // 算法配置参数
    private NSGAIIConfiguration config;
    // 食物数据库
    private List<Food> foodDatabase;
    // 用户个人信息
    private UserProfile userProfile;
    // 目标评估器,用于评估解决方案的各项目标值
    private MultiObjectiveEvaluator objectiveEvaluator;
    // 交叉算子,用于生成新的解决方案
    private MealCrossover crossover;
    // 变异算子,用于增加解的多样性
    private MealMutation mutation;
    // 选择算子,用于选择优秀个体
    private MealSelection selection;
    // 日志记录器,用于记录算法运行过程
    private NSGAIILogger logger;
    
    // 不同营养素的达成率范围映射
    private Map<String, double[]> nutrientAchievementRates = new HashMap<>();
    
    // 目标营养素，用于计算达成率
    private Nutrition targetNutrients;
    
    /**
     * 构造函数
     * @param config 算法配置
     * @param foodDatabase 食物数据库
     * @param userProfile 用户配置文件
     */
    public NSGAIIMealPlanner(NSGAIIConfiguration config, List<Food> foodDatabase, UserProfile userProfile, Map<String, double[]> nutrientAchievementRates) {
        this.config = config;
        this.foodDatabase = foodDatabase;
        this.userProfile = userProfile;
        this.objectiveEvaluator = new MultiObjectiveEvaluator(userProfile);
        this.crossover = new MealCrossover(config.getCrossoverRate());
        this.mutation = new MealMutation(config.getMutationRate(), foodDatabase);
        this.selection = new MealSelection();
        this.logger = new NSGAIILogger();
        this.nutrientAchievementRates = nutrientAchievementRates;
    }
    
    
    /**
     * 生成一餐的膳食方案
     * @param targetNutrients 目标营养素需求
     * @param requireStaple 是否要求包含主食
     * @return 最优的膳食方案列表（帕累托前沿）
     */
    public List<MealSolution> generateMeal(Nutrition targetNutrients, boolean requireStaple) {
        // 保存目标营养素，用于计算达成率
        this.targetNutrients = targetNutrients;
        
        // // 设置变异器的营养素达成率范围，确保一致性
        // mutation.setNutrientAchievementRateRange(minNutrientAchievementRate, maxNutrientAchievementRate);
        
        // 设置变异器的营养素达成率范围映射
        mutation.setNutrientAchievementRates(nutrientAchievementRates);
        
        // 将目标营养素传递给变异器，以便精确计算营养素达成率
        mutation.setTargetNutrients(targetNutrients);
        
        // 优先使用营养素敏感度分析变异策略
        mutation.setMutationType(com.mealplanner.genetic.operators.MealMutation.MutationType.NUTRIENT_SENSITIVITY);
        
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
    private Population initializePopulation(Nutrition targetNutrients, boolean requireStaple) {
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
    private void evaluatePopulation(Population population, Nutrition targetNutrients) {
        for (MealSolution solution : population.getSolutions()) {
            List<ObjectiveValue> objectiveValues = objectiveEvaluator.evaluate(solution, targetNutrients);
            solution.setObjectiveValues(objectiveValues);
        }
    }
    
    /**
     * 创建子代种群
     */
    private Population createOffspringPopulation(Population parentPopulation, Nutrition targetNutrients, boolean requireStaple) {
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
        
        // 如果没有解决方案满足要求，则放宽条件，选择营养素偏离度最低的三个解决方案
        if (filteredSolutions.isEmpty()) {
            logger.warning("没有解决方案满足所有营养素达成率的要求");
            
            // 按照热量和主要营养素（碳水、蛋白质、脂肪）偏离度升序排序所有解决方案（偏离度越低越好）
            List<MealSolution> sortedSolutions = allParetoFront.stream()
                    .sorted(Comparator.comparing(
                        solution -> calculateMainNutrientsDeviationScore(solution, targetNutrients)))
                    .collect(Collectors.toList());
            
            // 选择前三个解决方案（如果有的话）
            int topCount = Math.min(3, sortedSolutions.size());
            if (topCount > 0) {
                logger.info("选择热量和主要营养素偏离度最低的" + topCount + "个解决方案作为替代");
                filteredSolutions.addAll(sortedSolutions.subList(0, topCount));
                
                // 记录每个选中方案的营养素偏离度和平均达成率
                for (int i = 0; i < topCount; i++) {
                    MealSolution solution = sortedSolutions.get(i);
                    double mainDeviationScore = calculateMainNutrientsDeviationScore(solution, targetNutrients);
                    double overallDeviationScore = calculateNutrientDeviationScore(solution, targetNutrients);
                    double achievementScore = calculateAverageNutrientAchievement(solution, targetNutrients);
                    logger.info("替代方案 #" + (i+1) + 
                               " 热量及主要营养素偏离度: " + String.format("%.4f", mainDeviationScore) +
                               ", 整体营养素偏离度: " + String.format("%.4f", overallDeviationScore) + 
                               ", 平均营养素达成率: " + String.format("%.2f", achievementScore));
                }
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
    private boolean checkAllNutrientsAchievement(MealSolution solution, Nutrition targetNutrients) {
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        
        // 检查主要营养素（热量、碳水、蛋白质、脂肪）
        if (targetNutrients.calories > 0) {
            double caloriesRatio = actualNutrients.calories / targetNutrients.calories;
            double[] caloriesRange = getNutrientAchievementRate("calories");
            if (caloriesRatio < caloriesRange[0] || caloriesRatio > caloriesRange[1]) {
                return false;
            }
        }
        
        if (targetNutrients.carbohydrates > 0) {
            double carbsRatio = actualNutrients.carbohydrates / targetNutrients.carbohydrates;
            double[] carbsRange = getNutrientAchievementRate("carbohydrates");
            if (carbsRatio < carbsRange[0] || carbsRatio > carbsRange[1]) {
                return false;
            }
        }
        
        if (targetNutrients.protein > 0) {
            double proteinRatio = actualNutrients.protein / targetNutrients.protein;
            double[] proteinRange = getNutrientAchievementRate("protein");
            if (proteinRatio < proteinRange[0] || proteinRatio > proteinRange[1]) {
                return false;
            }
        }
        
        if (targetNutrients.fat > 0) {
            double fatRatio = actualNutrients.fat / targetNutrients.fat;
            double[] fatRange = getNutrientAchievementRate("fat");
            if (fatRatio < fatRange[0] || fatRatio > fatRange[1]) {
                return false;
            }
        }
        
        // 微量元素可以选择性检查，这里也一并检查
        if (targetNutrients.calcium > 0) {
            double calciumRatio = actualNutrients.calcium / targetNutrients.calcium;
            double[] calciumRange = getNutrientAchievementRate("calcium");
            if (calciumRatio < calciumRange[0] || calciumRatio > calciumRange[1]) {
                return false;
            }
        }
        
        if (targetNutrients.potassium > 0) {
            double potassiumRatio = actualNutrients.potassium / targetNutrients.potassium;
            double[] potassiumRange = getNutrientAchievementRate("potassium");
            if (potassiumRatio < potassiumRange[0] || potassiumRatio > potassiumRange[1]) {
                return false;
            }
        }
        
        if (targetNutrients.sodium > 0) {
            double sodiumRatio = actualNutrients.sodium / targetNutrients.sodium;
            double[] sodiumRange = getNutrientAchievementRate("sodium");
            if (sodiumRatio < sodiumRange[0] || sodiumRatio > sodiumRange[1]) {
                return false;
            }
        }
        
        if (targetNutrients.magnesium > 0) {
            double magnesiumRatio = actualNutrients.magnesium / targetNutrients.magnesium;
            double[] magnesiumRange = getNutrientAchievementRate("magnesium");
            if (magnesiumRatio < magnesiumRange[0] || magnesiumRatio > magnesiumRange[1]) {
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
    private double calculateAverageNutrientAchievement(MealSolution solution, Nutrition targetNutrients) {
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        
        // 使用Map存储各营养素的达成率和得分，而不是数组
        Map<String, Double> nutrientRatios = new HashMap<>();
        Map<String, Double> nutrientScores = new HashMap<>();
        
        // 计算各营养素的达成率
        if (targetNutrients.calories > 0) {
            double ratio = actualNutrients.calories / targetNutrients.calories;
            nutrientRatios.put("calories", ratio);
        }
        
        if (targetNutrients.carbohydrates > 0) {
            double ratio = actualNutrients.carbohydrates / targetNutrients.carbohydrates;
            nutrientRatios.put("carbohydrates", ratio);
        }
        
        if (targetNutrients.protein > 0) {
            double ratio = actualNutrients.protein / targetNutrients.protein;
            nutrientRatios.put("protein", ratio);
        }
        
        if (targetNutrients.fat > 0) {
            double ratio = actualNutrients.fat / targetNutrients.fat;
            nutrientRatios.put("fat", ratio);
        }
        
        if (targetNutrients.calcium > 0) {
            double ratio = actualNutrients.calcium / targetNutrients.calcium;
            nutrientRatios.put("calcium", ratio);
        }
        
        if (targetNutrients.potassium > 0) {
            double ratio = actualNutrients.potassium / targetNutrients.potassium;
            nutrientRatios.put("potassium", ratio);
        }
        
        if (targetNutrients.sodium > 0) {
            double ratio = actualNutrients.sodium / targetNutrients.sodium;
            nutrientRatios.put("sodium", ratio);
        }
        
        if (targetNutrients.magnesium > 0) {
            double ratio = actualNutrients.magnesium / targetNutrients.magnesium;
            nutrientRatios.put("magnesium", ratio);
        }
        
        // 计算每个营养素的得分
        // 当达成率在[minRate, maxRate]范围内时，得分为1.0
        // 当达成率超出范围时，得分根据偏离程度降低
        for (Map.Entry<String, Double> entry : nutrientRatios.entrySet()) {
            String nutrientName = entry.getKey();
            double ratio = entry.getValue();
            double[] range = getNutrientAchievementRate(nutrientName);
            double minRate = range[0];
            double maxRate = range[1];
            
            double score;
            if (ratio >= minRate && ratio <= maxRate) {
                score = 1.0;
            } else if (ratio < minRate) {
                score = ratio / minRate;
            } else {
                score = maxRate / ratio;
            }
            
            nutrientScores.put(nutrientName, score);
        }
        
        // 计算平均得分
        double totalScore = 0;
        for (double score : nutrientScores.values()) {
            totalScore += score;
        }
        
        return nutrientScores.isEmpty() ? 0 : totalScore / nutrientScores.size();
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
     * 获取特定营养素的达成率范围
     * @param nutrientName 营养素名称
     * @return 达成率范围数组 [最小达成率, 最大达成率]，如果未找到则返回默认范围
     */
    public double[] getNutrientAchievementRate(String nutrientName) {
        return nutrientAchievementRates.getOrDefault(nutrientName, 
                new double[]{0.9, 1.1}); // 使用NutrientObjectiveConfig中的默认值
    }
    
    /**
     * 计算解决方案的平均营养素达成率得分
     * 此方法可供外部调用，用于展示解决方案的营养素达成情况
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 平均营养素达成率得分（0-1之间）
     */
    public double calculateSolutionNutrientScore(MealSolution solution, Nutrition targetNutrients) {
        return calculateAverageNutrientAchievement(solution, targetNutrients);
    }
    
    /**
     * 计算解决方案的营养素偏离得分
     * 偏离得分越低表示解决方案越接近理想的营养素达成率范围
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 营养素偏离得分（越低越好）
     */
    private double calculateNutrientDeviationScore(MealSolution solution, Nutrition targetNutrients) {
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        Map<String, Double> nutrientDeviations = new HashMap<>();
        
        // 计算各营养素的偏离度
        if (targetNutrients.calories > 0) {
            double ratio = actualNutrients.calories / targetNutrients.calories;
            double[] range = getNutrientAchievementRate("calories");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]);
            nutrientDeviations.put("calories", deviation);
        }
        
        if (targetNutrients.carbohydrates > 0) {
            double ratio = actualNutrients.carbohydrates / targetNutrients.carbohydrates;
            double[] range = getNutrientAchievementRate("carbohydrates");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]);
            nutrientDeviations.put("carbohydrates", deviation);
        }
        
        if (targetNutrients.protein > 0) {
            double ratio = actualNutrients.protein / targetNutrients.protein;
            double[] range = getNutrientAchievementRate("protein");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]);
            nutrientDeviations.put("protein", deviation);
        }
        
        if (targetNutrients.fat > 0) {
            double ratio = actualNutrients.fat / targetNutrients.fat;
            double[] range = getNutrientAchievementRate("fat");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]);
            nutrientDeviations.put("fat", deviation);
        }
        
        if (targetNutrients.calcium > 0) {
            double ratio = actualNutrients.calcium / targetNutrients.calcium;
            double[] range = getNutrientAchievementRate("calcium");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]);
            nutrientDeviations.put("calcium", deviation);
        }
        
        if (targetNutrients.potassium > 0) {
            double ratio = actualNutrients.potassium / targetNutrients.potassium;
            double[] range = getNutrientAchievementRate("potassium");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]);
            nutrientDeviations.put("potassium", deviation);
        }
        
        if (targetNutrients.sodium > 0) {
            double ratio = actualNutrients.sodium / targetNutrients.sodium;
            double[] range = getNutrientAchievementRate("sodium");
            // 对于钠这类限制性营养素，过量的偏离应该受到更严厉的惩罚
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]);
            if (ratio > range[1]) {
                deviation *= 1.5; // 钠过量的偏离权重更高
            }
            nutrientDeviations.put("sodium", deviation);
        }
        
        if (targetNutrients.magnesium > 0) {
            double ratio = actualNutrients.magnesium / targetNutrients.magnesium;
            double[] range = getNutrientAchievementRate("magnesium");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]);
            nutrientDeviations.put("magnesium", deviation);
        }
        
        // 计算平均偏离度
        double totalDeviation = 0;
        for (double deviation : nutrientDeviations.values()) {
            totalDeviation += deviation;
        }
        
        return nutrientDeviations.isEmpty() ? 0 : totalDeviation / nutrientDeviations.size();
    }
    
    /**
     * 计算实际比率与目标范围的偏离度
     * @param ratio 实际比率
     * @param minRate 最小达成率
     * @param maxRate 最大达成率
     * @return 偏离度（0表示在范围内，正值表示偏离范围的程度）
     */
    private double calculateDeviationFromRange(double ratio, double minRate, double maxRate) {
        if (ratio >= minRate && ratio <= maxRate) {
            return 0.0; // 在范围内，偏离为0
        } else if (ratio < minRate) {
            return minRate - ratio; // 不足的偏离
        } else {
            return ratio - maxRate; // 过量的偏离
        }
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
     * 计算解决方案的热量和主要营养素（碳水、蛋白质、脂肪）偏离得分
     * 此方法专注于评估解决方案对热量和三大主要营养素的达成情况
     * @param solution 解决方案
     * @param targetNutrients 目标营养素
     * @return 热量和主要营养素偏离得分（越低越好）
     */
    private double calculateMainNutrientsDeviationScore(MealSolution solution, Nutrition targetNutrients) {
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        Map<String, Double> nutrientDeviations = new HashMap<>();
        
        // 分配权重，热量权重较高
        double caloriesWeight = 1.2;  // 热量有更高的权重
        double carbsWeight = 1.0;
        double proteinWeight = 1.0;
        double fatWeight = 1.0;
        
        // 计算热量的偏离度
        if (targetNutrients.calories > 0) {
            double ratio = actualNutrients.calories / targetNutrients.calories;
            double[] range = getNutrientAchievementRate("calories");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]) * caloriesWeight;
            nutrientDeviations.put("calories", deviation);
        }
        
        // 计算碳水化合物的偏离度
        if (targetNutrients.carbohydrates > 0) {
            double ratio = actualNutrients.carbohydrates / targetNutrients.carbohydrates;
            double[] range = getNutrientAchievementRate("carbohydrates");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]) * carbsWeight;
            nutrientDeviations.put("carbohydrates", deviation);
        }
        
        // 计算蛋白质的偏离度
        if (targetNutrients.protein > 0) {
            double ratio = actualNutrients.protein / targetNutrients.protein;
            double[] range = getNutrientAchievementRate("protein");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]) * proteinWeight;
            nutrientDeviations.put("protein", deviation);
        }
        
        // 计算脂肪的偏离度
        if (targetNutrients.fat > 0) {
            double ratio = actualNutrients.fat / targetNutrients.fat;
            double[] range = getNutrientAchievementRate("fat");
            double deviation = calculateDeviationFromRange(ratio, range[0], range[1]) * fatWeight;
            nutrientDeviations.put("fat", deviation);
        }
        
        // 计算加权平均偏离度
        double totalDeviation = 0;
        double totalWeight = 0;
        
        for (String nutrient : nutrientDeviations.keySet()) {
            double deviation = nutrientDeviations.get(nutrient);
            double weight = 1.0;
            
            if (nutrient.equals("calories")) {
                weight = caloriesWeight;
            } else if (nutrient.equals("carbohydrates")) {
                weight = carbsWeight;
            } else if (nutrient.equals("protein")) {
                weight = proteinWeight;
            } else if (nutrient.equals("fat")) {
                weight = fatWeight;
            }
            
            totalDeviation += deviation * weight;
            totalWeight += weight;
        }
        
        return nutrientDeviations.isEmpty() ? 0 : totalDeviation / totalWeight;
    }
} 