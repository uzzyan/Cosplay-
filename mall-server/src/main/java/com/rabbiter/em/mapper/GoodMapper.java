package com.rabbiter.em.mapper;

import com.rabbiter.em.entity.Good;
import com.rabbiter.em.entity.Standard;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.em.entity.dto.GoodDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GoodMapper extends BaseMapper<Good> {

    @Select("select * from good_standard where good_id = #{id}")
    List<Standard> getStandardById(int id);

    List<GoodDTO> findFrontGoods();

    @Update("update good set is_delete = 1 where id = #{id}")
    void fakeDelete(Long id);

    void insertGood(@Param("good") Good good);

    @Select("SELECT discount * MIN(price) FROM good_standard gs, good WHERE good.id = gs.good_id AND good.id = #{id} ")
    BigDecimal getMinPrice(Long id);

    @Select("SELECT IFNULL(SUM(store),0) FROM good_standard WHERE good_id = #{id}")
    Integer getTotalStore(Long id);

    boolean saleGood(@Param("id")Long goodId,@Param("count") int count,@Param("money") BigDecimal totalPrice);


    @Select("SELECT * FROM `good` WHERE is_delete = 0 AND status = 1 ORDER BY sale_money DESC LIMIT 0,#{num}")
    List<Good> getSaleRank(int num);

    List<Map<String, Object>> getMinPricesByIds(@Param("ids") List<Long> ids);

    List<Map<String, Object>> getTotalStoreByIds(@Param("ids") List<Long> ids);
}
