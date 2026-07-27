package com.admin.system.controller;

import com.admin.system.annotation.OperationLog;
import com.admin.system.common.result.Result;
import com.admin.system.dto.*;
import com.admin.system.service.PermissionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 * 提供权限的增删改查、权限树形结构查询等接口
 */
@Tag(name = "权限管理", description = "权限CRUD与权限树接口")
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    /** 权限服务，处理权限相关的业务逻辑 */
    private final PermissionService permissionService;

    /**
     * 获取权限树形结构
     * 返回所有启用状态的权限，按父子关系组装为树形结构
     *
     * @return 权限树形列表
     */
    @Operation(summary = "获取权限树形结构")
    @GetMapping("/tree")
    @OperationLog(operation = "查询权限树", module = "权限管理")
    public Result<List<PermissionVO>> tree() {
        return Result.success(permissionService.tree());
    }

    /**
     * 分页查询权限列表
     * 按排序和创建时间排序，返回分页权限列表
     *
     * @param pageNum  页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 分页权限列表
     */
    @Operation(summary = "分页查询权限列表")
    @GetMapping
    public Result<Page<PermissionVO>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(permissionService.pageList(pageNum, pageSize));
    }

    /**
     * 创建权限
     * 创建新的权限项，需提供权限编码、名称、类型等信息
     *
     * @param request 创建权限请求
     * @return 创建后的权限信息
     */
    @Operation(summary = "创建权限")
    @PostMapping
    @OperationLog(operation = "创建权限", module = "权限管理")
    public Result<PermissionVO> create(@Valid @RequestBody PermissionCreateRequest request) {
        return Result.success(permissionService.create(request));
    }

    /**
     * 更新权限
     * 更新权限信息，可修改名称、类型、路径、图标、排序、状态等
     *
     * @param id      权限ID
     * @param request 更新权限请求
     * @return 更新后的权限信息
     */
    @Operation(summary = "更新权限")
    @PutMapping("/{id}")
    @OperationLog(operation = "更新权限", module = "权限管理")
    public Result<PermissionVO> update(@PathVariable Long id,
                                       @Valid @RequestBody PermissionUpdateRequest request) {
        return Result.success(permissionService.update(id, request));
    }

    /**
     * 删除权限
     * 删除权限项，若存在子权限则不允许删除，同时清除角色与该权限的关联关系
     *
     * @param id 权限ID
     * @return 空结果
     */
    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    @OperationLog(operation = "删除权限", module = "权限管理")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success();
    }
}
