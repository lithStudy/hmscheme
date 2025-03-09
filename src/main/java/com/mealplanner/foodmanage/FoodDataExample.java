package com.mealplanner.foodmanage;
import java.io.IOException;
import java.util.List;
import com.mealplanner.Food;

/**
 * 食物数据解析器使用示例
 */
public class FoodDataExample {
    
    public static void main(String[] args) {
        try {
            // 创建解析器
            FoodExcelParser parser = new FoodExcelParser();
            
            // 从CSV文件解析食物数据
            System.out.println("从CSV文件解析食物数据...");
            String filePath = "src/main/resources/food_data_template.csv";
            List<Food> foodsFromCSV = parser.parseFoodDataFromFile(filePath);
            System.out.println("成功解析 " + foodsFromCSV.size() + " 条食物数据");
            
            // 打印食物信息
            System.out.println("\n解析的食物信息：");
            for (Food food : foodsFromCSV) {
                System.out.printf("食物：%s (%s)\n", food.getName(), food.getCategory());
                System.out.printf("  - 营养成分：碳水 %.1fg, 蛋白质 %.1fg, 脂肪 %.1fg\n", 
                        food.getCarbohydrates(), food.getProtein(), food.getFat());
                System.out.printf("  - 份量：%s\n", food.getPortionDescription());
                System.out.println();
            }
            
            // 生成代码
            System.out.println("\n生成的Food对象代码：");
            String codeString = parser.generateFoodCodeString(foodsFromCSV.subList(0, 2)); // 只显示前两个食物的代码
            System.out.println(codeString);
            
            // 将食物数据保存为新的CSV文件
            String exportFilePath = "src/main/resources/exported_foods.csv";
            parser.saveFoodListToCSV(foodsFromCSV, exportFilePath);
            System.out.println("\n已将食物数据导出到文件：" + exportFilePath);
            
            // 如何在MealPlanner中使用生成的代码
            System.out.println("\n在MealPlanner中使用生成的代码示例：");
            System.out.println("private void initializeFoodDatabase() {");
            System.out.println("    foodDatabase = new ArrayList<>();");
            System.out.println("    // 添加食物数据");
            System.out.println(codeString);
            System.out.println("}");
            
        } catch (IOException e) {
            System.err.println("解析文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 