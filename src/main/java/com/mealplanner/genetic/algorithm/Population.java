package com.mealplanner.genetic.algorithm;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表示NSGA-II算法中的种群
 */
public class Population {
    // 种群中的解决方案
    private List<MealSolution> solutions;
    
    // 按目标分类的解决方案
    private Map<String, List<MealSolution>> solutionsByObjective;
    
    /**
     * 构造函数
     * @param solutions 解决方案列表
     */
    public Population(List<MealSolution> solutions) {
        this.solutions = new ArrayList<>(solutions);
        this.solutionsByObjective = new HashMap<>();
    }
    
    /**
     * 获取种群大小
     * @return 种群大小
     */
    public int size() {
        return solutions.size();
    }
    
    /**
     * 获取所有解决方案
     * @return 解决方案列表
     */
    public List<MealSolution> getSolutions() {
        return solutions;
    }
    
    /**
     * 按目标值对解决方案进行排序
     * @param objectiveIndex 目标索引
     */
    public void sortByObjective(int objectiveIndex) {
        solutions.sort((a, b) -> {
            double aValue = a.getObjectiveValue(objectiveIndex).getValue();
            double bValue = b.getObjectiveValue(objectiveIndex).getValue();
            return Double.compare(aValue, bValue);
        });
    }
    
    /**
     * 更新按目标分类的解决方案映射
     */
    public void updateSolutionsByObjective() {
        solutionsByObjective.clear();
        
        if (solutions.isEmpty()) {
            return;
        }
        
        // 获取第一个解决方案的目标值列表
        List<ObjectiveValue> objectives = solutions.get(0).getObjectiveValues();
        
        // 为每个目标创建按该目标值排序的解决方案列表
        for (int i = 0; i < objectives.size(); i++) {
            final int index = i;
            String objectiveName = objectives.get(i).getName();
            
            List<MealSolution> sortedSolutions = solutions.stream()
                    .sorted(Comparator.comparing(solution -> solution.getObjectiveValue(index).getValue()))
                    .collect(Collectors.toList());
            
            solutionsByObjective.put(objectiveName, sortedSolutions);
        }
    }
    
    /**
     * 获取按特定目标排序的解决方案列表
     * @param objectiveName 目标名称
     * @return 排序后的解决方案列表
     */
    public List<MealSolution> getSolutionsSortedByObjective(String objectiveName) {
        if (solutionsByObjective.isEmpty()) {
            updateSolutionsByObjective();
        }
        
        return solutionsByObjective.getOrDefault(objectiveName, new ArrayList<>());
    }
    
    /**
     * 获取第i个前沿的解决方案
     * @param frontIndex 前沿索引
     * @return 解决方案列表
     */
    public List<MealSolution> getFront(int frontIndex) {
        return solutions.stream()
                .filter(solution -> solution.getRank() == frontIndex)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有前沿
     * @return 按前沿索引分组的解决方案映射
     */
    public Map<Integer, List<MealSolution>> getAllFronts() {
        Map<Integer, List<MealSolution>> fronts = new HashMap<>();
        
        for (MealSolution solution : solutions) {
            int rank = solution.getRank();
            fronts.computeIfAbsent(rank, k -> new ArrayList<>()).add(solution);
        }
        
        return fronts;
    }
    
    /**
     * 获取最大前沿索引
     * @return 最大前沿索引
     */
    public int getMaxFrontIndex() {
        return solutions.stream()
                .mapToInt(MealSolution::getRank)
                .max()
                .orElse(0);
    }
    
    /**
     * 合并两个种群
     * @param population1 第一个种群
     * @param population2 第二个种群
     * @return 合并后的新种群
     */
    public static Population merge(Population population1, Population population2) {
        List<MealSolution> mergedSolutions = new ArrayList<>();
        mergedSolutions.addAll(population1.getSolutions());
        mergedSolutions.addAll(population2.getSolutions());
        
        return new Population(mergedSolutions);
    }
    
    /**
     * 获取种群的统计信息
     * @return 统计信息字符串
     */
    public String getStatistics() {
        if (solutions.isEmpty()) {
            return "空种群";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // 种群大小
        sb.append("种群大小: ").append(solutions.size()).append("\n");
        
        // 前沿分布
        Map<Integer, List<MealSolution>> fronts = getAllFronts();
        sb.append("前沿分布: ");
        for (Map.Entry<Integer, List<MealSolution>> entry : fronts.entrySet()) {
            sb.append("前沿").append(entry.getKey())
              .append(": ").append(entry.getValue().size())
              .append("个解; ");
        }
        sb.append("\n");
        
        // 目标值统计
        if (!solutions.isEmpty() && !solutions.get(0).getObjectiveValues().isEmpty()) {
            List<ObjectiveValue> objectives = solutions.get(0).getObjectiveValues();
            
            for (int i = 0; i < objectives.size(); i++) {
                final int index = i;
                String objectiveName = objectives.get(i).getName();
                
                double minValue = solutions.stream()
                        .mapToDouble(s -> s.getObjectiveValue(index).getValue())
                        .min().orElse(0);
                
                double maxValue = solutions.stream()
                        .mapToDouble(s -> s.getObjectiveValue(index).getValue())
                        .max().orElse(0);
                
                double avgValue = solutions.stream()
                        .mapToDouble(s -> s.getObjectiveValue(index).getValue())
                        .average().orElse(0);
                
                sb.append(objectiveName).append(" 统计: ")
                  .append("最小值=").append(String.format("%.3f", minValue))
                  .append(", 最大值=").append(String.format("%.3f", maxValue))
                  .append(", 平均值=").append(String.format("%.3f", avgValue))
                  .append("\n");
            }
        }
        
        return sb.toString();
    }
} 