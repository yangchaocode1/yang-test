package com.admin.system.dto;

import lombok.Data;

@Data
public class QueryCondition {

    private String fieldCode;
    private String operator;
    private String value;
    private String valueTo;
}
