package com.admin.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DataModelVO {

    private Long id;
    private String modelCode;
    private String modelName;
    private String description;
    private String tableName;
    private Integer status;
    private List<DataFieldVO> fields;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
