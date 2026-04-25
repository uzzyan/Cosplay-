package com.rabbiter.em.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;

/**
 * 字符编码配置类
 * 确保所有HTTP请求和响应都使用UTF-8编码
 * 解决MySQL数据在前端显示中文乱码的问题
 */
@Configuration
public class EncodingConfig implements WebMvcConfigurer {

    /**
     * 字符编码过滤器
     * 强制所有请求和响应使用UTF-8编码
     */
    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding(StandardCharsets.UTF_8.name());
        filter.setForceRequestEncoding(true);
        filter.setForceResponseEncoding(true);
        return filter;
    }

    /**
     * 字符串消息转换器
     * 确保字符串类型的HTTP响应使用UTF-8编码
     */
    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }
}
