package com.mealplanner.genetic.util;

/**
 * NSGA-II算法配置类
 */
public class NSGAIIConfiguration {
    // 种群大小
    private int populationSize = 50;
    
    // 最大代数
    private int maxGenerations = 100;
    
    // 交叉率
    private double crossoverRate = 0.9;
    
    // 变异率
    private double mutationRate = 0.2;
    
    // 膳食中最少食物数量
    private int minFoodsPerMeal = 4;
    
    // 膳食中最多食物数量
    private int maxFoodsPerMeal = 8;
    
    // 锦标赛选择规模
    private int tournamentSize = 2;
    
    // 最小帕累托解数量（提前终止条件）
    private int minParetoSolutions = 10;
    
    // 早期终止检查间隔（代数）
    private int earlyTerminationCheckInterval = 5;
    
    // 提前终止判断连续不改进代数
    private int maxGenerationsWithoutImprovement = 10;
    
    // 随机种子（为null时使用系统时间）
    private Long randomSeed = null;
    
    // 并行执行
    private boolean parallelExecution = false;
    
    /**
     * 默认构造函数
     */
    public NSGAIIConfiguration() {
        // 使用默认值
    }
    
    /**
     * 构造函数
     * @param populationSize 种群大小
     * @param maxGenerations 最大代数
     * @param crossoverRate 交叉率
     * @param mutationRate 变异率
     */
    public NSGAIIConfiguration(int populationSize, int maxGenerations, double crossoverRate, double mutationRate) {
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
    }
    
    /**
     * 创建小型运行配置（快速但质量较低）
     * @return 小型配置
     */
    public static NSGAIIConfiguration createSmallConfiguration() {
        NSGAIIConfiguration config = new NSGAIIConfiguration();
        config.setPopulationSize(20);
        config.setMaxGenerations(30);
        config.setCrossoverRate(0.9);
        config.setMutationRate(0.3);
        config.setMinParetoSolutions(5);
        return config;
    }
    
    /**
     * 创建标准运行配置（质量与速度平衡）
     * @return 标准配置
     */
    public static NSGAIIConfiguration createStandardConfiguration() {
        NSGAIIConfiguration config = new NSGAIIConfiguration();
        config.setPopulationSize(50);
        config.setMaxGenerations(100);
        config.setCrossoverRate(0.9);
        config.setMutationRate(0.2);
        config.setMinParetoSolutions(10);
        return config;
    }
    
    /**
     * 创建大型运行配置（高质量但速度较慢）
     * @return 大型配置
     */
    public static NSGAIIConfiguration createLargeConfiguration() {
        NSGAIIConfiguration config = new NSGAIIConfiguration();
        config.setPopulationSize(100);
        config.setMaxGenerations(200);
        config.setCrossoverRate(0.9);
        config.setMutationRate(0.3);
        config.setMinParetoSolutions(20);
        config.setParallelExecution(true);
        return config;
    }
    
    // Getters and Setters
    
    public int getPopulationSize() {
        return populationSize;
    }
    
    public void setPopulationSize(int populationSize) {
        if (populationSize <= 0) {
            throw new IllegalArgumentException("种群大小必须为正整数");
        }
        this.populationSize = populationSize;
    }
    
    public int getMaxGenerations() {
        return maxGenerations;
    }
    
    public void setMaxGenerations(int maxGenerations) {
        if (maxGenerations <= 0) {
            throw new IllegalArgumentException("最大代数必须为正整数");
        }
        this.maxGenerations = maxGenerations;
    }
    
    public double getCrossoverRate() {
        return crossoverRate;
    }
    
    public void setCrossoverRate(double crossoverRate) {
        if (crossoverRate < 0 || crossoverRate > 1) {
            throw new IllegalArgumentException("交叉率必须在0到1之间");
        }
        this.crossoverRate = crossoverRate;
    }
    
    public double getMutationRate() {
        return mutationRate;
    }
    
