package com.rabbiter.em.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.Good;
import com.rabbiter.em.entity.Standard;
import com.rabbiter.em.entity.dto.GoodDTO;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.mapper.GoodMapper;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.rabbiter.em.constants.RedisConstants.GOOD_TOKEN_KEY;
import static com.rabbiter.em.constants.RedisConstants.GOOD_TOKEN_TTL;

/**
 * 商品服务类
 * @author uzzyan
 */

@Service
public class GoodService extends ServiceImpl<GoodMapper, Good> {

    @Resource
    private GoodMapper goodMapper;
    @Resource
    private RedisTemplate<String, Good> redisTemplate;

    /**
     * 根据ID获取商品详情 (Get Good By ID)
     * 功能描述：优先从Redis缓存中获取商品信息，若缓存未命中则查询数据库并写入缓存。
     * @param id 商品ID
     * @return Good 商品实体对象
     * @exception ServiceException 当数据库中也查不到该商品时抛出。
     */
    public Good getGoodById(Long id) {
        String redisKey = GOOD_TOKEN_KEY + id;
        // 从redis中查，若有则返回
        ValueOperations<String, Good> valueOperations = redisTemplate.opsForValue();
        Good redisGood = valueOperations.get(redisKey);
        if(redisGood!=null){
            redisTemplate.expire(redisKey,GOOD_TOKEN_TTL, TimeUnit.MINUTES);
            return redisGood;
        }
        // 若redis中没有则去数据库查
        LambdaQueryWrapper<Good> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Good::getIsDelete,false);
        queryWrapper.eq(Good::getId,id);
        Good dbGood = getOne(queryWrapper);
        if(dbGood!=null){
            // 将商品信息存入redis
            valueOperations.set(redisKey,dbGood);
            redisTemplate.expire(redisKey,GOOD_TOKEN_TTL, TimeUnit.MINUTES);
            return dbGood;
        }
        // 数据库中没有则返回异常
        throw new ServiceException(Constants.NO_RESULT,"无结果");

    }

    /**
     * 获取商品规格信息 (Get Good Standard)
     * @param id 商品ID
     * @return String 规格信息的JSON字符串
     */
    public String getStandard(int id){
        List<Standard> standards = goodMapper.getStandardById(id);
        if(standards.size()==0){
            throw new ServiceException(Constants.NO_RESULT,"无结果");
        }
        return JSON.toJSONString(standards);
    }

    /**
     * 获取商品最低价格 (Get Minimum Price)
     * @param id 商品ID
     * @return BigDecimal 最低价格
     */
    public BigDecimal getMinPrice(Long id){
        return goodMapper.getMinPrice(id);
    }

    /**
     * 查询首页推荐商品 (Find Front Goods)
     * @return List<GoodDTO> 推荐商品列表
     */
    public List<GoodDTO> findFrontGoods() {
        List<GoodDTO> list = goodMapper.findFrontGoods();
        if (list.isEmpty()) return list;
        List<Long> ids = list.stream().map(GoodDTO::getId).collect(Collectors.toList());
        Map<Long, Integer> storeMap = batchGetTotalStore(ids);
        for (GoodDTO good : list) {
            Integer totalStore = storeMap.getOrDefault(good.getId(), 0);
            good.setTotalStore(totalStore);
            good.setSoldOut(totalStore == 0);
        }
        return list;
    }


    /**
     * 逻辑删除商品 (Fake Delete Good)
     * 功能描述：将商品标记为删除状态，并清除Redis缓存。
     * @param id 商品ID
     */
    public void deleteGood(Long id) {
        redisTemplate.delete(GOOD_TOKEN_KEY+id);
        goodMapper.fakeDelete(id);
    }

    /**
     * 保存或更新商品 (Save or Update Good)
     * 功能描述：新增商品时设置创建时间，更新商品时清除Redis缓存。
     * @param good 商品实体对象
     * @return Long 商品ID
     */
    public Long saveOrUpdateGood(Good good) {
        if(good.getId()==null){
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            good.setCreateTime(df.format(LocalDateTime.now()));
            goodMapper.insertGood(good);
        }else{
            saveOrUpdate(good);
            redisTemplate.delete(GOOD_TOKEN_KEY + good.getId());
        }
        return good.getId();
    }

    /**
     * 设置商品推荐状态 (Set Recommend Status)
     * @param id 商品ID
     * @param isRecommend 是否推荐
     * @return boolean 操作结果
     */
    public boolean setRecommend(Long id,Boolean isRecommend) {
        LambdaUpdateWrapper<Good> goodsLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        goodsLambdaUpdateWrapper.eq(Good::getId,id)
                .set(Good::getRecommend,isRecommend);
        return update(goodsLambdaUpdateWrapper);
    }

    /**
     * 获取销量排行 (Get Sales Rank)
     * @param num 获取数量
     * @return List<Good> 商品列表
     */
    public List<Good> getSaleRank(int num) {
        return goodMapper.getSaleRank(num);
    }


    /**
     * 更新商品信息 (Update Good)
     * @param good 商品对象
     */
    public void update(Good good) {
        updateById(good);
        redisTemplate.delete(GOOD_TOKEN_KEY + good.getId());
    }

    /**
     * 分页查询商品 (Page Query Goods)
     * 功能描述：支持按名称/描述模糊查询、按分类ID筛选，并自动填充最低价格。
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param searchText 搜索关键字
     * @param categoryId 分类ID
     * @return IPage<GoodDTO> 分页结果
     */
    public IPage<GoodDTO> findPage(Integer pageNum, Integer pageSize, String searchText, Integer categoryId) {
        LambdaQueryWrapper<Good> query = Wrappers.<Good>lambdaQuery().orderByDesc(Good::getId);
        // 对名称和描述进行模糊查询（不区分大小写）
        if (StrUtil.isNotBlank(searchText)) {
            query.and(wrapper -> wrapper
                .apply("LOWER(name) LIKE LOWER({0})", "%" + searchText + "%")
                .or()
                .apply("LOWER(description) LIKE LOWER({0})", "%" + searchText + "%")
                .or()
                .eq(Good::getId, searchText)
            );
        }
        if(categoryId != null){
            query.eq(Good::getCategoryId,categoryId);
        }
        // 筛除掉已被删除的商品
        query.eq(Good::getIsDelete,false);
        query.eq(Good::getStatus, 1);
        IPage<Good> page = this.page(new Page<>(pageNum, pageSize), query);
        // 把good转为dto
        IPage<GoodDTO> goodDTOPage = page.convert(good -> {
            GoodDTO goodDTO = new GoodDTO();
            BeanUtil.copyProperties(good, goodDTO);
            return goodDTO;
        });
        List<GoodDTO> records = goodDTOPage.getRecords();
        if (!records.isEmpty()) {
            List<Long> ids = records.stream().map(GoodDTO::getId).collect(Collectors.toList());
            Map<Long, BigDecimal> priceMap = batchGetMinPrice(ids);
            Map<Long, Integer> storeMap = batchGetTotalStore(ids);
            for (GoodDTO good : records) {
                good.setPrice(priceMap.get(good.getId()));
                Integer totalStore = storeMap.getOrDefault(good.getId(), 0);
                good.setTotalStore(totalStore);
                good.setSoldOut(totalStore == 0);
            }
        }
        return goodDTOPage;
    }

    public IPage<Good> findFullPage(Integer pageNum, Integer pageSize, String searchText, Integer categoryId) {
        LambdaQueryWrapper<Good> query = Wrappers.<Good>lambdaQuery().orderByDesc(Good::getId);
        if (StrUtil.isNotBlank(searchText)) {
            query.and(wrapper -> wrapper
                .apply("LOWER(name) LIKE LOWER({0})", "%" + searchText + "%")
                .or()
                .apply("LOWER(description) LIKE LOWER({0})", "%" + searchText + "%")
                .or()
                .eq(Good::getId, searchText)
            );
        }
        if(categoryId != null){
            query.eq(Good::getCategoryId,categoryId);
        }
        query.eq(Good::getIsDelete,false);
        IPage<Good> page = this.page(new Page<>(pageNum, pageSize), query);
        List<Good> records = page.getRecords();
        if (!records.isEmpty()) {
            List<Long> ids = records.stream().map(Good::getId).collect(Collectors.toList());
            Map<Long, BigDecimal> priceMap = batchGetMinPrice(ids);
            Map<Long, Integer> storeMap = batchGetTotalStore(ids);
            for (Good good : records) {
                good.setPrice(priceMap.get(good.getId()));
                Integer totalStore = storeMap.getOrDefault(good.getId(), 0);
                good.setTotalStore(totalStore);
                good.setSoldOut(totalStore == 0);
            }
        }
        return page;
    }

    private Map<Long, BigDecimal> batchGetMinPrice(List<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyMap();
        List<Map<String, Object>> rows = goodMapper.getMinPricesByIds(ids);
        Map<Long, BigDecimal> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long goodId = ((Number) row.get("goodId")).longValue();
            Object val = row.get("minPrice");
            map.put(goodId, val != null ? new BigDecimal(val.toString()) : null);
        }
        return map;
    }

    private Map<Long, Integer> batchGetTotalStore(List<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyMap();
        List<Map<String, Object>> rows = goodMapper.getTotalStoreByIds(ids);
        Map<Long, Integer> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long goodId = ((Number) row.get("goodId")).longValue();
            Object val = row.get("totalStore");
            map.put(goodId, val != null ? ((Number) val).intValue() : 0);
        }
        return map;
    }
}
