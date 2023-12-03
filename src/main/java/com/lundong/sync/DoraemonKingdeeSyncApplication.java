package com.lundong.sync;

import com.lundong.sync.event.CustomServletAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DoraemonKingdeeSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoraemonKingdeeSyncApplication.class, args);
    }

    // 注入扩展实例到 IOC 容器
    @Bean
    public CustomServletAdapter getServletAdapter() {
        return new CustomServletAdapter();
    }

}
