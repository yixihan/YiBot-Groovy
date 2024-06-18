package com.yixihan.yibot.plugins


import cn.hutool.core.util.NumberUtil
import cn.hutool.system.SystemUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.yixihan.yibot.comm.TriggerWorldConst
import com.yixihan.yibot.permission.Permission
import com.yixihan.yibot.utils.BotUtils
import com.yixihan.yibot.utils.SystemUtils
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component
import oshi.SystemInfo

import java.lang.management.ManagementFactory

/**
 * bot 机器状态查询 插件
 *
 * @author yixihan
 * @date 2024-05-17 15:54
 */
@Slf4j
@Component
class StatusPlugins extends BotPlugin {

    @Override
    @Permission(allowAnonymous = true, allowGroup = true, allowPrivate = false, allowMaster = true)
    int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (BotUtils.validateMsg(event, bot, TriggerWorldConst.STATUS_WORLD)) {
            getBotStatus(bot, event)
        }
        return super.onAnyMessage(bot, event)
    }

    static void getBotStatus(Bot bot, AnyMessageEvent event) {
        def runtimeBean = ManagementFactory.getRuntimeMXBean()
        def systemInfo = new SystemInfo()

        // cpu info
        def hardware = systemInfo.hardware
        def processor = hardware.processor
        def cpuModel = processor.getProcessorIdentifier()
        def cpuUsage = processor.getSystemCpuLoad(2000)

        // memory info
        def memory = hardware.memory
        def totalMemory = memory.getTotal()
        def availableMemory = memory.getAvailable()
        def usedMemory = totalMemory - availableMemory

        // mother info
        def computerSystem = systemInfo.hardware.getComputerSystem()

        StringBuilder sb = new StringBuilder()
        sb.append("[Base Info]\n")
                .append("Bot UpTime: ${SystemUtils.getSystemRunTime()}\n")
                .append("Groovy Version: ${GroovySystem.getVersion()}\n")
                .append("JVM Version: ${runtimeBean.vmVersion}\n")
                .append("JVM Vendor: ${runtimeBean.vmVendor}\n")
                .append("JVM name: ${runtimeBean.vmName}\n")
                .append("[Run Info]\n")
                .append("CPU Num: ${hardware.processor.logicalProcessorCount}\n")
                .append("CPU Usage: ${NumberUtil.round(cpuUsage * 100, 2)}%\n")
                .append("Total Memory: ${calMemory(totalMemory)}\n")
                .append("Usable Memory: ${calMemory(usedMemory)}\n")
                .append("Free Memory: ${calMemory(availableMemory)}\n")
                .append("[System Info]\n")
                .append("OS Name: ${SystemUtil.getOsInfo().getName()}\n")
                .append("OS Arch: ${SystemUtil.getOsInfo().getArch()}\n")
                .append("OS Version: ${SystemUtil.getOsInfo().getVersion()}\n")
                .append("[Hardware Info]\n")
                .append("Device Model: ${computerSystem.getModel()}\n")
                .append("Device Manufacturer: ${computerSystem.getManufacturer()}\n")
                .append("CPU Name: ${cpuModel.name}\n")
                .append("CPU Model: ${hardware.processor.logicalProcessorCount}\n")
                .append("MotherBoard Model: ${computerSystem.getBaseboard().getModel()}\n")
                .append("MotherBoard Version: ${computerSystem.getBaseboard().getVersion()}\n")
                .append("MotherBoard Manufacturer: ${computerSystem.getBaseboard().getManufacturer()}")

        bot.sendMsg(event, sb.substring(0, sb.length() - 1), false)
    }

    static final Long ONE_B = 1
    static final Long ONE_KB = ONE_B * 1024
    static final Long ONE_MB = ONE_KB * 1024
    static final Long ONE_GB = ONE_MB * 1024


    static String calMemory(long memory) {
        if (memory >= ONE_GB) {
            return "${NumberUtil.div(memory, ONE_GB, 2)} GB"
        } else if (memory >= ONE_MB) {
            return "${NumberUtil.div(memory, ONE_MB, 2)} MB"
        } else if (memory >= ONE_KB) {
            return "${NumberUtil.div(memory, ONE_KB, 2)} KB"
        } else {
            return "${NumberUtil.div(memory, ONE_B, 2)} B"
        }
    }
}
