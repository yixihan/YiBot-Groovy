package com.yixihan.yibot.bean.repeater

import cn.hutool.core.annotation.Alias
import groovy.util.logging.Slf4j
import lombok.Data

/**
 * 随机复读 bean
 *
 * @author yixihan
 * @date 2024-05-16 09:43
 */
@Slf4j
@Data
class RandomRepeaterBean {

    /**
     * 触发词
     */
    @Alias(value = "text")
    String text

    /**
     * 权重
     */
    @Alias(value = "weight")
    Double weight

    RandomRepeaterBean(String text, Double weight) {
        this.text = text
        this.weight = weight
    }

    static RandomRepeaterBean add(String text, String weightStr) {
        try {
            Double weight = Double.parseDouble(weightStr)
            if (weight > 1) {
                throw new RuntimeException("权重大于1")
            }
            return new RandomRepeaterBean(text, weight)
        } catch (Exception e) {
            log.warn("随机复读词添加失败: [${e.message}]")
            return null
        }
    }

    String toCsvData() {
        return text + "," + weight
    }
}
