package com.rabbiter.em.service;

import com.rabbiter.em.entity.Good;
import com.rabbiter.em.mapper.GoodMapper;
import com.rabbiter.em.mapper.OrderGoodsMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Resource
    private OrderGoodsMapper orderGoodsMapper;

    @Resource
    private GoodMapper goodMapper;

    /**
     * 商品推荐 (Recommend Goods)
     * 功能描述：基于用户历史交互数据，采用基于物品的协同过滤算法 (Item-based Collaborative Filtering)。
     * 计算物品之间的余弦相似度，根据用户历史购买记录预测用户对未购买商品的兴趣度。
     * 返回结果：前面是算法推荐的商品，后面补充热销商品，总共显示更多商品。
     *
     * @param userId 用户ID
     * @return List<Good> 推荐商品列表
     */
    public List<Good> recommend(Long userId) {
        // 1. 获取所有用户-商品交互数据
        List<Map<String, Object>> interactions = orderGoodsMapper.selectUserItemInteractions();

        // 2. 检查数据量，冷启动阶段直接使用热销推荐
        if (interactions.size() < 10) {
            List<Good> coldStart = getTopSellingGoods(12);
            populatePrices(coldStart);
            return coldStart;
        }

        try {
            // 3. 构建用户-物品评分矩阵 (User-Item Matrix)
            Map<Long, Map<Long, Double>> userItemMatrix = new HashMap<>();
            Map<Long, Set<Long>> itemUsersMap = new HashMap<>();

            for (Map<String, Object> interaction : interactions) {
                Long uId = Long.parseLong(interaction.get("userId").toString());
                Long iId = Long.parseLong(interaction.get("goodId").toString());

                userItemMatrix.computeIfAbsent(uId, k -> new HashMap<>()).put(iId, 1.0);
                itemUsersMap.computeIfAbsent(iId, k -> new HashSet<>()).add(uId);
            }

            // 4. 获取目标用户的已购买商品
            Map<Long, Double> targetUserLiked = userItemMatrix.get(userId);
            if (targetUserLiked == null || targetUserLiked.isEmpty()) {
                return getTopSellingGoods(12);
            }

            // 5. 计算物品相似度 (Item Similarity) - 余弦相似度
            Map<Long, Double> scores = new HashMap<>();

            for (Long likedItemId : targetUserLiked.keySet()) {
                Set<Long> usersWhoBoughtLikedItem = itemUsersMap.get(likedItemId);

                for (Long candidateItemId : itemUsersMap.keySet()) {
                    if (targetUserLiked.containsKey(candidateItemId)) continue;

                    Set<Long> usersWhoBoughtCandidate = itemUsersMap.get(candidateItemId);

                    Set<Long> intersection = new HashSet<>(usersWhoBoughtLikedItem);
                    intersection.retainAll(usersWhoBoughtCandidate);

                    if (intersection.isEmpty()) continue;

                    double similarity = intersection.size() / Math.sqrt(usersWhoBoughtLikedItem.size() * usersWhoBoughtCandidate.size());
                    scores.put(candidateItemId, scores.getOrDefault(candidateItemId, 0.0) + similarity);
                }
            }

            // 6. 排序并获取算法推荐的商品ID（最多6个）
            List<Long> recommendedIds = scores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(6)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 7. 获取热销商品作为补充（12个）
            List<Good> topSellingGoods = getTopSellingGoods(12);

            // 8. 合并结果：算法推荐 + 热销商品（去重）
            List<Good> result = new ArrayList<>();
            Set<Long> addedIds = new HashSet<>();

            if (!recommendedIds.isEmpty()) {
                List<Good> recommendedGoods = goodMapper.selectBatchIds(recommendedIds);
                for (Good good : recommendedGoods) {
                    if (!addedIds.contains(good.getId())) {
                        result.add(good);
                        addedIds.add(good.getId());
                    }
                }
            }

            for (Good good : topSellingGoods) {
                if (!addedIds.contains(good.getId())) {
                    result.add(good);
                    addedIds.add(good.getId());
                }
            }

            // 9. 填充价格信息
            populatePrices(result);
            return result;

        } catch (Exception e) {
            return getTopSellingGoods(12);
        }
    }

    /**
     * 兜底方案：获取热销商品
     * @param limit 返回数量
     */
    private List<Good> getTopSellingGoods(int limit) {
        List<Good> goods = goodMapper.selectList(null).stream()
                .filter(g -> g.getStatus() != null && g.getStatus() == 1)
                .filter(g -> !Boolean.TRUE.equals(g.getIsDelete()))  // 排除已删除商品
                .sorted((g1, g2) -> g2.getSales().compareTo(g1.getSales()))
                .limit(limit)
                .collect(Collectors.toList());
        populatePrices(goods);
        return goods;
    }

    /**
     * 批量填充商品价格（从规格表查询最低价）
     * @param goods 商品列表
     */
    private void populatePrices(List<Good> goods) {
        if (goods == null || goods.isEmpty()) return;
        List<Long> ids = goods.stream().map(Good::getId).collect(Collectors.toList());
        List<Map<String, Object>> priceRows = goodMapper.getMinPricesByIds(ids);
        Map<Long, BigDecimal> priceMap = new HashMap<>();
        for (Map<String, Object> row : priceRows) {
            Long goodId = ((Number) row.get("goodId")).longValue();
            Object val = row.get("minPrice");
            priceMap.put(goodId, val != null ? new BigDecimal(val.toString()) : null);
        }
        for (Good good : goods) {
            good.setPrice(priceMap.get(good.getId()));
        }
    }
}
