package com.yixihan.yibot.plugins


import cn.hutool.core.util.NumberUtil
import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.mikuac.shiro.model.ArrayMsg
import com.yixihan.yibot.permission.Permission
import com.yixihan.yibot.utils.ScriptUtils
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Component

/**
 * url 解析插件
 *
 * @author yixihan
 * @date 2024-06-13 15:39
 */
@Slf4j
@Component
class UrlParsePlugin extends BotPlugin {

    @Override
    @Permission(onlyAllowMaster = false, excludeGroup = true)
    int onGroupMessage(Bot bot, GroupMessageEvent event) {
        String url = getUrl(event.getMessage())
        if (StrUtil.isNotBlank(url)) {
            parseUrl(url, bot, event)
        }
        return super.onGroupMessage(bot, event)
    }


    void parseUrl(String url, Bot bot, GroupMessageEvent event) {
        if (isBilibiliUrl(url)) {
            parseBilibiliUrl(url, bot, event)
        } else if (isZhihuUrl(url)) {
            parseZhihuUrl(url, bot, event)
        } else if (isGithubUrl(url)) {
            parseGithubUrl(url, bot, event)
        } else {
            // do not thing
        }
    }

    static String getUrl(String message) {
        List<ArrayMsg> arrayMsg = ShiroUtils.rawToArrayMsg(message)
        ArrayMsg jsonMsg = arrayMsg.find { it.type == MsgTypeEnum.json }
        if (ObjUtil.isNotEmpty(jsonMsg)) {
            JSONObject msgJsonData = JSONUtil.parseObj(jsonMsg?.data?.data)
            return msgJsonData.getJSONObject("meta").getJSONObject("detail_1").getStr("qqdocurl")
        }
        final String urlPattern = "^(https?://|ftp://)[\\w\\-._~:/?#\\[\\]@!\$&'()*+,;%=]+\$"

        if (StrUtil.isBlank(message)) {
            return false
        }

        List<String> allUrl = message.split(" ")?.findAll { it.matches(urlPattern) }

        return allUrl.isEmpty() || allUrl?.size() > 1 ? StrUtil.EMPTY : allUrl.first()
    }


    static Boolean isBilibiliUrl(String url) {
        return url.contains("bilibili.com") || url.contains("b23.tv")
    }

    static Boolean isGithubUrl(String url) {
        final  String githubPattern = "^(https?://)?(www\\.)?github\\.com/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+(.*|\$)"
        return url.matches(githubPattern)
    }

    static Boolean isZhihuUrl(String url) {
        return url.contains("zhihu.com")
    }

    static void parseBilibiliUrl(String url, Bot bot, GroupMessageEvent event) {
        String data = ScriptUtils.runPythonScript("script/bilibili.py", url)
        if (StrUtil.isEmpty(data)) {
            return
        }

        JSONObject jsonData = JSONUtil.parseObj(data)
        bot.sendGroupMsg(event.getGroupId(), buildBilibiliMsg(jsonData), false)
    }

    static void parseGithubUrl(String url, Bot bot, GroupMessageEvent event) {
        String data = ScriptUtils.runPythonScript("script/github.py", url)
        if (StrUtil.isEmpty(data)) {
            return
        }

        JSONObject jsonData = JSONUtil.parseObj(data)
        bot.sendGroupMsg(event.getGroupId(), buildeGithubMsg(jsonData), false)
    }

    void parseZhihuUrl(String url, Bot bot, GroupMessageEvent event) {

    }

    static String buildBilibiliMsg(JSONObject jsonData) {
        String text = StrUtil.builder()
                .append("标题：").append(jsonData.getStr("title")).append("\n")
                .append("简介：").append(jsonData.getStr("desc").length() > 100 ? jsonData.getStr("desc").substring(0, 100) + "..." : jsonData.getStr("desc")).append("\n")
                .append("UP：").append(jsonData.getStr("author")).append("\n")
                .append("播放：").append(formatNumber(jsonData.getBigDecimal("watch"))).append("\t")
                .append("弹幕：").append(formatNumber(jsonData.getBigDecimal("barrage"))).append("\n")
                .append("点赞：").append(formatNumber(jsonData.getBigDecimal("like"))).append("\t")
                .append("收藏：").append(formatNumber(jsonData.getBigDecimal("collection"))).append("\n")
                .append("分享：").append(formatNumber(jsonData.getBigDecimal("share"))).append("\t")
                .append("投币：").append(formatNumber(jsonData.getBigDecimal("coin"))).append("\n")
                .append(jsonData.getStr("avUrl")).append("\n")
                .append(jsonData.getStr("bvUrl")).toString()

        return MsgUtils.builder()
                .img(jsonData.getStr("img"))
                .text(text)
                .build()
    }

    static String buildeGithubMsg(JSONObject jsonData) {
        String text = StrUtil.builder()
                .append("Repo Name：").append(jsonData.getStr("repoName")).append("\n")
                .append("Author：").append(jsonData.getStr("author")).append("\n")
                .append("About：").append(jsonData.getStr("about")).append("\n")
                .append("Star：").append(jsonData.getStr("star")).append("\n")
                .append("Fork：").append(jsonData.getStr("fork")).append("\n")
                .append("Issue：").append(jsonData.getStr("issue")).append("\n")
                .append("Pull Request：").append(jsonData.getStr("fork")).append("\n")
                .append("Language：").append(jsonData.getStr("language")).append("\n")
                .append(jsonData.getStr("url")).toString()

        return MsgUtils.builder()
                .text(text)
                .build()
    }

    static String formatNumber(Number number) {
        BigDecimal num = NumberUtil.toBigDecimal(number)
        // 100000000 亿
        BigDecimal billion = BigDecimal.valueOf(100000000)
        if (billion <= num) {
            return NumberUtil.div(num, billion, 2) + "亿"
        }

        // 10000 万
        BigDecimal million = BigDecimal.valueOf(10000)
        if (million <= num) {
            return NumberUtil.div(num, million, 2) + "万"
        }

        return num
    }
}
