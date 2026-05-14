package com.rabbiter.em.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.Good;
import com.rabbiter.em.entity.Order;
import com.rabbiter.em.entity.OrderGoods;
import com.rabbiter.em.entity.OrderItem;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.mapper.GoodMapper;
import com.rabbiter.em.mapper.OrderGoodsMapper;
import com.rabbiter.em.mapper.OrderMapper;
import com.rabbiter.em.mapper.StandardMapper;
import com.rabbiter.em.utils.TokenUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.rabbiter.em.constants.RedisConstants.GOOD_TOKEN_KEY;

/**
 * 订单服务类
 * @author uzzyan
 */

@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderGoodsMapper orderGoodsMapper;
    @Resource
    private StandardMapper standardMapper;
    @Resource
    private GoodMapper goodMapper;
    @Resource
    private CartService cartService;
    @Resource
    private RedisTemplate<String, Good> redisTemplate;

    /**
     * 保存订单信息 (Save Order)
     * 功能描述：接收前端传来的订单对象，生成订单号，插入数据库，并处理订单关联的商品信息，最后清除购物车。
     * @param order 订单实体对象 (Order entity)，包含商品列表JSON字符串和购物车ID。
     * @return String 生成的订单号 (Order Number)。
     * @exception ServiceException 可能抛出数据库插入失败异常。
     * @example orderService.saveOrder(new Order(...));
     */
    @Transactional
    public String saveOrder(Order order) {
        // 设置下单用户ID
        order.setUserId(TokenUtils.getCurrentUser().getId());
        // 生成唯一订单号：日期时间 + 6位随机数
        String orderNo = DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6);
        order.setOrderNo(orderNo);
        // 设置创建时间
        order.setCreateTime(DateUtil.now());
        // 插入订单主表
        orderMapper.insert(order);

        OrderGoods orderGoods = new OrderGoods();
        orderGoods.setOrderId(order.getId());
        // 遍历解析order里携带的goods数组，并用orderItem对象来接收
        String goods = order.getGoods();
        List<OrderItem> orderItems = JSON.parseArray(goods, OrderItem.class);
        for (OrderItem orderItem : orderItems) {
            long good_id = orderItem.getId();
            String standard = orderItem.getStandard();
            int num = orderItem.getNum();
            orderGoods.setGoodId(good_id);
            orderGoods.setCount(num);
            orderGoods.setStandard(standard);
            // 插入到订单商品关联表 (order_goods)
            orderGoodsMapper.insert(orderGoods);
        }
        // 支付成功/下单后清除对应购物车项
        cartService.removeById(order.getCartId());
        return orderNo;
    }

    /**
     * 订单付款处理 (Pay Order)
     * 功能描述：修改订单状态为已支付，扣减商品库存，增加商品销量和销售额，同步更新Redis缓存。
     * @param orderNo 订单编号 (Order Number)。
     * @exception ServiceException 当库存不足、商品ID非法或不存在时抛出。
     */
    @Transactional
    public void payOrder(String orderNo) {
        // 1. 更改数据库订单状态为已支付
        orderMapper.payOrder(orderNo);
        
        // 2. 获取订单详情用于库存和销量处理
        Map<String, Object> orderMap = orderMapper.selectByOrderNo(orderNo);
        int count = (int) orderMap.get("count");
        Object goodIdObj = orderMap.get("goodId");
        Long goodId = null;
        
        // ID 类型转换与校验
        if(goodIdObj instanceof Long) {
            goodId = (Long) goodIdObj;
        } else if(goodIdObj != null) {
            try {
                goodId = Long.parseLong(goodIdObj.toString());
            } catch (NumberFormatException e) {
                throw new ServiceException(Constants.CODE_500, "商品ID不正确");
            }
        }

        if(goodId == null) {
            throw new ServiceException(Constants.CODE_500, "商品ID不存在");
        }
        Good dbGood = goodMapper.selectById(goodId);
        if (dbGood == null || Boolean.TRUE.equals(dbGood.getIsDelete()) || (dbGood.getStatus() != null && dbGood.getStatus() == 0)) {
            throw new ServiceException(Constants.CODE_500, "商品已下架");
        }
        
        String standard = (String) orderMap.get("standard");
        
        // 3. 校验并扣减库存 (Deduct Stock)
        int rows = standardMapper.deductStore(goodId, standard, count);
        if (rows == 0) {
            throw new ServiceException(Constants.CODE_500, "库存不足");
        }

        // 4. 增加商品销量和销售额 (Update Sales)
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderLambdaQueryWrapper.eq(Order::getOrderNo, orderNo);
        Order one = getOne(orderLambdaQueryWrapper);
        BigDecimal totalPrice = one.getTotalPrice();
        goodMapper.saleGood(goodId, count, totalPrice);

        // 5. 同步更新 Redis 缓存中的销量数据
        String redisKey = GOOD_TOKEN_KEY + goodId;
        ValueOperations<String, Good> valueOperations = redisTemplate.opsForValue();
        Good good = valueOperations.get(redisKey);
        if(!ObjectUtils.isEmpty(good)) {
            good.setSales(good.getSales() + count);
            valueOperations.set(redisKey, good);
        }
    }

    public List<Map<String, Object>> selectByUserId(int userId) {
        return orderMapper.selectByUserId(userId);
    }

    public Map<String, Object> selectByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    public List<Map<String, Object>> export(String orderNo, String state) {
        return orderMapper.selectExport(orderNo, state);
    }

    /**
     * 确认收货 (Receive Order)
     * @param orderNo 订单编号
     * @return boolean 是否成功
     */
    public boolean receiveOrder(String orderNo) {
        return orderMapper.receiveOrder(orderNo);
    }

    /**
     * 订单发货 (Delivery)
     * @param orderNo 订单编号
     */
    public void delivery(String orderNo) {
        LambdaUpdateWrapper<Order> orderLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        orderLambdaUpdateWrapper.eq(Order::getOrderNo, orderNo)
                .set(Order::getState, "已发货");
        update(orderLambdaUpdateWrapper);
    }

}
