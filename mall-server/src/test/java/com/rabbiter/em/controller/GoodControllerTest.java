package com.rabbiter.em.controller;

import com.rabbiter.em.common.Result;
import com.rabbiter.em.entity.Good;
import com.rabbiter.em.service.GoodService;
import com.rabbiter.em.service.RecommendationService;
import com.rabbiter.em.service.StandardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GoodControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GoodService goodService;

    @Mock
    private StandardService standardService;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private GoodController goodController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(goodController).build();
    }

    @Test
    void testFindAll_ReturnsFrontGoods() throws Exception {
        // Arrange
        List<com.rabbiter.em.entity.dto.GoodDTO> goods = new ArrayList<>();
        com.rabbiter.em.entity.dto.GoodDTO g1 = new com.rabbiter.em.entity.dto.GoodDTO();
        g1.setId(1L);
        g1.setName("Good 1");
        goods.add(g1);
        
        when(goodService.findFrontGoods()).thenReturn(goods);

        // Act & Assert
        mockMvc.perform(get("/api/good")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].name").value("Good 1"));
    }

    @Test
    void testRecommend_ReturnsRecommendedGoods() throws Exception {
        // Arrange
        List<Good> goods = new ArrayList<>();
        Good g1 = new Good();
        g1.setId(2L);
        g1.setName("Rec Good");
        goods.add(g1);
        
        when(recommendationService.recommend(1L)).thenReturn(goods);

        // Act & Assert
        mockMvc.perform(get("/api/good/recommend/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Rec Good"));
    }
}
