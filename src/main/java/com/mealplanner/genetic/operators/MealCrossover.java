package com.mealplanner.genetic.operators;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实现膳食解决方案的交叉操作
 */
public class MealCrossover {
    // 交叉概率
    private double crossoverRate;
    
    // 交叉类型枚举
    public enum CrossoverType {
        UNIFORM,      // 均匀交叉
        ONE_POINT,    // 单点交叉
        TWO_POINT,    // 两点交叉
        BLENDING      // 混合交叉
    }
    
    // 默认交叉类型
    private CrossoverType crossoverType = CrossoverType.UNIFORM;
    
    /**
     * 构造函数
     * @param crossoverRate 交叉概率
     */
    public MealCrossover(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }
    
    /**
     * 构造函数
     * @param crossoverRate 交叉概率
     * @param crossoverType 交叉类型
     */
    public MealCrossover(double crossoverRate, CrossoverType crossoverType) {
        this.crossoverRate = crossoverRate;
        this.crossoverType = crossoverType;
    }
    
    /**
     * 对两个父代解决方案应用交叉操作
     * @param parent1 父代1
     * @param parent2 父代2
     * @return 生成的子代列表
     */
    public List<MealSolution> apply(MealSolution parent1, MealSolution parent2) {
        // 如果随机值大于交叉率，不执行交叉
        if (Math.random() > crossoverRate) {
            return Arrays.asList(parent1.copy(), parent2.copy());
        }
        
        // 根据交叉类型执行不同的交叉操作
        switch (crossoverType) {
            case ONE_POINT:
                return onePointCrossover(parent1, parent2);
            case TWO_POINT:
                return twoPointCrossover(parent1, parent2);
            case BLENDING:
                return blendingCrossover(parent1, parent2);
            case UNIFORM:
            default:
                return uniformCrossover(parent1, parent2);
        }
    }
    
    /**
     * 均匀交叉操作
     */
    private List<MealSolution> uniformCrossover(MealSolution parent1, MealSolution parent2) {
        // 创建两个子代的食物基因列表
        List<FoodGene> child1Genes = new ArrayList<>();
        List<FoodGene> child2Genes = new ArrayList<>();
        
        // 合并两个父代的所有食物
        Set<String> allFoodNames = new HashSet<>();
        
        for (FoodGene gene : parent1.getFoodGenes()) {
            allFoodNames.add(gene.getFood().getName());
        }
        
        for (FoodGene gene : parent2.getFoodGenes()) {
            allFoodNames.add(gene.getFood().getName());
        }
        
        // 随机选择每种食物是否包含在子代中
        Random random = new Random();
        
        for (String foodName : allFoodNames) {
            // 从父代1中查找食物
            Optional<FoodGene> gene1 = parent1.getFoodGenes().stream()
                    .filter(g -> g.getFood().getName().equals(foodName))
                    .findFirst();
            
            // 从父代2中查找食物
            Optional<FoodGene> gene2 = parent2.getFoodGenes().stream()
                    .filter(g -> g.getFood().getName().equals(foodName))
                    .findFirst();
            
            // 根据随机概率决定食物是否包含在子代中
            boolean includeInChild1 = random.nextBoolean();
            boolean includeInChild2 = random.nextBoolean();
            
            // 为子代1添加食物
            if (includeInChild1) {
                if (gene1.isPresent()) {
                    child1Genes.add(gene1.get().copy());
                } else if (gene2.isPresent()) {
                    child1Genes.add(gene2.get().copy());
                }
            }
            
            // 为子代2添加食物
            if (includeInChild2) {
                if (gene2.isPresent()) {
                    child2Genes.add(gene2.get().copy());
                } else if (gene1.isPresent()) {
                    child2Genes.add(gene1.get().copy());
                }
            }
        }
        
        // 创建子代解决方案
        MealSolution child1 = new MealSolution(child1Genes);
        MealSolution child2 = new MealSolution(child2Genes);
        
        return Arrays.asList(child1, child2);
    }
    
