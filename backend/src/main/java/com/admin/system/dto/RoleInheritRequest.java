package com.admin.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoleInheritRequest {

    @NotNull(message = "父角色ID列表不能为空")
    private List<Long> parentRoleIds;
}
