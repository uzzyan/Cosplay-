package com.rabbiter.em.service;

import com.rabbiter.em.entity.Order;
import com.rabbiter.em.entity.Good;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.entity.OrderGoods;
import com.rabbiter.em.mapper.GoodMapper;
import com.rabbiter.em.mapper.OrderGoodsMapper;
import com.rabbiter.em.mapper.OrderMapper;
import com.rabbiter.em.mapper.StandardMapper;
import com.rabbiter.em.utils.TokenUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderGoodsMapper orderGoodsMapper;

    @Mock
    private StandardMapper standardMapper;

    @Mock
    private GoodMapper goodMapper;

    @Mock
    private CartService cartService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OrderService orderService;

    private MockedStatic<TokenUtils> tokenUtilsMock;

    @BeforeEach
    void setUp() {
        tokenUtilsMock = Mockito.mockStatic(TokenUtils.class);
        // Manually inject baseMapper for ServiceImpl methods
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
    }

    @AfterEach
    void tearDown() {
        tokenUtilsMock.close();
    }

    @Test
    void testReceiveOrder() {
        String orderNo = "ORDER123";
        orderService.receiveOrder(orderNo);
        verify(orderMapper, times(1)).receiveOrder(orderNo);
    }

    @Test
    void testSaveOrder() {
        // Arrange
        Order order = new Order();
        order.setCartId(1L);
        // JSON string for goods
        String goodsJson = "[{\"id\":1, \"standard\":\"M\", \"num\":2}]";
        order.setGoods(goodsJson);

        User user = new User();
        user.setId(100); // Fixed: int
        
        tokenUtilsMock.when(TokenUtils::getCurrentUser).thenReturn(user);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        when(orderGoodsMapper.insert(any(OrderGoods.class))).thenReturn(1);
        when(cartService.removeById(anyLong())).thenReturn(true);

        // Act
        String orderNo = orderService.saveOrder(order);

        // Assert
        assertNotNull(orderNo);
        verify(orderMapper, times(1)).insert(any(Order.class));
        verify(orderGoodsMapper, times(1)).insert(any(OrderGoods.class)); // 1 item in list
        verify(cartService, times(1)).removeById(1L);
    }

    @Test
    void testPayOrder() {
        // Arrange
        String orderNo = "ORDER123";
        
        doNothing().when(orderMapper).payOrder(orderNo);
        
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("count", 2);
        orderMap.put("goodId", 1L);
        orderMap.put("standard", "M");
        when(orderMapper.selectByOrderNo(orderNo)).thenReturn(orderMap);
        
        when(standardMapper.deductStore(1L, "M", 2)).thenReturn(1);

        Good good = new Good();
        good.setIsDelete(false);
        good.setStatus(1);
        good.setSales(0);
        when(goodMapper.selectById(1L)).thenReturn(good);
        
        // Mocking getOne call for totalPrice
        Order order = new Order();
        order.setTotalPrice(new java.math.BigDecimal("100.00"));
        when(orderMapper.selectOne(any(), anyBoolean())).thenReturn(order);
        
        when(goodMapper.saleGood(anyLong(), anyInt(), any())).thenReturn(true);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null); // Simple case

        // Act
        orderService.payOrder(orderNo);

        // Assert
        verify(orderMapper, times(1)).payOrder(orderNo);
        verify(standardMapper, times(1)).deductStore(eq(1L), eq("M"), eq(2));
        verify(goodMapper, times(1)).saleGood(eq(1L), eq(2), any());
    }
}
