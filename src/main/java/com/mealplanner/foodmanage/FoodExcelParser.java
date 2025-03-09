package com.mealplanner.foodmanage;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.mealplanner.Food;
import com.mealplanner.Nutrition;
import com.mealplanner.Portion;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel和CSV食物数据解析器
 * 用于从Excel或CSV文件中读取食物数据并转换为Food对象
 */
public class FoodExcelParser {
    
    // 定义表头名称常量
    private static final String HEADER_NAME = "食物名称";
    private static final String HEADER_CATEGORY = "食物类别";
    private static final String HEADER_CARBS = "碳水化合物(g)";
    private static final String HEADER_PROTEIN = "蛋白质(g)";
    private static final String HEADER_FAT = "脂肪(g)";
    private static final String HEADER_CALCIUM = "钙(mg)";
    private static final String HEADER_POTASSIUM = "钾(mg)";
    private static final String HEADER_SODIUM = "钠(mg)";
    private static final String HEADER_MAGNESIUM = "镁(mg)";
    private static final String HEADER_WEIGHT = "重量(g)";
    private static final String HEADER_DISPLAY_UNIT = "显示单位";
    private static final String HEADER_DISPLAY_AMOUNT = "显示数量";
    
    /**
     * 从Excel或CSV文件中解析食物数据
     * @param filePath 文件路径
     * @return 食物列表
     * @throws IOException 如果文件读取失败
     */
    public List<Food> parseFoodDataFromFile(String filePath) throws IOException {
        if (filePath.endsWith(".csv")) {
            return parseFoodDataFromCSV(filePath);
        } else if (filePath.endsWith(".xlsx") || filePath.endsWith(".xls")) {
            return parseFoodDataFromExcel(filePath);
        } else {
            throw new IllegalArgumentException("不支持的文件类型，仅支持.xls、.xlsx和.csv格式");
        }
    }
    
