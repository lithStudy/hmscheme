package com.mealplanner.foodmanage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.mealplanner.Food;

/**
 * 详细营养数据解析器使用示例
 */
public class NutritionDataExample {
    
    public static void main(String[] args) {
        try {
            // 创建解析器
            NutritionDataParser parser = new NutritionDataParser();
            
            // 解析CSV文件
            System.out.println("解析营养数据文件...");
            String filePath = "src/main/resources/nutrition_data.csv"; // 使用营养数据文件
            List<Map<String, String>> dataList = parser.parseNutritionDataFromFile(filePath);
            System.out.println("成功解析 " + dataList.size() + " 条食物营养数据");
            
            // 打印解析的数据
            if (!dataList.isEmpty()) {
                Map<String, String> firstFood = dataList.get(0);
                System.out.println("\n第一条食物数据详情：");
                System.out.println("样品编号: " + firstFood.getOrDefault("樣品編號", ""));
                System.out.println("食品分类: " + firstFood.getOrDefault("食品分類", ""));
                System.out.println("样品名称: " + firstFood.getOrDefault("樣品名稱", ""));
                System.out.println("俗名: " + firstFood.getOrDefault("俗名", ""));
                System.out.println("热量(kcal): " + firstFood.getOrDefault("熱量(kcal)", ""));
                System.out.println("碳水化合物(g): " + firstFood.getOrDefault("總碳水化合物(g)", ""));
                System.out.println("蛋白质(g): " + firstFood.getOrDefault("粗蛋白(g)", ""));
                System.out.println("脂肪(g): " + firstFood.getOrDefault("粗脂肪(g)", ""));
                System.out.println("钠(mg): " + firstFood.getOrDefault("鈉(mg)", ""));
                System.out.println("钾(mg): " + firstFood.getOrDefault("鉀(mg)", ""));
                System.out.println("钙(mg): " + firstFood.getOrDefault("鈣(mg)", ""));
                System.out.println("镁(mg): " + firstFood.getOrDefault("鎂(mg)", ""));
                System.out.println("铁(mg): " + firstFood.getOrDefault("鐵(mg)", ""));
                System.out.println("锌(mg): " + firstFood.getOrDefault("鋅(mg)", ""));
                System.out.println("磷(mg): " + firstFood.getOrDefault("磷(mg)", ""));
                System.out.println("膳食纤维(g): " + firstFood.getOrDefault("膳食纖維(g)", ""));
                System.out.println("维生素B1(mg): " + firstFood.getOrDefault("維生素B1(mg)", ""));
                System.out.println("维生素B2(mg): " + firstFood.getOrDefault("維生素B2(mg)", ""));
                System.out.println("维生素C(mg): " + firstFood.getOrDefault("維生素C(mg)", ""));
            }
            
            // 转换为Food对象
            List<Food> foodList = parser.convertToFoodObjects(dataList);
            System.out.println("\n成功转换 " + foodList.size() + " 个Food对象");
            
            // 打印Food对象信息
            if (!foodList.isEmpty()) {
                Food firstFood = foodList.get(0);
                System.out.println("\n转换后的Food对象信息：");
                System.out.println("名称: " + firstFood.getName());
                System.out.println("类别: " + firstFood.getCategory());
                System.out.println("碳水化合物: " + firstFood.getCarbohydrates() + "g");
                System.out.println("蛋白质: " + firstFood.getProtein() + "g");
                System.out.println("脂肪: " + firstFood.getFat() + "g");
                System.out.println("钙: " + firstFood.getCalcium() + "mg");
                System.out.println("钾: " + firstFood.getPotassium() + "mg");
                System.out.println("钠: " + firstFood.getSodium() + "mg");
                System.out.println("镁: " + firstFood.getMagnesium() + "mg");
                System.out.println("热量: " + firstFood.getCalories() + "kcal");
                System.out.println("份量: " + firstFood.getPortionDescription());
            }
            
            // 生成代码
            System.out.println("\n生成的Food对象代码：");
            String codeString = parser.generateFoodCodeString(foodList.subList(0, Math.min(2, foodList.size())));
            System.out.println(codeString);
            
            // 导出为Excel文件
            String excelFilePath = "src/main/resources/exported_nutrition.xlsx";
            parser.exportToExcel(foodList, excelFilePath);
            System.out.println("\n已将营养数据导出到Excel文件：" + excelFilePath);
            
        } catch (IOException e) {
            System.err.println("解析文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 