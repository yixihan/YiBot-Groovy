package com.yixihan.yibot.job

import cn.hutool.core.util.ReflectUtil
import com.mikuac.shiro.core.Bot
import com.yixihan.yibot.utils.BotUtils
import com.yixihan.yibot.utils.SpringContextUtils
import com.yixihan.yibot.utils.SystemUtils
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

import java.lang.reflect.Method

/**
 * job 执行服务
 *
 * @author yixihan
 * @date 2024-05-11 15:05
 */
@Service
class JobService {

    static Boolean initRunJobFlag = false

    @Scheduled(cron = "0/5 * * * * ?")
    static void initRunJob() {
        while (!initRunJobFlag) {
            if (!SystemUtils.isStart()) {
                continue
            }
            Bot bot = BotUtils.getBot()
            Map map = SpringContextUtils.springContext.getBeansOfType(StartJob)
            Method runMethod = ReflectUtil.getMethodByName(StartJob, "run")
            map.values().each {
                ReflectUtil.invoke(it, runMethod, bot)
            }
            initRunJobFlag = true
        }
    }
}
