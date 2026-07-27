package com.admin.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class DataModelQueryRequest {

    private String keyword;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderBy = "created_time";
    private String orderDirection = "DESC";
}
