package com.admin.system.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogVO {

    private Long id;

    private Long userId;

    private String username;

    private String operation;

    private String module;

    private String targetType;

    private String targetId;

    private String oldValue;

    private String newValue;

    private String ipAddress;

    private LocalDateTime createdTime;
}
