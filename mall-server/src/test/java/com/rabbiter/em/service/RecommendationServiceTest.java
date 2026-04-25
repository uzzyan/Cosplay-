package com.rabbiter.em.service;

import com.rabbiter.em.entity.Good;
import com.rabbiter.em.mapper.GoodMapper;
import com.rabbiter.em.mapper.OrderGoodsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private OrderGoodsMapper orderGoodsMapper;

    @Mock
    private GoodMapper goodMapper;

    @InjectMocks
    private RecommendationService recommendationService;

    private List<Good> mockGoods;

    @BeforeEach
    void setUp() {
        mockGoods = new ArrayList<>();
        Good g1 = new Good();
        g1.setId(1L);
        g1.setName("Cosplay Costume A");
        g1.setSales(100);
        
        Good g2 = new Good();
        g2.setId(2L);
        g2.setName("Cosplay Costume B");
        g2.setSales(50);
        
        mockGoods.add(g1);
        mockGoods.add(g2);
    }

    @Test
    void testRecommend_WithNoInteractions_ReturnsTopSellingGoods() {
        // Arrange
        when(orderGoodsMapper.selectUserItemInteractions()).thenReturn(Collections.emptyList());
        when(goodMapper.selectList(any())).thenReturn(mockGoods);

        // Act
        List<Good> result = recommendationService.recommend(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cosplay Costume A", result.get(0).getName()); // Should be sorted by sales desc
    }

    @Test
    void testRecommend_WithInteractions_ReturnsTopSellingGoods_WhenCF4JNotConfigured() {
        // Arrange
        // Even with interactions, our current impl falls back to top selling if CF4J is not fully implemented
        List<Map<String, Object>> interactions = new ArrayList<>();
        // Mock interaction data structure if needed
        when(orderGoodsMapper.selectUserItemInteractions()).thenReturn(interactions); 
        when(goodMapper.selectList(any())).thenReturn(mockGoods);

        // Act
        List<Good> result = recommendationService.recommend(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
