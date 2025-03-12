package com.mealplanner.genetic.operators;

import com.mealplanner.genetic.algorithm.Population;
import com.mealplanner.genetic.model.MealSolution;

import java.util.*;

/**
 * 实现膳食解决方案的选择操作
 */
public class MealSelection {
    // 选择类型枚举
    public enum SelectionType {
        TOURNAMENT,     // 锦标赛选择
        ROULETTE_WHEEL, // 轮盘赌选择
        RANK_BASED      // 基于等级的选择
    }
    
    // 默认选择类型
    private SelectionType selectionType = SelectionType.TOURNAMENT;
    
    // 锦标赛规模
    private int tournamentSize = 2;
    
    /**
     * 默认构造函数
     */
    public MealSelection() {
    }
    
    /**
     * 构造函数
     * @param selectionType 选择类型
     */
    public MealSelection(SelectionType selectionType) {
        this.selectionType = selectionType;
    }
    
    /**
     * 构造函数
     * @param selectionType 选择类型
     * @param tournamentSize 锦标赛规模
     */
    public MealSelection(SelectionType selectionType, int tournamentSize) {
        this.selectionType = selectionType;
        this.tournamentSize = tournamentSize;
    }
    
    /**
     * 从种群中选择指定数量的解决方案
     * @param population 种群
     * @param count 选择数量
     * @return 选择的解决方案列表
     */
    public List<MealSolution> select(Population population, int count) {
        List<MealSolution> selected = new ArrayList<>();
        
        // 根据选择类型执行不同的选择操作
        switch (selectionType) {
            case ROULETTE_WHEEL:
                selected = rouletteWheelSelection(population, count);
                break;
            case RANK_BASED:
                selected = rankBasedSelection(population, count);
                break;
            case TOURNAMENT:
            default:
                selected = tournamentSelection(population, count);
                break;
        }
        
        return selected;
    }
    
    /**
     * 锦标赛选择
     * @param population 种群
     * @param count 选择数量
     * @return 选择的解决方案列表
     */
    private List<MealSolution> tournamentSelection(Population population, int count) {
        List<MealSolution> selected = new ArrayList<>();
        Random random = new Random();
        List<MealSolution> solutions = population.getSolutions();
        
        if (solutions.isEmpty()) {
            return selected;
        }
        
        for (int i = 0; i < count; i++) {
            // 随机选择tournamentSize个解决方案
            List<MealSolution> tournament = new ArrayList<>();
            for (int j = 0; j < tournamentSize; j++) {
                int index = random.nextInt(solutions.size());
                tournament.add(solutions.get(index));
            }
            
            // 从锦标赛中选择最好的解决方案（等级最低，拥挤度最高）
            MealSolution best = tournament.get(0);
            for (int j = 1; j < tournament.size(); j++) {
                MealSolution current = tournament.get(j);
                
                // 比较等级
                if (current.getRank() < best.getRank()) {
                    best = current;
                } else if (current.getRank() == best.getRank()) {
                    // 同等级，比较拥挤度
                    if (current.getCrowdingDistance() > best.getCrowdingDistance()) {
                        best = current;
                    }
                }
            }
            
            selected.add(best);
        }
        
        return selected;
    }
    
    /**
     * 轮盘赌选择
     * @param population 种群
     * @param count 选择数量
     * @return 选择的解决方案列表
     */
    private List<MealSolution> rouletteWheelSelection(Population population, int count) {
        List<MealSolution> selected = new ArrayList<>();
        List<MealSolution> solutions = population.getSolutions();
        
        if (solutions.isEmpty()) {
            return selected;
        }
        
        // 计算每个解决方案的适应度
        double[] fitness = new double[solutions.size()];
        double totalFitness = 0;
        
        for (int i = 0; i < solutions.size(); i++) {
            // 适应度与等级成反比，与拥挤度成正比
            fitness[i] = 1.0 / (solutions.get(i).getRank() + 1) * (solutions.get(i).getCrowdingDistance() + 1);
            totalFitness += fitness[i];
        }
        
        // 计算累积概率
        double[] cumulativeProbability = new double[solutions.size()];
        for (int i = 0; i < solutions.size(); i++) {
            if (i == 0) {
                cumulativeProbability[i] = fitness[i] / totalFitness;
            } else {
                cumulativeProbability[i] = cumulativeProbability[i - 1] + fitness[i] / totalFitness;
            }
        }
        
        // 选择
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            double r = random.nextDouble();
            for (int j = 0; j < solutions.size(); j++) {
                if (r <= cumulativeProbability[j]) {
                    selected.add(solutions.get(j));
                    break;
                }
            }
        }
        
        return selected;
    }
    
    /**
     * 基于等级的选择
     * @param population 种群
     * @param count 选择数量
     * @return 选择的解决方案列表
     */
    private List<MealSolution> rankBasedSelection(Population population, int count) {
        List<MealSolution> selected = new ArrayList<>();
        List<MealSolution> solutions = population.getSolutions();
        
        if (solutions.isEmpty()) {
            return selected;
        }
        
        // 按等级和拥挤度排序
        List<MealSolution> sortedSolutions = new ArrayList<>(solutions);
        sortedSolutions.sort((a, b) -> {
            // 首先按等级排序
            int rankComparison = Integer.compare(a.getRank(), b.getRank());
            if (rankComparison != 0) {
                return rankComparison;
            }
            
            // 同等级按拥挤度降序排序
            return Double.compare(b.getCrowdingDistance(), a.getCrowdingDistance());
        });
        
        // 选择前count个
        for (int i = 0; i < Math.min(count, sortedSolutions.size()); i++) {
            selected.add(sortedSolutions.get(i));
        }
        
        return selected;
    }
    
    /**
     * 获取选择类型
     * @return 选择类型
     */
    public SelectionType getSelectionType() {
        return selectionType;
    }
    
    /**
     * 设置选择类型
     * @param selectionType 选择类型
     */
    public void setSelectionType(SelectionType selectionType) {
        this.selectionType = selectionType;
    }
    
    /**
     * 获取锦标赛规模
     * @return 锦标赛规模
     */
    public int getTournamentSize() {
        return tournamentSize;
    }
    
    /**
     * 设置锦标赛规模
     * @param tournamentSize 锦标赛规模
     */
    public void setTournamentSize(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }
}