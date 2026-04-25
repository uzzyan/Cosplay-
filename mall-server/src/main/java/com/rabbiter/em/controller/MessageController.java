package com.rabbiter.em.controller;

import cn.hutool.core.util.StrUtil;
import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.Message;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.service.MessageService;
import com.rabbiter.em.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Authority(AuthorityType.requireLogin)
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @PostMapping
    public Result create(@RequestBody Message message) {
        if (message == null) {
            throw new ServiceException(Constants.CODE_500, "参数错误");
        }
        if (StrUtil.isBlank(message.getTitle())) {
            throw new ServiceException("400", "标题不能为空");
        }
        if (StrUtil.isBlank(message.getContent())) {
            throw new ServiceException("400", "内容不能为空");
        }
        if (StrUtil.isBlank(message.getContact())) {
            throw new ServiceException("400", "联系方式不能为空");
        }
        User currentUser = TokenUtils.getCurrentUser();
        message.setId(null);
        message.setUserId(currentUser.getId().longValue());
        message.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (message.getCustomIntent() == null) {
            message.setCustomIntent(false);
        }
        message.setReply(null);
        message.setReplyTime(null);
        messageService.save(message);
        return Result.success();
    }

    @GetMapping("/mine/page")
    public Result pageMine(@RequestParam int pageNum, @RequestParam int pageSize) {
        User currentUser = TokenUtils.getCurrentUser();
        return Result.success(messageService.pageMine(pageNum, pageSize, currentUser.getId().longValue()));
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        Message message = messageService.getById(id);
        if (message == null) {
            return Result.error(Constants.NO_RESULT, "未找到留言");
        }
        User currentUser = TokenUtils.getCurrentUser();
        boolean isAdmin = currentUser != null && "admin".equals(currentUser.getRole());
        if (!isAdmin && (message.getUserId() == null || !message.getUserId().equals(currentUser.getId().longValue()))) {
            return Result.error(Constants.CODE_403, "无权限");
        }
        return Result.success(message);
    }

    @Authority(AuthorityType.requireAuthority)
    @GetMapping("/page")
    public Result pageAdmin(@RequestParam int pageNum,
                            @RequestParam int pageSize,
                            @RequestParam(required = false) Boolean customIntent,
                            @RequestParam(required = false, defaultValue = "") String searchText) {
        return Result.success(messageService.pageAdmin(pageNum, pageSize, customIntent, searchText));
    }

    @Authority(AuthorityType.requireAuthority)
    @PutMapping("/reply")
    public Result reply(@RequestBody Message message) {
        if (message == null || message.getId() == null) {
            return Result.error("400", "参数错误");
        }
        if (StrUtil.isBlank(message.getReply())) {
            return Result.error("400", "回复内容不能为空");
        }
        Message db = messageService.getById(message.getId());
        if (db == null) {
            return Result.error(Constants.NO_RESULT, "未找到留言");
        }
        db.setReply(message.getReply());
        db.setReplyTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        messageService.updateById(db);
        return Result.success();
    }

    @Authority(AuthorityType.requireAuthority)
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        boolean ok = messageService.removeById(id);
        if (ok) {
            return Result.success();
        }
        return Result.error(Constants.CODE_500, "删除失败");
    }
}
