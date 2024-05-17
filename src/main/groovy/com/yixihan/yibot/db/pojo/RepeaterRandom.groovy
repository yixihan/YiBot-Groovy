package com.yixihan.yibot.db.pojo


import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor
import lombok.experimental.Accessors

/**
 * 随机复读表
 *
 * @author yixihan
 * @since 2024-05-17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
class RepeaterRandom extends BaseModel {


    @Serial
    private static final long serialVersionUID = 3933275632225982658L

    String text

    Double weight

    Long groupId

    public static final String ID = "id"

    public static final String TEXT = "text"

    public static final String WIGHT = "weight"

    public static final String GROUP_ID = "group_id"

    public static final String CREATE_DATE = "create_date"

    public static final String CREATE_BY = "create_by"

    public static final String DEL_FLAG = "del_flag"

}
