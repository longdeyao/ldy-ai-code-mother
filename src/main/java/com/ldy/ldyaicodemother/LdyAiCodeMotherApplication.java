package com.ldy.ldyaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ldy.ldyaicodemother.mapper")
public class LdyAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(LdyAiCodeMotherApplication.class, args);
    }

}
