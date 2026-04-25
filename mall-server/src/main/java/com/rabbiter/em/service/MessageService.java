package com.rabbiter.em.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.entity.Message;
import com.rabbiter.em.mapper.MessageMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    public IPage<Message> pageAdmin(int pageNum, int pageSize, Boolean customIntent, String searchText) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        if (customIntent != null) {
            wrapper.eq("custom_intent", customIntent);
        }
        if (StrUtil.isNotBlank(searchText)) {
            wrapper.and(w -> w.like("title", searchText).or().like("contact", searchText));
        }
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }

    public IPage<Message> pageMine(int pageNum, int pageSize, Long userId) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return this.page(page, wrapper);
    }
}