    public void setMutationRate(double mutationRate) {
        if (mutationRate < 0 || mutationRate > 1) {
            throw new IllegalArgumentException("变异率必须在0到1之间");
        }
        this.mutationRate = mutationRate;
    }
    
    public int getMinFoodsPerMeal() {
        return minFoodsPerMeal;
    }
    
    public void setMinFoodsPerMeal(int minFoodsPerMeal) {
        if (minFoodsPerMeal <= 0) {
            throw new IllegalArgumentException("最少食物数量必须为正整数");
        }
        if (minFoodsPerMeal > maxFoodsPerMeal) {
            throw new IllegalArgumentException("最少食物数量不能大于最多食物数量");
        }
        this.minFoodsPerMeal = minFoodsPerMeal;
    }
    
    public int getMaxFoodsPerMeal() {
        return maxFoodsPerMeal;
    }
    
    public void setMaxFoodsPerMeal(int maxFoodsPerMeal) {
        if (maxFoodsPerMeal <= 0) {
            throw new IllegalArgumentException("最多食物数量必须为正整数");
        }
        if (maxFoodsPerMeal < minFoodsPerMeal) {
            throw new IllegalArgumentException("最多食物数量不能小于最少食物数量");
        }
        this.maxFoodsPerMeal = maxFoodsPerMeal;
    }
    
    public int getTournamentSize() {
        return tournamentSize;
    }
    
    public void setTournamentSize(int tournamentSize) {
        if (tournamentSize <= 0) {
            throw new IllegalArgumentException("锦标赛规模必须为正整数");
        }
        this.tournamentSize = tournamentSize;
    }
    
    public int getMinParetoSolutions() {
        return minParetoSolutions;
    }
    
    public void setMinParetoSolutions(int minParetoSolutions) {
        if (minParetoSolutions <= 0) {
            throw new IllegalArgumentException("最小帕累托解数量必须为正整数");
        }
        this.minParetoSolutions = minParetoSolutions;
    }
    
    public int getEarlyTerminationCheckInterval() {
        return earlyTerminationCheckInterval;
    }
    
    public void setEarlyTerminationCheckInterval(int earlyTerminationCheckInterval) {
        if (earlyTerminationCheckInterval <= 0) {
            throw new IllegalArgumentException("早期终止检查间隔必须为正整数");
        }
        this.earlyTerminationCheckInterval = earlyTerminationCheckInterval;
    }
    
    public int getMaxGenerationsWithoutImprovement() {
        return maxGenerationsWithoutImprovement;
    }
    
    public void setMaxGenerationsWithoutImprovement(int maxGenerationsWithoutImprovement) {
        if (maxGenerationsWithoutImprovement <= 0) {
            throw new IllegalArgumentException("最大不改进代数必须为正整数");
        }
        this.maxGenerationsWithoutImprovement = maxGenerationsWithoutImprovement;
    }
    
    public Long getRandomSeed() {
        return randomSeed;
    }
    
    public void setRandomSeed(Long randomSeed) {
        this.randomSeed = randomSeed;
    }
    
    public boolean isParallelExecution() {
        return parallelExecution;
    }
    
    public void setParallelExecution(boolean parallelExecution) {
        this.parallelExecution = parallelExecution;
    }
    
    @Override
    public String toString() {
        return "NSGAIIConfiguration{" +
                "populationSize=" + populationSize +
                ", maxGenerations=" + maxGenerations +
                ", crossoverRate=" + crossoverRate +
                ", mutationRate=" + mutationRate +
                ", minFoodsPerMeal=" + minFoodsPerMeal +
                ", maxFoodsPerMeal=" + maxFoodsPerMeal +
                ", tournamentSize=" + tournamentSize +
                ", minParetoSolutions=" + minParetoSolutions +
                ", earlyTerminationCheckInterval=" + earlyTerminationCheckInterval +
                ", maxGenerationsWithoutImprovement=" + maxGenerationsWithoutImprovement +
                ", randomSeed=" + randomSeed +
                ", parallelExecution=" + parallelExecution +
                '}';
    }
} 