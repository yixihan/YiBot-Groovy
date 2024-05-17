package com.yixihan.yibot.db.service

import com.yixihan.yibot.db.pojo.OffWorkTime
import com.baomidou.mybatisplus.extension.service.IService

/**
 * <p>
 * 下班时间表 服务类
 * </p>
 *
 * @author yixihan
 * @since 2024-05-17
 */
interface OffWorkTimeService extends IService<OffWorkTime> {

    void addOffWorkTime(OffWorkTime offWorkTime)

    void delOffWorkTime(String offWorkTime)

    List<OffWorkTime> showOffWorkTime()
}
