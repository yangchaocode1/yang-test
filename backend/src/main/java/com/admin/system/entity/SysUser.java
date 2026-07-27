package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 * 对应数据库表sys_user，存储用户基本信息、状态、有效期等
 */
@Data
@TableName("sys_user")
public class SysUser {

    /** 用户ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，用于登录，唯一 */
    private String username;

    /** 密码，BCrypt加密存储 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 账号过期时间，过期后无法登录 */
    private LocalDateTime expireTime;

    /** 是否需要修改密码：0-否，1-是 */
    private Integer mustChangePassword;

    /** 创建人ID */
    private Long createdBy;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新人ID */
    private Long updatedBy;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    /** 逻辑删除标记：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
