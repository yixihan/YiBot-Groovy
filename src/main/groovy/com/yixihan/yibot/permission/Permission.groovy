package com.yixihan.yibot.permission

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * description
 *
 * @author yixihan
 * @date 2024-06-18 14:22
 */
@Documented
@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)
@interface Permission {

    // 仅允许 master 用户访问
    boolean onlyAllowMaster() default false

    // 排除群不允许访问
    boolean excludeGroup() default true
}