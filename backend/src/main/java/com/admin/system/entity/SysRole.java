package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色实体
 * 对应数据库表sys_role，存储角色基本信息、排序、状态等
 */
@Data
@TableName("sys_role")
public class SysRole {

    /** 角色ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码，唯一标识，如ADMIN、USER */
    private String roleCode;

    /** 角色名称，如"管理员"、"普通用户" */
    private String roleName;

    /** 角色描述 */
    private String description;

    /** 父角色ID，用于角色层级关系 */
    private Long parentId;

    /** 排序序号，值越小越靠前 */
    private Integer sortOrder;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    /** 逻辑删除标记：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
