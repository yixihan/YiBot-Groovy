package com.yixihan.yibot.plugins

import cn.hutool.core.date.DateUnit
import cn.hutool.core.date.DateUtil
import cn.hutool.core.util.StrUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.yixihan.yibot.utils.BotUtils
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

/**
 * description
 *
 * @author yixihan
 * @date 2024-05-11 16:14
 */
@Slf4j
@Component
class OffWorkPlugin extends BotPlugin {

    static final String OFF_WORK_TRIGGER_WORLD = "下班时间"
    static final String ADD_OFF_WORK_TRIGGER_WORLD = "下班时间 添加"
    static final String DEL_OFF_WORK_TRIGGER_WORLD = "下班时间 删除"
    static final String HELP_OFF_WORK_TRIGGER_WORLD = "下班时间 帮助"
    static final String SHOW_OFF_WORK_TRIGGER_WORLD = "下班时间 列表"

    @Override
    int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (BotUtils.validateGroupMsg(event, bot, ADD_OFF_WORK_TRIGGER_WORLD)) {
            addOffWorkTime(StrUtil.splitTrim(event.message, " ").last())
            bot.sendGroupMsg(event.groupId, showOffWorkTime(), false)
        } else if (BotUtils.validateGroupMsg(event, bot, DEL_OFF_WORK_TRIGGER_WORLD)) {
            delOffWorkTime(StrUtil.splitTrim(event.message, " ").last())
            bot.sendGroupMsg(event.groupId, showOffWorkTime(), false)
        } else if (BotUtils.validateGroupMsg(event, bot, HELP_OFF_WORK_TRIGGER_WORLD)) {
            bot.sendGroupMsg(event.groupId, offWorkTimeHelp(), false)
        } else if (BotUtils.validateGroupMsg(event, bot, SHOW_OFF_WORK_TRIGGER_WORLD)) {
            bot.sendGroupMsg(event.groupId, showOffWorkTime(), false)
        } else if (BotUtils.validateGroupMsg(event, bot, OFF_WORK_TRIGGER_WORLD)) {
            bot.sendGroupMsg(event.groupId, getOffWorkTime(), false)
        }
        return super.onGroupMessage(bot, event)
    }


    @Override
    int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        if (BotUtils.validatePrivateMsg(event, bot, OFF_WORK_TRIGGER_WORLD)) {
            bot.sendPrivateMsg(event.userId, getOffWorkTime(), false)
        }
        return super.onPrivateMessage(bot, event)
    }

    static final TreeSet<String> OFF_WORK_TIME_LIST = [
            "17:00",
            "17:30",
            "18:00",
            "18:30",
            "19:00"
    ]

    static void addOffWorkTime(String offWorkTime) {
        OFF_WORK_TIME_LIST.add(offWorkTime.trim().replace("：", ":"))
    }

    static void delOffWorkTime(String offWorkTime) {
        OFF_WORK_TIME_LIST.remove(offWorkTime)
    }

    static String showOffWorkTime() {
        StringBuilder sb = new StringBuilder()

        sb.append("下班时间列表:\n")
        OFF_WORK_TIME_LIST.each {
            sb.append(it).append("\n")
        }

        return sb.substring(0, sb.length() - 1)
    }

    static String offWorkTimeHelp() {
        return "下班时间 用法 \n" +
                "下班时间 -- 还需多久才下班 \n" +
                "下班时间 添加 HH:mm -- 添加新的下班时间 \n" +
                "下班时间 删除 HH:mm -- 删除存在的下班时间 \n" +
                "下班时间 列表 -- 展示所有的下班时间 \n" +
                "下班时间 帮助 -- 展示下班时间帮助面板"
    }

    static String getOffWorkTime() {
        Date now = new Date()
        StringBuilder sb = new StringBuilder()

        OFF_WORK_TIME_LIST.each {
            sb.append(between(now, it)).append("\n")
        }

        return sb.substring(0, sb.length() - 1)
    }

    static String between(Date nowTime, String offWorkTimeStr) {
        Date offWorkTime = DateUtil.parse(DateUtil.formatDate(nowTime) + " " + offWorkTimeStr)
        if (DateUtil.compare(nowTime, offWorkTime) < 0) {
            long betweenSeconds = DateUtil.between(nowTime, offWorkTime, DateUnit.SECOND)
            long betweenMin = (long) (betweenSeconds / 60)
            betweenSeconds = betweenSeconds % 60
            return "下班时间：${DateUtil.format(offWorkTime, "HH:mm")}，还有 ${betweenMin} min ${betweenSeconds} s"
        } else {
            return "下班时间：${DateUtil.format(offWorkTime, "HH:mm")}，已下班"
        }
    }

}
