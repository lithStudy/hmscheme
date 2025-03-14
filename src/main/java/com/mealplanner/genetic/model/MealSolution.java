package com.mealplanner.genetic.model;

import com.mealplanner.model.Food;
import com.mealplanner.model.FoodCategory;
import com.mealplanner.model.Nutrition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表示一个膳食解决方案（相当于遗传算法中的染色体）
 */
public class MealSolution {
    // 解决方案中包含的食物及其摄入量
    private List<FoodGene> foodGenes;
    
    // 非支配排序的等级
    private int rank;
    
    // 拥挤度距离
    private double crowdingDistance;
    
    // 目标值列表
    private List<ObjectiveValue> objectiveValues;
    
    // 缓存的营养素总和，避免重复计算
    private Nutrition cachedTotalNutrients;
    
    /**
     * 构造函数
     * @param foodGenes 食物基因列表
     */
    public MealSolution(List<FoodGene> foodGenes) {
        this.foodGenes = new ArrayList<>(foodGenes);
        this.rank = 0;
        this.crowdingDistance = 0;
        this.objectiveValues = new ArrayList<>();
        this.cachedTotalNutrients = null;
    }
    
    /**
     * 创建随机的膳食解决方案
     * @param foodDatabase 食物数据库
     * @param minFoods 最少食物数量
     * @param maxFoods 最多食物数量
     * @param requireStaple 是否需要主食
     * @return 随机创建的膳食解决方案
     */
    public static MealSolution createRandom(List<Food> foodDatabase, int minFoods, int maxFoods, boolean requireStaple) {
        if (foodDatabase == null || foodDatabase.isEmpty()) {
            throw new IllegalArgumentException("食物数据库不能为空");
        }
        
        Random random = new Random();
        List<FoodGene> genes = new ArrayList<>();
        
        // 食物数量
        int foodCount = random.nextInt(maxFoods - minFoods + 1) + minFoods;
        
        // 如果需要主食，先添加一个主食
        if (requireStaple) {
            // 筛选所有主食
            List<Food> staples = foodDatabase.stream()
                    .filter(food -> FoodCategory.STAPLE.equals(food.getCategory()))
                    .collect(Collectors.toList());
            
            if (!staples.isEmpty()) {
                // 随机选择一个主食
                Food staple = staples.get(random.nextInt(staples.size()));
                
                // 为主食随机生成一个在推荐范围内的摄入量
                double minIntake = staple.getRecommendedIntakeRange().getMinIntake();
                double maxIntake = staple.getRecommendedIntakeRange().getMaxIntake();
                double intake = minIntake + random.nextDouble() * (maxIntake - minIntake);
                
                // 将摄入量四舍五入为整数
                intake = Math.round(intake);
                
                // 添加主食基因
                genes.add(new FoodGene(staple, intake));
                
                // 减少需要随机选择的食物数量
                foodCount--;
            }
        }
        
        // 创建候选食物列表，排除已选择的食物
        List<Food> candidateFoods = new ArrayList<>(foodDatabase);
        candidateFoods.removeAll(genes.stream().map(FoodGene::getFood).collect(Collectors.toList()));
        
        // 随机选择其余食物
        for (int i = 0; i < foodCount && !candidateFoods.isEmpty(); i++) {
            // 随机选择一个食物
            int index = random.nextInt(candidateFoods.size());
            Food food = candidateFoods.get(index);
            
            // 为食物随机生成一个在推荐范围内的摄入量
            double minIntake = food.getRecommendedIntakeRange().getMinIntake();
            double maxIntake = food.getRecommendedIntakeRange().getMaxIntake();
            double intake = minIntake + random.nextDouble() * (maxIntake - minIntake);
            
            // 将摄入量四舍五入为整数
            intake = Math.round(intake);
            
            // 添加食物基因
            genes.add(new FoodGene(food, intake));
            
            // 从候选食物列表中移除已选食物
            candidateFoods.remove(index);
        }
        
        return new MealSolution(genes);
    }
    
    /**
     * 创建解决方案的深拷贝
     * @return 解决方案的拷贝
     */
    public MealSolution copy() {
        List<FoodGene> genesCopy = foodGenes.stream()
                .map(FoodGene::copy)
                .collect(Collectors.toList());
        
        MealSolution copy = new MealSolution(genesCopy);
        copy.rank = this.rank;
        copy.crowdingDistance = this.crowdingDistance;
        
        if (this.objectiveValues != null) {
            copy.objectiveValues = this.objectiveValues.stream()
                    .map(ObjectiveValue::copy)
                    .collect(Collectors.toList());
        }
        
        return copy;
    }
    
