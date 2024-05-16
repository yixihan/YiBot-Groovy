package com.yixihan.yibot.utils

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * description
 *
 * @author yixihan
 * @date 2024-05-11 15:08
 */
final class Bean {
    private Bean() {}

    final static <T> T get(Class<T> requiredType) {
        SpringContextUtils.springContext.getBean(requiredType)
    }

    final static Object get(String name) {
        SpringContextUtils.springContext.getBean(name)
    }
}

final class SpringContextUtils {
    private SpringContextUtils() {}
    public static ApplicationContext springContext
}

@Component
class SpringContextAware implements ApplicationContextAware {
    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.springContext = applicationContext
    }
}

