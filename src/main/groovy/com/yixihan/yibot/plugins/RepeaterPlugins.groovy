package com.yixihan.yibot.plugins


import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.Pair
import cn.hutool.core.text.csv.CsvReadConfig
import cn.hutool.core.text.csv.CsvReader
import cn.hutool.core.text.csv.CsvWriter
import cn.hutool.core.util.RandomUtil
import cn.hutool.core.util.StrUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.yixihan.yibot.bean.repeater.RandomRepeaterBean
import com.yixihan.yibot.utils.BotUtils
import groovy.util.logging.Slf4j
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

    static final String ADD_RANDOM_REPEATER_WORLD = "随机复读 添加"

    @Override
    int onGroupMessage(Bot bot, GroupMessageEvent event) {
        repeater(bot, event)
        if (BotUtils.validateGroupMsg(event, bot, ADD_RANDOM_REPEATER_WORLD)) {
            addRandomRepeater(bot, event)
        }
        return super.onGroupMessage(bot, event)
    }

    static File filePath = FileUtil.file("repeater/randomText.csv")
    static Set<RandomRepeaterBean> randomRepeaterList = []
    static Map<Long, Pair<String, Integer>> msgMap = new ConcurrentHashMap<>()


    static {
        // 项目启动, 读取 csv 文件
        CsvReader reader = new CsvReader(filePath, CsvReadConfig.defaultConfig())

        randomRepeaterList = reader.read(new FileReader(filePath), RandomRepeaterBean.class)
    }

    static repeater(Bot bot, GroupMessageEvent event) {
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
//            log.info("group[${event.groupId}], 复读词: [${message}], 复读次数: [${msgMap.get(event.groupId).value}]")
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
     */
    static randomRepeater(Bot bot, GroupMessageEvent event) {
        String message = event.message
        if (BotUtils.validateMsgType(message, MsgTypeEnum.video)) {
            return
        }
        if (BotUtils.validateMsgType(message, MsgTypeEnum.image)) {
            if (RandomUtil.randomDouble(1) < 0.1) {
                sendMsg("随机复读", bot, event)
            }
            return
        }
        for (item in randomRepeaterList) {
            if (StrUtil.contains(message, item.text) && RandomUtil.randomDouble(1) < item.weight) {
                sendMsg("随机复读", bot, event)
                return
            }
        }
    }

    static sendMsg(String type, Bot bot, GroupMessageEvent event) {
        log.info("复读[${type}] 触发成功, 复读群: [${event.groupId}], 复读内容: [${event.message}]")
        bot.sendGroupMsg(event.groupId, event.message, false)
    }

    static void addRandomRepeater(Bot bot, GroupMessageEvent event) {
        String[] split = event.message.split(" ")
        String text = split[split.length - 2]
        String wight = split[split.length - 1]
        // todo 做到数据库里面
        randomRepeaterList.add(RandomRepeaterBean.add(text, wight))
        CsvWriter writer = new CsvWriter(filePath)
        writer.writeBeans(randomRepeaterList)
        bot.sendGroupMsg(event.groupId, "触发词: ${text}, 添加成功", false)
    }
}