    /**
     * 单点交叉操作
     */
    private List<MealSolution> onePointCrossover(MealSolution parent1, MealSolution parent2) {
        List<FoodGene> genes1 = parent1.getFoodGenes();
        List<FoodGene> genes2 = parent2.getFoodGenes();
        
        if (genes1.isEmpty() || genes2.isEmpty()) {
            return Arrays.asList(parent1.copy(), parent2.copy());
        }
        
        // 选择交叉点
        Random random = new Random();
        int crossoverPoint1 = random.nextInt(Math.max(genes1.size(), 1));
        int crossoverPoint2 = random.nextInt(Math.max(genes2.size(), 1));
        
        // 创建子代基因
        List<FoodGene> child1Genes = new ArrayList<>();
        List<FoodGene> child2Genes = new ArrayList<>();
        
        // 子代1：父代1的前半部分 + 父代2的后半部分
        for (int i = 0; i < crossoverPoint1; i++) {
            child1Genes.add(genes1.get(i).copy());
        }
        
        for (int i = crossoverPoint2; i < genes2.size(); i++) {
            // 避免重复食物
            final int index = i;  // 创建一个不可变的副本
            if (child1Genes.stream().noneMatch(g -> g.getFood().getName().equals(genes2.get(index).getFood().getName()))) {
                child1Genes.add(genes2.get(index).copy());
            }
        }
        
        // 子代2：父代2的前半部分 + 父代1的后半部分
        for (int i = 0; i < crossoverPoint2; i++) {
            child2Genes.add(genes2.get(i).copy());
        }
        
        for (int i = crossoverPoint1; i < genes1.size(); i++) {
            // 避免重复食物
            final int index = i;  // 创建一个不可变的副本
            if (child2Genes.stream().noneMatch(g -> g.getFood().getName().equals(genes1.get(index).getFood().getName()))) {
                child2Genes.add(genes1.get(index).copy());
            }
        }
        
        return Arrays.asList(new MealSolution(child1Genes), new MealSolution(child2Genes));
    }
    
    /**
     * 两点交叉操作
     */
    private List<MealSolution> twoPointCrossover(MealSolution parent1, MealSolution parent2) {
        List<FoodGene> genes1 = parent1.getFoodGenes();
        List<FoodGene> genes2 = parent2.getFoodGenes();
        
        if (genes1.size() < 2 || genes2.size() < 2) {
            return onePointCrossover(parent1, parent2);
        }
        
        // 选择两个交叉点
        Random random = new Random();
        int point1Parent1 = random.nextInt(genes1.size() - 1);
        int point2Parent1 = point1Parent1 + 1 + random.nextInt(genes1.size() - point1Parent1 - 1);
        
        int point1Parent2 = random.nextInt(genes2.size() - 1);
        int point2Parent2 = point1Parent2 + 1 + random.nextInt(genes2.size() - point1Parent2 - 1);
        
        // 创建子代基因
        List<FoodGene> child1Genes = new ArrayList<>();
        List<FoodGene> child2Genes = new ArrayList<>();
        
        // 子代1：父代1的开始和结束部分 + 父代2的中间部分
        // 第一段：父代1的开始到第一个交叉点
        for (int i = 0; i < point1Parent1; i++) {
            child1Genes.add(genes1.get(i).copy());
        }
        
        // 第二段：父代2的第一个到第二个交叉点
        for (int i = point1Parent2; i < point2Parent2; i++) {
            // 避免重复食物
            final int index = i;  // 创建一个不可变的副本
            if (child1Genes.stream().noneMatch(g -> g.getFood().getName().equals(genes2.get(index).getFood().getName()))) {
                child1Genes.add(genes2.get(index).copy());
            }
        }
        
        // 第三段：父代1的第二个交叉点到结束
        for (int i = point2Parent1; i < genes1.size(); i++) {
            // 避免重复食物
            final int index = i;  // 创建一个不可变的副本
            if (child1Genes.stream().noneMatch(g -> g.getFood().getName().equals(genes1.get(index).getFood().getName()))) {
                child1Genes.add(genes1.get(index).copy());
            }
        }
        
        // 子代2：父代2的开始和结束部分 + 父代1的中间部分
        // 第一段：父代2的开始到第一个交叉点
        for (int i = 0; i < point1Parent2; i++) {
            child2Genes.add(genes2.get(i).copy());
        }
        
        // 第二段：父代1的第一个到第二个交叉点
        for (int i = point1Parent1; i < point2Parent1; i++) {
            // 避免重复食物
            final int index = i;  // 创建一个不可变的副本
            if (child2Genes.stream().noneMatch(g -> g.getFood().getName().equals(genes1.get(index).getFood().getName()))) {
                child2Genes.add(genes1.get(index).copy());
            }
        }
        
        // 第三段：父代2的第二个交叉点到结束
        for (int i = point2Parent2; i < genes2.size(); i++) {
            // 避免重复食物
            final int index = i;  // 创建一个不可变的副本
            if (child2Genes.stream().noneMatch(g -> g.getFood().getName().equals(genes2.get(index).getFood().getName()))) {
                child2Genes.add(genes2.get(index).copy());
            }
        }
        
        return Arrays.asList(new MealSolution(child1Genes), new MealSolution(child2Genes));
    }
    
