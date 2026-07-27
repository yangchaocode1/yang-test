package com.admin.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfigCreateRequest {

    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100")
    private String configKey;

    @Size(max = 2000, message = "配置值长度不能超过2000")
    private String configValue;

    @NotBlank(message = "配置类型不能为空")
    @Pattern(regexp = "UI|SECURITY|NOTIFICATION", message = "配置类型必须为UI、SECURITY或NOTIFICATION")
    private String configType;

    @Size(max = 500, message = "描述长度不能超过500")
    private String description;
}
