package com.rabbiter.em.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.entity.Notice;
import com.rabbiter.em.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

@Service
public class NoticeService extends ServiceImpl<NoticeMapper, Notice> {
}
