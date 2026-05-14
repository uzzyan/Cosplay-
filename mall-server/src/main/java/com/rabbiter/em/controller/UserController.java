package com.rabbiter.em.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.LoginForm;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.entity.dto.UserDTO;
import com.rabbiter.em.service.UserService;
import com.rabbiter.em.utils.TokenUtils;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 * @author uzzyan
 */

/*
这个注解表示该控制器下所有接口都可以通过跨域访问，注解内可以指定某一域名
也可以配置config类
 */
@CrossOrigin
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 用户登录接口 (Login Endpoint)
     * @param loginForm 登录表单
     * @return Result<UserDTO> 包含Token和用户信息的统一响应对象
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginForm loginForm) {
        UserDTO dto = userService.login(loginForm);
        return Result.success(dto);
    }

    /**
     * 用户注册接口 (Register Endpoint)
     * @param loginForm 注册表单
     * @return Result<User> 包含新用户信息的统一响应对象
     */
    @PostMapping("/register")
    public Result register(@RequestBody LoginForm loginForm) {
        User user = userService.register(loginForm);
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setRole(user.getRole());
        return Result.success(dto);
    }

    /**
     * 获取用户信息接口 (Get User Info)
     * @param username 用户名
     * @return Result<User> 用户信息
     */
    @GetMapping("/userinfo/{username}")
    public Result getUserInfoByName(@PathVariable String username) {
        User one = userService.getOne(username);
        return Result.success(one);
    }

    /**
     * 获取当前登录用户ID (Get Current User ID)
     * @return Result<Long> 用户ID
     */
    @GetMapping("/userid")
    public Result getUserId() {
        return Result.success(TokenUtils.getCurrentUser().getId());
    }

    /**
     * 获取所有用户列表 (Get All Users)
     * @return Result<List<User>> 用户列表
     */
    @Authority(AuthorityType.requireAuthority)
    @GetMapping("/user/")
    public Result findAll() {
        List<User> list = userService.list();
        return Result.success(list);
    }

    /**
     * 保存或更新用户 (Save or Update User)
     * 需要管理员权限：防止普通用户通过此接口把自己提权为 admin
     * @param user 用户对象
     * @return Result 操作结果
     */
    @Authority(AuthorityType.requireAuthority)
    @PostMapping("/user")
    public Result save(@RequestBody User user) {
        return userService.saveUpdate(user);
    }

    /**
     * 根据ID删除用户 (Delete User By ID)
     * 需要管理员权限
     * @param id 用户ID
     * @return Result 操作结果
     */
    @Authority(AuthorityType.requireAuthority)
    @DeleteMapping("/user/{id}")
    public Result deleteById(@PathVariable int id) {
        boolean isSuccessful = userService.removeById(id);
        if (isSuccessful) {
            return Result.success();
        } else {
            return Result.error(Constants.CODE_500, "删除失败");
        }
    }

    /**
     * 批量删除用户 (Batch Delete Users)
     * 需要管理员权限
     * @param ids 用户ID列表
     * @return Result 操作结果
     */
    @Authority(AuthorityType.requireAuthority)
    @PostMapping("/user/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        boolean isSuccessful = userService.removeBatchByIds(ids);
        if (isSuccessful) {
            return Result.success();
        } else {
            return Result.error(Constants.CODE_500, "删除失败");
        }
    }

    /**
     * 分页查询用户 (Page Query Users)
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param id 用户ID（模糊查询）
     * @param username 用户名（模糊查询）
     * @param nickname 昵称（模糊查询）
     * @return Result<IPage<User>> 分页结果
     */
    @Authority(AuthorityType.requireAuthority)
    @GetMapping("/user/page")
    public Result findPage(@RequestParam int pageNum,
                           @RequestParam int pageSize,
                           String id,
                           String username,
                           String nickname) {
        IPage<User> userPage = new Page<>(pageNum, pageSize);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotEmpty(id)) {
            userQueryWrapper.like("id", id);
        }
        if (StrUtil.isNotEmpty(username)) {
            userQueryWrapper.like("username", username);
        }
        if (StrUtil.isNotEmpty(nickname)) {
            userQueryWrapper.like("nickname", nickname);
        }
        userQueryWrapper.orderByDesc("id");
        return Result.success(userService.page(userPage, userQueryWrapper));
    }

    /**
     * 重置密码
     *
     * @param id          用户id
     * @param newPassword 新密码
     * @return 结果
     */
    @Authority(AuthorityType.requireAuthority)
    @PostMapping("/user/resetPassword")
    public Result resetPassword(@RequestParam String id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
}
