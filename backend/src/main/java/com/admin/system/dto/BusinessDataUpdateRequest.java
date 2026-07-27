package com.admin.system.dto;

import lombok.Data;

import java.util.Map;

@Data
public class BusinessDataUpdateRequest {

    private Map<String, Object> data;
}
