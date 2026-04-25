package com.rabbiter.em.controller;

import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.Address;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.service.AddressService;
import com.rabbiter.em.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Authority(AuthorityType.requireLogin)
@RestController
@RequestMapping("/api/address")
public class AddressController {
    @Resource
    private AddressService addressService;

    private boolean isAdmin() {
        User current = TokenUtils.getCurrentUser();
        return current != null && "admin".equals(current.getRole());
    }

    @GetMapping("/{userId}")
    public Result findAllById(@PathVariable Long userId) {
        User current = TokenUtils.getCurrentUser();
        if (current.getId().longValue() != userId && !isAdmin()) {
            throw new ServiceException(Constants.CODE_403, "无权访问他人地址");
        }
        return Result.success(addressService.findAllById(userId));
    }

    @Authority(AuthorityType.requireAuthority)
    @GetMapping
    public Result findAll() {
        List<Address> list = addressService.list();
        return Result.success(list);
    }

    @PostMapping
    public Result save(@RequestBody Address address) {
        if (address.getLinkPhone() == null || !address.getLinkPhone().matches("\\d{11}")) {
            return Result.error(Constants.CODE_500, "联系电话必须为11位数字");
        }
        address.setUserId(TokenUtils.getCurrentUser().getId().longValue());
        boolean b = addressService.saveOrUpdate(address);
        if(b){
            return Result.success();
        }else{
            return Result.error(Constants.CODE_500,"保存地址失败");
        }
    }

    @PutMapping
    public Result update(@RequestBody Address address) {
        if (address.getLinkPhone() != null && !address.getLinkPhone().matches("\\d{11}")) {
            return Result.error(Constants.CODE_500, "联系电话必须为11位数字");
        }
        if (address.getId() != null && !isAdmin()) {
            Address existing = addressService.getById(address.getId());
            if (existing == null || !existing.getUserId().equals(TokenUtils.getCurrentUser().getId().longValue())) {
                throw new ServiceException(Constants.CODE_403, "无权修改该地址");
            }
        }
        address.setUserId(TokenUtils.getCurrentUser().getId().longValue());
        addressService.updateById(address);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        if (!isAdmin()) {
            Address address = addressService.getById(id);
            if (address == null || !address.getUserId().equals(TokenUtils.getCurrentUser().getId().longValue())) {
                throw new ServiceException(Constants.CODE_403, "无权删除该地址");
            }
        }
        addressService.removeById(id);
        return Result.success();
    }
}
