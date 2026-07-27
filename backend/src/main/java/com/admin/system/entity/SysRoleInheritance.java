package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 角色继承关系实体
 * 对应数据库表sys_role_inheritance，建立角色之间的继承关系
 * 子角色自动获得父角色的所有权限
 */
@Data
@TableName("sys_role_inheritance")
public class SysRoleInheritance {

    /** 关联ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父角色ID */
    private Long parentRoleId;

    /** 子角色ID */
    private Long childRoleId;
}
