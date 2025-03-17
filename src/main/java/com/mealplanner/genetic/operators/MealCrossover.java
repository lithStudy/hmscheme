package com.mealplanner.genetic.operators;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.model.FoodCategory;

import java.util.*;

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
     * 对两个解决方案进行交叉操作
     * @param parent1 父代解决方案1
     * @param parent2 父代解决方案2
     * @return 交叉操作产生的两个子代解决方案列表
     */
    public List<MealSolution> apply(MealSolution parent1, MealSolution parent2) {
        // 如果随机值大于交叉率，不执行交叉，直接返回父代的拷贝
        if (Math.random() > crossoverRate) {
            List<MealSolution> offspring = new ArrayList<>();
            offspring.add(parent1.copy());
            offspring.add(parent2.copy());
            return offspring;
        }
        
        // 执行交叉操作
        return performCrossover(parent1, parent2);
    }
    
    /**
     * 执行交叉操作的具体实现
     * @param parent1 父代解决方案1
     * @param parent2 父代解决方案2
     * @return 交叉操作产生的两个子代解决方案列表
     */
    private List<MealSolution> performCrossover(MealSolution parent1, MealSolution parent2) {
        List<MealSolution> offspring = new ArrayList<>();
        
        // 创建两个子代的基因列表
        List<FoodGene> child1Genes = new ArrayList<>();
        List<FoodGene> child2Genes = new ArrayList<>();
        
        // 分别获取父代的主食和非主食基因
        List<FoodGene> parent1Staples = new ArrayList<>();
        List<FoodGene> parent1NonStaples = new ArrayList<>();
        List<FoodGene> parent2Staples = new ArrayList<>();
        List<FoodGene> parent2NonStaples = new ArrayList<>();
        
        // 分类父代1的基因
        for (FoodGene gene : parent1.getFoodGenes()) {
            if (FoodCategory.STAPLE.equals(gene.getFood().getCategory())) {
                parent1Staples.add(gene);
            } else {
                parent1NonStaples.add(gene);
            }
        }
        
        // 分类父代2的基因
        for (FoodGene gene : parent2.getFoodGenes()) {
            if (FoodCategory.STAPLE.equals(gene.getFood().getCategory())) {
                parent2Staples.add(gene);
            } else {
                parent2NonStaples.add(gene);
            }
        }
        
        // 随机决定主食的交叉方式
        Random random = new Random();
        
        // 确保每个子代有且只有一个主食
        if (!parent1Staples.isEmpty() && !parent2Staples.isEmpty()) {
            // 两个父代都有主食
            if (random.nextBoolean()) {
                // 子代1继承父代1的主食，子代2继承父代2的主食
                child1Genes.add(parent1Staples.get(0).copy());
                child2Genes.add(parent2Staples.get(0).copy());
            } else {
                // 子代1继承父代2的主食，子代2继承父代1的主食
                child1Genes.add(parent2Staples.get(0).copy());
                child2Genes.add(parent1Staples.get(0).copy());
            }
        } else if (!parent1Staples.isEmpty()) {
            // 只有父代1有主食，两个子代都继承父代1的主食
            FoodGene staple = parent1Staples.get(0).copy();
            child1Genes.add(staple);
            child2Genes.add(staple.copy());
        } else if (!parent2Staples.isEmpty()) {
            // 只有父代2有主食，两个子代都继承父代2的主食
            FoodGene staple = parent2Staples.get(0).copy();
            child1Genes.add(staple);
            child2Genes.add(staple.copy());
        }
        
        // 对非主食基因执行单点交叉
        int parent1Size = parent1NonStaples.size();
        int parent2Size = parent2NonStaples.size();
        
        if (parent1Size > 0 && parent2Size > 0) {
            // 选择交叉点
            int crossoverPoint1 = random.nextInt(parent1Size);
            int crossoverPoint2 = random.nextInt(parent2Size);
            
            // 构建子代1：父代1前半部分 + 父代2后半部分
            for (int i = 0; i < crossoverPoint1; i++) {
                child1Genes.add(parent1NonStaples.get(i).copy());
            }
            
            for (int i = crossoverPoint2; i < parent2Size; i++) {
                child1Genes.add(parent2NonStaples.get(i).copy());
            }
            
            // 构建子代2：父代2前半部分 + 父代1后半部分
            for (int i = 0; i < crossoverPoint2; i++) {
                child2Genes.add(parent2NonStaples.get(i).copy());
            }
            
            for (int i = crossoverPoint1; i < parent1Size; i++) {
                child2Genes.add(parent1NonStaples.get(i).copy());
            }
        } else if (parent1Size > 0) {
            // 父代2没有非主食基因，子代均分父代1的非主食基因
            int midPoint = parent1Size / 2;
            
            for (int i = 0; i < midPoint; i++) {
                child1Genes.add(parent1NonStaples.get(i).copy());
            }
            
            for (int i = midPoint; i < parent1Size; i++) {
                child2Genes.add(parent1NonStaples.get(i).copy());
            }
        } else if (parent2Size > 0) {
            // 父代1没有非主食基因，子代均分父代2的非主食基因
            int midPoint = parent2Size / 2;
            
            for (int i = 0; i < midPoint; i++) {
                child1Genes.add(parent2NonStaples.get(i).copy());
            }
            
            for (int i = midPoint; i < parent2Size; i++) {
                child2Genes.add(parent2NonStaples.get(i).copy());
            }
        }
        
        // 移除重复食物
        removeDuplicateFoods(child1Genes);
        removeDuplicateFoods(child2Genes);
        
        // 创建子代解决方案
        offspring.add(new MealSolution(child1Genes));
        offspring.add(new MealSolution(child2Genes));
        
        return offspring;
    }
    
    /**
     * 移除基因列表中的重复食物
     * @param genes 基因列表
     */
    private void removeDuplicateFoods(List<FoodGene> genes) {
        Set<String> foodNames = new HashSet<>();
        Iterator<FoodGene> iterator = genes.iterator();
        
        while (iterator.hasNext()) {
            FoodGene gene = iterator.next();
            if (!foodNames.add(gene.getFood().getName())) {
                iterator.remove();
            }
        }
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