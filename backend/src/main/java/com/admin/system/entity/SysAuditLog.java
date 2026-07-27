package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 * 对应数据库表sys_audit_log，记录业务数据变更的审计信息
 * 包括操作类型、操作对象、变更前后值、操作人、IP地址等
 */
@Data
@TableName("sys_audit_log")
public class SysAuditLog {

    /** 日志ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 操作类型，如CREATE、UPDATE、DELETE、ASSIGN_PERMISSIONS */
    private String operation;

    /** 操作模块，如ROLE、PERMISSION */
    private String module;

    /** 目标类型，如ROLE、PERMISSION */
    private String targetType;

    /** 目标ID */
    private String targetId;

    /** 变更前的值（JSON格式） */
    private String oldValue;

    /** 变更后的值（JSON格式） */
    private String newValue;

    /** 操作IP地址 */
    private String ipAddress;

    /** 创建时间 */
    private LocalDateTime createdTime;
}
