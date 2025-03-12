# 膳食规划系统 - 饮食因素模块

## 概述

饮食因素模块是膳食规划系统的一个可扩展组件，用于评估食物是否符合用户的各种饮食需求和偏好。该模块采用插件式架构，允许轻松添加新的饮食影响因素，而无需修改核心代码。

## 主要组件

### 1. DietaryFactor 接口

所有饮食因素都必须实现此接口，它定义了以下方法：

- `boolean isFoodSuitable(Food food, UserProfile userProfile)`: 检查食物是否符合该饮食因素的要求
- `double calculateScoreAdjustment(Food food, UserProfile userProfile)`: 计算食物在该饮食因素下的评分调整
- `String getName()`: 获取该饮食因素的名称
- `int getPriority()`: 获取该饮食因素的优先级
- `boolean isMandatory()`: 判断该因素是否为强制性的

### 2. DietaryFactorRegistry 类

饮食因素注册表，用于管理所有饮食影响因素。它提供以下功能：

- 注册新的饮食因素
- 移除现有的饮食因素
- 获取所有注册的饮食因素
- 获取所有强制性/非强制性的饮食因素
- 重置为默认的饮食因素

### 3. DietaryEvaluator 类

饮食评估器，用于评估食物是否符合用户的饮食要求，并计算食物的偏好评分。它提供以下功能：

- 检查食物是否符合用户的饮食要求
- 计算食物的偏好评分调整系数
- 注册和移除饮食因素

## 内置饮食因素

系统内置了以下饮食因素：

1. **AllergyFactor**: 检查食物是否含有用户过敏的成分
2. **ReligiousRestrictionFactor**: 检查食物是否符合用户的宗教饮食限制
3. **DislikedFoodFactor**: 检查食物是否是用户不喜欢的
4. **SpicyLevelFactor**: 检查食物的辣度是否符合用户的接受范围
5. **FlavorPreferenceFactor**: 根据用户的口味偏好调整食物评分
6. **CookingMethodFactor**: 根据用户偏好的烹饪方式调整食物评分
7. **HealthConditionFactor**: 根据用户的健康状况调整食物评分

## 如何使用

### 基本使用

```java
// 创建用户档案
UserProfile userProfile = new UserProfile(...);

// 创建饮食评估器
DietaryEvaluator evaluator = new DietaryEvaluator(userProfile);

// 检查食物是否适合用户
Food food = ...;
boolean isSuitable = evaluator.isFoodSuitable(food);

// 计算食物的偏好评分
double preferenceScore = evaluator.calculatePreferenceScore(food);
```

### 在膳食优化器中使用

```java
// 创建膳食优化器
MealOptimizer optimizer = new MealOptimizer(userProfile);

// 设置偏好因素权重（0-1之间）
optimizer.setPreferenceWeight(0.3);

// 优化膳食
List<Food> optimizedMeal = optimizer.optimizeMeal(foods, targetNutrients);
```

## 如何扩展

### 创建新的饮食因素

1. 创建一个实现 `DietaryFactor` 接口的新类
2. 实现所有必需的方法
3. 注册该饮食因素

示例：

```java
public class MyCustomFactor implements DietaryFactor {
    
    private static final int PRIORITY = 70; // 设置优先级
    
    @Override
    public boolean isFoodSuitable(Food food, UserProfile userProfile) {
        // 实现检查逻辑
        return true;
    }
    
    @Override
    public double calculateScoreAdjustment(Food food, UserProfile userProfile) {
        // 实现评分调整逻辑
        return 1.0;
    }
    
    @Override
    public String getName() {
        return "我的自定义因素";
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
    
    @Override
    public boolean isMandatory() {
        return false; // 是否为强制性因素
    }
}
```

### 注册新的饮食因素

```java
// 创建新的饮食因素
MyCustomFactor customFactor = new MyCustomFactor();

// 方法1：通过饮食评估器注册
DietaryEvaluator evaluator = new DietaryEvaluator(userProfile);
evaluator.registerFactor(customFactor);

// 方法2：通过饮食因素注册表注册
DietaryFactorRegistry registry = DietaryFactorRegistry.getInstance();
registry.registerFactor(customFactor);
```

## 优先级说明

饮食因素的优先级决定了它们在评估过程中的顺序。优先级范围为0-100，数值越大优先级越高。内置因素的优先级如下：

- AllergyFactor: 100（最高优先级）
- ReligiousRestrictionFactor: 95
- DislikedFoodFactor: 90
- SpicyLevelFactor: 80
- HealthConditionFactor: 85
- FlavorPreferenceFactor: 60
- CookingMethodFactor: 50

## 强制性与非强制性因素

- **强制性因素**：如果食物不符合强制性因素的要求，它将被直接排除在食谱之外。例如，过敏原、宗教限制等。
- **非强制性因素**：这些因素不会排除食物，但会影响食物的评分。例如，口味偏好、烹饪方式等。 