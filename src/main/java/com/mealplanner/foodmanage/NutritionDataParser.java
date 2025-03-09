package com.mealplanner.foodmanage;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.mealplanner.Food;
import com.mealplanner.Nutrition;
import com.mealplanner.Portion;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 详细营养数据解析器
 * 用于从Excel文件中读取详细的食物营养数据
 */
public class NutritionDataParser {
    
    /**
     * 从Excel文件中解析食物营养数据
     * @param filePath 文件路径
     * @return 营养数据列表
     * @throws IOException 如果文件读取失败
     */
    public List<NutritionData> parseNutritionDataFromFile(String filePath) throws IOException {
        return parseNutritionDataFromExcel(filePath);
    }
    
    /**
     * 从Excel文件中解析营养数据
     * @param filePath Excel文件路径
     * @return 营养数据列表
     * @throws IOException 如果文件读取失败
     */
    private List<NutritionData> parseNutritionDataFromExcel(String filePath) throws IOException {
        List<NutritionData> dataList = new ArrayList<>();
        
        // 使用EasyExcel读取Excel文件，直接映射到NutritionData对象
        EasyExcel.read(filePath, NutritionData.class, new AnalysisEventListener<NutritionData>() {
            @Override
            public void invoke(NutritionData data, AnalysisContext context) {
                dataList.add(data);
            }
            
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("Excel文件解析完成，共解析 " + dataList.size() + " 条数据");
            }
        }).sheet().doRead();
        
        return dataList;
    }
    
    /**
     * 将营养数据转换为Food对象
     * @param dataList 营养数据列表
     * @return Food对象列表
     */
    public List<Food> convertToFoodObjects(List<NutritionData> dataList) {
        List<Food> foodList = new ArrayList<>();
        
        for (NutritionData data : dataList) {
            Food food = data.toFood();
            if (food != null) {
                foodList.add(food);
            }
        }
        
        return foodList;
    }
    
    /**
     * 将食物数据导出为Excel文件
     * @param dataList 营养数据列表
     * @param filePath 输出文件路径
     */
    public void exportToExcel(List<NutritionData> dataList, String filePath) {
        // 使用EasyExcel写入Excel文件
        EasyExcel.write(filePath, NutritionData.class)
                .sheet("营养数据")
                .doWrite(dataList);
        
        System.out.println("已将营养数据导出到Excel文件：" + filePath);
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        try {
            NutritionDataParser parser = new NutritionDataParser();
            
            // 解析Excel文件
            System.out.println("解析营养数据文件...");
            String filePath = "src/main/resources/all.xlsx";
            List<NutritionData> dataList = parser.parseNutritionDataFromFile(filePath);
            System.out.println("成功解析 " + dataList.size() + " 条食物营养数据");
            
            // 导出为Excel文件
            parser.exportToExcel(dataList, "src/main/resources/exported_nutrition.xlsx");
            
        } catch (IOException e) {
            System.err.println("解析文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 