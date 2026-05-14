package com.rabbiter.em;

import com.rabbiter.em.utils.PathUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * U次元 Cosplay商城 - 主启动类
 * @author uzzyan
 */
@MapperScan("com.rabbiter.em.mapper")
@SpringBootApplication
public class MallApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallApplication.class, args);
    }

}
