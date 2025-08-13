# NSGA-II 多目标遗传算法膳食规划：算法与参数说明

本文档详细说明本项目中 NSGA-II 多目标遗传算法在膳食规划中的实现原理、目标设计、算子细节、可调参数及调优建议，并给出端到端运行流程与结果解读方法。

---

## 1. 背景与总体思路

- 目标：针对用户个体（性别、年龄、身高、体重、活动水平、健康状况等），在给定的食物数据库中，自动生成满足营养达成率与个性化偏好的单餐膳食方案集合（帕累托前沿）。
- 方法：采用 NSGA-II（非支配排序遗传算法，带拥挤度维持多样性）进行多目标优化，最大化多个目标评分（所有目标均为「值越大越好」的评分，范围通常为 0~1）。

核心流程（类 `com.mealplanner.genetic.algorithm.NSGAIIMealPlanner`）：
1) 初始化种群（随机生成膳食方案，支持主食约束）。
2) 评估每个个体的多目标评分（营养素目标、用户偏好、多样性、平衡性）。
3) 非支配排序（`NonDominatedSorting`），分配等级 Rank。
4) 拥挤距离计算（`CrowdingDistanceCalculator`），维持前沿解多样性。
5) 基于等级与拥挤度的选择（`MealSelection`）。
6) 交叉（`MealCrossover`）与变异（`MealMutation`）产生子代；与父代合并后再次排序与截断，进入下一代。
7) 终止条件满足后，输出第一前沿，并基于营养素达成率做最终过滤与兜底策略。

---

## 2. 个性化建模与目标体系

### 2.1 个性化营养需求与达成率范围
- 每日营养目标：`NutrientType.getDailyIntakes(UserProfile)` 按用户 TDEE 与宏量营养素比例估算每日热量与三大营养素，并为微量元素提供个体化默认值。
- 单餐目标：服务层按 `meals.per_day` 等比分配到单餐（`MealPlanningService`）。
- 达成率范围：`NutrientType.getNutrientRates(UserProfile)` 根据疾病等健康状况收紧或放宽各营养素的达成区间（默认范围见 `NutrientType` 枚举定义中 `defaultAchievementRange`）。
- 营养权重：`NutrientType.getNutrientWeights(UserProfile)` 可依据健康状况动态调整各营养素在总评分中的相对重要性。

### 2.2 多目标评价器 `MultiObjectiveEvaluator`
聚合四类目标，输出 `List<ObjectiveValue>`：
- 营养素目标（多项）：`NutrientObjective`
  - 为每个营养素创建一个目标，采用相应评分策略：
    - 热量：`CalorieScoringStrategy`（区间内线性插值、区间外二次/指数惩罚）。
    - 钠：`StrictExcessPenaltyScoringStrategy`（对超量更严格的指数惩罚）。
    - 其他：`DefaultNutrientScoringStrategy`（区间内线性，区间外平滑衰减）。
  - 硬约束门槛：默认 0.8（`hardConstraintThreshold`）。
- 用户偏好目标：`UserPreferenceObjective`
  - 考虑过敏、宗教禁忌、不喜欢、辣度容忍度、口味偏好等；存在严重违规将大幅降分。
- 多样性目标：`FoodDiversityObjective`
  - 类别覆盖率与理想类别分布接近度（默认：主食/蔬菜/水果/肉类/鱼/蛋/乳/油脂比例），并考虑简单的食物组合合理性。
- 营养平衡目标：`NutrientBalanceObjective`
  - 宏量营养素热量比例相对理想比例（来自 `NutrientRatio.calculateNutrientRatio(userProfile)`）的偏差；
  - 摄入量合理性（食材摄入更接近推荐默认值得分更高），并融合热量接近目标值的得分。

`ObjectiveValue` 统一以 0~1 为分值、带权重；NSGA-II 的支配关系与拥挤度均基于这些值（值越大越优）。

---

## 3. NSGA-II 细节实现

