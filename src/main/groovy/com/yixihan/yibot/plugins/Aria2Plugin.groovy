package com.yixihan.yibot.plugins

import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.json.JSONUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.yixihan.yibot.comm.TriggerWorldConst
import com.yixihan.yibot.comm.builder.Aria2Builder
import com.yixihan.yibot.permission.Permission
import com.yixihan.yibot.utils.BotUtils
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Component

/**
 * Aria2 插件
 *
 * @author yixihan
 * @date 2024-06-13 15:38
 */
@Slf4j
@Component
class Aria2Plugin extends BotPlugin {

    @Override
    @Permission(allowAnonymous = false, allowGroup = false, allowPrivate = true, allowMaster = true)
    int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        try {
            if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.UPLOAD_FILE_ARIA2)) {
                upload(bot, event)
            } else if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.DOWNLOAD_FILE_ARIA2)) {
                download(bot, event)
            } else if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.LIST_FILE_ARIA2)) {
                list(bot, event)
            } else if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.STATUS_FILE_ARIA2)) {
                status(bot, event)
            } else if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.HELATH_FILE_ARIA2)) {
                health(bot, event)
            } else if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.HELP_FILE_ARIA2)) {
                aria2Show(bot, event)
            }
        } catch (Exception e) {
            bot.sendPrivateMsg(event.userId, "请求失败, 错误信息如下", false)
            bot.sendPrivateMsg(event.userId, e.getMessage(), false)
        }

        return super.onPrivateMessage(bot, event)
    }

    static void upload(Bot bot, PrivateMessageEvent event) {
        String[] keys = event.message.split(" ")
        String url
        String filePath = "bot"

        if (keys.length < 3) {
            bot.sendPrivateMsg(event.userId, "参数错误", false)
            return
        }
        url = keys[2]
        if (keys.length >= 4) {
            filePath = keys[3]
        }
        String data = Aria2Builder.build()
                .upload()
                .filePath(filePath)
                .url(url)
                .done()

        if (JSONUtil.isTypeJSON(data)) {
            bot.sendPrivateMsg(event.userId, "请求成功, 返回信息如下", false)
            bot.sendPrivateMsg(event.userId, JSONUtil.parseObj(data).toStringPretty(), false)
        } else {
            throw ExceptionUtil.wrapRuntime(data)
        }
    }

    static void download(Bot bot, PrivateMessageEvent event) {
        String[] keys = event.message.split(" ")
        String fileId

        if (keys.length < 3) {
            bot.sendPrivateMsg(event.userId, "参数错误", false)
            return
        }
        fileId = keys[2]

        String data = Aria2Builder.build()
                .download()
                .fileId(fileId)
                .done()

        bot.sendPrivateMsg(event.userId, "请求成功, 下载链接: ${data}", false)
    }

    static void list(Bot bot, PrivateMessageEvent event) {
        String[] keys = event.message.split(" ")

        String filePath = "bot"
        String fileName = ""
        Long page = 0
        Long pageSize = 10

        if (keys.length >= 6) {
            pageSize = Long.parseLong(keys[5])
        } else if (keys.length >= 5) {
            page = Long.parseLong(keys[4])
        } else if (keys.length >= 4) {
            fileName = keys[3]
        } else if (keys.length >= 3) {
            filePath = keys[2]
        }

        String data = Aria2Builder.build()
                .list()
                .filePath(filePath)
                .fileName(fileName)
                .page(page)
                .pageSize(pageSize)
                .done()
        if (JSONUtil.isTypeJSON(data)) {
            bot.sendPrivateMsg(event.userId, "请求成功, 返回信息如下", false)
            bot.sendPrivateMsg(event.userId, JSONUtil.parseObj(data).toStringPretty(), false)
        } else {
            throw ExceptionUtil.wrapRuntime(data)
        }
    }

    static void status(Bot bot, PrivateMessageEvent event) {
        String[] keys = event.message.split(" ")

        String fileId = ""
        if (keys.length >= 3) {
            fileId = keys[2]
        }

        String data = Aria2Builder.build()
                .status()
                .fileId(fileId)
                .done()
        if (JSONUtil.isTypeJSON(data)) {
            bot.sendPrivateMsg(event.userId, "请求成功, 返回信息如下", false)
            bot.sendPrivateMsg(event.userId, JSONUtil.parseObj(data).toStringPretty(), false)
        } else {
            throw ExceptionUtil.wrapRuntime(data)
        }
    }

    static void health(Bot bot, PrivateMessageEvent event) {
        String data = Aria2Builder.build()
                .health()
                .done()

        if (JSONUtil.isTypeJSON(data)) {
            bot.sendPrivateMsg(event.userId, "请求成功, 返回信息如下", false)
            bot.sendPrivateMsg(event.userId, JSONUtil.parseObj(data).toStringPretty(), false)
        } else {
            throw ExceptionUtil.wrapRuntime(data)
        }
    }

    static void aria2Show(Bot bot, PrivateMessageEvent event) {
        StringBuilder sb = new StringBuilder()
        sb.append("aria 2帮助面板\n")
                .append("ps: {var} 必填项, [var] 选填项}\n")
                .append("${TriggerWorldConst.UPLOAD_FILE_ARIA2} {url} [filePath] -- 上传文件\n")
                .append("${TriggerWorldConst.DOWNLOAD_FILE_ARIA2} {fileId}-- 获取下载文件链接\n")
                .append("${TriggerWorldConst.LIST_FILE_ARIA2} [filePath] [fileName] [page] [pageSize] -- 分页搜索文件\n")
                .append("${TriggerWorldConst.STATUS_FILE_ARIA2} [fileId]-- 查看 aira2 下载进度\n")
                .append("${TriggerWorldConst.HELATH_FILE_ARIA2} -- aria2 健康检查\n")
                .append("${TriggerWorldConst.HELP_FILE_ARIA2} -- aria 2 帮助面板")


        bot.sendPrivateMsg(event.userId, sb.substring(0, sb.length() - 1), false)

    }
}
