package com.yixihan.yibot.permission

import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.ObjectUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.MessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.yixihan.yibot.config.BotConfig
import com.yixihan.yibot.utils.Bean
import com.yixihan.yibot.utils.BotUtils
import lombok.extern.slf4j.Slf4j
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
            // 2. 允许匿名用户访问, 直接放行
            if (permission.allowAnonymous()) {
                return joinPoint.proceed()
            }

            // 3. 获取方法形参信息
            Bot bot = BotUtils.getBot()
            Object arg = joinPoint.getArgs()[1]

            // 3.1 方法不是消息监控则直接放行
            if (arg instanceof MessageEvent) {
                MessageEvent event = (MessageEvent) arg
                if (permission.allowMaster() && event.userId == getMasterId()) {
                    // 3.2 调用者为 master, 直接放行
                    return joinPoint.proceed()
                }

                if (event instanceof GroupMessageEvent) {
                    // 3.3 群聊消息且允许调用, 放行
                    if (permission.allowGroup()) {
                        return joinPoint.proceed()
                    } else {
                        bot.sendGroupMsg(event.groupId, "权限不足,请联系管理员 ${getMasterId()}", false)
                        throw ExceptionUtil.wrapRuntime("Permission Validate Failed")
                    }
                }
                if (event instanceof PrivateMessageEvent) {
                    // 3.4 私聊消息且允许调用, 放行
                    if (permission.allowPrivate()) {
                        return joinPoint.proceed()
                    } else {
                        bot.sendPrivateMsg(event.userId, "权限不足,请联系管理员 ${getMasterId()}", false)
                        throw ExceptionUtil.wrapRuntime("Permission Validate Failed")
                    }
                }
            } else {
                return joinPoint.proceed()
            }

            throw ExceptionUtil.wrapRuntime("Permission Validate Failed")
        } catch (Throwable e) {
            throw ExceptionUtil.wrapRuntime(e)
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