package com.admin.system.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ReferenceDataVO {

    private Long id;
    private Long modelId;
    private String modelName;
    private String displayValue;
    private Map<String, Object> data;
}
