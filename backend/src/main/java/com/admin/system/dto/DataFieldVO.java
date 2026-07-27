package com.admin.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DataFieldVO {

    private Long id;
    private Long modelId;
    private String fieldCode;
    private String fieldName;
    private String fieldType;
    private Integer required;
    private Integer uniqueFlag;
    private Long referenceModelId;
    private String referenceModelName;
    private Integer sortOrder;
    private List<String> options;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
