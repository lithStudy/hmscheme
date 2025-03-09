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

## 👥 贡献指南

欢迎贡献代码、报告问题或提出新功能建议！请遵循以下步骤：

1. Fork本仓库
2. 创建您的特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启一个Pull Request

## 📄 许可证

本项目采用MIT许可证 - 详情请参见[LICENSE](LICENSE)文件 