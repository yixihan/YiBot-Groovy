package com.yixihan.yibot.job


import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.action.response.GroupInfoResp
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

/**
 * 获取群信息
 *
 * @author yixihan
 * @date 2024-05-11 14:29
 */
@Component
@Slf4j
class GroupInfoJob extends BotPlugin implements StartJob {

    @Override
    void run(Bot bot) {
        log.info("init group info")
        initGroupInfo(bot)
        log.info("init group info successful")
    }

    static void initGroupInfo(Bot bot) {
        bot.getGroupList().data.each {
            getGroupDetails(bot, it)
        }
    }

    static void getGroupDetails(Bot bot, GroupInfoResp group) {
        List<GroupMemberInfoResp> groupMemberInfo = bot.getGroupMemberList(group.groupId).data
        JSONObject json = new JSONObject()
        json.putOpt("groupInfo", group)
        json.putOpt("groupMemberInfo", groupMemberInfo)
        // todo 存进 redis
        log.info("group[${group.getGroupName()}] info init successful")
    }
}
