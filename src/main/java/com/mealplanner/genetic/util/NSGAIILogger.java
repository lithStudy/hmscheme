package com.mealplanner.genetic.util;

import com.mealplanner.genetic.algorithm.Population;
import com.mealplanner.genetic.model.FoodGene;
import com.mealplanner.genetic.model.MealSolution;
import com.mealplanner.genetic.model.ObjectiveValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * NSGA-II算法日志类
 */
public class NSGAIILogger {
    private static final Logger logger = Logger.getLogger(NSGAIILogger.class.getName());
    
    // 日志级别
    public enum LogLevel {
        NONE,
        ERROR,
        WARNING,
        INFO,
        DEBUG,
        TRACE
    }
    
    // 当前日志级别
    private LogLevel logLevel = LogLevel.INFO;
    
    // 是否在控制台输出
    private boolean consoleOutput = true;
    
    // 是否输出详细的目标值
    private boolean verboseObjectives = false;
    
    // 是否输出详细的种群状态
    private boolean verbosePopulation = false;
    
    // 算法开始时间
    private long startTime;
    
    /**
     * 构造函数
     */
    public NSGAIILogger() {
        startTime = System.currentTimeMillis();
    }
    
    /**
     * 构造函数
     * @param logLevel 日志级别
     */
    public NSGAIILogger(LogLevel logLevel) {
        this.logLevel = logLevel;
        startTime = System.currentTimeMillis();
    }
    
    /**
     * 记录算法开始
     * @param config 算法配置
     */
    public void startAlgorithm(NSGAIIConfiguration config) {
        startTime = System.currentTimeMillis();
        
        if (logLevel.ordinal() >= LogLevel.INFO.ordinal()) {
            log("NSGA-II算法开始执行");
            log("配置：" + config.toString());
        }
    }
    
    /**
     * 记录初始种群生成
     * @param population 初始种群
     */
    public void logInitialPopulation(Population population) {
        if (logLevel.ordinal() >= LogLevel.INFO.ordinal()) {
            log("初始种群生成完成，大小：" + population.size());
        }
        
        if (verbosePopulation && logLevel.ordinal() >= LogLevel.DEBUG.ordinal()) {
            log(population.getStatistics());
        }
    }
    
    /**
     * 记录代际开始
     * @param generation 代数
     */
    public void startGeneration(int generation) {
        if (logLevel.ordinal() >= LogLevel.DEBUG.ordinal()) {
            log("开始第 " + (generation + 1) + " 代");
        }
    }
    
    /**
     * 记录代际信息
     * @param generation 代数
     * @param population 当前代种群
     * @param paretoFront 当前代帕累托前沿
     */
    public void logGeneration(int generation, Population population, List<MealSolution> paretoFront) {
        if (logLevel.ordinal() >= LogLevel.INFO.ordinal()) {
            log("第 " + (generation + 1) + " 代完成");
            log("帕累托前沿解数量：" + paretoFront.size());
            
            if (verbosePopulation) {
                log(population.getStatistics());
            }
            
            if (verboseObjectives && !paretoFront.isEmpty()) {
                MealSolution bestSolution = paretoFront.get(0);
                log("前沿解示例：");
                logSolution(bestSolution);
            }
        }
    }
    
    /**
     * 记录提前终止
     * @param generation 终止的代数
     */
    public void logEarlyTermination(int generation) {
        if (logLevel.ordinal() >= LogLevel.INFO.ordinal()) {
            log("在第 " + (generation + 1) + " 代提前终止算法");
        }
    }
    
    /**
     * 记录最终解决方案
     * @param solutions 最终的帕累托前沿解
     */
    public void logFinalSolutions(List<MealSolution> solutions) {
        if (logLevel.ordinal() >= LogLevel.INFO.ordinal()) {
            log("算法执行完成，生成了 " + solutions.size() + " 个帕累托最优解");
            log("总耗时：" + formatElapsedTime(System.currentTimeMillis() - startTime));
            
            if (verboseObjectives && !solutions.isEmpty()) {
                log("\n最优解的目标值：");
                for (int i = 0; i < Math.min(3, solutions.size()); i++) {
                    log("\n解决方案 #" + (i + 1) + ":");
                    logSolution(solutions.get(i));
                }
            }
        }
    }
    
