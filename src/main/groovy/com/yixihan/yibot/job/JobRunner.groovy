package com.yixihan.yibot.job

import cn.hutool.core.date.DateUnit
import cn.hutool.core.date.DateUtil
import cn.hutool.core.thread.ThreadUtil
import cn.hutool.core.util.ObjUtil
import com.yixihan.yibot.model.JobParam
import com.yixihan.yibot.utils.BotUtils
import com.yixihan.yibot.utils.SystemUtils
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

/**
 * job 执行器
 *
 * @author yixihan
 * @date 2024-06-17 17:16
 */
@Slf4j
@Service
class JobRunner {

    /**
     * 运行 job
     *
     * @param job job
     */
    static void runJob(Job job) {
        runJob(job, null)
    }

    /**
     * 运行 job
     *
     * @param job job
     * @param param job 运行参数
     */
    static void runJob(Job job, JobParam param) {
        try {
            while (!SystemUtils.isStart()) {
                log.warn("System is not start now ...")
                ThreadUtil.safeSleep(1 * 1000)
            }
            log.info("job[{}] start run", job.jobName())
            if (ObjUtil.isNull(param)) {
                param = new JobParam()
            }
            if (ObjUtil.isNull(param.bot)) {
                param.bot = BotUtils.getBot()
            }
            Date startDate = new Date()

            // 执行 job
            job.run(param)
            // job 执行成功
            Date finishDate = new Date()
            log.info("job[{}] run success, cost time: {} ms", job.jobName(), DateUtil.between(startDate, finishDate, DateUnit.MS))
        } catch (Throwable e) {
            log.error("job[{}] run err, msg: {}", job.jobName(), e.getMessage())
        }
    }
}
