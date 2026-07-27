package com.admin.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class BusinessDataCreateRequest {

    @NotNull(message = "模型ID不能为空")
    private Long modelId;

    private Map<String, Object> data;
}
