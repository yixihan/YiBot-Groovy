package com.yixihan.yibot.utils

/**
 * description
 *
 * @author yixihan
 * @date 2024-05-11 14:56
 */
class SystemUtils {

    static Boolean startFlag

    static Boolean isStart() {
        return startFlag
    }

    static Boolean setStart() {
        startFlag = true
    }
}
