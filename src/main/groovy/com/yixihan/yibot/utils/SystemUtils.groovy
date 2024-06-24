package com.yixihan.yibot.utils


import cn.hutool.core.date.DateUtil
import cn.hutool.core.thread.ThreadUtil
import cn.hutool.http.HttpRequest
import groovy.util.logging.Slf4j
import org.springframework.core.env.Environment

import java.time.Duration
import java.time.LocalDateTime

/**
 * 系统 工具类
 *
 * @author yixihan
 * @date 2024-05-11 14:56
 */
@Slf4j
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

    static Boolean unStart() {
        startFlag = false
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

    static void shutdownSystem() {
        Map<String, String> header = [:]
        header.put("Content-Type", "application/vnd.spring-boot.actuator.v3+json")
        header.put("Transfer-Encoding", "chunked")
        log.info("System is Shutdown Now...")
        String shutdownUrl = "http://localhost:${Bean.get(Environment).getProperty("server.port")}/actuator/shutdown"
        HttpRequest.post(shutdownUrl)
                .addHeaders(header)
                .execute()

        // 异步暂停 5s, 等待 actuator shutdown
        ThreadUtil.execAsync {
            ThreadUtil.safeSleep(5 * 1000)
            System.exit(0)
        }
    }
}
