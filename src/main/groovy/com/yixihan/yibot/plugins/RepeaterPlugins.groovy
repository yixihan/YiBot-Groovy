package com.yixihan.yibot.plugins

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.lang.Pair
import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.RandomUtil
import cn.hutool.core.util.StrUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.yixihan.yibot.comm.TriggerWorldConst
import com.yixihan.yibot.db.pojo.RepeaterExcludeGroup
import com.yixihan.yibot.db.pojo.RepeaterRandom
import com.yixihan.yibot.db.service.RepeaterService
import com.yixihan.yibot.utils.BotUtils
import groovy.util.logging.Slf4j
import jakarta.annotation.Resource
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap

/**
 * 复读机 插件
 *
 * @author yixihan
 * @date 2024-05-16 09:34
 */
@Slf4j
@Component
class RepeaterPlugins extends BotPlugin {

    @Resource
    RepeaterService service

    static Map<Long, Pair<String, Integer>> msgMap = new ConcurrentHashMap<>()
    static List<Long> excludeGroupList = []
    static Map<Long, List<RepeaterRandom>> randomMap = [:]

    @Override
    int onGroupMessage(Bot bot, GroupMessageEvent event) {
        repeaterTrigger(event, bot)
        repeater(bot, event)
        return super.onGroupMessage(bot, event)
    }

