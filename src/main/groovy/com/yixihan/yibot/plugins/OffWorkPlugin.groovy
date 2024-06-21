package com.yixihan.yibot.plugins

import cn.hutool.core.date.DateUnit
import cn.hutool.core.date.DateUtil
import cn.hutool.core.util.StrUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.yixihan.yibot.comm.TriggerWorldConst
import com.yixihan.yibot.db.pojo.OffWorkTime
import com.yixihan.yibot.db.service.OffWorkTimeService
import com.yixihan.yibot.permission.Permission
import com.yixihan.yibot.utils.BotUtils
import groovy.util.logging.Slf4j
import jakarta.annotation.Resource
import org.springframework.stereotype.Component

/**
 * 下班时间 插件
 *
 * @author yixihan
 * @date 2024-05-11 16:14
 */
@Slf4j
@Component
class OffWorkPlugin extends BotPlugin {

    @Resource
    OffWorkTimeService service

    @Override
    @Permission(onlyAllowMaster = false, excludeGroup = true)
    int onGroupMessage(Bot bot, GroupMessageEvent event) {
        offWorkTrigger(event, bot)
        return super.onGroupMessage(bot, event)
    }

    @Override
    @Permission(onlyAllowMaster = false, excludeGroup = true)
    int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        offWorkTrigger(event, bot)
        return super.onPrivateMessage(bot, event)
    }

    void offWorkTrigger(GroupMessageEvent event, Bot bot) {
        if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.ADD_OFF_WORK_WORLD)) {
            addOffWorkTime(StrUtil.splitTrim(event.message, " ").last(), event.userId)
            bot.sendGroupMsg(event.groupId, showOffWorkTime(), false)
        } else if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.DEL_OFF_WORK_WORLD)) {
            delOffWorkTime(StrUtil.splitTrim(event.message, " ").last())
            bot.sendGroupMsg(event.groupId, showOffWorkTime(), false)
        } else if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.HELP_OFF_WORK_WORLD)) {
            bot.sendGroupMsg(event.groupId, offWorkTimeHelp(), false)
        } else if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.SHOW_OFF_WORK_WORLD)) {
            bot.sendGroupMsg(event.groupId, showOffWorkTime(), false)
        } else if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.OFF_WORK_WORLD)) {
            bot.sendGroupMsg(event.groupId, getOffWorkTime(), false)
        }
    }

    void offWorkTrigger(PrivateMessageEvent event, Bot bot) {
        if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.OFF_WORK_WORLD)) {
            bot.sendPrivateMsg(event.userId, getOffWorkTime(), false)
        } else if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.DEL_OFF_WORK_WORLD)) {
            delOffWorkTime(StrUtil.splitTrim(event.message, " ").last())
            bot.sendPrivateMsg(event.userId, showOffWorkTime(), false)
        } else if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.HELP_OFF_WORK_WORLD)) {
            bot.sendPrivateMsg(event.userId, offWorkTimeHelp(), false)
        } else if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.SHOW_OFF_WORK_WORLD)) {
            bot.sendPrivateMsg(event.userId, showOffWorkTime(), false)
        } else if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.OFF_WORK_WORLD)) {
            bot.sendPrivateMsg(event.userId, getOffWorkTime(), false)
        }
    }

    /**
     * 添加新的下班时间
     *
     * @param offWorkTime 下班时间
     * @param createUserId 添加人 qq 号
     */
    void addOffWorkTime(String offWorkTime, Long createUserId) {
        OffWorkTime time = new OffWorkTime()
        time.offWorkTime = parseOffWorkTime(offWorkTime)
        time.createBy = createUserId
        service.addOffWorkTime(time)
    }

    /**
     * 删除现有的下班时间
     * @param offWorkTime 下班时间
     */
    void delOffWorkTime(String offWorkTime) {
        service.delOffWorkTime(parseOffWorkTime(offWorkTime))
    }

    /**
     * 展示所有的下班时间
     * @return 下班时间
     */
    String showOffWorkTime() {
        StringBuilder sb = new StringBuilder()

        sb.append("下班时间列表:\n")
        service.showOffWorkTime().each {
            sb.append(it.offWorkTime).append("\n")
        }

        return sb.substring(0, sb.length() - 1)
    }

    /**
     * 下班时间 - 帮助面板
     * @return 下班时间 - 帮助面板
     */
    static String offWorkTimeHelp() {
        return "下班时间 用法\n" +
                "${TriggerWorldConst.OFF_WORK_WORLD} -- 还需多久才下班\n" +
                "${TriggerWorldConst.ADD_OFF_WORK_WORLD} HH:mm -- 添加新的下班时间\n" +
                "${TriggerWorldConst.DEL_OFF_WORK_WORLD} HH:mm -- 删除存在的下班时间\n" +
                "${TriggerWorldConst.SHOW_OFF_WORK_WORLD} -- 展示所有的下班时间\n" +
                "${TriggerWorldConst.HELP_OFF_WORK_WORLD} -- 展示下班时间帮助面板"
    }

    /**
     * 获取下班时间
     * @return 下班时间
     */
    String getOffWorkTime() {
        Date now = new Date()
        StringBuilder sb = new StringBuilder()

        service.showOffWorkTime().each {
            sb.append(between(now, it.offWorkTime)).append("\n")
        }

        return sb.substring(0, sb.length() - 1)
    }

    /**
     * 计算下班时间
     * @param nowTime 现在时间
     * @param offWorkTimeStr 下班时间
     * @return 两者时差 (带分 & 秒)
     */
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

    /**
     * 格式化处理下班时间, 中文 (：) 处理为 英文 (:), 去除多余的空格
     *
     * @param offWorkTime 下班时间
     * @return 标准格式的下班时间
     */
    static String parseOffWorkTime(String offWorkTime) {
        try {
            offWorkTime = offWorkTime.trim().replace("：", ":")
            DateUtil.parseTimeToday(offWorkTime)
            return offWorkTime
        } catch (Exception e) {
            log.warn("下班时间 ==> 时间格式化失败, ${e.message}")
            return null
        }
    }

}
