package com.rabbiter.em.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;

/**
 * Token工具类
 * @author uzzyan
 */

@Component
public class TokenUtils {

    @Value("${jwt.secret}")
    private String jwtSecretValue;

    private static String jwtSecret;

    @PostConstruct
    public void init() {
        jwtSecret = this.jwtSecretValue;
    }

    /** Token 默认有效期：24 小时 */
    private static final long TOKEN_EXPIRE_MS = 24 * 60 * 60 * 1000L;

    public static String genToken(String userId, String username){
        // Bug7修复：添加 JWT 过期时间（24h），防止 Token 永久有效
        String token = JWT.create()
                .withAudience(userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRE_MS))
                .sign(Algorithm.HMAC256(jwtSecret));
        return token;
    }

    public static String getJwtSecret() {
        return jwtSecret;
    }

    public static User getCurrentUser(){
        try{
            return UserHolder.getUser();
        }catch (Exception e){
            return null;
        }
    }

    public static boolean validateLogin(){
        try{
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            String token = request.getHeader("token");
            if(StringUtils.hasLength(token)){
                JWT.decode(token).getAudience();
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            throw new ServiceException(Constants.CODE_401,"登录状态失效！");
        }
    }

    public static boolean validateAuthority(){
        try{
            User user = getCurrentUser();
            if(user.getRole().equals("admin")){
                return true;
            }
        }catch (Exception e){
            return false;
        }
        throw new ServiceException(Constants.CODE_403,"无权限！");
    }

}