### 3.1 非支配排序 `NonDominatedSorting`
- 定义：解 A 支配解 B，当 A 在所有目标上均不差于 B，且至少在一个目标上优于 B。
- 本实现中目标值越大越好，且会忽略权重为 0 的目标。
- 输出：为每个解赋予等级 Rank=1,2,3,…，Rank=1 为第一前沿。

### 3.2 拥挤距离 `CrowdingDistanceCalculator`
- 对同一前沿内，各目标分别按值排序，边界点拥挤度设为 +∞，中间点累加相邻差分/全局范围归一化后的距离。
- 用于同层截断时的多样性保持：拥挤度越大，个体越优先保留。

### 3.3 选择 `MealSelection`
- 默认锦标赛选择（可切换到轮盘赌或等级选择）。
- 锦标赛内部以 Rank 先比，拥挤度次比。
- 锦标赛规模由 `tournamentSize` 控制（默认 2）。

### 3.4 交叉 `MealCrossover`
- 交叉率 `crossoverRate` 控制是否触发。
- 特性：确保每个子代“主食”有且仅有一个；对非主食部分采用单点交叉；去重同名食物。

### 3.5 变异 `MealMutation`
- 变异率 `mutationRate` 控制是否触发。支持多种策略：
  - 摄入量微调（在推荐范围内随机偏移）。
  - 食物替换（同类别替换，保主食约束）。
  - 添加/移除食物（结合是否已含主食）。
  - 热量优化（将总热量向目标靠拢）。
  - 营养素敏感度（NUTRIENT_SENSITIVITY）：
    - 计算当前方案各营养素达成率；
    - 找出不足/过量营养素；
    - 估计各食材对目标营养素的贡献度；
    - 选择最相关的 1~3 个食材，按贡献度与差距强度，定量上/下调克重，确保仍在推荐范围内；
    - 维持方案有效性（主食唯一、无重复、克重在推荐范围）。
- 重要外部输入：营养素达成率范围映射与单餐目标营养素（由 `NSGAIIMealPlanner` 注入）。

### 3.6 终止条件与结果过滤
- 终止：
  - 达到最大代数 `maxGenerations`；或
  - 第一前沿解数 ≥ `minParetoSolutions` 且所有解满足 “总体评分 ≥ 0.8 且硬约束满足”。
- 结果：取 Rank=1 的前沿解，再根据营养素达成率范围做硬过滤；若为空，则从 Rank=1 中按“平均目标分”降序选取最多 3 个兜底解。

---

## 4. 参数一览与配置来源

### 4.1 算法参数（`NSGAIIConfiguration`）
- `populationSize`：种群大小，默认 50（小/标准/大 配置分别为 20/50/200）。
- `maxGenerations`：最大代数，默认 100（小/标准/大 为 30/100/200）。
- `crossoverRate`：交叉概率，默认 0.9。
- `mutationRate`：变异概率，默认 0.2~0.3（配置档案决定）。
- `minFoodsPerMeal` / `maxFoodsPerMeal`：每餐最少/最多食物数，默认 4 / 8。
- `tournamentSize`：锦标赛规模，默认 2。
- `minParetoSolutions`：提前终止所需的最小帕累托解数，默认 10。
- `earlyTerminationCheckInterval` / `maxGenerationsWithoutImprovement`：预留的早停检查粒度与容忍连续不改进代数（当前主流程未显式使用）。
- `randomSeed`：随机种子（可复现实验）。
- `parallelExecution`：并行标志（当前实现未启用并行段）。

配置入口：由 `MealPlanningService` 通过 `AppConfig` 读取 `algorithm.profile`（small/standard/large）创建预设；也可在代码中直接 set。

