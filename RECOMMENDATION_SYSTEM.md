# 推荐系统技术文档 (Recommendation System Documentation)

## 1. 概述
本项目采用了 **基于物品的协同过滤 (Item-based Collaborative Filtering)** 算法来实现商品的个性化推荐。该系统通过分析用户的历史购买行为，计算商品之间的相似度，从而向用户推荐与其已购商品相似的其他商品。

## 2. 算法原理
协同过滤算法是推荐系统中最经典且应用最广泛的算法之一。本系统选择 **Item-based CF** 而非 User-based CF，主要基于以下考虑：
*   **稳定性**：商品集合相对用户集合更稳定，相似度矩阵更新频率较低。
*   **解释性**：可以向用户解释"因为您购买了A，所以推荐B"，用户体验更好。

### 2.1 相似度计算
我们采用了 **余弦相似度 (Cosine Similarity)** 来衡量两个商品之间的相似程度。

$$
Similarity(i, j) = \cos(\vec{i}, \vec{j}) = \frac{|U_i \cap U_j|}{\sqrt{|U_i| \times |U_j|}}
$$

其中：
*   $U_i$ 表示购买了商品 $i$ 的用户集合。
*   $U_j$ 表示购买了商品 $j$ 的用户集合。
*   分子 $|U_i \cap U_j|$ 表示同时购买了商品 $i$ 和 $j$ 的用户数量。
*   分母 $\sqrt{|U_i| \times |U_j|}$ 用于归一化，消除热门商品因购买人数多而产生的偏差。

## 3. 实现细节

### 3.1 技术选型
*   **语言**：Java
*   **实现方式**：原生 Java 手动实现
*   **原因**：最初尝试引入 `cf4j` 推荐库，但发现其依赖的 `freemarker` 版本与 Spring Boot 项目存在冲突，导致启动失败。为保证系统稳定性和轻量化，决定手动实现 Item-based CF 核心逻辑。

### 3.2 核心流程
推荐服务 `RecommendationService.java` 的处理流程如下：

1.  **数据加载**：
    从数据库 `order_goods` 表中查询所有用户与商品的交互记录（购买记录）。
    ```java
    List<Map<String, Object>> interactions = orderGoodsMapper.selectUserItemInteractions();
    ```

2.  **冷启动检查**：
    如果系统交互数据过少（< 10 条），无法计算有效的相似度，则直接降级为 **热销推荐** 策略。

3.  **构建评分矩阵**：
    *   构建 **用户-物品矩阵 (User-Item Matrix)**：`Map<UserId, Map<GoodId, Rating>>`，用于快速获取某用户买过的商品。
    *   构建 **物品-用户倒排表 (Item-Users Map)**：`Map<GoodId, Set<UserId>>`，用于快速获取买过某商品的用户集合，加速相似度计算。
    *   *注：本项目采用隐式反馈，用户购买行为即视为评分 1.0。*

4.  **计算推荐得分**：
    遍历目标用户已购买的商品列表，寻找候选商品（未购买过）。
    对每个候选商品，计算其与用户已购商品的相似度，并累加得分。
    ```java
    double similarity = intersection.size() / Math.sqrt(usersWhoBoughtLikedItem.size() * usersWhoBoughtCandidate.size());
    scores.put(candidateItemId, scores.getOrDefault(candidateItemId, 0.0) + similarity);
    ```

5.  **排序与截断**：
    将候选商品按得分从高到低排序，取前 6 个作为推荐结果。

6.  **兜底策略**：
    如果计算出的推荐列表为空（例如用户是新用户，无购买记录），则自动降级为返回销量最高的商品列表。

## 4. 代码结构
推荐逻辑封装在 `com.rabbiter.em.service.RecommendationService` 类中。

| 方法名 | 功能描述 |
| :--- | :--- |
| `recommend(Long userId)` | 主推荐入口，包含完整 CF 算法逻辑和异常处理。 |
| `getTopSellingGoods()` | 私有辅助方法，返回销量前 6 的商品，作为兜底方案。 |

## 5. 优势与局限
### 优势
*   **轻量级**：无额外重型依赖，启动快，内存占用低。
*   **可控性**：代码逻辑完全透明，易于调试和修改参数。
*   **鲁棒性**：包含完善的冷启动和异常降级机制，确保接口始终可用。

### 局限
*   **实时性**：目前是实时计算，随着数据量增大，计算耗时会增加。未来可优化为离线计算相似度矩阵 + 实时查询。
*   **单一数据源**：目前仅使用了购买数据，未利用浏览、收藏等其他行为数据。
