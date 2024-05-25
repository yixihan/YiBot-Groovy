package com.yixihan.yibot.config

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.thread.ThreadUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotContainer
import com.yixihan.yibot.utils.SystemUtils
import groovy.util.logging.Slf4j
import jakarta.annotation.Resource
import lombok.Getter
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

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
