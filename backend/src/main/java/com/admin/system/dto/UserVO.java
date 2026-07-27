package com.admin.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private LocalDateTime expireTime;
    private Integer mustChangePassword;
    private List<RoleVO> roles;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @Data
    public static class RoleVO {
        private Long id;
        private String roleCode;
        private String roleName;
    }
}
