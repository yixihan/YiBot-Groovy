package com.yixihan.yibot.plugins

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.io.FileUtil
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
        bot.sendGroupMsg(event.getGroupId(), buildeBilibiliMsg(jsonData), false)
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

    static String buildeBilibiliMsg(JSONObject jsonData) {
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


    static void main(String[] args) {
        String msg
//        msg = "【高温干旱的河北农田咋样了？【主播说三农】】 https://www.bilibili.com/video/BV1ey411q7Ke/?share_source=copy_web&vd_source=5e38c5db882c1a505ea5a21aa5ad86f9";
//        msg = "[CQ:json,data={\"ver\":\"1.0.0.19\"&#44;\"prompt\":\"&#91;QQ小程序&#93;女孩酷爱野外露营，还专挑下雨天气，躲在豪华帐篷里面做饭睡大觉\"&#44;\"config\":{\"type\":\"normal\"&#44;\"width\":0&#44;\"height\":0&#44;\"forward\":1&#44;\"autoSize\":0&#44;\"ctime\":1718468601&#44;\"token\":\"73a3f0230eebcad179a469f68490f308\"}&#44;\"needShareCallBack\":false&#44;\"app\":\"com.tencent.miniapp_01\"&#44;\"view\":\"view_8C8E89B49BE609866298ADDFF2DBABA4\"&#44;\"meta\":{\"detail_1\":{\"appid\":\"1109937557\"&#44;\"appType\":0&#44;\"title\":\"哔哩哔哩\"&#44;\"desc\":\"女孩酷爱野外露营，还专挑下雨天气，躲在豪华帐篷里面做饭睡大觉\"&#44;\"icon\":\"http:\\/\\/miniapp.gtimg.cn\\/public\\/appicon\\/432b76be3a548fc128acaa6c1ec90131_200.jpg\"&#44;\"preview\":\"pubminishare-30161.picsz.qpic.cn\\/7624413f-3dbb-49a4-90fe-96d04ba0e270\"&#44;\"url\":\"m.q.qq.com\\/a\\/s\\/519f3e69d1019cf985304b260d4fc3eb\"&#44;\"scene\":1036&#44;\"host\":{\"uin\":1097282916&#44;\"nick\":\"雨霁云霭\"}&#44;\"shareTemplateId\":\"8C8E89B49BE609866298ADDFF2DBABA4\"&#44;\"shareTemplateData\":{}&#44;\"qqdocurl\":\"https://b23.tv/XqX0qHD?share_medium=android&amp;share_source=qq&amp;bbid=XY6655E6B57950F836C29BD47455FD9BC2F61&amp;ts=1718468599137\"&#44;\"showLittleTail\":\"\"&#44;\"gamePoints\":\"\"&#44;\"gamePointsUrl\":\"\"}}}]";
        msg = "https://github.com/spring-projects/spring-boot"
//        msg = "https://github.com/yixihan/yixihan"
        println isGithubUrl("https://github.com/yixihan/yixihan")
        println isGithubUrl("https://github.com/yixihan/yicloud")
        println isGithubUrl("https://github.com/yixihan/yicode")
        println isGithubUrl("https://github.com/spring-projects/spring-boot")
        File path = FileUtil.file("script/github.py")
        println ScriptUtils.runPythonScript(path.getPath(), getUrl(msg))

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
