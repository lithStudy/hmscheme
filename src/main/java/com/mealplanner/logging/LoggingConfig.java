package com.mealplanner.logging;

/**
 * 日志系统配置类
 * 用于统一管理日志系统的设置
 */
public class LoggingConfig {
    
    /**
     * 日志详细程度
     */
    public enum LogLevel {
        /** 仅输出必要信息 */
        BASIC,
        /** 输出一般信息 */
        NORMAL,
        /** 输出详细信息 */
        DETAILED,
        /** 输出调试信息 */
        DEBUG
    }
    
    private static LogLevel currentLevel = LogLevel.NORMAL;
    private static boolean colorEnabled = true;
    private static boolean logToFile = false;
    private static String logFilePath = "mealplanner.log";
    
    /**
     * 初始化日志系统
     * @param level 日志详细程度
     * @param useColor 是否使用颜色
     * @param logToFile 是否记录到文件
     * @param logFilePath 日志文件路径（如果记录到文件）
     */
    public static void initialize(LogLevel level, boolean useColor, boolean logToFile, String logFilePath) {
        currentLevel = level;
        colorEnabled = useColor;
        LoggingConfig.logToFile = logToFile;
        LoggingConfig.logFilePath = logFilePath;
        
        // 配置MealPlannerLogger
        MealPlannerLogger.setUseColor(colorEnabled);
        MealPlannerLogger.setDetailedLogs(level == LogLevel.DETAILED || level == LogLevel.DEBUG);
        
        // 输出初始化信息
        MealPlannerLogger.section("膳食规划系统日志");
        MealPlannerLogger.info("日志系统初始化完成");
        MealPlannerLogger.info("日志级别: " + level);
        if (logToFile) {
            MealPlannerLogger.info("日志文件: " + logFilePath);
        }
        MealPlannerLogger.divider();
    }
    
    /**
     * 使用默认设置初始化日志系统
     */
    public static void initialize() {
        initialize(LogLevel.NORMAL, true, false, "mealplanner.log");
    }
    
    /**
     * 设置日志详细程度
     * @param level 日志详细程度
     */
    public static void setLogLevel(LogLevel level) {
        currentLevel = level;
        MealPlannerLogger.setDetailedLogs(level == LogLevel.DETAILED || level == LogLevel.DEBUG);
        MealPlannerLogger.info("日志级别已更改为: " + level);
    }
    
    /**
     * 获取当前日志详细程度
     * @return 日志详细程度
     */
    public static LogLevel getLogLevel() {
        return currentLevel;
    }
    
    /**
     * 启用或禁用颜色输出
     * @param enabled 是否启用颜色
     */
    public static void setColorEnabled(boolean enabled) {
        colorEnabled = enabled;
        MealPlannerLogger.setUseColor(enabled);
    }
    
    /**
     * 检查颜色输出是否启用
     * @return 如果启用则返回true
     */
    public static boolean isColorEnabled() {
        return colorEnabled;
    }
    
    /**
     * 设置是否记录到文件
     * @param enabled 是否记录到文件
     */
    public static void setLogToFile(boolean enabled) {
        logToFile = enabled;
    }
    
    /**
     * 检查是否记录到文件
     * @return 如果记录到文件则返回true
     */
    public static boolean isLogToFile() {
        return logToFile;
    }
    
    /**
     * 设置日志文件路径
     * @param path 日志文件路径
     */
    public static void setLogFilePath(String path) {
        logFilePath = path;
    }
    
    /**
     * 获取日志文件路径
     * @return 日志文件路径
     */
    public static String getLogFilePath() {
        return logFilePath;
    }
} 