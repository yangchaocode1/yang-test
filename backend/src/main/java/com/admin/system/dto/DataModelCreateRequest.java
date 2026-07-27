package com.admin.system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DataModelCreateRequest {

    @NotBlank(message = "模型编码不能为空")
    @Size(max = 50, message = "模型编码最长50个字符")
    private String modelCode;

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称最长100个字符")
    private String modelName;

    private String description;

    @NotBlank(message = "表名不能为空")
    @Size(max = 100, message = "表名最长100个字符")
    private String tableName;

    @Valid
    private List<DataFieldRequest> fields;
}
