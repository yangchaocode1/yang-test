package com.admin.system.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PermissionVO {

    private Long id;

    private String permissionCode;

    private String permissionName;

    private String permissionType;

    private Long parentId;

    private String path;

    private String icon;

    private Integer sortOrder;

    private Integer status;

    private List<PermissionVO> children;
}
