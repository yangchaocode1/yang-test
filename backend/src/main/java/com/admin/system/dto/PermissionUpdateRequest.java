package com.admin.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionUpdateRequest {

    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    private String permissionType;

    private Long parentId;

    private String path;

    private String icon;

    private Integer sortOrder;

    private Integer status;
}