### 4.2 应用配置（`src/main/resources/config.properties`）
- `foods.excel`：食物数据库 Excel 路径。
- `export.excel`：导出结果 Excel 路径（新增或覆盖）。
- `exclude.categories`：构建食物数据库时需排除的类别（如 FRUIT,OIL,PASTRY,MILK）。
- `require.staple`：是否强制每餐必须且仅有一个主食（true/false）。
- `meals.per_day`：每天餐数，用于将每日目标等比分摊到单餐。
- `algorithm.profile`：算法规模配置（small/standard/large）。
- `log.level` / `log.verboseObjectives` / `log.verbosePopulation`：日志级别与详细度。
- `random.seed`：随机种子（留空则使用系统时间）。

---

## 5. 集中调参（AppConfig 常量，不走配置文件）

为便于快速调参，目标体系相关权重与结构开关统一集中在 `com.mealplanner.config.AppConfig` 的常量中（仅影响目标优化行为；不会改动 Excel 路径、日志等级等应用配置）。所有常量在代码中有中文注释，以下列出要点：

- 是否展开复合目标（聚合/展开开关）
  - `OBJ_EXPAND_COMPOSITE`: true 时将复合目标拆成子目标（营养素按宏/微组、多样性与平衡性拆分子维度）；false 为聚合模式。
- 顶层目标权重（参与平均分与“足够好”判断；非支配排序仍按目标值大小比较）
  - `W_NUTRIENTS_MACRO` / `W_NUTRIENTS_MICRO`: 营养素宏/微两组复合目标权重
  - `W_PREFERENCE`: 用户偏好目标权重
  - `W_DIVERSITY`: 多样性目标权重
  - `W_BALANCE`: 平衡性目标权重
- 多样性子权重（内部自动归一化，三者建议和为 1）
  - `W_DIV_CATEGORY`: 类别多样性（覆盖率+分布）
  - `W_DIV_ATTRIBUTE`: 特性多样性（烹饪方式、口味、辣度）
  - `W_DIV_COMBINATION`: 组合合理性（主食/蔬菜/蛋白搭配）
- 平衡性子权重
  - `W_BALANCE_MACRO_RATIO`: 宏量配比占比（剩余给“摄入合理性”）
- 偏好子权重（偏好目标内部各因素影响力度）
  - `W_PREF_FLAVOR`: 口味匹配
  - `W_PREF_ALLERGEN`: 过敏原惩罚
  - `W_PREF_RELIGION`: 宗教禁忌惩罚
  - `W_PREF_DISLIKE`: 不喜欢惩罚
  - `W_PREF_SPICY`: 辣度偏差惩罚

应用位置：
- `MultiObjectiveEvaluator` 在构造时从 `AppConfig` 注入上述权重；
- `NSGAIIMealPlanner` 通过 `setExpandCompositeObjectives(AppConfig.isExpandCompositeObjectives())` 应用展开开关。

说明：集中常量便于代码内一处调参；如需运行时切换，可将这些常量替换为命令行/配置中心输入，但当前实现按需求固定为常量。

---

## 5. 有效性与约束处理

- 方案有效性（`MealSolution.isValid(requireStaple)`)：
  - 至少包含 1 种食物；
  - 若要求主食，主食数量必须为 1；
  - 无重复食物；
  - 每个食材克重在其推荐最小/最大范围内。
- 生成与变异过程中均有校验与修复逻辑：
  - 随机生成：若要求主食，先放入一个主食，再从其他类别中抽取；
  - 交叉/变异：在应用后若无效，进行局部修复或回退。

---

## 6. 运行流程（端到端）

入口：`com.mealplanner.GeneticMealPlannerDemo`

服务流程（`MealPlanningService.run`）：
1) 读取配置（`AppConfig`）。
2) 加载并清洗食物库（Excel → `NutritionDataParser` → `Food` 对象）。
3) 构建用户档案 `UserProfile`（含健康状况）。
4) 选择算法配置档案（small/standard/large）→ 构建 `NSGAIIConfiguration`。
5) 初始化规划器 `NSGAIIMealPlanner`，同步日志配置。
6) 计算单餐目标营养素（按 `meals.per_day` 等分每日需求）。
7) 运行 `generateMeal(targetNutrients, requireStaple)`。
8) 导出结果到 Excel（`MealSolutionExcelExporter`）。
9) 控制台打印前若干解的概要、营养达成率与目标分项得分。

