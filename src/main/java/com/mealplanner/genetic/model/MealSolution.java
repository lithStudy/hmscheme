package com.mealplanner.genetic.model;

import com.mealplanner.model.Food;
import com.mealplanner.model.FoodCategory;
import com.mealplanner.model.NutrientType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 膳食解决方案类（遗传算法中的个体/染色体）
 * 表示一个完整的膳食搭配方案，包含多种食物及其摄入量
 * 每个解决方案都有对应的目标函数值、非支配排名和拥挤度距离
 * 用于NSGA-II多目标优化算法中的个体表示和操作
 */
public class MealSolution {
    /** 解决方案中包含的食物基因列表，每个基因代表一种食物及其摄入量 */
    private List<FoodGene> foodGenes;
    
    /** 非支配排序等级，数值越小表示解的质量越好（1为最优前沿） */
    private int rank;
    
    /** 拥挤度距离，用于保持解的多样性，距离越大表示该解在目标空间中越独特 */
    private double crowdingDistance;
    
    /** 目标函数值列表，包含营养、偏好、多样性等多个优化目标的评分 */
    private List<ObjectiveValue> objectiveValues;
    
    /** 缓存的营养素总量，避免重复计算提高性能 */
    private Map<NutrientType, Double> cachedTotalNutrients;
    
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
     * 创建随机膳食解决方案的工厂方法
     * 根据指定约束条件随机生成一个有效的膳食搭配方案
     * 支持主食约束、食物数量限制和类别概率选择
     * @param foodDatabase 可选择的食物数据库
     * @param minFoods 膳食中最少包含的食物种类数
     * @param maxFoods 膳食中最多包含的食物种类数
     * @param requireStaple 是否要求必须包含一个主食
     * @return 随机生成的有效膳食解决方案
     */
    public static MealSolution createRandom(List<Food> foodDatabase, int minFoods, int maxFoods, boolean requireStaple) {
        if (foodDatabase == null || foodDatabase.isEmpty()) {
            throw new IllegalArgumentException("食物数据库不能为空");
        }
        
        Random random = new Random();
        List<FoodGene> genes = new ArrayList<>();
        
        // 确定本次生成的食物种类总数
        int foodCount = random.nextInt(maxFoods - minFoods + 1) + minFoods;
        
        // 第一步：如果要求主食，优先添加一个主食
        if (requireStaple) {
            // 从数据库中筛选出所有主食类食物
            List<Food> staples = foodDatabase.stream()
                    .filter(food -> FoodCategory.STAPLE.equals(food.getCategory()))
                    .collect(Collectors.toList());
            
            if (!staples.isEmpty()) {
                // 随机选择一个主食
                Food staple = staples.get(random.nextInt(staples.size()));
                
                // 在推荐摄入量范围内随机生成摄入量
                double minIntake = staple.getRecommendedIntakeRange().getMinIntake();
                double maxIntake = staple.getRecommendedIntakeRange().getMaxIntake();
                double intake = minIntake + random.nextDouble() * (maxIntake - minIntake);
                
                // 摄入量取整（克为单位）
                intake = Math.round(intake);
                
                // 创建主食基因并加入解决方案
                genes.add(new FoodGene(staple, intake));
                
                // 剩余需要选择的食物数量减1
                foodCount--;
            }
        }
        
        // 第二步：准备剩余食物的候选列表
        List<Food> candidateFoods = new ArrayList<>(foodDatabase);
        // 移除已选择的食物，避免重复
        candidateFoods.removeAll(genes.stream().map(FoodGene::getFood).collect(Collectors.toList()));
        
        // 如果要求主食唯一性，从候选列表中移除所有剩余主食
        if (requireStaple) {
            candidateFoods.removeIf(food -> FoodCategory.STAPLE.equals(food.getCategory()));
        }
        
        // 按食物类别对候选食物进行分组，便于按类别概率选择
        Map<FoodCategory, List<Food>> foodsByCategory = candidateFoods.stream()
                .collect(Collectors.groupingBy(Food::getCategory));
        
        // 第三步：基于类别概率随机选择剩余食物
        for (int i = 0; i < foodCount && !candidateFoods.isEmpty(); i++) {
            // 根据各类别的选择概率权重选择食物类别
            FoodCategory selectedCategory = selectCategoryByProbability(foodsByCategory.keySet(), random);
            
            // 处理类别选择失败或该类别食物已用完的情况
            if (selectedCategory == null || foodsByCategory.get(selectedCategory) == null || foodsByCategory.get(selectedCategory).isEmpty()) {
                // 重新筛选出仍有可选食物的类别
                List<FoodCategory> availableCategories = foodsByCategory.keySet().stream()
                        .filter(category -> foodsByCategory.get(category) != null && !foodsByCategory.get(category).isEmpty())
                        .collect(Collectors.toList());
                
                if (availableCategories.isEmpty()) {
                    break; // 所有食物都已选完，提前结束
                }
                
                // 从可用类别中随机选择一个
                selectedCategory = availableCategories.get(random.nextInt(availableCategories.size()));
            }
            
            // 从选定类别中随机选择一个具体食物
            List<Food> foodsInCategory = foodsByCategory.get(selectedCategory);
            Food selectedFood = foodsInCategory.get(random.nextInt(foodsInCategory.size()));
            
            // 在该食物的推荐摄入量范围内随机生成摄入量
            double minIntake = selectedFood.getRecommendedIntakeRange().getMinIntake();
            double maxIntake = selectedFood.getRecommendedIntakeRange().getMaxIntake();
            double intake = minIntake + random.nextDouble() * (maxIntake - minIntake);
            
            // 摄入量取整
            intake = Math.round(intake);
            
            // 创建食物基因并加入解决方案
            genes.add(new FoodGene(selectedFood, intake));
            
            // 从候选列表中移除已选食物，防止重复选择
            foodsInCategory.remove(selectedFood);
            candidateFoods.remove(selectedFood);
            
            // 如果该类别的食物全部用完，从分组中移除该类别
            if (foodsInCategory.isEmpty()) {
                foodsByCategory.remove(selectedCategory);
            }
        }
        
        return new MealSolution(genes);
    }
    
