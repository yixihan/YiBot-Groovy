package com.yixihan.yibot.db.service.impl

import cn.hutool.core.util.ObjUtil
import cn.hutool.core.util.StrUtil
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.yixihan.yibot.db.mapper.OffWorkTimeMapper
import com.yixihan.yibot.db.pojo.OffWorkTime
import com.yixihan.yibot.db.service.OffWorkTimeService
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

/**
 * <p>
 * 下班时间表 服务实现类
 * </p>
 *
 * @author yixihan
 * @since 2024-05-17
 */
@Slf4j
@Service
class OffWorkTimeServiceImpl extends ServiceImpl<OffWorkTimeMapper, OffWorkTime> implements OffWorkTimeService {

    @Override
    void addOffWorkTime(OffWorkTime offWorkTime) {
        if (ObjUtil.isNull(offWorkTime) || StrUtil.isBlank(offWorkTime.offWorkTime)) {
            return
        }
        Long count = count(new QueryWrapper<OffWorkTime>()
                .eq(StrUtil.isNotBlank(offWorkTime.offWorkTime), OffWorkTime.OFF_WORK_TIME, offWorkTime.offWorkTime))
        if (count > 0) {
            return
        }

        save(offWorkTime)
    }

    @Override
    void delOffWorkTime(String offWorkTime) {
        if (StrUtil.isBlank(offWorkTime)) {
            return
        }
        List<Long> idList = list(new QueryWrapper<OffWorkTime>()
                .eq(StrUtil.isNotBlank(offWorkTime), OffWorkTime.OFF_WORK_TIME, offWorkTime))
                .collect { it.id }

        removeByIds(idList)
    }

    @Override
    List<OffWorkTime> showOffWorkTime() {
        return list(new QueryWrapper<OffWorkTime>()
                .orderByAsc(OffWorkTime.OFF_WORK_TIME))
    }
}
