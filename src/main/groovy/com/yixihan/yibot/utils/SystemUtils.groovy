package com.yixihan.yibot.utils


import cn.hutool.core.date.DateUtil

import java.time.Duration
import java.time.LocalDateTime

/**
 * description
 *
 * @author yixihan
 * @date 2024-05-11 14:56
 */
class SystemUtils {

    static Boolean startFlag
    static Date startDate

    static Boolean isStart() {
        return startFlag
    }

    static Boolean setStart() {
        startFlag = true
        startDate = new Date()
    }

    static Date getSystemStartDate() {
        return startDate
    }

    static String getSystemRunTime() {
        LocalDateTime nowTime = DateUtil.toLocalDateTime(new Date())
        LocalDateTime startDate = DateUtil.toLocalDateTime(getSystemStartDate())

        // 计算时间差
        Duration duration = Duration.between(startDate, nowTime)

        // 格式化输出
        def days = duration.toDays()
        duration = duration.minusDays(days) // 从duration中减去整天数，以便单独计算剩余的小时、分钟、秒
        return String.format('%dd %02d:%02d:%02d', days, duration.toHours(), duration.toMinutes() % 60, duration.getSeconds() % 60)
    }
}
