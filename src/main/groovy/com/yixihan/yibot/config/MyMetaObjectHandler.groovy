package com.yixihan.yibot.config

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import org.apache.ibatis.reflection.MetaObject
import org.springframework.stereotype.Component

/**
 * mybatis plus auto fill
 *
 * @author yixihan
 * @date 2024-05-17 09:58
 */
@Component
class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "createDate", Date.class, new Date())

    }

    @Override
    void updateFill(MetaObject metaObject) {

    }
}