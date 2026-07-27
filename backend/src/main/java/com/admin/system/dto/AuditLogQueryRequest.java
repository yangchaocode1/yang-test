package com.admin.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogQueryRequest {

    private Long userId;

    private String module;

    private String operation;

    private String targetType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
