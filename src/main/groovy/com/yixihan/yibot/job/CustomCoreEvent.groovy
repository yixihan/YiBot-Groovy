package com.yixihan.yibot.job

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.CoreEvent
import com.yixihan.yibot.config.BotConfig
import com.yixihan.yibot.utils.SystemUtils
import groovy.util.logging.Slf4j
import jakarta.annotation.Resource
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

/**
 * 客户端上下线事件
 *
 * @author yixihan
 * @date 2024-05-11 15:43
 */
@Slf4j
@Primary
@Component
class CustomCoreEvent extends CoreEvent {

    @Resource
    BotConfig botConfig

    @Override
    void online(Bot bot) {
        SystemUtils.setStart()
        log.info("system initial successful")
        bot.sendPrivateMsg(botConfig.masterId, "已上线", false)
    }

    @Override
    void offline(long account) {
        log.info("诶～我又离线了")
    }
}
