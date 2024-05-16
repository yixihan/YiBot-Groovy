package com.yixihan.yibot.utils

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.thread.ThreadUtil
import cn.hutool.core.util.StrUtil
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotContainer
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.MessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.mikuac.shiro.model.ArrayMsg
import com.yixihan.yibot.config.BotConfig
import groovy.util.logging.Slf4j

/**
 * bot 工具类
 *
 * @author yixihan
 * @date 2024-05-11 15:17
 */
@Slf4j
class BotUtils {

    static Bot getBot() {
        BotContainer botContainer = Bean.get(BotContainer)
        while (CollUtil.isEmpty(botContainer.robots)) {
            ThreadUtil.safeSleep(1 * 1000L)
        }
        return botContainer.robots.get(Bean.get(BotConfig).id)
    }

    static Boolean validateMsg(MessageEvent event, Bot bot, String triggerWorld) {
        Boolean flag
        if (event instanceof GroupMessageEvent) {
            List<ArrayMsg> msg = ShiroUtils.rawToArrayMsg(event.message)
            boolean atFlag = false
            boolean offWorkFlag = false
            msg.each {
                if (it.type == MsgTypeEnum.at && (it.data.get("qq") as Long) == bot.selfId) {
                    atFlag = true
                }
                if (it.type == MsgTypeEnum.text && StrUtil.startWith(StrUtil.trim(it.data.get("text")), triggerWorld)) {
                    offWorkFlag = true
                }
            }
            flag = atFlag & offWorkFlag

        } else if (event instanceof PrivateMessageEvent) {
            flag = StrUtil.startWith(event.message, triggerWorld)
        } else {
            flag = false
        }

        if (flag) {
            log.info("触发词: [${triggerWorld}] 触发成功, 事件: [${event.class.simpleName}], 触发人: [${event.userId}]")
        }
        return flag
    }

    static Boolean validateGroupMsg(GroupMessageEvent event, Bot bot, String triggerWorld) {
        return validateMsg(event, bot, triggerWorld)
    }

    static Boolean validatePrivateMsg(PrivateMessageEvent event, Bot bot, String triggerWorld) {
        return validateMsg(event, bot, triggerWorld)
    }
}
