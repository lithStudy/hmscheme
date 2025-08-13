package com.mealplanner.config;

import java.util.HashSet;
import java.util.Set;

import com.mealplanner.genetic.util.NSGAIILogger.LogLevel;
import com.mealplanner.model.FoodCategory;
import com.mealplanner.model.HealthConditionType;

/**
 * 应用程序集中配置类
 * 管理所有可调节的配置参数，包括：
 * - 应用运行参数（文件路径、算法配置等）
 * - 用户档案默认值
 * - 目标函数权重配置
 * - 多样性和平衡性子维度权重
 * - 用户偏好相关权重
 */
public class AppConfig {
    // 说明：本类集中管理所有"可调配置"，均以 public static 常量形式存在；不再使用配置文件或访问方法。

    // 单例模式持有者
    private static class Holder {
        private static final AppConfig INSTANCE = new AppConfig();
    }

    /**
     * 获取配置类实例
     * @return 配置类单例
     */
    public static AppConfig getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 私有构造函数，防止外部实例化
     */
    private AppConfig() { }

    // ===== 应用与运行参数（常量） =====
    
    /** Excel食物数据库文件路径 */
    public static final String FOODS_EXCEL = "src/main/resources/foods.xlsx";
    
    /** 导出结果Excel文件路径 */
    public static final String EXPORT_EXCEL = "meal_solutions.xlsx";
    
    /** 是否要求每餐必须且仅有一个主食类食物 */
    public static final boolean REQUIRE_STAPLE = true;
    
    /** 每日用餐次数（用于将每日营养目标等比分摊到单餐） */
    public static final int MEALS_PER_DAY = 3;
    
    /** 算法配置档案：small（小型）| standard（标准）| large（大型） */
    public static final String ALGO_PROFILE = "large";
    
    /** 日志记录等级 */
    public static final LogLevel LOG_LEVEL = LogLevel.INFO;
    
    /** 是否开启详细目标函数日志 */
    public static final boolean LOG_VERBOSE_OBJECTIVES = false;
    
    /** 是否开启详细种群状态日志 */
    public static final boolean LOG_VERBOSE_POPULATION = false;
    
    /** 随机种子（为null时使用系统时间作为种子） */
    public static final Long RANDOM_SEED = null;

    
    /** 构建食物库时要排除的食物类别集合 */
    public static final Set<FoodCategory> EXCLUDED_CATEGORIES = new HashSet<FoodCategory>() {{
        add(FoodCategory.FRUIT);     // 水果
        add(FoodCategory.OIL);       // 油脂
        add(FoodCategory.PASTRY);    // 糕点
        add(FoodCategory.MILK);      // 乳制品
    }};

    // ===== 用户档案默认值（可根据需要调整） =====
    
    /** 用户性别：M（男）/F（女） */
    public static final String USER_GENDER = "M";
    
    /** 用户年龄（岁） */
    public static final int USER_AGE = 30;
    
    /** 用户身高（厘米） */
    public static final int USER_HEIGHT_CM = 170;
    
    /** 用户体重（公斤） */
    public static final int USER_WEIGHT_KG = 65;
    
    /** 用户活动水平系数（1.2~1.9，1.55为中等活动水平） */
    public static final double USER_ACTIVITY = 1.55;
    
    /** 用户健康状况类型数组 */
    public static final HealthConditionType[] USER_HEALTH_CONDITIONS = new HealthConditionType[] {
            HealthConditionType.HYPERTENSION,  // 高血压
            HealthConditionType.DIABETES       // 糖尿病
    };

    // ===== 目标体系相关配置（不再走配置文件） =====
    // 说明：以下配置仅用于"目标权重与结构"的集中调参，不影响数据路径、日志等应用级配置。


    // ===== 顶层目标权重配置 =====
    // 注：这些权重参与加权平均与"是否足够好"的判断，不改变非支配排序的方向
    
    /** 营养素-宏量组权重（热量/碳水化合物/蛋白质/脂肪） */
    public static final double W_NUTRIENTS_MACRO = 0.5;
    
    /** 营养素-微量组权重（矿物质/维生素等） */
    public static final double W_NUTRIENTS_MICRO = 0.5;
    
    /** 用户偏好目标权重 */
    public static final double W_PREFERENCE = 0.2;
    
    /** 食物多样性目标权重 */
    public static final double W_DIVERSITY = 0.2;
    
    /** 营养平衡目标权重 */
    public static final double W_BALANCE = 0.2;

    // ===== 多样性子维度权重配置 =====
    // 注：三者之和建议为1，系统会自动归一化
    
    /** 类别多样性权重（食物类别覆盖度+分布均匀性） */
    public static final double W_DIV_CATEGORY = 0.5;
    
    /** 特性多样性权重（烹饪方式、口味、辣度等；当前默认影响较小） */
    public static final double W_DIV_ATTRIBUTE = 0.0;
    
    /** 组合合理性权重（主食/蔬菜/蛋白质等搭配是否均衡） */
    public static final double W_DIV_COMBINATION = 0.2;

    // ===== 平衡性子维度权重配置 =====
    
    /** 
     * 宏量营养素配比权重 vs 摄入合理性权重的比例
     * 取值范围[0,1]，例如0.6表示宏量配比占60%，摄入合理性占40%
     */
    public static final double W_BALANCE_MACRO_RATIO = 0.6;

    // ===== 用户偏好子维度权重配置 =====
    // 注：用于在偏好目标内部细化不同偏好因素的影响力度
    
    /** 口味匹配权重：匹配到的口味特征越多得分越高 */
    public static final double W_PREF_FLAVOR = 0.3;
    
    /** 过敏因子权重：出现过敏原时的扣分强度（通常设置为较大值） */
    public static final double W_PREF_ALLERGEN = 1.0;
    
    /** 宗教禁忌权重：违反宗教饮食禁忌的扣分强度（通常设置为较大值） */
    public static final double W_PREF_RELIGION = 1.0;
    
    /** 不喜欢食物权重：命中"不喜欢"食物的扣分强度 */
    public static final double W_PREF_DISLIKE = 0.8;
    
    /** 辣度偏好权重：与用户可接受辣度的偏差惩罚强度 */
    public static final double W_PREF_SPICY = 0.6;

}

