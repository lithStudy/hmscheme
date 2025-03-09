# 食物数据解析器

这个项目提供了工具，用于从Excel或CSV文件中解析食物数据，并将其转换为Java代码中的Food对象。

## 功能特点

- 支持从.xls、.xlsx和.csv格式的文件中读取食物数据
- 自动将数据行转换为Food对象
- 生成可直接复制到代码中的Food对象初始化代码
- 支持将Food对象列表导出为CSV文件
- 支持解析详细的营养数据（包括维生素、矿物质、脂肪酸等）

## 解析器类型

项目提供了两种解析器：

### 1. FoodExcelParser

适用于简单的食物数据格式，包含基本的营养信息（碳水化合物、蛋白质、脂肪、钙、钾、钠、镁）。

### 2. NutritionDataParser

适用于详细的营养数据格式，可以解析包含大量营养元素的表格，如维生素、矿物质、脂肪酸、氨基酸等。

## 使用方法

### 基本食物数据格式

Excel或CSV文件应包含以下列（按顺序）：

1. 食物名称 (String)
2. 食物类别 (String) - 例如："staple"（主食）、"vegetable"（蔬菜）等
3. 碳水化合物 (g) (Double)
4. 蛋白质 (g) (Double)
5. 脂肪 (g) (Double)
6. 钙 (mg) (Double)
7. 钾 (mg) (Double)
8. 钠 (mg) (Double)
9. 镁 (mg) (Double)
10. 重量 (g) (Double)
11. 显示单位 (String) - 例如："克"、"个"、"片"等
12. 显示数量 (Double) - 例如：100.0、1.0、2.0等

### 详细营养数据格式

支持更复杂的表格格式，如台湾食品成分数据库格式，包含以下字段（部分）：

- 樣品編號 - 样品编号
- 食品分類 - 食品分类
- 樣品名稱 - 样品名称
- 熱量(kcal) - 热量
- 粗蛋白(g) - 蛋白质
- 粗脂肪(g) - 脂肪
- 總碳水化合物(g) - 总碳水化合物
- 鈉(mg) - 钠
- 鉀(mg) - 钾
- 鈣(mg) - 钙
- 鎂(mg) - 镁
- 以及更多详细的营养元素...

### 示例代码

#### 使用FoodExcelParser

```java
// 创建解析器
FoodExcelParser parser = new FoodExcelParser();

// 解析Excel或CSV文件
List<Food> foods = parser.parseFoodDataFromFile("food_data.xlsx"); // 或 "food_data.csv"

// 输出解析结果
System.out.println("成功解析 " + foods.size() + " 条食物数据");

// 生成可复制的代码
String codeString = parser.generateFoodCodeString(foods);
System.out.println(codeString);

// 将食物列表保存为CSV文件
parser.saveFoodListToCSV(foods, "exported_foods.csv");
```

#### 使用NutritionDataParser

```java
// 创建解析器
NutritionDataParser parser = new NutritionDataParser();

// 解析详细营养数据文件
List<Map<String, String>> dataList = parser.parseNutritionDataFromFile("nutrition_data.csv");
System.out.println("成功解析 " + dataList.size() + " 条食物营养数据");

// 转换为Food对象
List<Food> foodList = parser.convertToFoodObjects(dataList);

// 生成代码
String codeString = parser.generateFoodCodeString(foodList);
System.out.println(codeString);
```

### 示例文件

项目中包含了示例文件：

1. `food_data_template.csv` - 基本食物数据模板
2. `nutrition_data.csv` - 详细营养数据模板（台湾食品成分数据库格式）

## 项目依赖

本项目使用Maven管理依赖，主要依赖包括：

- Apache POI (5.2.3) - 用于Excel文件处理

## 安装与运行

1. 确保已安装Java 11或更高版本
2. 确保已安装Maven
3. 克隆或下载本项目
4. 在项目根目录执行：`mvn clean install`
5. 运行示例：
   - 基本解析器：`java -cp target/meal-planner-1.0-SNAPSHOT.jar FoodDataExample`
   - 详细解析器：`java -cp target/meal-planner-1.0-SNAPSHOT.jar NutritionDataExample`

## 注意事项

- 确保文件格式正确，否则可能导致解析错误
- 数值单元格应包含数字，而非文本格式的数字
- 如果文件中有空行，解析器会自动跳过
- CSV文件的分隔符：
  - 基本食物数据使用逗号(,)作为分隔符
  - 详细营养数据使用竖线(|)作为分隔符 