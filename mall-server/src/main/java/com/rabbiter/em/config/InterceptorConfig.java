package com.rabbiter.em.config;

import com.rabbiter.em.interceptor.AuthorityInterceptor;
import com.rabbiter.em.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Resource
    JwtInterceptor jwtInterceptor;
    @Resource
    AuthorityInterceptor authorityInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //jwt拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login","/register","/avatar/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-resources", "/favicon.ico")
                .order(0)
        ;
        //权限校验拦截器
        registry.addInterceptor(authorityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login","/register","/avatar/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-resources", "/favicon.ico")
                .order(1)
        ;

        WebMvcConfigurer.super.addInterceptors(registry);
    }


}