---

## 7. 结果解读

- 平均得分：`NSGAIIMealPlanner.calculateAverageObjectiveScore` 基于各目标分与权重的加权均值（0~1）。
- 营养素比较：显示实际/目标与达成率百分比，并基于达成率范围打标 [不足] / [达标] / [过量]。
- 宏量营养素热量比例：显示三大营养素热量占比，辅助判断配比是否合理。
- 目标分项：逐个列出目标名、得分与权重，便于诊断是哪个目标限制了方案质量。

---

## 8. 调参建议与常见问题

### 8.1 调参建议
- 质量 vs 速度：
  - `populationSize` 与 `maxGenerations` 越大，通常解越好但耗时越长。一般先用 `standard` 调试，再切 `large` 出最终结果。
- 多样性与收敛：
  - `mutationRate` 适当提高（如 0.2→0.3）可避免早熟，但过大影响收敛。
  - `tournamentSize` 增大选择压力 → 收敛更快但易丢多样性；反之则更保多样性。
- 目标体系：
  - 调整 `NutrientType.getNutrientWeights` 的权重映射，突出关键营养素。
  - `MultiObjectiveEvaluator.setGoodEnoughThreshold` 可从 0.8 微调为 0.75~0.85，以影响提前终止与“足够好”判断。
  - `FoodDiversityObjective` 的理想分布与权重可按业务侧偏好调整。
- 约束：
  - `require.staple` 会显著影响搜索空间与可行性，确保食物库主食类别充足。
- 数据：
  - Excel 食物库的营养数据与摄入推荐范围务必准确、覆盖充分；缺失与异常值会恶化搜索质量。

### 8.2 常见问题
- 第一前沿经筛后为空：
  - 框架会回退选取 Rank=1 中平均目标分最高的最多 3 个方案；
  - 可降低营养素达成率约束（放宽 `NutrientType` 对应范围），或提高 `maxGenerations`、`populationSize`。
- 解过于雷同：
  - 提升 `mutationRate`，或降低 `tournamentSize` 以提升多样性；
  - 检查食物库是否类别与条目过少。

---

## 9. 扩展点

- 新增目标：继承 `AbstractObjectiveEvaluator` 并在 `MultiObjectiveEvaluator` 中注册。
- 自定义营养素评分：实现 `NutrientScoringStrategy` 并在 `NutrientObjective` 构造时注入。
- 强化健康状况建模：扩展 `NutrientType` 中疾病-营养素范围与每日摄入量的联动表。
- 并行评估：依据 `NSGAIIConfiguration.parallelExecution`，可在评估或算子层引入并行化（当前未启用）。

---

## 10. 关键类参考

- 算法主控：`genetic.algorithm.NSGAIIMealPlanner`
- 排序与拥挤度：`genetic.algorithm.NonDominatedSorting`，`genetic.algorithm.CrowdingDistanceCalculator`
- 种群：`genetic.algorithm.Population`
- 选择/交叉/变异：`genetic.operators.MealSelection`，`genetic.operators.MealCrossover`，`genetic.operators.MealMutation`
- 目标体系：`genetic.objectives.*`（`NutrientObjective` / `UserPreferenceObjective` / `FoodDiversityObjective` / `NutrientBalanceObjective`）
- 用户与营养：`model.UserProfile`，`model.NutrientType`，`model.NutrientRatio`
- 服务与配置：`service.MealPlanningService`，`config.AppConfig`，`src/main/resources/config.properties`

---

如需补充更多业务域规则（例如特定疾病的食材禁忌或烹饪法偏好）、数据字典（Excel 字段与单位）、或导出格式细则，可在此文档后续章节继续扩展。
