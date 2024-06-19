package com.yixihan.yibot.permission


import cn.hutool.core.util.ObjUtil
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.MessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.yixihan.yibot.config.BotConfig
import com.yixihan.yibot.db.pojo.ExcludeGroup
import com.yixihan.yibot.db.service.ExcludeGroupService
import com.yixihan.yibot.utils.Bean
import groovy.util.logging.Slf4j
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component

import java.lang.reflect.Method

/**
 * description
 *
 * @author yixihan
 * @date 2024-06-18 14:41
 */
@Slf4j
@Aspect
@Component
class PermissionAspect {

    @Around(value = "@annotation(com.yixihan.yibot.permission.Permission)")
    static Object checkPermission(ProceedingJoinPoint joinPoint) {
        return permissionCheck(joinPoint)
    }

    static permissionCheck(ProceedingJoinPoint joinPoint) {
        if (ObjUtil.isNull(joinPoint)) {
            return null
        }

        try {
            Permission permission = getPermission(joinPoint)

            // 1. 无该注解, 直接放行
            if (ObjUtil.isNull(permission)) {
                return joinPoint.proceed()
            }
            // 2. 允许非 master 用户 & 排除群访问 放行
            if (!permission.onlyAllowMaster() && !permission.excludeGroup()) {
                return joinPoint.proceed()
            }

            // 3. 获取方法形参信息
            Object arg = joinPoint.getArgs()[1]

            // 3.1 方法不是消息监控则直接放行
            if (!arg instanceof MessageEvent) {
                return joinPoint.proceed()
            }
            MessageEvent event = (MessageEvent) arg
            // 3.2 仅允许 master 用户且调用者为 master, 直接放行
            if (permission.onlyAllowMaster() && event.userId == getMasterId()) {
                return joinPoint.proceed()
            }

            // 3.3 群聊消息
            if (event instanceof GroupMessageEvent) {
                if (!permission.excludeGroup()) {
                    // 允许排除组访问, 放行
                    return joinPoint.proceed()
                } else if (permission.excludeGroup() && Bean.get(ExcludeGroupService).count(new QueryWrapper<ExcludeGroup>().eq(ExcludeGroup.GROUP_ID, event.groupId)) == 0){
                    // 不允许排除组但群不在排除组以内, 放行
                    return joinPoint.proceed()
                }
            }
            // 3.4 私聊消息仅允许 master 用户访问
            if (event instanceof PrivateMessageEvent && event.userId == getMasterId()) {
                return joinPoint.proceed()
            }

            log.warn("Permission Validate Failed. plugin: ${joinPoint.getSignature().getDeclaringTypeName()}, event: ${event}")
            return BotPlugin.MESSAGE_IGNORE
        } catch (Exception e) {
            log.warn("Permission Validate Failed: {}", e.message)
            return BotPlugin.MESSAGE_IGNORE
        }
    }

    static Long getMasterId() {
        return Bean.get(BotConfig).masterId
    }

    private static Permission getPermission(JoinPoint joinPoint) throws NoSuchMethodException {
        Signature signature = joinPoint.getSignature()
        Method method = ((MethodSignature) signature).getMethod()
        Class<?> targetClass = joinPoint.getTarget().getClass()
        Method targetMethod = targetClass.getDeclaredMethod(signature.getName(), method.getParameterTypes())
        return targetMethod.getAnnotation(Permission.class)
    }
}