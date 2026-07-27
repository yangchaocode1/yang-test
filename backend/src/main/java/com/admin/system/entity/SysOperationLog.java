package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 对应数据库表sys_operation_log，记录系统操作的详细日志
 * 包括操作方法、参数、返回值、IP地址、耗时等
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    /** 日志ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 操作描述，格式为[模块]操作名 */
    private String operation;

    /** 调用方法，格式为类名.方法名 */
    private String method;

    /** 请求参数（JSON格式） */
    private String params;

    /** 返回结果（JSON格式，截断至2000字符） */
    private String result;

    /** 操作IP地址 */
    private String ipAddress;

    /** 执行耗时（毫秒） */
    private Long duration;

    /** 创建时间 */
    private LocalDateTime createdTime;
}
