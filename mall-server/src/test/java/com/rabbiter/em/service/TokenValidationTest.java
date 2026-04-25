package com.rabbiter.em.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.rabbiter.em.constants.RedisConstants;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.utils.TokenUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TokenValidationTest {
    @Resource(name = "userRedisTemplate")
    RedisTemplate<String, User> redisTemplate;

    @Test
    public void testTokenAndRedisConsistency() {
        // 1. 模拟登录写入
        String token = "test-token-" + System.currentTimeMillis();
        User user = new User();
        user.setId(1);
        user.setUsername("admin");
        redisTemplate.opsForValue().set(RedisConstants.USER_TOKEN_KEY + token, user);

        // 2. 模拟拦截器读取
        User retrievedUser = redisTemplate.opsForValue().get(RedisConstants.USER_TOKEN_KEY + token);
        
        // 断言 1：Redis 存取必须一致（验证序列化无误）
        assertNotNull(retrievedUser, "Redis 读取失败，请检查序列化配置");
        assertEquals("admin", retrievedUser.getUsername());

        // 3. 模拟 JWT 验签
        String jwtToken = TokenUtils.genToken("1", "admin");
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TokenUtils.getJwtSecret())).build();
        assertDoesNotThrow(() -> jwtVerifier.verify(jwtToken), "JWT 验签失败，请检查密钥配置");
        
        System.out.println("Token Validation Test - PASSED!");
    }
}
