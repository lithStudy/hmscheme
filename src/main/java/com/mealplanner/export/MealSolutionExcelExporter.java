package com.mealplanner.export;

import com.alibaba.excel.EasyExcel;
import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;
import com.mealplanner.model.Nutrition;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * 膳食方案Excel导出工具类
 */
public class MealSolutionExcelExporter {
    
    private static final String DEFAULT_SHEET_NAME = "膳食方案";

    public static void export(List<MealSolution> solutions,Nutrition targetNutrients){
        // 添加导出到Excel的功能
        // 设置Excel文件路径
        String defaultExcelFilePath = "meal_solutions.xlsx";
        String excelFilePath = defaultExcelFilePath;
        
        // 检查文件是否存在
        File excelFile = new File(defaultExcelFilePath);
        if (excelFile.exists()) {
            // System.out.println("\nExcel文件 '" + defaultExcelFilePath + "' 已存在。");
            // System.out.println("1. 追加到现有文件");
            // System.out.println("2. 创建新文件");
            // System.out.print("请选择操作 (默认: 1): ");
            
            // Scanner scanner = new Scanner(System.in);
            // String choice = scanner.nextLine().trim();
            String choice = "1";
            
            if ("2".equals(choice)) {
                // 创建带时间戳的新文件名
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String timestamp = dateFormat.format(new Date());
                excelFilePath = "meal_solutions_" + timestamp + ".xlsx";
                System.out.println("将创建新文件: " + excelFilePath);
            } else {
                System.out.println("将追加到现有文件: " + excelFilePath);
            }
        } else {
            System.out.println("\nExcel文件 '" + defaultExcelFilePath + "' 不存在，将创建新文件。");
        }
        
        exportToExcel(solutions, targetNutrients, excelFilePath);
        System.out.println("膳食方案已导出到Excel文件: " + excelFilePath);
    }

    
    /**
     * 导出膳食方案到Excel文件
     * @param solutions 膳食方案列表
     * @param targetNutrients 目标营养素
     * @param filePath 文件路径
     */
    private static void exportToExcel(List<MealSolution> solutions, Nutrition targetNutrients, String filePath) {
        List<MealSolutionExcelData> dataList = convertToExcelData(solutions, targetNutrients);
        
        // 检查文件是否存在
        File file = new File(filePath);
        boolean fileExists = file.exists();
        
        if (fileExists) {
            // 追加到现有文件
            appendToExistingExcel(dataList, filePath);
        } else {
            // 创建新文件
            createNewExcel(dataList, filePath);
        }
    }
    
    /**
     * 创建新的Excel文件
     * @param dataList 数据列表
     * @param filePath 文件路径
     */
    private static void createNewExcel(List<MealSolutionExcelData> dataList, String filePath) {
        EasyExcel.write(filePath, MealSolutionExcelData.class)
                .sheet(DEFAULT_SHEET_NAME)
                .doWrite(dataList);
    }
    
