package com.yixihan.yibot.utils

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * description
 *
 * @author yixihan
 * @date 2024-05-11 15:08
 */
class ApplicationContextUtils implements ApplicationContextAware {

    static ApplicationContext applicationContext

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext
    }

    static ApplicationContext getApplicationContext() {
        return applicationContext
    }


}
