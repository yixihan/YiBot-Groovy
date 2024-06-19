package com.yixihan.yibot.db.service.impl

import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.StrUtil
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.yixihan.yibot.db.pojo.ExcludeGroup
import com.yixihan.yibot.db.pojo.RepeaterRandom
import com.yixihan.yibot.db.service.ExcludeGroupService
import com.yixihan.yibot.db.service.RepeaterRandomService
import com.yixihan.yibot.db.service.RepeaterService
import jakarta.annotation.Resource
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

/**
 * 复读插件 service
 *
 * @author yixihan
 * @date 2024-05-17 10:54
 */
@Slf4j
@Service
class RepeaterServiceImpl implements RepeaterService {

    @Resource
    ExcludeGroupService excludeGroupService

    @Resource
    RepeaterRandomService randomService

    @Override
    void addExcludeGroup(ExcludeGroup group) {
        if (ObjUtil.isNull(group) || ObjUtil.isNull(group.groupId)) {
            return
        }
        Long count = excludeGroupService.count(new QueryWrapper<ExcludeGroup>()
                .eq(ExcludeGroup.GROUP_ID, group.groupId))

        if (count > 0) {
            return
        }

        excludeGroupService.save(group)
    }

    @Override
    void delExcludeGroup(Long groupId) {
        if (ObjUtil.isNull(groupId)) {
            return
        }
        List<Long> idList = excludeGroupService.list(new QueryWrapper<ExcludeGroup>()
                .eq(ObjUtil.isNotNull(groupId), ExcludeGroup.GROUP_ID, groupId))
                .collect { it.id }

        excludeGroupService.removeByIds(idList)
    }

    @Override
    List<Long> showExcludeGroup() {
        return excludeGroupService.list().collect { it.groupId }
    }

    @Override
    void addRandomText(RepeaterRandom randomText) {
        if (ObjUtil.isNull(randomText) || StrUtil.isBlank(randomText.text) || ObjUtil.isNull(randomText.weight) || ObjUtil.isNull(randomText.groupId) || randomText.weight > 1) {
            return
        }
        RepeaterRandom oldRandomText = randomService.getOne(new QueryWrapper<RepeaterRandom>()
                .eq(RepeaterRandom.TEXT, randomText.text)
                .eq(RepeaterRandom.GROUP_ID, randomText.groupId)
        )

        if (ObjUtil.isNotNull(oldRandomText)) {
            randomText.id = oldRandomText.id
        }

        randomService.saveOrUpdate(randomText)
    }

    @Override
    void delRandomText(String text, Long groupId) {
        if (StrUtil.isBlank(text) || ObjUtil.isNull(groupId)) {
            return
        }
        List<Long> idList = randomService.list(new QueryWrapper<RepeaterRandom>()
                .eq(RepeaterRandom.TEXT, text)
                .eq(RepeaterRandom.GROUP_ID, groupId))
                .collect { it.id }

        randomService.removeByIds(idList)
    }

    @Override
    List<RepeaterRandom> showRandomText(Long groupId) {
        if (ObjUtil.isNull(groupId)) {
            return []
        }
        return randomService.list(new QueryWrapper<RepeaterRandom>()
                .eq(RepeaterRandom.GROUP_ID, groupId))
    }
}
