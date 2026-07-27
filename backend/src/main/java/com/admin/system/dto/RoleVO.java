package com.admin.system.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoleVO {

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    private Long parentId;

    private String parentName;

    private Integer sortOrder;

    private Integer status;

    private List<Long> permissionIds;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
