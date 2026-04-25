package com.rabbiter.em.controller;

import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.Comment;
import com.rabbiter.em.service.CommentService;
import com.rabbiter.em.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @Authority(AuthorityType.noRequire)
    @GetMapping("/good/{goodId}")
    public Result findByGoodId(@PathVariable Long goodId) {
        return Result.success(commentService.findByGoodId(goodId));
    }

    @PostMapping
    public Result save(@RequestBody Comment comment) {
        if (comment.getId() == null) {
            comment.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            comment.setUserId(TokenUtils.getCurrentUser().getId().longValue());
        }
        commentService.saveOrUpdate(comment);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        commentService.removeById(id);
        return Result.success();
    }
}
