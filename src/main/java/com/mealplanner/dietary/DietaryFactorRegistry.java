package com.mealplanner.dietary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 饮食因素注册表
 * 用于管理所有饮食影响因素
 */
public class DietaryFactorRegistry {
    
    private static DietaryFactorRegistry instance;
    private List<DietaryFactor> factors;
    
    /**
     * 私有构造函数，初始化默认的饮食因素
     */
    private DietaryFactorRegistry() {
        factors = new ArrayList<>();
        
        // 注册默认的饮食因素
        registerDefaultFactors();
    }
    
    /**
     * 获取单例实例
     * @return DietaryFactorRegistry实例
     */
    public static synchronized DietaryFactorRegistry getInstance() {
        if (instance == null) {
            instance = new DietaryFactorRegistry();
        }
        return instance;
    }
    
    /**
     * 注册默认的饮食因素
     */
    private void registerDefaultFactors() {
        // 注册各种饮食因素，按优先级排序
        registerFactor(new AllergyFactor());
        registerFactor(new ReligiousRestrictionFactor());
        registerFactor(new DislikedFoodFactor());
        registerFactor(new SpicyLevelFactor());
        registerFactor(new FlavorPreferenceFactor());
        registerFactor(new CookingMethodFactor());
    }
    
    /**
     * 注册一个新的饮食因素
     * @param factor 要注册的饮食因素
     */
    public void registerFactor(DietaryFactor factor) {
        if (factor != null) {
            factors.add(factor);
            // 按优先级排序
            Collections.sort(factors, Comparator.comparingInt(DietaryFactor::getPriority).reversed());
        }
    }
    
    /**
     * 移除一个饮食因素
     * @param factorName 要移除的饮食因素名称
     * @return 如果成功移除则返回true，否则返回false
     */
    public boolean removeFactor(String factorName) {
        return factors.removeIf(factor -> factor.getName().equals(factorName));
    }
    
    /**
     * 获取所有注册的饮食因素
     * @return 饮食因素列表
     */
    public List<DietaryFactor> getAllFactors() {
        return Collections.unmodifiableList(factors);
    }
    
    /**
     * 获取所有强制性的饮食因素
     * @return 强制性饮食因素列表
     */
    public List<DietaryFactor> getMandatoryFactors() {
        List<DietaryFactor> mandatoryFactors = new ArrayList<>();
        for (DietaryFactor factor : factors) {
            if (factor.isMandatory()) {
                mandatoryFactors.add(factor);
            }
        }
        return mandatoryFactors;
    }
    
    /**
     * 获取所有非强制性的饮食因素
     * @return 非强制性饮食因素列表
     */
    public List<DietaryFactor> getNonMandatoryFactors() {
        List<DietaryFactor> nonMandatoryFactors = new ArrayList<>();
        for (DietaryFactor factor : factors) {
            if (!factor.isMandatory()) {
                nonMandatoryFactors.add(factor);
            }
        }
        return nonMandatoryFactors;
    }
    
    /**
     * 清除所有注册的饮食因素
     */
    public void clearAllFactors() {
        factors.clear();
    }
    
    /**
     * 重置为默认的饮食因素
     */
    public void resetToDefaults() {
        clearAllFactors();
        registerDefaultFactors();
    }
} 