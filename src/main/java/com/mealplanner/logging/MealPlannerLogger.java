package com.mealplanner.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.mealplanner.Food;
import com.mealplanner.MealNutrients;

/**
 * 膳食规划系统日志工具类
 * 用于统一管理和格式化系统的日志输出
 */
public class MealPlannerLogger {
    
    // 日志级别
    public enum LogLevel {
        INFO, WARNING, ERROR, SUCCESS, DEBUG
    }
    
    // ANSI颜色代码
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BOLD = "\u001B[1m";
    
    private static boolean useColor = true;
    private static boolean showTimestamp = true;
    private static boolean detailedLogs = false;
    private static int indentLevel = 0;
    
    /**
     * 设置是否使用颜色输出
     * @param use 如果为true则使用颜色
     */
    public static void setUseColor(boolean use) {
        useColor = use;
    }
    
    /**
     * 设置是否显示时间戳
     * @param show 如果为true则显示时间戳
     */
    public static void setShowTimestamp(boolean show) {
        showTimestamp = show;
    }
    
    /**
     * 设置是否输出详细日志
     * @param detailed 如果为true则输出详细日志
     */
    public static void setDetailedLogs(boolean detailed) {
        detailedLogs = detailed;
    }
    
    /**
     * 增加缩进级别
     */
    public static void increaseIndent() {
        indentLevel++;
    }
    
    /**
     * 减少缩进级别
     */
    public static void decreaseIndent() {
        if (indentLevel > 0) {
            indentLevel--;
        }
    }
    
    /**
     * 重置缩进级别
     */
    public static void resetIndent() {
        indentLevel = 0;
    }
    
    /**
     * 打印日志消息
     * @param level 日志级别
     * @param message 日志消息
     */
    public static void log(LogLevel level, String message) {
        if (!detailedLogs && level == LogLevel.DEBUG) {
            return; // 如果不是详细日志模式，则不输出DEBUG级别的消息
        }
        
        StringBuilder builder = new StringBuilder();
        
        // 添加时间戳
        if (showTimestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            builder.append("[").append(sdf.format(new Date())).append("] ");
        }
        
        // 添加日志级别
        String levelStr = "[" + level.toString() + "]";
        if (useColor) {
            switch (level) {
                case INFO:
                    levelStr = colorize(levelStr, ANSI_BLUE);
                    break;
                case WARNING:
                    levelStr = colorize(levelStr, ANSI_YELLOW);
                    break;
                case ERROR:
                    levelStr = colorize(levelStr, ANSI_RED);
                    break;
                case SUCCESS:
                    levelStr = colorize(levelStr, ANSI_GREEN);
                    break;
                case DEBUG:
                    levelStr = colorize(levelStr, ANSI_PURPLE);
                    break;
            }
        }
        builder.append(levelStr).append(" ");
        
        // 添加缩进
        for (int i = 0; i < indentLevel; i++) {
            builder.append("  ");
        }
        
        // 添加消息
        builder.append(message);
        
        // 输出日志
        System.out.println(builder.toString());
    }
    
