package com.yixihan.yibot.plugins

import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.yixihan.yibot.comm.TriggerWorldConst
import com.yixihan.yibot.comm.builder.Aria2Builder
import com.yixihan.yibot.permission.Permission
import com.yixihan.yibot.utils.BotUtils
import groovy.util.logging.Slf4j
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
    @Permission(onlyAllowMaster = true)
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
            } else if (BotUtils.validatePrivateMsg(event, bot, TriggerWorldConst.HEALTH_FILE_ARIA2)) {
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
        String filePath = "/bot/"

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
            JSONObject jsonData = JSONUtil.parseObj(data)
            StringBuilder sb = StrUtil.builder()
                    .append("上传任务创建 ${jsonData.getStr("status") == "ERROR" ? "失败" : "成功"}").append("\n")

            if (jsonData.getStr("status") == "ERROR") {
                sb.append("错误信息: ${jsonData.getStr("errMsg")}").append("\n")
                        .append("错误堆栈: ${jsonData.getStr("errTrace")}")
            } else {
                sb.append("jobId: ${jsonData.getStr("jobId")}").append("\n")
                        .append("gid: ${jsonData.getStr("gid")}")
            }
            bot.sendPrivateMsg(event.userId, sb.toString(), false)
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
        String[] keys = event.message.substring(TriggerWorldConst.LIST_FILE_ARIA2.length()).split(" ")

        String filePath = "/bot/"
        String fileName = ""
        Long page = 1
        Long pageSize = 10

        for (String key in keys) {
            if (StrUtil.startWith(key, "fp=")) {
                filePath = key.substring("fp=".length())
            }
            if (StrUtil.startWith(key, "fn=")) {
                fileName = key.substring("fn=".length())
            }
            if (StrUtil.startWith(key, "p=")) {
                page = Long.parseLong(key.substring("p=".length()))
            }
            if (StrUtil.startWith(key, "ps=")) {
                pageSize = Long.parseLong(key.substring("ps=".length()))
            }
        }

        String data = Aria2Builder.build()
                .list()
                .filePath(filePath)
                .fileName(fileName)
                .page(page)
                .pageSize(pageSize)
                .done()
        if (JSONUtil.isTypeJSON(data)) {
            JSONObject jsonData = JSONUtil.parseObj(data)
            StringBuilder sb = StrUtil.builder()
                    .append("路径信息： ${jsonData.getJSONObject("pathInfo").getStr("pathName")}").append("\n")
            JSONObject fileData = jsonData.getJSONObject("fileList")
            if (ObjUtil.isNotEmpty(fileData)) {
                sb.append("当前页：${fileData.getStr("currentPage")}，页大小：${fileData.getStr("pageSize")}，总计文件数量：${fileData.getStr("totalPageCount")}").append("\n")
                for (final def item in fileData.getJSONArray("records").jsonIter()) {
                    sb.append("文件名：${item.getStr("saveName")}，文件类型：${item.getStr("fileType")}，" +
                            "当前状态：${item.getStr("downloadStatus")}，文件大小：${item.getStr("uiFileSize")}，" +
                            "上传时间：${item.getStr("createTime")}，fileId：${item.getStr("refId")}").append("\n")
                }
            } else {
                sb.append("文件为空")
            }
            bot.sendPrivateMsg(event.userId, sb.toString(), false)
        } else {
            throw ExceptionUtil.wrapRuntime(data)
        }
    }

    static void status(Bot bot, PrivateMessageEvent event) {
        String[] keys = event.message.substring(TriggerWorldConst.STATUS_FILE_ARIA2.length()).split(" ")

        String jobId = ""
        String gid = ""
        for (String key in keys) {
            if (StrUtil.startWith(key, "jobId=")) {
                jobId = key.substring("jobId=".length())
            }
            if (StrUtil.startWith(key, "gid=")) {
                gid = key.substring("gid=".length())
            }
        }

        String data = Aria2Builder.build()
                .status()
                .jobId(jobId)
                .gid(gid)
                .done()
        if (JSONUtil.isTypeJSON(data)) {
            JSONObject jsonData = JSONUtil.parseObj(data)
            StringBuilder sb = StrUtil.builder()
            if (StrUtil.isAllBlank(jobId, gid)) {
                sb.append("Aria2 全局状态").append("\n")
                        .append("下载速度：${jsonData.getStr("downloadSpeed")}").append("\n")
                        .append("上传速度：${jsonData.getStr("uploadSpeed")}").append("\n")
                        .append("活动任务数量：${jsonData.getStr("activeNum")}").append("\n")
                        .append("等待任务数量：${jsonData.getStr("waitingNum")}")
            } else {
                sb.append("job[${jsonData.getStr("jobId")}]-gid[${jsonData.getStr("gid")}] 当前状态").append("\n")
                        .append("状态：${jsonData.getStr("downloadSpeed")}").append("\n")
                        .append("下载速度：${jsonData.getStr("downloadSpeed")}").append("\n")
                        .append("上传速度：${jsonData.getStr("uploadSpeed")}").append("\n")
                        .append("任务总大小：${jsonData.getStr("totalLength")}").append("\n")
                        .append("已下载大小：${jsonData.getStr("completeLength")}").append("\n")
                        .append("下载进度：${jsonData.getStr("progress")}").append("\n")
                        .append("已连接的服务器：${jsonData.getStr("connections")}").append("\n")
                        .append("错误信息：${jsonData.getStr("errMsg")}")
            }
            bot.sendPrivateMsg(event.userId, sb.toString(), false)
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
                .append("${TriggerWorldConst.LIST_FILE_ARIA2} [fp=filePath] [fn=fileName] [p=page] [s=pageSize] -- 分页搜索文件\n")
                .append("${TriggerWorldConst.STATUS_FILE_ARIA2} [jobId=\$jobId] [gid=\$gid]-- 查看 aira2 下载进度\n")
                .append("${TriggerWorldConst.HEALTH_FILE_ARIA2} -- aria2 健康检查\n")
                .append("${TriggerWorldConst.HELP_FILE_ARIA2} -- aria 2 帮助面板")


        bot.sendPrivateMsg(event.userId, sb.substring(0, sb.length() - 1), false)

    }
}