    /**
     * 基于概率权重选择食物类别
     * 实现轮盘赌选择算法，概率越高的类别越容易被选中
     * @param categories 可选的食物类别集合
     * @param random 随机数生成器
     * @return 被选中的食物类别，如果输入为空则返回null
     */
    private static FoodCategory selectCategoryByProbability(Set<FoodCategory> categories, Random random) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        
        // 计算所有可用类别的选择概率总和
        double totalProbability = categories.stream()
                .mapToDouble(FoodCategory::getSelectionProbability)
                .sum();
        
        // 如果总概率为0或负数，则改为等概率随机选择
        if (totalProbability <= 0) {
            List<FoodCategory> categoryList = new ArrayList<>(categories);
            return categoryList.get(random.nextInt(categoryList.size()));
        }
        
        // 生成[0, totalProbability)范围内的随机值
        double randomValue = random.nextDouble() * totalProbability;
        
        // 轮盘赌选择：累计概率达到随机值时选中对应类别
        double cumulativeProbability = 0.0;
        for (FoodCategory category : categories) {
            cumulativeProbability += category.getSelectionProbability();
            if (randomValue <= cumulativeProbability) {
                return category;
            }
        }
        
        // 防止浮点数精度问题导致没有选中，返回任意一个类别
        return categories.iterator().next();
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
     * 验证膳食解决方案的有效性
     * 检查解决方案是否满足所有约束条件和业务规则
     * @param requireStaple 是否要求必须包含主食
     * @return true表示解决方案有效，false表示违反了某些约束
     */
    public boolean isValid(boolean requireStaple) {
        // 约束1：解决方案不能为空
        if (foodGenes.isEmpty()) {
            return false;
        }
        
        // 约束2：主食约束检查
        if (requireStaple) {
            // 统计主食数量
            long stapleCount = foodGenes.stream()
                    .filter(gene -> FoodCategory.STAPLE.equals(gene.getFood().getCategory()))
                    .count();
            
            // 必须有且仅有一个主食
            if (stapleCount != 1) {
                return false;
            }
        }
        
        // 约束3：食物唯一性检查（不能有重复食物）
        Set<String> foodNames = new HashSet<>();
        for (FoodGene gene : foodGenes) {
            if (!foodNames.add(gene.getFood().getName())) {
                return false; // 发现重复食物
            }
        }
        
        // 约束4：摄入量范围检查（每种食物的摄入量必须在推荐范围内）
        for (FoodGene gene : foodGenes) {
            double intake = gene.getIntake();
            double minIntake = gene.getFood().getRecommendedIntakeRange().getMinIntake();
            double maxIntake = gene.getFood().getRecommendedIntakeRange().getMaxIntake();
            
            if (intake < minIntake || intake > maxIntake) {
                return false; // 摄入量超出合理范围
            }
        }
        
        return true; // 通过所有约束检查
    }
    
