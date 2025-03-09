package com.mealplanner.foodmanage;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EasyExcel使用示例
 */
public class EasyExcelExample {
    
    public static void main(String[] args) {
        try {
            // 读取Excel文件
            readExcel();
            
            // 写入Excel文件
            writeExcel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 读取Excel文件示例
     */
    private static void readExcel() throws IOException {
        String filePath = "src/main/resources/nutrition_data.xlsx";
        
        // 检查文件是否存在
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("文件不存在: " + filePath);
            return;
        }
        
        System.out.println("开始读取Excel文件: " + filePath);
        
        // 使用EasyExcel读取Excel文件
        List<NutritionData> dataList = new ArrayList<>();
        
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
        
        // 打印解析结果
        if (!dataList.isEmpty()) {
            NutritionData firstData = dataList.get(0);
            System.out.println("\n第一条数据信息:");
            System.out.println("样品编号: " + firstData.getSampleId());
            System.out.println("食品分类: " + firstData.getFoodCategory());
            System.out.println("样品名称: " + firstData.getSampleName());
            System.out.println("碳水化合物(g): " + firstData.getCarbohydrates());
            System.out.println("蛋白质(g): " + firstData.getProtein());
            System.out.println("脂肪(g): " + firstData.getFat());
        }
    }
    
    /**
     * 写入Excel文件示例
     */
    private static void writeExcel() {
        String filePath = "src/main/resources/example_output.xlsx";
        
        // 创建示例数据
        List<NutritionData> dataList = new ArrayList<>();
        
        NutritionData data1 = new NutritionData();
        data1.setSampleId("E001");
        data1.setFoodCategory("水果");
        data1.setSampleName("苹果");
        data1.setCommonName("红富士");
        data1.setCalories("52");
        data1.setCarbohydrates("13.8");
        data1.setProtein("0.3");
        data1.setFat("0.2");
        dataList.add(data1);
        
        NutritionData data2 = new NutritionData();
        data2.setSampleId("E002");
        data2.setFoodCategory("蔬菜");
        data2.setSampleName("西兰花");
        data2.setCommonName("绿花菜");
        data2.setCalories("34");
        data2.setCarbohydrates("6.6");
        data2.setProtein("2.8");
        data2.setFat("0.4");
        dataList.add(data2);
        
        // 使用EasyExcel写入Excel文件
        EasyExcel.write(filePath, NutritionData.class)
                .sheet("营养数据")
                .doWrite(dataList);
        
        System.out.println("Excel文件写入成功: " + filePath);
    }
} 