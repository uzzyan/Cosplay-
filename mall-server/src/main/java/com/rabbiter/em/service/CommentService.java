package com.rabbiter.em.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.entity.Comment;
import com.rabbiter.em.mapper.CommentMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentService extends ServiceImpl<CommentMapper, Comment> {

    @Resource
    private CommentMapper commentMapper;

    public List<Comment> findByGoodId(Long goodId) {
        return commentMapper.findByGoodId(goodId);
    }
}
