package com.admin.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PermissionAssignRequest {

    @NotNull(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
}
