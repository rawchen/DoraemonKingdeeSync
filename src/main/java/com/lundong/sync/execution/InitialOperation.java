package com.lundong.sync.execution;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Spring Boot启动后自动执行
 *
 * @author RawChen
 * @date 2023-12-03 17:21
 */
@Slf4j
@Component
@Order(1)
public class InitialOperation implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // 初始化
    }

}
