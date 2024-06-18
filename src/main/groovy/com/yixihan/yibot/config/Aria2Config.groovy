package com.yixihan.yibot.config

import groovy.util.logging.Slf4j
import lombok.Getter
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

/**
 * description
 *
 * @author yixihan
 * @date 2024-06-18 10:40
 */
@Slf4j
@Getter
@Component
@ConfigurationProperties(prefix = "aria2")
class Aria2Config implements InitializingBean {

    String baseUrl
    String upload
    String download
    String list
    String status
    String health

    @Override
    void afterPropertiesSet() throws Exception {
        log.info("aria2 baseUrl: ${baseUrl}")
    }
}