    /**
     * 追加数据到现有Excel文件
     * @param dataList 数据列表
     * @param filePath 文件路径
     */
    private static void appendToExistingExcel(List<MealSolutionExcelData> dataList, String filePath) {
        try {
            // 首先读取现有数据
            List<MealSolutionExcelData> existingData = new ArrayList<>();
            try {
                // 尝试读取现有数据
                EasyExcel.read(filePath, MealSolutionExcelData.class, new com.alibaba.excel.event.AnalysisEventListener<MealSolutionExcelData>() {
                    @Override
                    public void invoke(MealSolutionExcelData data, com.alibaba.excel.context.AnalysisContext context) {
                        existingData.add(data);
                    }
                    
                    @Override
                    public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) {
                        // 分析完成
                    }
                }).sheet(DEFAULT_SHEET_NAME).doRead();
                System.out.println("成功读取现有Excel文件，包含 " + existingData.size() + " 条记录");
            } catch (Exception e) {
                System.out.println("读取现有Excel文件失败，将创建新文件: " + e.getMessage());
            }
            
            // 合并现有数据和新数据
            List<MealSolutionExcelData> combinedData = new ArrayList<>(existingData);
            combinedData.addAll(dataList);
            System.out.println("合并后共有 " + combinedData.size() + " 条记录 (现有: " + existingData.size() + ", 新增: " + dataList.size() + ")");
            
            // 写入合并后的数据
            EasyExcel.write(filePath, MealSolutionExcelData.class)
                    .sheet(DEFAULT_SHEET_NAME)
                    .doWrite(combinedData);
            
        } catch (Exception e) {
            System.err.println("追加数据到Excel文件失败: " + e.getMessage());
            e.printStackTrace();
            
            // 如果追加失败，尝试创建新文件
            createNewExcel(dataList, filePath);
        }
    }
    
    /**
     * 将膳食方案转换为Excel数据模型
     * @param solutions 膳食方案列表
     * @param targetNutrients 目标营养素
     * @return Excel数据模型列表
     */
    private static List<MealSolutionExcelData> convertToExcelData(List<MealSolution> solutions, Nutrition targetNutrients) {
        List<MealSolutionExcelData> dataList = new ArrayList<>();
        
        // 生成时间戳，用于标识同一批次的方案
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        
        for (int i = 0; i < solutions.size(); i++) {
            MealSolution solution = solutions.get(i);
            MealSolutionExcelData data = new MealSolutionExcelData();
            
            // 设置方案ID
            data.setSolutionId(timestamp + "_" + (i + 1));
            
            // 设置食物清单
            data.setFoodList(formatFoodList(solution.getFoodGenes()));
            
            // 设置营养素达成率
            setNutrientAchievements(data, solution, targetNutrients);
            
            // 设置三大营养素热量比例
            setMacroNutrientPercentages(data, solution);
            
            // 设置目标评分
            setObjectiveScores(data, solution);
            
            // 计算并设置营养素偏离度
            calculateAndSetDeviationScore(data, solution, targetNutrients);
            
            dataList.add(data);
        }
        
        return dataList;
    }
    
    /**
     * 格式化食物清单
     * @param foodGenes 食物基因列表
     * @return 格式化后的食物清单字符串
     */
    private static String formatFoodList(List<FoodGene> foodGenes) {
        return foodGenes.stream()
                .map(gene -> gene.getFood().getName() + "(" + (int)gene.getIntake() + "g)")
                .collect(Collectors.joining(", "));
    }
    
    /**
     * 设置营养素达成率
     * @param data Excel数据模型
     * @param solution 膳食方案
     * @param targetNutrients 目标营养素
     */
    private static void setNutrientAchievements(MealSolutionExcelData data, MealSolution solution, Nutrition targetNutrients) {
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        
        // 计算各营养素的达成率
        if (targetNutrients.calories > 0) {
            double ratio = actualNutrients.calories / targetNutrients.calories;
            data.setCaloriesAchievement(formatRatio(ratio) + " (" + (int)actualNutrients.calories + "/" + (int)targetNutrients.calories + ")");
        }
        
        if (targetNutrients.carbohydrates > 0) {
            double ratio = actualNutrients.carbohydrates / targetNutrients.carbohydrates;
            data.setCarbsAchievement(formatRatio(ratio) + " (" + formatDecimal(actualNutrients.carbohydrates) + "/" + formatDecimal(targetNutrients.carbohydrates) + ")");
        }
        
        if (targetNutrients.protein > 0) {
            double ratio = actualNutrients.protein / targetNutrients.protein;
            data.setProteinAchievement(formatRatio(ratio) + " (" + formatDecimal(actualNutrients.protein) + "/" + formatDecimal(targetNutrients.protein) + ")");
        }
        
        if (targetNutrients.fat > 0) {
            double ratio = actualNutrients.fat / targetNutrients.fat;
            data.setFatAchievement(formatRatio(ratio) + " (" + formatDecimal(actualNutrients.fat) + "/" + formatDecimal(targetNutrients.fat) + ")");
        }
        
        if (targetNutrients.calcium > 0) {
            double ratio = actualNutrients.calcium / targetNutrients.calcium;
            data.setCalciumAchievement(formatRatio(ratio) + " (" + (int)actualNutrients.calcium + "/" + (int)targetNutrients.calcium + ")");
        }
        
        if (targetNutrients.potassium > 0) {
            double ratio = actualNutrients.potassium / targetNutrients.potassium;
            data.setPotassiumAchievement(formatRatio(ratio) + " (" + (int)actualNutrients.potassium + "/" + (int)targetNutrients.potassium + ")");
        }
        
        if (targetNutrients.sodium > 0) {
            double ratio = actualNutrients.sodium / targetNutrients.sodium;
            data.setSodiumAchievement(formatRatio(ratio) + " (" + (int)actualNutrients.sodium + "/" + (int)targetNutrients.sodium + ")");
        }
        
        if (targetNutrients.magnesium > 0) {
            double ratio = actualNutrients.magnesium / targetNutrients.magnesium;
            data.setMagnesiumAchievement(formatRatio(ratio) + " (" + (int)actualNutrients.magnesium + "/" + (int)targetNutrients.magnesium + ")");
        }
    }
    
    /**
     * 设置三大营养素热量比例
     * @param data Excel数据模型
     * @param solution 膳食方案
     */
    private static void setMacroNutrientPercentages(MealSolutionExcelData data, MealSolution solution) {
        Nutrition nutrients = solution.calculateTotalNutrients();
        
        // 计算各营养素提供的热量
        double carbsCalories = nutrients.carbohydrates * 4; // 碳水：4kcal/g
        double proteinCalories = nutrients.protein * 4;     // 蛋白质：4kcal/g
        double fatCalories = nutrients.fat * 9;             // 脂肪：9kcal/g
        double totalCalories = carbsCalories + proteinCalories + fatCalories;
        
        if (totalCalories > 0) {
            // 计算各营养素热量占比
            double carbsPercentage = carbsCalories / totalCalories;
            double proteinPercentage = proteinCalories / totalCalories;
            double fatPercentage = fatCalories / totalCalories;
            
            // 设置到数据模型
            data.setCarbsCaloriePercentage(formatPercentage(carbsPercentage));
            data.setProteinCaloriePercentage(formatPercentage(proteinPercentage));
            data.setFatCaloriePercentage(formatPercentage(fatPercentage));
        }
    }
    
    /**
     * 设置目标评分
     * @param data Excel数据模型
     * @param solution 膳食方案
     */
    private static void setObjectiveScores(MealSolutionExcelData data, MealSolution solution) {
        List<ObjectiveValue> objectiveValues = solution.getObjectiveValues();
        if (objectiveValues == null || objectiveValues.isEmpty()) {
            return;
        }
        
        // 计算总体评分
        double totalWeightedScore = 0;
        double totalWeight = 0;
        
        for (ObjectiveValue value : objectiveValues) {
            String objectiveName = value.getName().toLowerCase();
            double score = value.getValue();
            
            // 根据目标名称设置对应的评分
            if (objectiveName.contains("balance")) {
                data.setBalanceScore(formatScore(score));
            } else if (objectiveName.contains("diversity")) {
                data.setDiversityScore(formatScore(score));
            } else if (objectiveName.contains("preference")) {
                data.setPreferenceScore(formatScore(score));
            } else if (objectiveName.contains("nutrient")) {
                // 这里可能需要根据实际情况调整
                data.setNutrientScore(formatScore(score));
            }
            
            // 累计加权分数
            totalWeightedScore += value.getWeightedValue();
            totalWeight += value.getWeight();
        }
        
        // 设置总体评分
        if (totalWeight > 0) {
            double overallScore = totalWeightedScore / totalWeight;
            data.setOverallScore(formatScore(overallScore));
        }
    }
    
    /**
     * 计算并设置营养素偏离度
     * @param data Excel数据模型
     * @param solution 膳食方案
     * @param targetNutrients 目标营养素
     */
    private static void calculateAndSetDeviationScore(MealSolutionExcelData data, MealSolution solution, Nutrition targetNutrients) {
        Nutrition actualNutrients = solution.calculateTotalNutrients();
        double totalDeviation = 0.0;
        int count = 0;
        
        // 计算各营养素的偏离度
        if (targetNutrients.calories > 0) {
            double ratio = actualNutrients.calories / targetNutrients.calories;
            double deviation = calculateDeviationFromRange(ratio, 0.9, 1.1); // 使用默认范围
            totalDeviation += deviation;
            count++;
        }
        
        if (targetNutrients.carbohydrates > 0) {
            double ratio = actualNutrients.carbohydrates / targetNutrients.carbohydrates;
            double deviation = calculateDeviationFromRange(ratio, 0.85, 1.15);
            totalDeviation += deviation;
            count++;
        }
        
        if (targetNutrients.protein > 0) {
            double ratio = actualNutrients.protein / targetNutrients.protein;
            double deviation = calculateDeviationFromRange(ratio, 0.9, 1.2);
            totalDeviation += deviation;
            count++;
        }
        
        if (targetNutrients.fat > 0) {
            double ratio = actualNutrients.fat / targetNutrients.fat;
            double deviation = calculateDeviationFromRange(ratio, 0.7, 1.1);
            totalDeviation += deviation;
            count++;
        }
        
        if (targetNutrients.calcium > 0) {
            double ratio = actualNutrients.calcium / targetNutrients.calcium;
            double deviation = calculateDeviationFromRange(ratio, 0.8, 1.5);
            totalDeviation += deviation;
            count++;
        }
        
        if (targetNutrients.potassium > 0) {
            double ratio = actualNutrients.potassium / targetNutrients.potassium;
            double deviation = calculateDeviationFromRange(ratio, 0.8, 1.5);
            totalDeviation += deviation;
            count++;
        }
        
        if (targetNutrients.sodium > 0) {
            double ratio = actualNutrients.sodium / targetNutrients.sodium;
            double deviation = calculateDeviationFromRange(ratio, 0.5, 1.0);
            // 对于钠这类限制性营养素，过量的偏离应该受到更严厉的惩罚
            if (ratio > 1.0) {
                deviation *= 1.5;
            }
            totalDeviation += deviation;
            count++;
        }
        
        if (targetNutrients.magnesium > 0) {
            double ratio = actualNutrients.magnesium / targetNutrients.magnesium;
            double deviation = calculateDeviationFromRange(ratio, 0.8, 1.5);
            totalDeviation += deviation;
            count++;
        }
        
        // 计算平均偏离度
        double averageDeviation = count > 0 ? totalDeviation / count : 0;
        data.setDeviationScore(formatScore(averageDeviation));
    }
    
    /**
     * 计算实际比率与目标范围的偏离度
     * @param ratio 实际比率
     * @param minRate 最小达成率
     * @param maxRate 最大达成率
     * @return 偏离度（0表示在范围内，正值表示偏离范围的程度）
     */
    private static double calculateDeviationFromRange(double ratio, double minRate, double maxRate) {
        if (ratio >= minRate && ratio <= maxRate) {
            return 0.0; // 在范围内，偏离为0
        } else if (ratio < minRate) {
            return minRate - ratio; // 不足的偏离
        } else {
            return ratio - maxRate; // 过量的偏离
        }
    }
    
    /**
     * 格式化比率
     * @param ratio 比率
     * @return 格式化后的字符串
     */
    private static String formatRatio(double ratio) {
        return String.format("%.2f", ratio);
    }
    
    /**
     * 格式化百分比
     * @param percentage 百分比（0-1之间）
     * @return 格式化后的字符串
     */
    private static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage * 100);
    }
    
    /**
     * 格式化评分
     * @param score 评分
     * @return 格式化后的字符串
     */
    private static String formatScore(double score) {
        return String.format("%.2f", score);
    }
    
    /**
     * 格式化小数
     * @param value 小数值
     * @return 格式化后的字符串
     */
    private static String formatDecimal(double value) {
        return String.format("%.1f", value);
    }
} 