    /**
     * 混合交叉操作（针对摄入量）
     */
    private List<MealSolution> blendingCrossover(MealSolution parent1, MealSolution parent2) {
        // 创建两个子代的基因列表
        List<FoodGene> child1Genes = new ArrayList<>();
        List<FoodGene> child2Genes = new ArrayList<>();
        
        // 获取两个父代的共同食物
        Map<String, FoodGene> parent1Map = parent1.getFoodGenes().stream()
                .collect(Collectors.toMap(gene -> gene.getFood().getName(), gene -> gene));
        
        Map<String, FoodGene> parent2Map = parent2.getFoodGenes().stream()
                .collect(Collectors.toMap(gene -> gene.getFood().getName(), gene -> gene));
        
        Set<String> commonFoods = new HashSet<>(parent1Map.keySet());
        commonFoods.retainAll(parent2Map.keySet());
        
        // 处理共同食物：混合摄入量
        for (String foodName : commonFoods) {
            FoodGene gene1 = parent1Map.get(foodName);
            FoodGene gene2 = parent2Map.get(foodName);
            
            double intake1 = gene1.getIntake();
            double intake2 = gene2.getIntake();
            
            // 混合系数
            double alpha = 0.3; // 可以调整这个参数
            
            // 计算混合后的摄入量
            double blendedIntake1 = intake1 * (1 - alpha) + intake2 * alpha;
            double blendedIntake2 = intake2 * (1 - alpha) + intake1 * alpha;
            
            // 将混合后的摄入量四舍五入为整数
            blendedIntake1 = Math.round(blendedIntake1);
            blendedIntake2 = Math.round(blendedIntake2);
            
            // 创建新的基因
            child1Genes.add(new FoodGene(gene1.getFood(), blendedIntake1));
            child2Genes.add(new FoodGene(gene2.getFood(), blendedIntake2));
        }
        
        // 获取父代1独有的食物
        Set<String> parent1Only = new HashSet<>(parent1Map.keySet());
        parent1Only.removeAll(commonFoods);
        
        // 获取父代2独有的食物
        Set<String> parent2Only = new HashSet<>(parent2Map.keySet());
        parent2Only.removeAll(commonFoods);
        
        // 随机决定独有食物的分配
        Random random = new Random();
        
        for (String foodName : parent1Only) {
            FoodGene gene = parent1Map.get(foodName);
            if (random.nextBoolean()) {
                child1Genes.add(gene.copy());
            } else {
                child2Genes.add(gene.copy());
            }
        }
        
        for (String foodName : parent2Only) {
            FoodGene gene = parent2Map.get(foodName);
            if (random.nextBoolean()) {
                child1Genes.add(gene.copy());
            } else {
                child2Genes.add(gene.copy());
            }
        }
        
        return Arrays.asList(new MealSolution(child1Genes), new MealSolution(child2Genes));
    }
    
    /**
     * 获取交叉率
     * @return 交叉率
     */
    public double getCrossoverRate() {
        return crossoverRate;
    }
    
    /**
     * 设置交叉率
     * @param crossoverRate 交叉率
     */
    public void setCrossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }
    
    /**
     * 获取交叉类型
     * @return 交叉类型
     */
    public CrossoverType getCrossoverType() {
        return crossoverType;
    }
    
    /**
     * 设置交叉类型
     * @param crossoverType 交叉类型
     */
    public void setCrossoverType(CrossoverType crossoverType) {
        this.crossoverType = crossoverType;
    }
}