    /**
     * 验证解决方案是否有效
     * @param requireStaple 是否需要主食
     * @return 是否有效
     */
    public boolean isValid(boolean requireStaple) {
        // 检查是否有足够的食物
        if (foodGenes.isEmpty()) {
            return false;
        }
        
        // 检查是否包含主食（如果需要）
        if (requireStaple) {
            boolean hasStaple = foodGenes.stream()
                    .anyMatch(gene -> FoodCategory.STAPLE.equals(gene.getFood().getCategory()));
            
            if (!hasStaple) {
                return false;
            }
        }
        
        // 检查是否有重复食物
        Set<String> foodNames = new HashSet<>();
        for (FoodGene gene : foodGenes) {
            if (!foodNames.add(gene.getFood().getName())) {
                return false; // 有重复食物
            }
        }
        
        // 检查所有食物的摄入量是否在合理范围内
        for (FoodGene gene : foodGenes) {
            double intake = gene.getIntake();
            double minIntake = gene.getFood().getRecommendedIntakeRange().getMinIntake();
            double maxIntake = gene.getFood().getRecommendedIntakeRange().getMaxIntake();
            
            if (intake < minIntake || intake > maxIntake) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 计算解决方案的总营养素
     * @return 总营养素
     */
    public Nutrition calculateTotalNutrients() {
        // 如果已经计算过，直接返回缓存结果
        if (cachedTotalNutrients != null) {
            return cachedTotalNutrients;
        }
        
        double totalCalories = 0;
        double totalCarbs = 0;
        double totalProtein = 0;
        double totalFat = 0;
        double totalCalcium = 0;
        double totalPotassium = 0;
        double totalSodium = 0;
        double totalMagnesium = 0;
        
        for (FoodGene gene : foodGenes) {
            Food food = gene.getFood();
            double intake = gene.getIntake();
            double ratio = intake / 100.0; // 食物营养成分通常以每100g为单位
            
            totalCalories += food.getNutrition().getCalories() * ratio;
            totalCarbs += food.getNutrition().getCarbohydrates() * ratio;
            totalProtein += food.getNutrition().getProtein() * ratio;
            totalFat += food.getNutrition().getFat() * ratio;
            totalCalcium += food.getNutrition().getCalcium() * ratio;
            totalPotassium += food.getNutrition().getPotassium() * ratio;
            totalSodium += food.getNutrition().getSodium() * ratio;
            totalMagnesium += food.getNutrition().getMagnesium() * ratio;
        }
        
        cachedTotalNutrients = new Nutrition(
                totalCalories,
                totalCarbs,
                totalProtein,
                totalFat,
                totalCalcium,
                totalPotassium,
                totalSodium,
                totalMagnesium
        );
        
        return cachedTotalNutrients;
    }
    
    /**
     * 添加食物
     * @param foodGene 要添加的食物基因
     */
    public void addFood(FoodGene foodGene) {
        foodGenes.add(foodGene);
        // 清除缓存
        cachedTotalNutrients = null;
    }
    
    /**
     * 移除食物
     * @param index 要移除的食物索引
     */
    public void removeFood(int index) {
        if (index >= 0 && index < foodGenes.size()) {
            foodGenes.remove(index);
            // 清除缓存
            cachedTotalNutrients = null;
        }
    }
    
    /**
     * 更新食物摄入量
     * @param index 食物索引
     * @param newIntake 新的摄入量
     */
    public void updateFoodIntake(int index, double newIntake) {
        if (index >= 0 && index < foodGenes.size()) {
            FoodGene gene = foodGenes.get(index);
            gene.setIntake(newIntake);
            // 清除缓存
            cachedTotalNutrients = null;
        }
    }
    
    /**
     * 获取食物类别统计
     * @return 每个类别的食物数量
     */
    public Map<FoodCategory, Integer> getFoodCategoryCount() {
        Map<FoodCategory, Integer> categoryCount = new HashMap<>();
        
        for (FoodGene gene : foodGenes) {
            FoodCategory category = gene.getFood().getCategory();
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }
        
        return categoryCount;
    }
    
    /**
     * 检查该解决方案是否支配另一个解决方案
     * @param other 另一个解决方案
     * @return 是否支配
     */
    public boolean dominates(MealSolution other) {
        boolean atLeastOneBetter = false;
        
        // 比较每个目标值
        for (int i = 0; i < objectiveValues.size(); i++) {
            ObjectiveValue thisObj = objectiveValues.get(i);
            ObjectiveValue otherObj = other.objectiveValues.get(i);
            
            // 如果本解在任何目标上更差，则不支配other
            if (thisObj.getValue() < otherObj.getValue()) {
                return false;
            }
            
            // 检查是否至少在一个目标上更好
            if (thisObj.getValue() > otherObj.getValue()) {
                atLeastOneBetter = true;
            }
        }
        
        return atLeastOneBetter;
    }
    
    // Getters and Setters
    
    public List<FoodGene> getFoodGenes() {
        return foodGenes;
    }
    
    public void setFoodGenes(List<FoodGene> foodGenes) {
        this.foodGenes = foodGenes;
        // 清除缓存
        cachedTotalNutrients = null;
    }
    
    public int getRank() {
        return rank;
    }
    
    public void setRank(int rank) {
        this.rank = rank;
    }
    
    public double getCrowdingDistance() {
        return crowdingDistance;
    }
    
    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }
    
    public List<ObjectiveValue> getObjectiveValues() {
        return objectiveValues;
    }
    
    public void setObjectiveValues(List<ObjectiveValue> objectiveValues) {
        this.objectiveValues = objectiveValues;
    }
    
    public ObjectiveValue getObjectiveValue(int index) {
        if (index >= 0 && index < objectiveValues.size()) {
            return objectiveValues.get(index);
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MealSolution[rank=").append(rank)
          .append(", crowdingDistance=").append(crowdingDistance)
          .append(", foods=[");
        
        for (FoodGene gene : foodGenes) {
            sb.append(gene.getFood().getName())
              .append("(").append(String.format("%d", (int)gene.getIntake())).append("g), ");
        }
        
        if (!foodGenes.isEmpty()) {
            sb.setLength(sb.length() - 2); // 移除最后的逗号和空格
        }
        
        sb.append("], objectives=[");
        
        for (ObjectiveValue obj : objectiveValues) {
            sb.append(obj.getName()).append("=")
              .append(String.format("%.3f", obj.getValue())).append(", ");
        }
        
        if (!objectiveValues.isEmpty()) {
            sb.setLength(sb.length() - 2); // 移除最后的逗号和空格
        }
        
        sb.append("]]");
        
        return sb.toString();
    }
} 