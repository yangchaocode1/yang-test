package com.admin.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class BusinessDataVO {

    private Long id;
    private Long modelId;
    private String modelName;
    private Map<String, Object> data;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
