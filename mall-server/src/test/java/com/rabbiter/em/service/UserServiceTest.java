package com.rabbiter.em.service;

import com.rabbiter.em.entity.LoginForm;
import com.rabbiter.em.entity.User;
import com.rabbiter.em.entity.dto.UserDTO;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.mapper.UserMapper;
import com.rabbiter.em.utils.TokenUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTemplate<String, User> redisTemplate;

    @Mock
    private ValueOperations<String, User> valueOperations;

    @InjectMocks
    private UserService userService;

    private MockedStatic<TokenUtils> tokenUtilsMock;

    @BeforeEach
    void setUp() {
        tokenUtilsMock = Mockito.mockStatic(TokenUtils.class);
    }

    @AfterEach
    void tearDown() {
        tokenUtilsMock.close();
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginForm form = new LoginForm();
        form.setUsername("testuser");
        form.setPassword("password");

        User user = new User();
        user.setId(1); // Fixed: int
        user.setUsername("testuser");
        user.setPassword("password");
        
        // Mock redis only here
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock ServiceImpl.getOne behavior by mocking userMapper.selectOne
        // ServiceImpl calls baseMapper.selectOne(queryWrapper, true)
        when(userMapper.selectOne(any(QueryWrapper.class), anyBoolean())).thenReturn(user);
        
        tokenUtilsMock.when(() -> TokenUtils.genToken(anyString(), anyString())).thenReturn("mock-token");

        // Act
        UserDTO result = userService.login(form);

        // Assert
        assertNotNull(result);
        assertEquals("mock-token", result.getToken());
        assertEquals("testuser", result.getUsername());
        
        verify(valueOperations, times(1)).set(anyString(), eq(user));
        verify(redisTemplate, times(1)).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testLogin_Fail_UserNotFound() {
        // Arrange
        LoginForm form = new LoginForm();
        form.setUsername("nonexistent");
        form.setPassword("anypassword");

        when(userMapper.selectOne(any(QueryWrapper.class), anyBoolean())).thenReturn(null);

        // Act & Assert
        ServiceException ex = assertThrows(ServiceException.class, () -> userService.login(form));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void testRegister_Success() {
        // Arrange
        LoginForm form = new LoginForm();
        form.setUsername("newuser");
        form.setPassword("Abc123!@");
        form.setPhone("13800138000");

        when(userMapper.selectOne(any(QueryWrapper.class), anyBoolean())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // Act
        User result = userService.register(form);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("新用户", result.getNickname());
        assertEquals("13800138000", result.getPhone());
        verify(userMapper, times(1)).insert(any(User.class));
    }
    
    @Test
    void testRegister_Fail_UserExists() {
        // Arrange
        LoginForm form = new LoginForm();
        form.setUsername("existinguser");
        form.setPassword("Abc123!@");
        form.setPhone("13800138000");
        
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        
        when(userMapper.selectOne(any(QueryWrapper.class), anyBoolean())).thenReturn(existingUser);
        
        // Act & Assert
        assertThrows(ServiceException.class, () -> userService.register(form));
    }

    @Test
    void testRegister_Fail_WeakPassword_NoSpecialChar() {
        LoginForm form = new LoginForm();
        form.setUsername("newuser");
        form.setPassword("Abc12345");
        form.setPhone("13800138000");
        assertThrows(ServiceException.class, () -> userService.register(form));
    }

    @Test
    void testRegister_Fail_WeakPassword_NoDigit() {
        LoginForm form = new LoginForm();
        form.setUsername("newuser");
        form.setPassword("Abcdef!@");
        form.setPhone("13800138000");
        assertThrows(ServiceException.class, () -> userService.register(form));
    }

    @Test
    void testRegister_Fail_WeakPassword_NoLetter() {
        LoginForm form = new LoginForm();
        form.setUsername("newuser");
        form.setPassword("123456!@");
        form.setPhone("13800138000");
        assertThrows(ServiceException.class, () -> userService.register(form));
    }

    @Test
    void testRegister_Fail_WeakPassword_TooShort() {
        LoginForm form = new LoginForm();
        form.setUsername("newuser");
        form.setPassword("Ab1!");
        form.setPhone("13800138000");
        assertThrows(ServiceException.class, () -> userService.register(form));
    }

    @Test
    void testRegister_Fail_WeakPassword_TooLong() {
        LoginForm form = new LoginForm();
        form.setUsername("newuser");
        form.setPassword("Abcdefg12345!");
        form.setPhone("13800138000");
        assertThrows(ServiceException.class, () -> userService.register(form));
    }
}
