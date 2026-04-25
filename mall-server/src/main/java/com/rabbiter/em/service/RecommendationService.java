package com.rabbiter.em.service;

import com.rabbiter.em.entity.Good;
import com.rabbiter.em.mapper.GoodMapper;
import com.rabbiter.em.mapper.OrderGoodsMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
     * 
     * @param userId 用户ID
     * @return List<Good> 推荐商品列表
     */
    public List<Good> recommend(Long userId) {
        // 1. 获取所有用户-商品交互数据
        List<Map<String, Object>> interactions = orderGoodsMapper.selectUserItemInteractions();
        
        // 2. 检查数据量，冷启动阶段使用热销推荐
        if (interactions.size() < 10) {
            return getTopSellingGoods();
        }

        try {
            // 3. 构建用户-物品评分矩阵 (User-Item Matrix)
            // Map<UserId, Map<GoodId, Rating>>
            Map<Long, Map<Long, Double>> userItemMatrix = new HashMap<>();
            // Map<GoodId, Set<UserId>> itemUsersMap (倒排表，加速计算)
            Map<Long, Set<Long>> itemUsersMap = new HashMap<>();

            for (Map<String, Object> interaction : interactions) {
                Long uId = Long.parseLong(interaction.get("userId").toString());
                Long iId = Long.parseLong(interaction.get("goodId").toString());
                
                userItemMatrix.computeIfAbsent(uId, k -> new HashMap<>()).put(iId, 1.0); // 隐式反馈，购买设为1.0
                itemUsersMap.computeIfAbsent(iId, k -> new HashSet<>()).add(uId);
            }

            // 4. 获取目标用户的已购买商品
            Map<Long, Double> targetUserLiked = userItemMatrix.get(userId);
            if (targetUserLiked == null || targetUserLiked.isEmpty()) {
                return getTopSellingGoods();
            }

            // 5. 计算物品相似度 (Item Similarity) - 余弦相似度
            // 只计算与目标用户已买商品有关的物品
            Map<Long, Double> scores = new HashMap<>();
            
            for (Long likedItemId : targetUserLiked.keySet()) {
                Set<Long> usersWhoBoughtLikedItem = itemUsersMap.get(likedItemId);
                
                for (Long candidateItemId : itemUsersMap.keySet()) {
                    if (targetUserLiked.containsKey(candidateItemId)) continue; // 跳过已买

                    Set<Long> usersWhoBoughtCandidate = itemUsersMap.get(candidateItemId);
                    
                    // 计算余弦相似度
                    // Intersection size
                    Set<Long> intersection = new HashSet<>(usersWhoBoughtLikedItem);
                    intersection.retainAll(usersWhoBoughtCandidate);
                    
                    if (intersection.isEmpty()) continue;
                    
                    double similarity = intersection.size() / Math.sqrt(usersWhoBoughtLikedItem.size() * usersWhoBoughtCandidate.size());
                    
                    // 累加得分 (Similarity * Rating(1.0))
                    scores.put(candidateItemId, scores.getOrDefault(candidateItemId, 0.0) + similarity);
                }
            }

            // 6. 排序并返回前6个
            List<Long> recommendedIds = scores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(6)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (recommendedIds.isEmpty()) {
                return getTopSellingGoods();
            }

            return goodMapper.selectBatchIds(recommendedIds);

        } catch (Exception e) {
            return getTopSellingGoods();
        }
    }

    /**
     * 兜底方案：获取热销商品
     */
    private List<Good> getTopSellingGoods() {
        return goodMapper.selectList(null).stream()
                .sorted((g1, g2) -> g2.getSales().compareTo(g1.getSales()))
                .limit(6)
                .collect(Collectors.toList());
    }
}
