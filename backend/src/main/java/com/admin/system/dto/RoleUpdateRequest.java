package com.admin.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleUpdateRequest {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String description;

    private Long parentId;

    private Integer sortOrder;

    private Integer status;
}
