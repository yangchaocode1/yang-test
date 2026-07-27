package com.admin.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogQueryRequest {

    private Long userId;
    private String username;
    private String operation;
    private String module;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
