package com.rabbiter.em.controller;

import cn.hutool.core.date.DateUtil;
import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.Cart;
import com.rabbiter.em.entity.Good;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.mapper.StandardMapper;
import com.rabbiter.em.service.CartService;
import com.rabbiter.em.service.GoodService;
import com.rabbiter.em.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Authority(AuthorityType.requireLogin)
@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Resource
    private CartService cartService;

    @Resource
    private GoodService goodService;

    @Resource
    private StandardMapper standardMapper;

    private boolean isAdmin() {
        User current = TokenUtils.getCurrentUser();
        return current != null && "admin".equals(current.getRole());
    }

    private void checkCartOwnership(Long cartId) {
        if (isAdmin()) return;
        Cart cart = cartService.getById(cartId);
        if (cart == null || !cart.getUserId().equals(TokenUtils.getCurrentUser().getId().longValue())) {
            throw new ServiceException("403", "无权访问该购物车项");
        }
    }

    @GetMapping("/{id}")
    public Result selectById(@PathVariable Long id) {
        checkCartOwnership(id);
        return Result.success(cartService.getById(id));
    }

    @Authority(AuthorityType.requireAuthority)
    @GetMapping
    public Result findAll() {
        List<Cart> list = cartService.list();
        return Result.success(list);
    }

    @GetMapping("/userid/{userId}")
    public Result selectByUserId(@PathVariable Long userId) {
        User current = TokenUtils.getCurrentUser();
        if (current.getId().longValue() != userId && !isAdmin()) {
            throw new ServiceException("403", "无权访问他人购物车");
        }
        return Result.success(cartService.selectByUserId(userId));
    }

    @PostMapping
    public Result save(@RequestBody Cart cart) {
        cart.setUserId(TokenUtils.getCurrentUser().getId().longValue());
        if (cart.getGoodId() == null) {
            throw new ServiceException("400", "商品不能为空");
        }
        if (cart.getCount() == null || cart.getCount() <= 0) {
            throw new ServiceException("400", "数量不正确");
        }
        if (cart.getStandard() == null || cart.getStandard().trim().isEmpty()) {
            throw new ServiceException("400", "请选择规格");
        }
        Good good = goodService.getById(cart.getGoodId());
        if (good == null || Boolean.TRUE.equals(good.getIsDelete()) || (good.getStatus() != null && good.getStatus() == 0)) {
            throw new ServiceException("500", "商品已下架");
        }
        int store = standardMapper.getStore(cart.getGoodId(), cart.getStandard());
        if (store < cart.getCount()) {
            throw new ServiceException("500", "库存不足");
        }
        cart.setCreateTime(DateUtil.now());
        cartService.saveOrUpdate(cart);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody Cart cart) {
        if (cart.getId() != null) {
            checkCartOwnership(cart.getId());
        }
        cart.setUserId(TokenUtils.getCurrentUser().getId().longValue());
        cartService.updateById(cart);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        checkCartOwnership(id);
        cartService.removeById(id);
        return Result.success();
    }
}
