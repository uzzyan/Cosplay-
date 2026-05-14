package com.rabbiter.em.controller;

import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.AfterSale;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.service.AfterSaleService;
import com.rabbiter.em.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 售后管理控制器
 * @author uzzyan
 */

@Authority(AuthorityType.requireLogin)
@RestController
@RequestMapping("/api/afterSale")
public class AfterSaleController {

    @Resource
    private AfterSaleService afterSaleService;

    @PostMapping
    public Result create(@RequestBody AfterSale afterSale) {
        User user = TokenUtils.getCurrentUser();
        afterSaleService.create(afterSale, user.getId().longValue());
        return Result.success();
    }

    @GetMapping("/mine/page")
    public Result pageMine(@RequestParam int pageNum, @RequestParam int pageSize) {
        User user = TokenUtils.getCurrentUser();
        return Result.success(afterSaleService.pageMine(pageNum, pageSize, user.getId().longValue()));
    }

    @Authority(AuthorityType.requireAuthority)
    @GetMapping("/page")
    public Result pageAdmin(@RequestParam int pageNum,
                            @RequestParam int pageSize,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String type,
                            @RequestParam(required = false) String orderNo) {
        return Result.success(afterSaleService.pageAdmin(pageNum, pageSize, status, type, orderNo));
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        AfterSale afterSale = afterSaleService.getById(id);
        if (afterSale == null) {
            return Result.error(Constants.NO_RESULT, "未找到申请");
        }
        User user = TokenUtils.getCurrentUser();
        boolean isAdmin = user != null && "admin".equals(user.getRole());
        if (!isAdmin && (afterSale.getUserId() == null || !afterSale.getUserId().equals(user.getId().longValue()))) {
            return Result.error(Constants.CODE_403, "无权限");
        }
        return Result.success(afterSale);
    }

    @Authority(AuthorityType.requireAuthority)
    @PutMapping("/handle")
    public Result handle(@RequestBody AfterSale afterSale) {
        afterSaleService.handle(afterSale.getId(), afterSale.getStatus());
        return Result.success();
    }
}
