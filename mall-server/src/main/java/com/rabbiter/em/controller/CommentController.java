package com.rabbiter.em.controller;

import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.Comment;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.exception.ServiceException;
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

    /**
     * 删除评论。评论作者可删除自己的评论，管理员可删除任意评论。
     * @param id 评论 ID
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        // Bug4修复：删除前校验当前用户是否是评论作者或管理员
        Comment comment = commentService.getById(id);
        if (comment == null) {
            throw new ServiceException(Constants.CODE_500, "评论不存在");
        }
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException(Constants.CODE_401, "请先登录");
        }
        boolean isAuthor = comment.getUserId() != null && comment.getUserId().equals(currentUser.getId().longValue());
        boolean isAdmin = "admin".equals(currentUser.getRole());
        if (!isAuthor && !isAdmin) {
            throw new ServiceException(Constants.CODE_403, "无权删除他人评论");
        }
        commentService.removeById(id);
        return Result.success();
    }
}
