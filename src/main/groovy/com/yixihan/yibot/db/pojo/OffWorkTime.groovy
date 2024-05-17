package com.yixihan.yibot.db.pojo


import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor
import lombok.experimental.Accessors

/**
 * 下班时间表
 *
 * @author yixihan
 * @since 2024-05-17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
class OffWorkTime extends BaseModel {

    @Serial
    private static final long serialVersionUID = 1253893951799830956L

    String offWorkTime

    public static final String ID = "id"

    public static final String OFF_WORK_TIME = "off_work_time"

    public static final String CREATE_DATE = "create_date"

    public static final String CREATE_BY = "create_by"

    public static final String DEL_FLAG = "del_flag"

}