    /**
     * 计算膳食解决方案的总营养素含量
     * 将所有食物基因的营养贡献累加，得到整个膳食的营养素总量
     * 使用缓存机制避免重复计算，提高性能
     * @return 包含所有营养素类型及其总量的映射表
     */
    public Map<NutrientType, Double> calculateTotalNutrients() {
        // 如果已经计算过且缓存有效，直接返回缓存结果
        if (cachedTotalNutrients != null) {
            return cachedTotalNutrients;
        }
        
        // 初始化所有营养素类型的总量为0
        Map<NutrientType, Double> totalNutrients = NutrientType.initNutrientItem();

        // 遍历每个食物基因，累加其营养贡献
        for (FoodGene gene : foodGenes) {
            Food food = gene.getFood();
            double intake = gene.getIntake(); // 实际摄入量（克）
            double ratio = intake / 100.0;   // 营养成分数据通常基于每100g，需要换算比例
            
            // 累加该食物对各营养素的贡献
            for (NutrientType type : food.getNutritionItems().keySet()) {
                double nutritionPer100g = food.getNutritionItems().get(type);
                double actualNutrition = nutritionPer100g * ratio;
                totalNutrients.merge(type, actualNutrition, Double::sum);
            }
        }
        
        // 缓存计算结果，避免重复计算
        cachedTotalNutrients = totalNutrients;
        return totalNutrients;
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
     * 判断当前解决方案是否帕累托支配另一个解决方案
     * 支配关系定义：在所有目标上都不劣于对方，且至少在一个目标上严格优于对方
     * 用于NSGA-II算法中的非支配排序
     * @param other 待比较的另一个膳食解决方案
     * @return true表示当前解支配other，false表示不支配
     */
    public boolean dominates(MealSolution other) {
        boolean atLeastOneBetter = false; // 标记是否至少在一个目标上更优
        
        // 逐一比较所有目标函数值
        for (int i = 0; i < objectiveValues.size(); i++) {
            ObjectiveValue thisObj = objectiveValues.get(i);
            ObjectiveValue otherObj = other.objectiveValues.get(i);

            // 跳过权重为0的目标（不参与比较）
            if (thisObj.getWeight() <= 0) {
                continue;
            }
            
            // 如果当前解在任何目标上更差，则不能支配对方
            if (thisObj.getValue() < otherObj.getValue()) {
                return false;
            }
            
            // 记录是否在某个目标上严格更优
            if (thisObj.getValue() > otherObj.getValue()) {
                atLeastOneBetter = true;
            }
        }
        
        // 只有在所有目标都不劣于对方，且至少一个目标严格更优时，才构成支配关系
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