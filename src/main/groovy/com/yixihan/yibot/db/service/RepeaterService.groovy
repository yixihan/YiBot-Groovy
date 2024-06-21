package com.yixihan.yibot.db.service

import com.yixihan.yibot.db.pojo.ExcludeGroup
import com.yixihan.yibot.db.pojo.RepeaterRandom

/**
 * <p>
 * 随机复读表 服务类
 * </p>
 *
 * @author yixihan
 * @since 2024-05-17
 */
interface RepeaterService {

    void addExcludeGroup(ExcludeGroup group)

    void delExcludeGroup(Long groupId)

    List<Long> showExcludeGroup()

    void addRandomText(RepeaterRandom randomText)

    void delRandomText(String text, Long groupId)

    List<RepeaterRandom> showRandomText(Long groupId)
}
