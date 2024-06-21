package com.yixihan.yibot.job

import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.core.thread.ThreadUtil
import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.mikuac.shiro.common.utils.MsgUtils
import com.yixihan.yibot.model.JobParam
import groovy.util.logging.Slf4j
import jakarta.annotation.Resource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 摸鱼日历生成 job
 *
 * @author yixihan
 * @date 2024-06-19 18:02
 */
@Slf4j
@Component
class CalendarJob implements Job {

    @Resource
    JobRunner jobRunner

    Integer retryCnt = 0

    Set<Long> groupList = [
            773690848
    ]

    @Override
    String jobCode() {
        return "CALENDAR_GENERATE_JOB"
    }

    @Override
    String jobName() {
        return "Calendar Generate Job"
    }

    @Override
    String jobDescription() {
        return "Generate Calendar Every day in 8:00"
    }

    @Override
    String jobSchedule() {
        return "8:00, every day"
    }

    @Override
    @Scheduled(cron = "0 55 7 * * ?")
    void execute() {
        jobRunner.runJob(this)
    }

    @Override
    void run(JobParam param) {
        Long groupId = param.details.groupId as Long
        if (ObjUtil.isNotEmpty(groupId)) {
            groupList.add(groupId)
        }
        while (retryCnt < 3) {
            String data = getCalendar()
            if (StrUtil.isEmpty(data)) {
                // 请求为空数据, 延时一分钟后再去请求
                retryCnt++
                ThreadUtil.safeSleep(60 * 1000)
            }
            JSONObject jsonData = JSONUtil.parseObj(data)
            String message = MsgUtils.builder()
                    .img(jsonData.getStr("url"))
                    .build()
            groupList.forEach { param.bot.sendGroupMsg(it, message, false) }
            break
        }

        retryCnt = 0
    }

    // 请求接口, 出错则返回空数据
    static String getCalendar() {
        final String calendarUrl = "https://api.vvhan.com/api/moyu?type=json"

        try {
            HttpResponse response = HttpRequest.get(calendarUrl)
                    .setConnectionTimeout(30 * 1000)
                    .execute()

            if (!response.ok) {
                ExceptionUtil.wrapRuntimeAndThrow(response.body())
            }
            return response.body()
        } catch (Exception e) {
            log.warn("calendar request err: ${e.message}")
            return StrUtil.EMPTY
        }
    }
}
