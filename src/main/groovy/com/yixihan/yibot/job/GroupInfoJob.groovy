package com.yixihan.yibot.job


import cn.hutool.json.JSONObject
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.action.response.GroupInfoResp
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp
import com.yixihan.yibot.model.JobParam
import groovy.util.logging.Slf4j
import jakarta.annotation.Resource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import java.util.concurrent.TimeUnit

/**
 * 获取群信息
 *
 * @author yixihan
 * @date 2024-05-11 14:29
 */
@Component
@Slf4j
class GroupInfoJob extends BotPlugin implements Job {

    @Resource
    JobRunner jobRunner

    @Override
    String jobCode() {
        return "INIT_GROUP_INFO"
    }

    @Override
    String jobName() {
        return "Init Group Info"
    }

    @Override
    String jobDescription() {
        return "Init Group Info"
    }

    @Override
    String jobSchedule() {
        return "Adhoc"
    }

    @Override
    @Scheduled(initialDelay = 10, timeUnit = TimeUnit.SECONDS, fixedDelay = Long.MAX_VALUE)
    void execute() {
        jobRunner.runJob(this)
    }

    @Override
    void run(JobParam param) {
        log.info("init group info")
        initGroupInfo(param.bot)
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
