package com.rabbiter.em.mapper;

import com.rabbiter.em.entity.OrderGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

public interface OrderGoodsMapper extends BaseMapper<OrderGoods> {

    @Select("SELECT t.user_id as userId, og.good_id as goodId FROM t_order t JOIN order_goods og ON t.id = og.order_id")
    List<Map<String, Object>> selectUserItemInteractions();
}
