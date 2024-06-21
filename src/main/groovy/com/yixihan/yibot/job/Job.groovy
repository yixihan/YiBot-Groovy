package com.yixihan.yibot.job

import com.yixihan.yibot.model.JobParam

/**
 * 系统启动时执行的 job
 *
 * @author yixihan
 * @date 2024-05-11 15:05
 */
interface Job {

    String jobCode();

    String jobName();

    String jobDescription();

    String jobSchedule();

    void execute();

    void run(JobParam param);
}