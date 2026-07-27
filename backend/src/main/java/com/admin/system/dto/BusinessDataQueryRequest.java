package com.admin.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class BusinessDataQueryRequest {

    private Long modelId;
    private List<QueryCondition> conditions;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String orderBy = "created_time";
    private String orderDirection = "DESC";
}
