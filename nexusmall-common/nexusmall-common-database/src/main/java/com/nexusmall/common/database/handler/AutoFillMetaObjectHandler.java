package com.nexusmall.common.database.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>
 * 生产级实践：
 * - 自动填充 create_time、update_time 等审计字段
 * - 减少重复代码，确保数据一致性
 * - 基于 LocalDateTime（Java 8 时间 API）
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Slf4j
@Component
@ConditionalOnClass(MetaObjectHandler.class)
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    /**
     * 创建时间字段
     */
    private static final String FIELD_CREATE_TIME = "createTime";
    
    /**
     * 更新时间字段
     */
    private static final String FIELD_UPDATE_TIME = "updateTime";

    /**
     * 插入时自动填充
     * <p>
     * 当执行 insert 操作时，自动填充 createTime 和 updateTime
     * </p>
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("【自动填充】执行插入填充");
        
        // 填充创建时间（如果实体中有该字段且值为空）
        this.strictInsertFill(metaObject, FIELD_CREATE_TIME, LocalDateTime.class, LocalDateTime.now());
        
        // 填充更新时间
        this.strictInsertFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时自动填充
     * <p>
     * 当执行 update 操作时，自动更新 updateTime
     * </p>
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("【自动填充】执行更新填充");
        
        // 只更新 updateTime，不修改 createTime
        this.strictUpdateFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