    /**
     * 打印普通信息
     * @param message 消息内容
     */
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }
    
    /**
     * 打印警告信息
     * @param message 消息内容
     */
    public static void warning(String message) {
        log(LogLevel.WARNING, message);
    }
    
    /**
     * 打印错误信息
     * @param message 消息内容
     */
    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }
    
    /**
     * 打印成功信息
     * @param message 消息内容
     */
    public static void success(String message) {
        log(LogLevel.SUCCESS, message);
    }
    
    /**
     * 打印调试信息
     * @param message 消息内容
     */
    public static void debug(String message) {
        log(LogLevel.DEBUG, message);
    }
    
    /**
     * 打印带标题的分隔线
     * @param title 标题
     */
    public static void section(String title) {
        String line = "====== " + title + " ======";
        if (useColor) {
            System.out.println(colorize(line, ANSI_CYAN + ANSI_BOLD));
        } else {
            System.out.println(line);
        }
    }
    
    /**
     * 打印简单分隔线
     */
    public static void divider() {
        System.out.println("----------------------------------------");
    }
    
    /**
     * 输出优化开始日志
     * @param initialScore 初始评分
     */
    public static void startOptimization(double initialScore) {
        section("开始膳食优化");
        info("初始评分: " + formatDouble(initialScore));
        increaseIndent();
    }
    
    /**
     * 输出优化迭代日志
     * @param iteration 迭代次数
     * @param currentScore 当前评分
     * @param improvement 改善分数
     */
    public static void optimizationIteration(int iteration, double currentScore, double improvement) {
        String message = String.format("迭代 %d: 评分 %.4f (改善: %.4f)", 
            iteration, currentScore, improvement);
        
        if (improvement > 0.05) {
            success(message);
        } else if (improvement > 0) {
            info(message);
        } else {
            warning(message);
        }
    }
    
    /**
     * 输出营养素要求放宽日志
     * @param deviation 当前偏差百分比
     */
    public static void nutrientRequirementRelaxed(double deviation) {
        warning("放宽营养素要求，当前偏差: " + formatDouble(deviation * 100) + "%");
    }
    
    /**
     * 输出优化完成日志
     * @param finalScore 最终评分
     * @param totalDeviation 最终营养素偏差
     */
    public static void completeOptimization(double finalScore, double totalDeviation) {
        decreaseIndent();
        success("优化完成，最终评分: " + formatDouble(finalScore));
        info("最终营养素偏差: " + formatDouble(totalDeviation * 100) + "%");
        divider();
    }
    
    /**
     * 输出膳食生成开始日志
     */
    public static void startMealGeneration() {
        section("开始生成膳食");
        increaseIndent();
    }
    
    /**
     * 输出主食添加日志
     * @param stapleFood 主食
     */
    public static void stapleAdded(Food stapleFood) {
        if (stapleFood != null) {
            success("添加主食: " + stapleFood.getName() + " " + stapleFood.getPortionDescription());
        } else {
            warning("未找到合适的主食");
        }
    }
    
    /**
     * 输出食物添加日志
     * @param food 食物
     */
    public static void foodAdded(Food food) {
        info("添加食物: " + food.getName() + " " + food.getPortionDescription());
    }
    
    /**
     * 输出食物筛选结果日志
     * @param foodName 食物名称
     * @param reason 筛选原因
     */
    public static void foodFiltered(String foodName, String reason) {
        if (detailedLogs) {
            debug("排除食物: " + foodName + " (" + reason + ")");
        }
    }
    
    /**
     * 输出膳食生成完成日志
     * @param mealSize 膳食中的食物数量
     * @param attempts 尝试次数
     * @param adjustments 调整次数
     */
    public static void completeMealGeneration(int mealSize, int attempts, int adjustments) {
        decreaseIndent();
        
        if (mealSize < 3) {
            warning("未能找到足够的食物组成一餐 (仅找到 " + mealSize + " 种食物)");
            warning("已尝试放宽要求 " + adjustments + " 次，共尝试 " + attempts + " 次筛选");
        } else {
            success("成功生成膳食，包含 " + mealSize + " 种食物");
            if (adjustments > 0) {
                info("放宽营养素要求 " + adjustments + " 次，共尝试 " + attempts + " 次筛选");
            }
        }
        
        divider();
    }
    
    /**
     * 输出膳食营养分析日志
     * @param actualNutrients 实际营养素
     * @param targetNutrients 目标营养素
     */
    public static void nutrientAnalysis(com.mealplanner.MealNutrients actualNutrients, com.mealplanner.MealNutrients targetNutrients) {
        section("膳食营养分析");
        
        info("热量:   " + formatDouble(actualNutrients.calories) + " / " + formatDouble(targetNutrients.calories) + " 大卡" +
             " (" + calculatePercentage(actualNutrients.calories, targetNutrients.calories) + ")");
        
        info("碳水:   " + formatDouble(actualNutrients.carbohydrates) + " / " + formatDouble(targetNutrients.carbohydrates) + " 克" +
             " (" + calculatePercentage(actualNutrients.carbohydrates, targetNutrients.carbohydrates) + ")");
        
        info("蛋白质: " + formatDouble(actualNutrients.protein) + " / " + formatDouble(targetNutrients.protein) + " 克" +
             " (" + calculatePercentage(actualNutrients.protein, targetNutrients.protein) + ")");
        
        info("脂肪:   " + formatDouble(actualNutrients.fat) + " / " + formatDouble(targetNutrients.fat) + " 克" +
             " (" + calculatePercentage(actualNutrients.fat, targetNutrients.fat) + ")");
        
        divider();
    }
    
    /**
     * 输出食物评分日志
     * @param foodName 食物名称
     * @param score 评分
     */
    public static void foodScored(String foodName, double score) {
        if (detailedLogs) {
            debug("食物评分: " + foodName + " = " + formatDouble(score));
        }
    }
    
    /**
     * 格式化小数
     * @param value 小数值
     * @return 格式化后的字符串
     */
    private static String formatDouble(double value) {
        return String.format("%.2f", value);
    }
    
    /**
     * 计算百分比
     * @param actual 实际值
     * @param target 目标值
     * @return 格式化的百分比字符串
     */
    private static String calculatePercentage(double actual, double target) {
        if (target == 0) {
            return "N/A";
        }
        
        double percentage = (actual / target) * 100;
        String result = formatDouble(percentage) + "%";
        
        if (useColor) {
            if (percentage >= 90 && percentage <= 110) {
                return colorize(result, ANSI_GREEN);
            } else if (percentage >= 80 && percentage <= 120) {
                return colorize(result, ANSI_CYAN);
            } else if (percentage >= 70 && percentage <= 130) {
                return colorize(result, ANSI_YELLOW);
            } else {
                return colorize(result, ANSI_RED);
            }
        }
        
        return result;
    }
    
    /**
     * 给文本添加颜色
     * @param text 文本
     * @param color 颜色代码
     * @return 添加颜色后的文本
     */
    private static String colorize(String text, String color) {
        return color + text + ANSI_RESET;
    }
} 