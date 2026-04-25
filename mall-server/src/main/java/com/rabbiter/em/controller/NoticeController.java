package com.rabbiter.em.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.Notice;
import com.rabbiter.em.service.NoticeService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @Authority(AuthorityType.noRequire)
    @GetMapping
    public Result findAll() {
        return Result.success(noticeService.list(new QueryWrapper<Notice>().orderByDesc("create_time")));
    }

    @Authority(AuthorityType.noRequire)
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum, @RequestParam Integer pageSize, @RequestParam(defaultValue = "") String title) {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        if (!"".equals(title)) {
            queryWrapper.like("title", title);
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(noticeService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @PostMapping
    public Result save(@RequestBody Notice notice) {
        if (notice.getId() == null) {
            notice.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        noticeService.saveOrUpdate(notice);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        noticeService.removeById(id);
        return Result.success();
    }
}
