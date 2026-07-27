package com.admin.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserUpdateRequest {

    private String realName;

    private String phone;

    private String email;

    private List<Long> roleIds;

    private Integer status;

    private LocalDateTime expireTime;
}
