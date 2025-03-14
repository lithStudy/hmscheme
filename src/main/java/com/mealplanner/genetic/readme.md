com.mealplanner.genetic/
├── algorithm/          // 核心算法组件
│   ├── NSGAIIMealPlanner.java      // 主算法控制类
│   ├── Population.java             // 种群管理
│   ├── NonDominatedSorting.java    // 非支配排序
│   └── CrowdingDistanceCalculator.java  // 拥挤度计算
├── model/              // 数据模型
│   ├── MealSolution.java           // 膳食解决方案（染色体）
│   ├── FoodGene.java               // 食物基因
│   └── ObjectiveValue.java         // 目标值封装
├── operators/          // 遗传操作
│   ├── MealCrossover.java          // 交叉操作
│   ├── MealMutation.java           // 变异操作
│   └── MealSelection.java          // 选择操作
├── objectives/         // 多目标评价
│   ├── NutrientObjective.java      // 营养素目标
│   ├── PreferenceObjective.java    // 用户偏好目标
│   ├── DiversityObjective.java     // 多样性目标
│   ├── BalanceObjective.java       // 餐食平衡目标
│   ├── AbstractObjectiveEvaluator.java    // 目标评价器抽象类
│   └── MultiObjectiveEvaluator.java // 多目标评价器
└── util/               // 辅助工具
    ├── NSGAIIConfiguration.java    // 算法配置
    └── NSGAIILogger.java           // 算法日志