    /**
     * 记录解决方案详情
     * @param solution 解决方案
     */
    public void logSolution(MealSolution solution) {
        if (logLevel.ordinal() >= LogLevel.DEBUG.ordinal()) {
            StringBuilder sb = new StringBuilder();
            sb.append("解决方案 [等级=").append(solution.getRank())
              .append(", 拥挤度=").append(String.format("%.3f", solution.getCrowdingDistance()))
              .append("]:\n");
            
            // 记录目标值
            sb.append("目标值:\n");
            for (ObjectiveValue obj : solution.getObjectiveValues()) {
                sb.append("  ").append(obj.getName()).append(": ")
                  .append(String.format("%.3f", obj.getValue()))
                  .append(" (权重=").append(String.format("%.2f", obj.getWeight())).append(")\n");
            }
            
            // 记录食物
            sb.append("食物列表:\n");
            for (FoodGene gene : solution.getFoodGenes()) {
                sb.append("  ").append(gene.getFood().getName())
                  .append(" (").append(String.format("%d", (int)gene.getIntake())).append("g), ")
                  .append("类别=").append(gene.getFood().getCategory()).append("\n");
            }
            
            // 记录营养素总值
            sb.append("营养素总值:\n");
            sb.append("  热量: ").append(String.format("%.1f", solution.calculateTotalNutrients().calories)).append(" kcal\n");
            sb.append("  碳水: ").append(String.format("%.1f", solution.calculateTotalNutrients().carbohydrates)).append(" g\n");
            sb.append("  蛋白质: ").append(String.format("%.1f", solution.calculateTotalNutrients().protein)).append(" g\n");
            sb.append("  脂肪: ").append(String.format("%.1f", solution.calculateTotalNutrients().fat)).append(" g\n");
            
            log(sb.toString());
        }
    }
    
    /**
     * 记录错误信息
     * @param message 错误信息
     */
    public void error(String message) {
        if (logLevel.ordinal() >= LogLevel.ERROR.ordinal()) {
            log("错误: " + message);
        }
    }
    
    /**
     * 记录警告信息
     * @param message 警告信息
     */
    public void warning(String message) {
        if (logLevel.ordinal() >= LogLevel.WARNING.ordinal()) {
            log("警告: " + message);
        }
    }
    
    /**
     * 记录信息
     * @param message 信息
     */
    public void info(String message) {
        if (logLevel.ordinal() >= LogLevel.INFO.ordinal()) {
            log(message);
        }
    }
    
    /**
     * 记录调试信息
     * @param message 调试信息
     */
    public void debug(String message) {
        if (logLevel.ordinal() >= LogLevel.DEBUG.ordinal()) {
            log("调试: " + message);
        }
    }
    
    /**
     * 记录跟踪信息
     * @param message 跟踪信息
     */
    public void trace(String message) {
        if (logLevel.ordinal() >= LogLevel.TRACE.ordinal()) {
            log("跟踪: " + message);
        }
    }
    
    /**
     * 输出日志
     * @param message 日志信息
     */
    private void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String logMessage = "[" + timestamp + "] " + message;
        
        // 记录到Logger
        logger.info(logMessage);
        
        // 输出到控制台
        if (consoleOutput) {
            System.out.println(logMessage);
        }
    }
    
    /**
     * 格式化耗时
     * @param elapsedMillis 耗时（毫秒）
     * @return 格式化的耗时字符串
     */
    private String formatElapsedTime(long elapsedMillis) {
        long seconds = elapsedMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        long remainingSeconds = seconds % 60;
        long remainingMillis = elapsedMillis % 1000;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d.%03d", hours, remainingMinutes, remainingSeconds, remainingMillis);
        } else if (minutes > 0) {
            return String.format("%02d:%02d.%03d", minutes, remainingSeconds, remainingMillis);
        } else {
            return String.format("%02d.%03d秒", seconds, remainingMillis);
        }
    }
    
    /**
     * 获取日志级别
     * @return 日志级别
     */
    public LogLevel getLogLevel() {
        return logLevel;
    }
    
    /**
     * 设置日志级别
     * @param logLevel 日志级别
     */
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }
    
    /**
     * 是否在控制台输出
     * @return 是否在控制台输出
     */
    public boolean isConsoleOutput() {
        return consoleOutput;
    }
    
    /**
     * 设置是否在控制台输出
     * @param consoleOutput 是否在控制台输出
     */
    public void setConsoleOutput(boolean consoleOutput) {
        this.consoleOutput = consoleOutput;
    }
    
    /**
     * 是否输出详细的目标值
     * @return 是否输出详细的目标值
     */
    public boolean isVerboseObjectives() {
        return verboseObjectives;
    }
    
    /**
     * 设置是否输出详细的目标值
     * @param verboseObjectives 是否输出详细的目标值
     */
    public void setVerboseObjectives(boolean verboseObjectives) {
        this.verboseObjectives = verboseObjectives;
    }
    
    /**
     * 是否输出详细的种群状态
     * @return 是否输出详细的种群状态
     */
    public boolean isVerbosePopulation() {
        return verbosePopulation;
    }
    
    /**
     * 设置是否输出详细的种群状态
     * @param verbosePopulation 是否输出详细的种群状态
     */
    public void setVerbosePopulation(boolean verbosePopulation) {
        this.verbosePopulation = verbosePopulation;
    }
}