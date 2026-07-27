package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 用户-角色关联实体
 * 对应数据库表sys_user_role，建立用户与角色的多对多关系
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {

    /** 关联ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;
}
