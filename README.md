# 膳食规划与营养数据解析系统

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-11%2B-blue)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
[![Maven](https://img.shields.io/badge/Maven-3.6.3-green)](https://maven.apache.org/)

一个基于Java的膳食规划和营养数据解析系统，支持从Excel文件中读取详细的食物营养数据，并提供膳食规划功能。

## 📋 目录

- [功能特点](#功能特点)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [使用指南](#使用指南)
  - [营养数据解析](#营养数据解析)
  - [膳食规划](#膳食规划)
- [数据格式](#数据格式)
- [开发指南](#开发指南)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

## ✨ 功能特点

### 营养数据解析
- 使用EasyExcel高效解析Excel格式的食物营养数据
- 支持通过表头名称（而非列序号）获取数据，更加灵活
- 支持复杂的营养数据格式，包含大量营养元素
- 自动将数据转换为Java对象，便于程序处理

### 膳食规划
- 基于用户个人信息（年龄、性别、体重等）计算营养需求
- 考虑用户健康状况（如高血压、糖尿病等）调整营养目标
- 根据营养目标和食物数据库生成膳食计划
- 支持自定义营养目标和食物偏好
- 智能计算食物的最佳摄入量，根据膳食需求动态调整食物量
- 多轮优化算法，对整体膳食组合进行全局优化，确保营养平衡

## 🛠️ 技术栈

- **Java 11+**: 核心编程语言
- **Maven**: 项目管理和构建工具
- **EasyExcel**: 高效Excel文件处理库
- **Lombok**: 简化Java代码的工具库

## 📁 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── mealplanner/
│   │           ├── Food.java                  # 食物基本类
│   │           ├── Nutrition.java             # 营养素类
│   │           ├── Portion.java               # 食物份量类
│   │           ├── IntakeRange.java           # 摄入量范围类
│   │           ├── MealOptimizer.java         # 膳食优化器
│   │           ├── MealPlanner.java           # 膳食规划器
│   │           ├── NutritionCalculator.java   # 营养需求计算器
│   │           ├── UserProfile.java           # 用户档案类
│   │           ├── NutrientTarget.java        # 营养目标类
│   │           ├── NutrientTargetManager.java # 营养目标管理器
│   │           ├── Main.java                  # 主程序入口
│   │           └── foodmanage/                # 食物数据管理模块
│   │               ├── NutritionData.java     # 营养数据模型类
│   │               ├── NutritionDataParser.java # 营养数据解析器
│   │               └── EasyExcelExample.java  # EasyExcel使用示例
│   └── resources/
│       └── nutrition_data.xlsx               # 示例营养数据文件
└── test/
    └── java/
        └── com/
            └── mealplanner/
                └── ...                       # 测试类
```

## 🚀 快速开始

### 前提条件
- Java 11或更高版本
- Maven 3.6.3或更高版本

### 安装步骤

1. 克隆仓库
```bash
git clone https://github.com/lithStudy/hmscheme.git
cd hmscheme
```

2. 使用Maven构建项目
```bash
mvn clean install
```

3. 运行示例
```bash
java -cp target/meal-planner-1.0-SNAPSHOT.jar com.mealplanner.Main
```

## 📖 使用指南

### 营养数据解析

使用EasyExcel注解方式解析Excel文件：

```java
// 创建解析器
NutritionDataParser parser = new NutritionDataParser();

// 解析Excel文件
List<NutritionData> dataList = parser.parseNutritionDataFromFile("nutrition_data.xlsx");
System.out.println("成功解析 " + dataList.size() + " 条食物营养数据");

// 转换为Food对象
List<Food> foodList = parser.convertToFoodObjects(dataList);

// 导出为Excel文件
parser.exportToExcel(dataList, "exported_nutrition.xlsx");
```

### 膳食规划

基于用户信息和营养目标生成膳食计划：

```java
// 创建用户档案
UserProfile userProfile = new UserProfile(
    70.0,   // 体重(kg)
    170.0,  // 身高(cm)
    30,     // 年龄
    "male", // 性别
    1.55,   // 活动系数（中度活动）
    new String[]{"hypertension", "diabetes"} // 健康状况
);

// 创建膳食规划器
MealPlanner mealPlanner = new MealPlanner(userProfile);

// 生成每日膳食计划
DailyMealPlan dailyPlan = mealPlanner.generateDailyMealPlan();

// 打印膳食计划
System.out.println(dailyPlan.toString());
```

## 📊 数据格式

### 营养数据Excel格式

系统支持以下格式的Excel文件：

| 樣品編號 | 食品分類 | 樣品名稱 | 內容物描述 | 俗名 | 熱量(kcal) | 粗蛋白(g) | 粗脂肪(g) | 總碳水化合物(g) | 鈉(mg) | 鉀(mg) | 鈣(mg) | 鎂(mg) | ... |
|---------|---------|---------|-----------|-----|-----------|----------|----------|---------------|-------|-------|-------|-------|-----|
| A0100101 | 穀物類 | 大麥仁 | 小薏仁,洋薏仁 | 珍珠薏仁 | 365 | 8.6 | 1.6 | 77.1 | 13 | 249 | 26 | 54 | ... |

### 使用EasyExcel注解映射

```java
@Data
public class NutritionData {
    @ExcelProperty("樣品編號")
    private String sampleId;
    
    @ExcelProperty("食品分類")
    private String foodCategory;
    
    @ExcelProperty("樣品名稱")
    private String sampleName;
    
    @ExcelProperty("粗蛋白(g)")
    private String protein;
    
    @ExcelProperty("粗脂肪(g)")
    private String fat;
    
    @ExcelProperty("總碳水化合物(g)")
    private String carbohydrates;
    
    // 其他字段...
}
```

## 🔧 开发指南

### 添加新的营养元素

1. 在`NutritionData.java`中添加新的字段和对应的EasyExcel注解
2. 在`toFood()`方法中处理新的营养元素
3. 如果需要，扩展`Nutrition`类以支持新的营养元素

### 自定义膳食规划算法

1. 修改`MealPlanner.java`中的`generateDailyMealPlan()`方法
2. 调整`NutrientTargetManager.java`中的营养目标计算逻辑
3. 修改`MealOptimizer.java`中的优化算法，可以尝试不同的优化策略

## 👥 贡献指南

欢迎贡献代码、报告问题或提出新功能建议！请遵循以下步骤：

1. Fork本仓库
2. 创建您的特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启一个Pull Request

## 📄 许可证

本项目采用MIT许可证 - 详情请参见[LICENSE](LICENSE)文件 

# 膳食计划系统改进说明

## 初次改进：食物摄入量个性化

### 问题描述

原始的膳食计划系统中存在一个问题：所有食物的摄入量都被假设为固定的100克，而不考虑不同食物的实际合理摄入量。这在现实应用中是不合理的，因为不同类别的食物有着不同的推荐摄入量。

### 改进内容

我们对系统进行了以下改进：

1. 在`Food`类中增加了`withIntake`方法，允许根据指定摄入量创建新的食物对象。
2. 增加了`getRecommendedIntake`方法，根据食物类别返回推荐摄入量：
   - 主食 (staple): 100g
   - 蔬菜 (vegetable): 150g
   - 水果 (fruit): 150g
   - 肉类 (meat): 75g
   - 鱼类 (fish): 75g
   - 蛋类 (egg): 50g
   - 乳制品 (milk): 200g
   - 油脂 (oil): 10g
   - 其他: 50g

3. 修改了`findBestStapleFood`和`findBestFood`方法，使用推荐摄入量而非固定的100g来创建和评估食物。
4. 调整了`generateMeal`方法中的警告信息，使其更加明确。

## 第二次改进：动态计算最佳摄入量

### 问题描述

上一次改进中，每种食物类别的推荐摄入量被设置为固定值，难以根据用户的具体营养需求进行灵活调整。为了解决这个问题，我们引入了摄入量范围和动态计算最佳摄入量的功能。

### 改进内容

我们对系统进行了以下改进：

1. 新增了`IntakeRange`类，定义食物的摄入量范围（最小值、最大值和默认值）。
2. 修改了`Food`类中的`getRecommendedIntakeRange`方法，返回每种食物类别的合理摄入量范围：
   - 主食：80-150g（默认100g）
   - 蔬菜：100-250g（默认150g）
   - 水果：100-250g（默认150g）
   - 肉类：50-100g（默认75g）
   - 鱼类：50-100g（默认75g）
   - 蛋类：25-75g（默认50g）
   - 乳制品：100-300g（默认200g）
   - 油脂：5-15g（默认10g）
   - 其他食物：25-75g（默认50g）

3. 添加了`calculateOptimalIntake`方法，该方法可以根据目标营养需求，动态计算食物的最佳摄入量：
   - 考虑热量需求比例
   - 考虑蛋白质需求比例
   - 考虑碳水化合物需求比例
   - 综合多种营养素需求，计算加权平均值
   - 确保计算结果在允许范围内

4. 更新了`findBestStapleFood`和`findBestFood`方法，使用动态计算的最佳摄入量而非固定值。

## 最新改进：多轮优化算法

### 问题描述

前两次改进中，系统在选择每种食物时单独计算最佳摄入量，但没有考虑到食物之间的相互影响，也没有对整体膳食进行全局优化。这可能导致最终的膳食方案在整体营养平衡上不够理想。

### 改进内容

我们对系统进行了以下改进：

1. 新增了`MealOptimizer`类，用于对已选食物组合进行多轮优化：
   - 使用迭代优化算法，在允许范围内调整每种食物的摄入量
   - 基于整体膳食的营养平衡进行评分
   - 使用高斯函数评估营养素匹配度，确保各营养素接近目标值
   - 实现收敛机制，当评分改善很小时提前结束优化

2. 修改了`MealPlanner`类，集成`MealOptimizer`：
   - 在生成膳食计划后，对每餐进行多轮优化
   - 优化早餐、午餐和晚餐的食物摄入量
   - 保持食物组合不变，只调整摄入量

3. 在`Main`类中添加了测试膳食多轮优化功能的方法：
   - 创建测试食物列表和目标营养需求
   - 对测试膳食进行多轮优化
   - 比较优化前后的营养成分差异

### 效果

最新改进后的系统具有以下优势：

1. 更加智能化的膳食计划，能够根据用户的具体营养需求动态调整食物摄入量
2. 在定义的范围内灵活变化，既保证营养摄入合理，又允许个性化调整
3. 更加精确的营养计算，为用户提供更加个性化的膳食建议
4. 食物组合更加优化，能够以更合理的摄入量满足营养需求
5. 通过多轮优化，实现整体膳食的全局优化，确保营养均衡
6. 支持不同优化策略，可根据不同的优化目标调整算法参数

这些改进使得膳食计划系统能够生成更加贴近用户实际需求的膳食方案，提高系统的实用性和用户体验，为用户提供真正个性化和营养均衡的膳食建议。 