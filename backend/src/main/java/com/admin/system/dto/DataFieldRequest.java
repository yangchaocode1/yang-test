package com.admin.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DataFieldRequest {

    @NotBlank(message = "字段编码不能为空")
    @Size(max = 50, message = "字段编码最长50个字符")
    private String fieldCode;

    @NotBlank(message = "字段名称不能为空")
    @Size(max = 100, message = "字段名称最长100个字符")
    private String fieldName;

    @NotBlank(message = "字段类型不能为空")
    private String fieldType;

    private Integer required;

    private Integer uniqueFlag;

    private Long referenceModelId;

    private Integer sortOrder;

    private List<String> options;
}
