package com.yixihan.yibot.config

import groovy.util.logging.Slf4j
import lombok.Getter
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * description
 *
 * @author yixihan
 * @date 2024-05-11 14:35
 */
@Slf4j
@Getter
@Component
@ConfigurationProperties(prefix = "bot")
class BotConfig implements InitializingBean {

    Long id

    Long masterId

    @Override
    void afterPropertiesSet() throws Exception {
        log.info("qq no: ${id}")
        log.info("master qq no: ${masterId}")
    }
}