    /**
     * 从Excel文件中解析食物数据
     * @param filePath Excel文件路径
     * @return 食物列表
     * @throws IOException 如果文件读取失败
     */
    private List<Food> parseFoodDataFromExcel(String filePath) throws IOException {
        List<Food> foodList = new ArrayList<>();
        
        // 使用EasyExcel读取Excel文件
        EasyExcel.read(filePath, new AnalysisEventListener<Map<Integer, String>>() {
            private boolean isFirstRow = true;
            private Map<String, Integer> headerMap = new HashMap<>(); // 存储表头名称与列索引的映射
            
            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                if (isFirstRow) {
                    // 处理标题行，建立表头名称与列索引的映射
                    for (Map.Entry<Integer, String> entry : data.entrySet()) {
                        headerMap.put(entry.getValue(), entry.getKey());
                    }
                    isFirstRow = false;
                    return;
                }
                
                try {
                    // 根据表头名称获取数据
                    String name = getValueByHeader(data, HEADER_NAME, "");
                    String category = getValueByHeader(data, HEADER_CATEGORY, "");
                    
                    // 解析营养信息
                    double carbs = parseDouble(getValueByHeader(data, HEADER_CARBS, "0"));
                    double protein = parseDouble(getValueByHeader(data, HEADER_PROTEIN, "0"));
                    double fat = parseDouble(getValueByHeader(data, HEADER_FAT, "0"));
                    double calcium = parseDouble(getValueByHeader(data, HEADER_CALCIUM, "0"));
                    double potassium = parseDouble(getValueByHeader(data, HEADER_POTASSIUM, "0"));
                    double sodium = parseDouble(getValueByHeader(data, HEADER_SODIUM, "0"));
                    double magnesium = parseDouble(getValueByHeader(data, HEADER_MAGNESIUM, "0"));
                    
                    // 创建营养对象
                    Nutrition nutrition = new Nutrition(carbs, protein, fat, calcium, potassium, sodium, magnesium);
                    
                    // 解析份量信息
                    double weight = parseDouble(getValueByHeader(data, HEADER_WEIGHT, "0"));
                    String displayUnit = getValueByHeader(data, HEADER_DISPLAY_UNIT, "克");
                    double displayAmount = parseDouble(getValueByHeader(data, HEADER_DISPLAY_AMOUNT, "0"));
                    
                    // 创建份量对象
                    Portion portion = new Portion(weight, displayUnit, displayAmount);
                    
                    // 创建并添加Food对象
                    Food food = new Food(name, category, nutrition, portion);
                    foodList.add(food);
                } catch (Exception e) {
                    System.err.println("解析Excel行数据失败: " + e.getMessage());
                }
            }
            
            /**
             * 根据表头名称获取数据值
             * @param data 行数据
             * @param headerName 表头名称
             * @param defaultValue 默认值
             * @return 数据值
             */
            private String getValueByHeader(Map<Integer, String> data, String headerName, String defaultValue) {
                Integer columnIndex = headerMap.get(headerName);
                if (columnIndex == null) {
                    System.out.println("警告: 未找到表头 '" + headerName + "'，使用默认值");
                    return defaultValue;
                }
                return data.getOrDefault(columnIndex, defaultValue);
            }
            
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("Excel文件解析完成，共解析 " + foodList.size() + " 条食物数据");
            }
        }).sheet().doRead();
        
        return foodList;
    }
    
    /**
     * 从CSV文件中解析食物数据
     * @param filePath CSV文件路径
     * @return 食物列表
     * @throws IOException 如果文件读取失败
     */
    private List<Food> parseFoodDataFromCSV(String filePath) throws IOException {
        List<Food> foodList = new ArrayList<>();
        Map<String, Integer> headerMap = new HashMap<>(); // 存储表头名称与列索引的映射
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 读取标题行
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV文件为空");
            }
            
            // 解析标题行，建立表头名称与列索引的映射
            String[] headers = headerLine.split(",");
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].trim(), i);
            }
            
            // 读取数据行
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                Food food = parseCSVLineToFood(line, headerMap);
                if (food != null) {
                    foodList.add(food);
                }
            }
        }
        
        return foodList;
    }
    
    /**
     * 解析CSV行数据为Food对象
     * @param line CSV行数据
     * @param headerMap 表头映射
     * @return Food对象
     */
    private Food parseCSVLineToFood(String line, Map<String, Integer> headerMap) {
        try {
            String[] values = line.split(",");
            
            // 根据表头名称获取数据
            String name = getValueByHeader(values, headerMap, HEADER_NAME, "");
            String category = getValueByHeader(values, headerMap, HEADER_CATEGORY, "");
            
            // 解析营养信息
            double carbs = parseDouble(getValueByHeader(values, headerMap, HEADER_CARBS, "0"));
            double protein = parseDouble(getValueByHeader(values, headerMap, HEADER_PROTEIN, "0"));
            double fat = parseDouble(getValueByHeader(values, headerMap, HEADER_FAT, "0"));
            double calcium = parseDouble(getValueByHeader(values, headerMap, HEADER_CALCIUM, "0"));
            double potassium = parseDouble(getValueByHeader(values, headerMap, HEADER_POTASSIUM, "0"));
            double sodium = parseDouble(getValueByHeader(values, headerMap, HEADER_SODIUM, "0"));
            double magnesium = parseDouble(getValueByHeader(values, headerMap, HEADER_MAGNESIUM, "0"));
            
            // 创建营养对象
            Nutrition nutrition = new Nutrition(carbs, protein, fat, calcium, potassium, sodium, magnesium);
            
            // 解析份量信息
            double weight = parseDouble(getValueByHeader(values, headerMap, HEADER_WEIGHT, "0"));
            String displayUnit = getValueByHeader(values, headerMap, HEADER_DISPLAY_UNIT, "克");
            double displayAmount = parseDouble(getValueByHeader(values, headerMap, HEADER_DISPLAY_AMOUNT, "0"));
            
            // 创建份量对象
            Portion portion = new Portion(weight, displayUnit, displayAmount);
            
            // 创建并返回Food对象
            return new Food(name, category, nutrition, portion);
        } catch (Exception e) {
            System.err.println("解析CSV行数据失败: " + e.getMessage() + ", 行: " + line);
            return null;
        }
    }
    
    /**
     * 根据表头名称获取数据值
     * @param values 行数据值数组
     * @param headerMap 表头映射
     * @param headerName 表头名称
     * @param defaultValue 默认值
     * @return 数据值
     */
    private String getValueByHeader(String[] values, Map<String, Integer> headerMap, String headerName, String defaultValue) {
        Integer columnIndex = headerMap.get(headerName);
        if (columnIndex == null || columnIndex >= values.length) {
            System.out.println("警告: 未找到表头 '" + headerName + "' 或索引超出范围，使用默认值");
            return defaultValue;
        }
        return values[columnIndex];
    }
    
    /**
     * 安全地解析字符串为double
     */
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * 生成Food对象的Java代码字符串
     * @param foodList 食物列表
     * @return 代码字符串
     */
    public String generateFoodCodeString(List<Food> foodList) {
        StringBuilder sb = new StringBuilder();
        
        for (Food food : foodList) {
            sb.append(String.format("foodDatabase.add(new Food(\"%s\", \"%s\", \n", 
                    food.getName(), food.getCategory()));
            
            Nutrition n = food.getNutrition();
            sb.append(String.format("    new Nutrition(%.1f, %.1f, %.1f, %.1f, %.1f, %.1f, %.1f),\n",
                    n.getCarbohydrates(), n.getProtein(), n.getFat(), 
                    n.getCalcium(), n.getPotassium(), n.getSodium(), n.getMagnesium()));
            
            Portion p = food.getPortion();
            sb.append(String.format("    new Portion(%.1f, \"%s\", %.1f)));\n\n",
                    p.getWeight(), p.getDisplayUnit(), p.getDisplayAmount()));
        }
        
        return sb.toString();
    }
    
    /**
     * 将食物列表保存为CSV文件
     * @param foodList 食物列表
     * @param filePath 输出文件路径
     * @throws IOException 如果文件写入失败
     */
    public void saveFoodListToCSV(List<Food> foodList, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // 写入标题行
            writer.println(String.join(",", 
                    HEADER_NAME, HEADER_CATEGORY, HEADER_CARBS, HEADER_PROTEIN, HEADER_FAT,
                    HEADER_CALCIUM, HEADER_POTASSIUM, HEADER_SODIUM, HEADER_MAGNESIUM,
                    HEADER_WEIGHT, HEADER_DISPLAY_UNIT, HEADER_DISPLAY_AMOUNT));
            
            // 写入数据行
            for (Food food : foodList) {
                Nutrition n = food.getNutrition();
                Portion p = food.getPortion();
                
                writer.printf("%s,%s,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%s,%.1f\n",
                        food.getName(), food.getCategory(),
                        n.getCarbohydrates(), n.getProtein(), n.getFat(),
                        n.getCalcium(), n.getPotassium(), n.getSodium(), n.getMagnesium(),
                        p.getWeight(), p.getDisplayUnit(), p.getDisplayAmount());
            }
        }
    }
    
    /**
     * 将食物列表保存为Excel文件
     * @param foodList 食物列表
     * @param filePath 输出文件路径
     * @throws IOException 如果文件写入失败
     */
    public void saveFoodListToExcel(List<Food> foodList, String filePath) throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        // 转换Food对象为Map
        for (Food food : foodList) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(HEADER_NAME, food.getName());
            dataMap.put(HEADER_CATEGORY, food.getCategory());
            dataMap.put(HEADER_CARBS, food.getCarbohydrates());
            dataMap.put(HEADER_PROTEIN, food.getProtein());
            dataMap.put(HEADER_FAT, food.getFat());
            dataMap.put(HEADER_CALCIUM, food.getCalcium());
            dataMap.put(HEADER_POTASSIUM, food.getPotassium());
            dataMap.put(HEADER_SODIUM, food.getSodium());
            dataMap.put(HEADER_MAGNESIUM, food.getMagnesium());
            dataMap.put(HEADER_WEIGHT, food.getPortion().getWeight());
            dataMap.put(HEADER_DISPLAY_UNIT, food.getPortion().getDisplayUnit());
            dataMap.put(HEADER_DISPLAY_AMOUNT, food.getPortion().getDisplayAmount());
            
            dataList.add(dataMap);
        }
        
        // 使用EasyExcel写入Excel文件
        EasyExcel.write(filePath)
                .sheet("食物数据")
                .head(createHead())
                .doWrite(dataList);
    }
    
    /**
     * 创建Excel表头
     */
    private List<List<String>> createHead() {
        List<List<String>> head = new ArrayList<>();
        List<String> headers = List.of(
                HEADER_NAME, HEADER_CATEGORY, HEADER_CARBS, HEADER_PROTEIN, HEADER_FAT,
                HEADER_CALCIUM, HEADER_POTASSIUM, HEADER_SODIUM, HEADER_MAGNESIUM,
                HEADER_WEIGHT, HEADER_DISPLAY_UNIT, HEADER_DISPLAY_AMOUNT
        );
        
        for (String header : headers) {
            List<String> headColumn = new ArrayList<>();
            headColumn.add(header);
            head.add(headColumn);
        }
        
        return head;
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        try {
            FoodExcelParser parser = new FoodExcelParser();
            
            // 解析CSV文件
            System.out.println("解析CSV文件...");
            List<Food> foodsFromCSV = parser.parseFoodDataFromFile("src/main/resources/food_data_template.csv");
            System.out.println("成功从CSV解析 " + foodsFromCSV.size() + " 条食物数据");
            
            // 生成代码
            System.out.println("\n生成的代码：");
            System.out.println(parser.generateFoodCodeString(foodsFromCSV));
            
            // 导出为Excel文件
            parser.saveFoodListToExcel(foodsFromCSV, "src/main/resources/exported_foods.xlsx");
            System.out.println("\n已将食物数据导出到Excel文件：src/main/resources/exported_foods.xlsx");
            
        } catch (IOException e) {
            System.err.println("解析文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 