    private void repeaterTrigger(GroupMessageEvent event, Bot bot) {
        if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.ADD_RANDOM_REPEATER_WORLD)) {
            addRandomRepeater(bot, event)
        } else if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.DEL_RANDOM_REPEATER_WORLD)) {
            delRandomRepeater(bot, event)
        } else if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.SHOW_RANDOM_REPEATER_WORLD)) {
            showRandomRepeater(bot, event)
        } else if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.ADD_REPEATER_EXCLUDE_GROUP_WORLD)) {
            addExcludeGroup(bot, event)
        } else if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.DEL_REPEATER_EXCLUDE_GROUP_WORLD)) {
            delExcludeGroup(bot, event)
        } else if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.SHOW_REPEATER_EXCLUDE_GROUP_WORLD)) {
            showExcludeGroup(bot, event)
        } else if (BotUtils.validateGroupMsg(event, bot, TriggerWorldConst.HELP_REPEATER_WORLD)) {
            repeaterShow(bot, event)
        }
    }


    /**
     * 复读
     * @param bot bot
     * @param event groupEvent
     */
    void repeater(Bot bot, GroupMessageEvent event) {
        if (excludeGroupList.isEmpty()) {
            excludeGroupList = service.showExcludeGroup()
        }
        if (excludeGroupList.contains(event.groupId)) {
            return
        }
        String message = event.message
        if (BotUtils.isAtSelf(message)) {
            return
        }
        if (BotUtils.validateMsgType(message, MsgTypeEnum.image)) {
            message = BotUtils.imgToBase64(message)
        }

        if (!msgMap.containsKey(event.groupId) || !StrUtil.equals(msgMap.get(event.groupId).key, message)) {
            msgMap.put(event.groupId, Pair.of(message, 1))
            randomRepeater(bot, event)
        } else if (msgMap.get(event.groupId).value < 4) {
            msgMap.put(event.groupId, Pair.of(message, msgMap.get(event.groupId).value + 1))
            randomRepeater(bot, event)
        } else {
            msgMap.remove(event.groupId)
            sendMsg("正常复读", bot, event)
        }
    }

    /**
     * 随机复读
     *
     * @param bot bot
     * @param event groupEvent
     */
    void randomRepeater(Bot bot, GroupMessageEvent event) {
        String message = event.message
        if (BotUtils.validateMsgType(message, MsgTypeEnum.video)) {
            return
        }
        if (BotUtils.validateMsgType(message, MsgTypeEnum.image)) {
            def random = RandomUtil.randomDouble(1)
            if (random < 0.03) {
                log.info("random value : [${random}]")
                sendMsg("随机复读", bot, event)
            }
            return
        }
        message = BotUtils.getTextMessage(message)
        List<RepeaterRandom> list
        if (randomMap.containsKey(event.groupId)) {
            list = randomMap.get(event.groupId)
        } else {
            list = service.showRandomText(event.groupId)
            randomMap.put(event.groupId, list)
        }

        for (item in list) {
            if (StrUtil.contains(message, item.text) && RandomUtil.randomDouble(1) < item.weight) {
                log.info("随机复读, 触发词[${item.text}]")
                sendMsg("随机复读", bot, event)
                return
            }
        }
    }


    /**
     * 添加随机复读词
     * @param bot bot
     * @param event groupEvent
     */
    void addRandomRepeater(Bot bot, GroupMessageEvent event) {
        String[] split = event.message.split(" ")
        String text = split[split.length - 2]
        String weight = split[split.length - 1]
        RepeaterRandom newRandom = parseRandomTextWight(event.groupId, text, weight)
        newRandom.createBy = event.userId
        if (ObjUtil.isNotNull(newRandom)) {
            service.addRandomText(newRandom)
            bot.sendGroupMsg(event.groupId, "触发词: ${text}, 添加成功", false)
            randomMap.remove(event.groupId)
        }
    }

    /**
     * 删除随机复读词
     * @param bot bot
     * @param event groupEvent
     */
    void delRandomRepeater(Bot bot, GroupMessageEvent event) {
        String text = event.message.split(" ").last()
        service.delRandomText(text, event.groupId)
        bot.sendGroupMsg(event.groupId, "触发词: ${text}, 删除成功", false)
        randomMap.remove(event.groupId)
    }

    /**
     * 展示随机复读词
     * @param bot bot
     * @param event groupEvent
     */
    void showRandomRepeater(Bot bot, GroupMessageEvent event) {
        StringBuilder sb = new StringBuilder()
        List<RepeaterRandom> list = service.showRandomText(event.groupId)
        if (CollUtil.isEmpty(list)) {
            bot.sendGroupMsg(event.groupId, "本群暂无随机复读词", false)
            return
        }
        sb.append("群号[${event.groupId}]随机复读词如下\n")
                .append("复读词\t权重\n")

        list.each {
            sb.append("${it.text}\t${it.weight}\n")
        }
        bot.sendGroupMsg(event.groupId, sb.substring(0, sb.length() - 1), false)
        if (!randomMap.containsKey(event.groupId)) {
            randomMap.put(event.groupId, list)
        }
    }

    /**
     * 添加复读排除群
     * @param bot bot
     * @param event groupEvent
     */
    void addExcludeGroup(Bot bot, GroupMessageEvent event) {
        String text = event.message.split(" ").last()
        try {
            RepeaterExcludeGroup group = new RepeaterExcludeGroup(
                    groupId: Long.parseLong(text),
                    createBy: event.userId
            )
            service.addExcludeGroup(group)
            bot.sendGroupMsg(event.groupId, "复读排除群: ${text}, 添加成功", false)
            excludeGroupList.add(event.groupId)
        } catch (Exception e) {
            log.warn("复读插件, 添加排除群失败: [${e.message}]")
            bot.sendGroupMsg(event.groupId, "复读排除群: ${text}, 添加失败: [${e.message}]", false)
        }
    }

    /**
     * 删除复读排除群
     * @param bot bot
     * @param event groupEvent
     */
    void delExcludeGroup(Bot bot, GroupMessageEvent event) {
        String text = event.message.split(" ").last()
        try {
            Long groupId = Long.parseLong(text)
            service.delExcludeGroup(groupId)
            bot.sendGroupMsg(event.groupId, "复读排除群: ${groupId}, 删除成功", false)
            excludeGroupList.remove(event.groupId)
        } catch (Exception e) {
            log.warn("复读插件, 添加排除群失败: [${e.message}]")
            bot.sendGroupMsg(event.groupId, "复读排除群: ${text}, 删除失败: [${e.message}]", false)
        }
    }

    /**
     * 展示复读排除群
     * @param bot bot
     * @param event groupEvent
     */
    void showExcludeGroup(Bot bot, GroupMessageEvent event) {
        StringBuilder sb = new StringBuilder()
        List<Long> list = service.showExcludeGroup()
        if (CollUtil.isEmpty(list)) {
            bot.sendGroupMsg(event.groupId, "暂无复读排除群", false)
            return
        }
        sb.append("复读排除群如下\n")

        list.each {
            sb.append("${it}\n")
        }

        bot.sendGroupMsg(event.groupId, sb.substring(0, sb.length() - 1), false)
        excludeGroupList = list
    }

    /**
     * 展示复读插件帮助面板
     * @param bot bot
     * @param event groupEvent
     */
    static void repeaterShow(Bot bot, GroupMessageEvent event) {
        StringBuilder sb = new StringBuilder()
        sb.append("复读插件帮助面板\n")
                .append("ps: 随机复读词仅作用于当前群\n")
                .append("${TriggerWorldConst.ADD_RANDOM_REPEATER_WORLD} -- 添加随机复读词\n")
                .append("${TriggerWorldConst.DEL_RANDOM_REPEATER_WORLD} -- 删除随机复读词\n")
                .append("${TriggerWorldConst.SHOW_RANDOM_REPEATER_WORLD} -- 展示随机复读词\n")
                .append("${TriggerWorldConst.ADD_REPEATER_EXCLUDE_GROUP_WORLD} -- 添加复读排除群\n")
                .append("${TriggerWorldConst.DEL_REPEATER_EXCLUDE_GROUP_WORLD} -- 删除复读排除群\n")
                .append("${TriggerWorldConst.SHOW_REPEATER_EXCLUDE_GROUP_WORLD} -- 展示复读排除群\n")
                .append("${TriggerWorldConst.HELP_REPEATER_WORLD} -- 复读插件帮助面板")


        bot.sendGroupMsg(event.groupId, sb.substring(0, sb.length() - 1), false)
    }

    /**
     * 发送消息
     * @param type 复读类型
     * @param bot bot
     * @param event groupEvent
     */
    static void sendMsg(String type, Bot bot, GroupMessageEvent event) {
        log.info("复读[${type}] 触发成功, 复读群: [${event.groupId}], 复读内容: [${event.message}]")
        bot.sendGroupMsg(event.groupId, event.message, false)
    }


    /**
     * 创建 RepeaterRandom bean
     * @param groupId 群号
     * @param text 复读词
     * @param weightStr 权重
     * @return RepeaterRandom
     */
    static RepeaterRandom parseRandomTextWight(Long groupId, String text, String weightStr) {
        try {
            RepeaterRandom newRandom = new RepeaterRandom(groupId: groupId,
                    text: text)
            Double weight = Double.parseDouble(weightStr)
            if (weight > 1) {
                throw new RuntimeException("权重大于1")
            }
            newRandom.weight = weight
            return newRandom
        } catch (Exception e) {
            log.warn("随机复读词添加失败: [${e.message}]")
            return null
        }
    }
}
