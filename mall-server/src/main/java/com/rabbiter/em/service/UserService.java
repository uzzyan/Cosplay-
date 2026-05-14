package com.rabbiter.em.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.common.Result;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.constants.RedisConstants;
import com.rabbiter.em.entity.LoginForm;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.entity.dto.UserDTO;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.mapper.UserMapper;
import com.rabbiter.em.utils.TokenUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 用户服务类
 * @author uzzyan
 */


@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]{6,12}$"
    );

    private void validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ServiceException("400", "密码必须为6-12位，且同时包含字母、数字和特殊符号");
        }
    }
    @Resource(name = "userRedisTemplate")
    RedisTemplate<String,User> redisTemplate;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public UserDTO login(LoginForm loginForm) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginForm.getUsername());
        User user = getOne(queryWrapper);
        if (user == null) {
            throw new ServiceException(Constants.CODE_403, "用户不存在");
        }
        String rawPassword = loginForm.getPassword();
        String storedPassword = user.getPassword();
        boolean matched = false;
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
            // BCrypt 格式密码
            String md5Password = cn.hutool.crypto.digest.DigestUtil.md5Hex(rawPassword);
            matched = PASSWORD_ENCODER.matches(md5Password, storedPassword);

        } else {
            // 旧 MD5 格式密码，需要手动重置为新格式
            throw new ServiceException("403", "密码格式已升级，请联系管理员重置密码");
            // // 旧 MD5 格式密码（前端传来的就是 MD5），直接比较
            // matched = storedPassword.equals(rawPassword);
            // if (matched) {
            //     // 透明升级：将旧 MD5 密码迁移为 BCrypt
            //     user.setPassword(PASSWORD_ENCODER.encode(rawPassword));
            //     // 改为：明文 → MD5 → BCrypt
            //
            //     this.updateById(user);
            // }
        }
        if (!matched) {
            throw new ServiceException(Constants.CODE_403, "用户名或密码错误");
        }
        String token = TokenUtils.genToken(user.getId().toString(), user.getUsername());
        redisTemplate.opsForValue().set(RedisConstants.USER_TOKEN_KEY + token, user);
        redisTemplate.expire(RedisConstants.USER_TOKEN_KEY + token, RedisConstants.USER_TOKEN_TTL, TimeUnit.MINUTES);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        userDTO.setToken(token);
        return userDTO;
    }

    public User register(LoginForm loginForm) {
        String username = loginForm.getUsername();
        String password = loginForm.getPassword();
        String phone = loginForm.getPhone();

        if (username == null || username.trim().isEmpty()) {
            throw new ServiceException("400", "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ServiceException("400", "密码不能为空");
        }
        //validatePassword(password);
        // Bug6修复：恢复注册时的密码强度校验，要求密码包含字母、数字和特殊符号
        validatePassword(password);
        if (phone == null || phone.trim().isEmpty()) {
            throw new ServiceException("400", "联系方式不能为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = getOne(queryWrapper);
        if (user != null) {
            throw new ServiceException(Constants.CODE_403, "用户名已被使用");
        } else {
            user = new User();
            BeanUtils.copyProperties(loginForm, user);
            // 服务端 BCrypt 加密（前端仍传 MD5，服务端再加密）
            String md5Password = cn.hutool.crypto.digest.DigestUtil.md5Hex(password);
            user.setPassword(PASSWORD_ENCODER.encode(md5Password));
            user.setNickname("新用户");
            user.setRole("user");
            save(user);
            return user;
        }
    }

    public User getOne(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    public Result saveUpdate(User user) {
        if (user.getId() != null) {
            User old = this.baseMapper.selectById(user.getId());
            old.setNickname(ObjectUtils.isEmpty(user.getNickname()) ? old.getNickname() : user.getNickname());
            old.setAvatarUrl(ObjectUtils.isEmpty(user.getAvatarUrl()) ? old.getAvatarUrl() : user.getAvatarUrl());
            // Bug2修复：只有管理员才能修改 role，防止普通用户自我提权
            User currentUser = TokenUtils.getCurrentUser();
            if (currentUser != null && "admin".equals(currentUser.getRole())) {
                old.setRole(ObjectUtils.isEmpty(user.getRole()) ? old.getRole() : user.getRole());
            }
            old.setPhone(ObjectUtils.isEmpty(user.getPhone()) ? old.getPhone() : user.getPhone());
            old.setEmail(ObjectUtils.isEmpty(user.getEmail()) ? old.getEmail() : user.getEmail());
            old.setAddress(ObjectUtils.isEmpty(user.getAddress()) ? old.getAddress() : user.getAddress());
            super.updateById(old);
            return Result.success("修改成功");
        } else {
            if (!ObjectUtils.isEmpty(this.getOne(user.getUsername()))) {
                return Result.error("400", "用户名已存在");
            }
            if (user.getNewPassword() != null) {
                validatePassword(user.getNewPassword());
                //user.setPassword(PASSWORD_ENCODER.encode(user.getNewPassword()));
                String md5New = cn.hutool.crypto.digest.DigestUtil.md5Hex(user.getNewPassword());
                user.setPassword(PASSWORD_ENCODER.encode(md5New));
            }
            super.save(user);
            return Result.success("新增成功");
        }
    }

    @Override
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    public void resetPassword(String id, String newPassword) {
        validatePassword(newPassword);
        User user = this.getById(id);
        if (user == null) {
            return;
        }
        //user.setPassword(PASSWORD_ENCODER.encode(newPassword));
        String md5New = cn.hutool.crypto.digest.DigestUtil.md5Hex(newPassword);
        user.setPassword(PASSWORD_ENCODER.encode(md5New));
        this.updateById(user);
    }
}
