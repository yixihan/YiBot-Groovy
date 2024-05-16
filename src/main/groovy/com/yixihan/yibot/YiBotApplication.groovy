package com.yixihan.yibot

import com.yixihan.yibot.utils.SystemUtils
import groovy.util.logging.Slf4j
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@Slf4j
@EnableScheduling
@EnableAsync
@SpringBootApplication
class YiBotApplication {

    static void main(String[] args) {
        SpringApplication.run(YiBotApplication, args)
    }

}
