package com.kaijie;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kaijie.mapper") // 告诉 Spring Boot 你的 Mapper/Dao 接口都在这个包下
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("====== 家庭医生在线服务系统 后端启动成功 ======");
    }
}