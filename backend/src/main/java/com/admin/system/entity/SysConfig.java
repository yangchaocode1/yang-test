package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体
 * 对应数据库表sys_config，存储系统级键值对配置
 * 配置按类型分组，如UI（界面配置）、SECURITY（安全配置）、NOTIFICATION（通知配置）
 */
@Data
@TableName("sys_config")
public class SysConfig {

    /** 配置ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键，同一类型下唯一 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置类型：UI、SECURITY、NOTIFICATION */
    private String configType;

    /** 配置描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
