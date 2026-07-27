package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统权限实体
 * 对应数据库表sys_permission，存储权限项信息，支持树形结构
 * 权限类型包括菜单(MENU)、按钮(BUTTON)、接口(API)等
 */
@Data
@TableName("sys_permission")
public class SysPermission {

    /** 权限ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限编码，唯一标识，如user:create、user:delete */
    private String permissionCode;

    /** 权限名称，如"创建用户"、"删除用户" */
    private String permissionName;

    /** 权限类型：MENU-菜单，BUTTON-按钮，API-接口 */
    private String permissionType;

    /** 父权限ID，null表示顶级权限 */
    private Long parentId;

    /** 前端路由路径 */
    private String path;

    /** 图标名称 */
    private String icon;

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
