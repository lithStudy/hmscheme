package com.mealplanner.genetic.algorithm;

import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;

import java.util.*;

/**
 * NSGA-II算法中的拥挤度距离计算实现
 */
public class CrowdingDistanceCalculator {
    
    /**
     * 计算种群中所有解的拥挤度距离
     * @param population 种群
     */
    public static void calculate(Population population) {
        // 获取按前沿分组的解决方案
        Map<Integer, List<MealSolution>> fronts = population.getAllFronts();
        
        // 对每个前沿计算拥挤度距离
        for (List<MealSolution> front : fronts.values()) {
            calculateCrowdingDistance(front);
        }
    }
    
    /**
     * 计算一个前沿中所有解的拥挤度距离
     * @param front 前沿（解决方案列表）
     */
    public static void calculateCrowdingDistance(List<MealSolution> front) {
        int size = front.size();
        
        if (size <= 2) {
            // 如果前沿中只有1或2个解，设置它们的拥挤度为无穷大
            for (MealSolution solution : front) {
                solution.setCrowdingDistance(Double.POSITIVE_INFINITY);
            }
            return;
        }
        
        // 重置所有解的拥挤度距离为0
        for (MealSolution solution : front) {
            solution.setCrowdingDistance(0);
        }
        
        // 获取目标数量
        int numObjectives = front.get(0).getObjectiveValues().size();
        
        // 对每个目标计算拥挤度距离
        for (int i = 0; i < numObjectives; i++) {
            final int objectiveIndex = i;
            
            // 按当前目标值排序
            front.sort((a, b) -> {
                double aValue = a.getObjectiveValue(objectiveIndex).getValue();
                double bValue = b.getObjectiveValue(objectiveIndex).getValue();
                return Double.compare(aValue, bValue);
            });
            
            // 获取当前目标的最小值和最大值
            double minValue = front.get(0).getObjectiveValue(objectiveIndex).getValue();
            double maxValue = front.get(size - 1).getObjectiveValue(objectiveIndex).getValue();
            
            // 设置边界点的拥挤度为无穷大
            front.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
            front.get(size - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);
            
            // 计算中间点的拥挤度
            if (maxValue > minValue) {
                for (int j = 1; j < size - 1; j++) {
                    double previousValue = front.get(j - 1).getObjectiveValue(objectiveIndex).getValue();
                    double nextValue = front.get(j + 1).getObjectiveValue(objectiveIndex).getValue();
                    
                    // 累加拥挤度距离
                    double distance = front.get(j).getCrowdingDistance();
                    distance += (nextValue - previousValue) / (maxValue - minValue);
                    front.get(j).setCrowdingDistance(distance);
                }
            }
        }
    }
    
    /**
     * 使用拥挤度比较器对解决方案进行排序
     * @param solutions 解决方案列表
     */
    public static void sortByCrowdingDistance(List<MealSolution> solutions) {
        solutions.sort((a, b) -> {
            // 首先按等级排序
            int rankComparison = Integer.compare(a.getRank(), b.getRank());
            if (rankComparison != 0) {
                return rankComparison;
            }
            
            // 同一等级的按拥挤度降序排序（拥挤度大的优先）
            return Double.compare(b.getCrowdingDistance(), a.getCrowdingDistance());
        });
    }
    
    /**
     * 获取一组解的多样性指标
     * @param solutions 解决方案列表
     * @return 多样性指标值
     */
    public static double getDiversityMetric(List<MealSolution> solutions) {
        if (solutions.size() <= 1) {
            return 0;
        }
        
        // 拥挤度距离的标准差
        double sum = 0;
        double sumSquared = 0;
        int count = 0;
        
        for (MealSolution solution : solutions) {
            double crowdingDistance = solution.getCrowdingDistance();
            if (!Double.isInfinite(crowdingDistance)) {
                sum += crowdingDistance;
                sumSquared += crowdingDistance * crowdingDistance;
                count++;
            }
        }
        
        if (count <= 1) {
            return 0;
        }
        
        double mean = sum / count;
        double variance = (sumSquared / count) - (mean * mean);
        return Math.sqrt(variance);
    }
}