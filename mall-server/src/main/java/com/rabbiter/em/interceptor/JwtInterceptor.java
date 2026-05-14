package com.rabbiter.em.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.rabbiter.em.annotation.Authority;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.constants.RedisConstants;
import com.rabbiter.em.entity.AuthorityType;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.service.UserService;
import com.rabbiter.em.utils.TokenUtils;
import com.rabbiter.em.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token验证拦截器
 * @author uzzyan
 */

/*
第一层拦截器，验证用户token,把redis中的user存到threadlocal
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    @Resource(name = "userRedisTemplate")
    RedisTemplate<String, User> redisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        //如果不是映射到方法，直接通过
        if(!(handler instanceof HandlerMethod)){
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> clazz = handlerMethod.getBeanType();
        Authority authority = null;
        if (method.isAnnotationPresent(Authority.class)) {
            authority = method.getAnnotation(Authority.class);
        } else if (clazz.isAnnotationPresent(Authority.class)) {
            authority = clazz.getAnnotation(Authority.class);
        }

        // 判断是否需要登录
        boolean requireLogin = (authority == null) || (authority.value() != AuthorityType.noRequire);

        //验证是否有token
        if(!StringUtils.hasLength(token)){
            if (requireLogin) {
                throw new ServiceException(Constants.TOKEN_ERROR, "token失效,请重新登陆");
            } else {
                return true;
            }
        }
        //通过token，将redis中的user存到threadlocal（UserHolder）
        User user = redisTemplate.opsForValue().get(RedisConstants.USER_TOKEN_KEY + token);

        if(user == null){
            if (requireLogin) {
                throw new ServiceException(Constants.TOKEN_ERROR, "token失效,请重新登陆");
            } else {
                return true;
            }
        }
        UserHolder.saveUser(user);
        //重置过期时间
        redisTemplate.expire(RedisConstants.USER_TOKEN_KEY +token, RedisConstants.USER_TOKEN_TTL, TimeUnit.MINUTES);
        //验证token
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TokenUtils.getJwtSecret())).build();
        try {
            jwtVerifier.verify(token);
        }catch (JWTVerificationException e){
            if (requireLogin) {
                throw new ServiceException(Constants.TOKEN_ERROR, "token验证失败，请重新登陆");
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
