package com.mealplanner.genetic.algorithm;

import com.mealplanner.genetic.model.MealSolution;

import java.util.*;

/**
 * NSGA-II算法中的非支配排序实现
 */
public class NonDominatedSorting {
    
    /**
     * 对种群进行非支配排序
     * 将每个解决方案分配到一个前沿
     * @param population 待排序的种群
     */
    public static void sort(Population population) {
        List<MealSolution> solutions = population.getSolutions();
        
        if (solutions.isEmpty()) {
            return;
        }
        
        // 重置所有解决方案的排名
        solutions.forEach(solution -> solution.setRank(0));
        
        // 存储每个解决方案支配的其他解决方案
        Map<MealSolution, List<MealSolution>> dominatedSolutions = new HashMap<>();
        
        // 存储每个解决方案被支配的次数
        Map<MealSolution, Integer> dominationCount = new HashMap<>();
        
        // 第一个前沿（非支配解集）
        List<MealSolution> firstFront = new ArrayList<>();
        
        // 第一步：计算每个解决方案的支配关系
        for (MealSolution p : solutions) {
            // 初始化
            dominatedSolutions.put(p, new ArrayList<>());
            dominationCount.put(p, 0);
            
            // 与其他解决方案比较
            for (MealSolution q : solutions) {
                if (p == q) continue;
                
                if (p.dominates(q)) {
                    // p支配q，将q添加到p支配的解决方案列表中
                    dominatedSolutions.get(p).add(q);
                } else if (q.dominates(p)) {
                    // q支配p，增加p的被支配次数
                    dominationCount.put(p, dominationCount.get(p) + 1);
                }
            }
            
            // 如果p不被任何解决方案支配，则属于第一个前沿
            if (dominationCount.get(p) == 0) {
                p.setRank(1);
                firstFront.add(p);
            }
        }
        
        // 当前前沿索引
        int frontIndex = 1;
        
        // 当前前沿
        List<MealSolution> currentFront = new ArrayList<>(firstFront);
        
        // 第二步：逐层构建前沿
        while (!currentFront.isEmpty()) {
            // 下一个前沿
            List<MealSolution> nextFront = new ArrayList<>();
            
            // 对当前前沿中的每个解决方案
            for (MealSolution p : currentFront) {
                // 对于p支配的每个解决方案q
                for (MealSolution q : dominatedSolutions.get(p)) {
                    // 减少q的被支配次数
                    dominationCount.put(q, dominationCount.get(q) - 1);
                    
                    // 如果q不再被任何解决方案支配，将其添加到下一个前沿
                    if (dominationCount.get(q) == 0) {
                        q.setRank(frontIndex + 1);
                        nextFront.add(q);
                    }
                }
            }
            
            // 增加前沿索引
            frontIndex++;
            
            // 更新当前前沿
            currentFront = nextFront;
        }
    }
    
    /**
     * 检查一个解决方案是否支配另一个解决方案
     * @param solution1 解决方案1
     * @param solution2 解决方案2
     * @return 如果solution1支配solution2则返回true
     */
    public static boolean dominates(MealSolution solution1, MealSolution solution2) {
        if (solution1 == null || solution2 == null) {
            return false;
        }
        
        if (solution1.getObjectiveValues().size() != solution2.getObjectiveValues().size()) {
            throw new IllegalArgumentException("解决方案的目标值数量不一致");
        }
        
        boolean atLeastOneBetter = false;
        
        for (int i = 0; i < solution1.getObjectiveValues().size(); i++) {
            double value1 = solution1.getObjectiveValue(i).getValue();
            double value2 = solution2.getObjectiveValue(i).getValue();
            
            if (value1 < value2) {
                return false; // solution1在一个目标上更差
            }
            
            if (value1 > value2) {
                atLeastOneBetter = true; // solution1在至少一个目标上更好
            }
        }
        
        return atLeastOneBetter;
    }
} 