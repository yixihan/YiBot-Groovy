package com.yixihan.yibot.db.pojo

import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableLogic
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

/**
 * description
 *
 * @author yixihan
 * @date 2024-05-17 09:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class BaseModel implements Serializable {

    @Serial
    static final long serialVersionUID = 1253894395799830956L

    @TableId(value = "id", type = IdType.AUTO)
    Long id

    @TableField(fill = FieldFill.INSERT)
    Date createDate

    Long createBy

    @TableLogic
    Boolean delFlag
}
