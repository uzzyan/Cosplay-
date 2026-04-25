package com.rabbiter.em.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.em.entity.Comment;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {
    @Select("select c.*,u.nickname as username,u.avatar_url as avatarUrl from comment c left join sys_user u on c.user_id = u.id where c.good_id = #{goodId} order by c.create_time desc")
    List<Comment> findByGoodId(Long goodId);
}
