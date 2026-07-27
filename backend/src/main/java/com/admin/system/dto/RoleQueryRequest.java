package com.admin.system.dto;

import lombok.Data;

@Data
public class RoleQueryRequest {

    private String keyword;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
