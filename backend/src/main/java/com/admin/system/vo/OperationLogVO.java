package com.admin.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVO {

    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String method;
    private String params;
    private String result;
    private String ipAddress;
    private Long duration;
    private LocalDateTime createdTime;
}
