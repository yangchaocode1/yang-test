package com.admin.system.dto;

import lombok.Data;

@Data
public class ConfigQueryRequest {

    private String configType;
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
