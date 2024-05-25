package com.yixihan.yibot.config

import groovy.util.logging.Slf4j
import lombok.Data
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * description
 *
 * @author yixihan
 * @date 2024-05-25 08:10
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "yibot.db")
class DbConfig implements InitializingBean {

    String host
    String port
    String db
    String username
    String password

    @Override
    void afterPropertiesSet() throws Exception {
        log.info("host: ${host}")
        log.info("port: ${port}")
        log.info("db: ${db}")
        log.info("username: ${username}")
    }
}
