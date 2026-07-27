package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 角色-权限关联实体
 * 对应数据库表sys_role_permission，建立角色与权限的多对多关系
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission {

    /** 关联ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 权限ID */
    private Long permissionId;
}
