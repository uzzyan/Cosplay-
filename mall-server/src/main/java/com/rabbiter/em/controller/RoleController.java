package com.rabbiter.em.controller;

import com.rabbiter.em.common.Result;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.utils.TokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {
    @PostMapping("/role")
    @GetMapping("/role")
    public Result getUserRole(){
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("401", "登录状态已失效，请重新登录");
        }
        return Result.success(currentUser.getRole());
    }
}
