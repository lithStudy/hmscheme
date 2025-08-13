package com.mealplanner;

import com.mealplanner.service.MealPlanningService;

/**
 * NSGA-II多目标遗传算法膳食规划演示类（入口）
 */
public class GeneticMealPlannerDemo {

    public static void main(String[] args) {
        System.out.println("NSGA-II多目标遗传算法膳食规划演示");
        System.out.println("====================================");
        new MealPlanningService().run();
    }
}