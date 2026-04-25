package com.rabbiter.em.config;

import com.rabbiter.em.common.Result;
import com.rabbiter.em.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result handle(ServiceException se){
        return Result.error(se.getCode(), se.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception e) {
        log.error("系统异常", e);
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("(using password: YES)")) {
                return Result.error("500", "数据库连接认证失败，请检查配置");
            } else if (message.contains("Table") && message.contains("doesn't exist")) {
                return Result.error("500", "数据库表不存在，请检查数据库初始化");
            } else if (message.contains("Unknown database")) {
                return Result.error("500", "数据库不存在，请检查数据库配置");
            } else if (message.contains("edis")) {
                return Result.error("500", "Redis连接失败，请检查Redis服务");
            } else if (message.contains("Failed to obtain JDBC Connection")) {
                return Result.error("500", "数据库连接失败，请检查数据库服务");
            } else if (message.contains("SQLSyntaxErrorException")) {
                return Result.error("500", "SQL语法错误");
            }
        }
        return Result.error("500", "系统内部错误");
    }
}
