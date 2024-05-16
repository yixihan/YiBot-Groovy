package com.yixihan.yibot.job

import com.mikuac.shiro.core.Bot

/**
 * 系统启动时执行的 job
 *
 * @author yixihan
 * @date 2024-05-11 15:05
 */
interface StartJob {

    void run(Bot bot)

}