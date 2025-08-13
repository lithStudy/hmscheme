package com.mealplanner.genetic.operators;

import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.model.FoodCategory;

import java.util.*;

/**
 * 膳食解决方案交叉操作器
 * 实现遗传算法中的交叉（杂交）操作，通过组合两个父代解决方案的基因
 * 来产生新的子代解决方案，保持膳食搭配的合理性和约束条件
 */
public class MealCrossover {
    /** 交叉概率，决定交叉操作的执行频率 */
    private double crossoverRate;
    
    /**
     * 交叉操作类型枚举
     * 定义不同的基因重组策略
     */
    public enum CrossoverType {
        UNIFORM,      // 均匀交叉：每个基因位置独立决定来源
        ONE_POINT,    // 单点交叉：在一个切割点分割基因序列
        TWO_POINT,    // 两点交叉：在两个切割点间交换基因片段
        BLENDING      // 混合交叉：基因数值的加权混合
    }
    
    /** 默认交叉类型 */
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
     * 对两个父代解决方案执行交叉操作
     * 根据交叉概率决定是否进行基因重组，保证主食约束和食物唯一性
     * @param parent1 第一个父代膳食解决方案
     * @param parent2 第二个父代膳食解决方案
     * @return 交叉操作产生的两个子代解决方案列表
     */
    public List<MealSolution> apply(MealSolution parent1, MealSolution parent2) {
        // 基于交叉概率决定是否执行交叉操作
        if (Math.random() > crossoverRate) {
            // 不执行交叉，直接返回父代的深拷贝
            List<MealSolution> offspring = new ArrayList<>();
            offspring.add(parent1.copy());
            offspring.add(parent2.copy());
            return offspring;
        }
        
        // 执行实际的交叉操作
        return performCrossover(parent1, parent2);
    }
    
    /**
     * 执行交叉操作的核心实现
     * 采用分层交叉策略：主食单独处理，非主食进行单点交叉
     * 确保每个子代都有合理的膳食结构
     * @param parent1 第一个父代解决方案
     * @param parent2 第二个父代解决方案
     * @return 交叉后产生的两个子代解决方案列表
     */
    private List<MealSolution> performCrossover(MealSolution parent1, MealSolution parent2) {
        List<MealSolution> offspring = new ArrayList<>();
        
        // 初始化两个子代的基因列表
        List<FoodGene> child1Genes = new ArrayList<>();
        List<FoodGene> child2Genes = new ArrayList<>();
        
        // 分别存储父代的主食和非主食基因
        List<FoodGene> parent1Staples = new ArrayList<>();
        List<FoodGene> parent1NonStaples = new ArrayList<>();
        List<FoodGene> parent2Staples = new ArrayList<>();
        List<FoodGene> parent2NonStaples = new ArrayList<>();
        
        // 将父代1的基因按类型分类
        for (FoodGene gene : parent1.getFoodGenes()) {
            if (FoodCategory.STAPLE.equals(gene.getFood().getCategory())) {
                parent1Staples.add(gene);
            } else {
                parent1NonStaples.add(gene);
            }
        }
        
        // 将父代2的基因按类型分类
        for (FoodGene gene : parent2.getFoodGenes()) {
            if (FoodCategory.STAPLE.equals(gene.getFood().getCategory())) {
                parent2Staples.add(gene);
            } else {
                parent2NonStaples.add(gene);
            }
        }
        
        // 处理主食基因的交叉
        Random random = new Random();
        
        // 确保每个子代都有且只有一个主食（膳食约束）
        if (!parent1Staples.isEmpty() && !parent2Staples.isEmpty()) {
            // 情况1：两个父代都有主食 - 随机分配主食给子代
            if (random.nextBoolean()) {
                // 直接继承：子代1←父代1主食，子代2←父代2主食
                child1Genes.add(parent1Staples.get(0).copy());
                child2Genes.add(parent2Staples.get(0).copy());
            } else {
                // 交叉继承：子代1←父代2主食，子代2←父代1主食
                child1Genes.add(parent2Staples.get(0).copy());
                child2Genes.add(parent1Staples.get(0).copy());
            }
        } else if (!parent1Staples.isEmpty()) {
            // 情况2：只有父代1有主食 - 两个子代都继承此主食
            FoodGene staple = parent1Staples.get(0).copy();
            child1Genes.add(staple);
            child2Genes.add(staple.copy());
        } else if (!parent2Staples.isEmpty()) {
            // 情况3：只有父代2有主食 - 两个子代都继承此主食
            FoodGene staple = parent2Staples.get(0).copy();
            child1Genes.add(staple);
            child2Genes.add(staple.copy());
        }
        // 情况4：两个父代都没有主食 - 子代也没有主食
        
        // 对非主食基因执行单点交叉操作
        int parent1Size = parent1NonStaples.size();
        int parent2Size = parent2NonStaples.size();
        
        if (parent1Size > 0 && parent2Size > 0) {
            // 情况1：两个父代都有非主食基因 - 执行单点交叉
            
            // 为每个父代随机选择一个交叉点
            int crossoverPoint1 = random.nextInt(parent1Size);
            int crossoverPoint2 = random.nextInt(parent2Size);
            
            // 构建子代1：父代1的前半部分 + 父代2的后半部分
            for (int i = 0; i < crossoverPoint1; i++) {
                child1Genes.add(parent1NonStaples.get(i).copy());
            }
            for (int i = crossoverPoint2; i < parent2Size; i++) {
                child1Genes.add(parent2NonStaples.get(i).copy());
            }
            
            // 构建子代2：父代2的前半部分 + 父代1的后半部分
            for (int i = 0; i < crossoverPoint2; i++) {
                child2Genes.add(parent2NonStaples.get(i).copy());
            }
            for (int i = crossoverPoint1; i < parent1Size; i++) {
                child2Genes.add(parent1NonStaples.get(i).copy());
            }
            
        } else if (parent1Size > 0) {
            // 情况2：只有父代1有非主食基因 - 两个子代均分这些基因
            int midPoint = parent1Size / 2;
            
            for (int i = 0; i < midPoint; i++) {
                child1Genes.add(parent1NonStaples.get(i).copy());
            }
            for (int i = midPoint; i < parent1Size; i++) {
                child2Genes.add(parent1NonStaples.get(i).copy());
            }
            
        } else if (parent2Size > 0) {
            // 情况3：只有父代2有非主食基因 - 两个子代均分这些基因
            int midPoint = parent2Size / 2;
            
            for (int i = 0; i < midPoint; i++) {
                child1Genes.add(parent2NonStaples.get(i).copy());
            }
            for (int i = midPoint; i < parent2Size; i++) {
                child2Genes.add(parent2NonStaples.get(i).copy());
            }
        }
        // 情况4：两个父代都没有非主食基因 - 子代也没有非主食基因
        
        // 清理重复食物，确保每个子代中食物的唯一性
        removeDuplicateFoods(child1Genes);
        removeDuplicateFoods(child2Genes);
        
        // 创建并返回两个子代解决方案
        offspring.add(new MealSolution(child1Genes));
        offspring.add(new MealSolution(child2Genes));
        
        return offspring;
    }
    
    /**
     * 移除基因列表中的重复食物
     * 使用食物名称作为唯一性标识，保留首次出现的食物基因
     * @param genes 待清理的基因列表
     */
    private void removeDuplicateFoods(List<FoodGene> genes) {
        Set<String> foodNames = new HashSet<>();
        Iterator<FoodGene> iterator = genes.iterator();
        
        while (iterator.hasNext()) {
            FoodGene gene = iterator.next();
            // 如果食物名称已存在，则移除重复的基因
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