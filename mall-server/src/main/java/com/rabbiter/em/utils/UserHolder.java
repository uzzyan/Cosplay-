package com.rabbiter.em.utils;

import com.rabbiter.em.entity.User;

/**
 * 用户ThreadLocal存储工具类
 * @author uzzyan
 */

public class UserHolder {
    private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public static void saveUser(User user){
        userThreadLocal.set(user);
    }

    public static User getUser(){
        return userThreadLocal.get();
    }

    public static void removeUser(){
        userThreadLocal.remove();
    }
}
