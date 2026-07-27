package com.admin.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfigVO {

    private Long id;
    private String configKey;
    private String configValue;
    private String configType;
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
