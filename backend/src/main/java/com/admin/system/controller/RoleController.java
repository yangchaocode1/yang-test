package com.admin.system.controller;

import com.admin.system.annotation.OperationLog;
import com.admin.system.common.result.Result;
import com.admin.system.dto.*;
import com.admin.system.service.RoleInheritanceService;
import com.admin.system.service.RoleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 提供角色的增删改查、权限分配、角色继承管理等接口
 */
@Tag(name = "角色管理", description = "角色CRUD、权限分配与继承管理接口")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    /** 角色服务，处理角色相关的业务逻辑 */
    private final RoleService roleService;

    /** 角色继承服务，处理角色继承关系的业务逻辑 */
    private final RoleInheritanceService roleInheritanceService;

    /**
     * 分页查询角色列表
     * 支持关键词搜索（角色编码、角色名称）、状态筛选
     *
     * @param request 查询条件，含关键词、状态、分页参数
     * @return 分页角色列表
     */
    @Operation(summary = "分页查询角色列表")
    @GetMapping
    @OperationLog(operation = "查询角色列表", module = "角色管理")
    public Result<Page<RoleVO>> pageList(RoleQueryRequest request) {
        return Result.success(roleService.pageList(request));
    }

    /**
     * 获取角色详情
     * 根据角色ID获取角色详细信息，包含已分配的权限ID列表
     *
     * @param id 角色ID
     * @return 角色详细信息
     */
    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @OperationLog(operation = "查询角色详情", module = "角色管理")
    public Result<RoleVO> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    /**
     * 创建角色
     * 创建新角色，需提供角色编码、角色名称等信息
     *
     * @param request 创建角色请求，包含角色编码、名称、描述、排序、状态等
     * @return 创建后的角色信息
     */
    @Operation(summary = "创建角色")
    @PostMapping
    @OperationLog(operation = "创建角色", module = "角色管理")
    public Result<RoleVO> create(@Valid @RequestBody RoleCreateRequest request) {
        return Result.success(roleService.create(request));
    }

    /**
     * 更新角色
     * 更新角色信息，可修改角色名称、描述、排序、状态等
     *
     * @param id      角色ID
     * @param request 更新角色请求
     * @return 更新后的角色信息
     */
    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @OperationLog(operation = "更新角色", module = "角色管理")
    public Result<RoleVO> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return Result.success(roleService.update(id, request));
    }

    /**
     * 删除角色
     * 删除角色，同时删除角色与权限的关联关系
     *
     * @param id 角色ID
     * @return 空结果
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @OperationLog(operation = "删除角色", module = "角色管理")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    /**
     * 获取所有角色（不分页）
     * 返回所有启用状态的角色列表，用于下拉选择等场景
     *
     * @return 角色列表
     */
    @Operation(summary = "获取所有角色（不分页）")
    @GetMapping("/all")
    @OperationLog(operation = "查询所有角色", module = "角色管理")
    public Result<List<RoleVO>> listAll() {
        return Result.success(roleService.listAll());
    }

    /**
     * 获取角色选项列表
     * 返回所有启用状态的角色列表，用于下拉选择框
     *
     * @return 角色列表
     */
    @Operation(summary = "获取角色选项列表")
    @GetMapping("/options")
    @OperationLog(operation = "查询角色选项", module = "角色管理")
    public Result<List<RoleVO>> listOptions() {
        return Result.success(roleService.listAll());
    }

    /**
     * 获取角色已分配的权限ID列表
     * 根据角色ID查询该角色已分配的所有权限ID
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Operation(summary = "获取角色已分配的权限ID列表")
    @GetMapping("/{roleId}/permissions")
    public Result<List<Long>> getPermissionIds(@PathVariable Long roleId) {
        return Result.success(roleService.getPermissionIds(roleId));
    }

    /**
     * 为角色分配权限
     * 替换角色当前的权限列表为新的权限列表
     *
     * @param roleId  角色ID
     * @param request 权限分配请求，包含权限ID列表
     * @return 空结果
     */
    @Operation(summary = "为角色分配权限")
    @PostMapping("/{roleId}/permissions")
    @OperationLog(operation = "分配角色权限", module = "角色管理")
    public Result<Void> assignPermissions(@PathVariable Long roleId,
                                          @Valid @RequestBody PermissionAssignRequest request) {
        roleService.assignPermissions(roleId, request.getPermissionIds());
        return Result.success();
    }

    /**
     * 获取角色继承的父角色列表
     * 查询指定角色通过继承关系获得的所有父角色
     *
     * @param roleId 角色ID
     * @return 父角色列表
     */
    @Operation(summary = "获取角色继承的父角色列表")
    @GetMapping("/{roleId}/inherited-roles")
    public Result<List<RoleVO>> getInheritedRoles(@PathVariable Long roleId) {
        return Result.success(roleInheritanceService.getInheritedRoles(roleId));
    }

    /**
     * 设置角色继承关系
     * 替换指定角色的父角色列表，会校验循环继承
     *
     * @param roleId  角色ID
     * @param request 继承请求，包含父角色ID列表
     * @return 空结果
     */
    @Operation(summary = "设置角色继承关系")
    @PostMapping("/{roleId}/inherit")
    @OperationLog(operation = "设置角色继承", module = "角色管理")
    public Result<Void> setInheritances(@PathVariable Long roleId,
                                        @Valid @RequestBody RoleInheritRequest request) {
        roleInheritanceService.setInheritances(roleId, request.getParentRoleIds());
        return Result.success();
    }

    /**
     * 移除继承关系
     * 移除指定角色与某个父角色之间的继承关系
     *
     * @param roleId       子角色ID
     * @param parentRoleId 父角色ID
     * @return 空结果
     */
    @Operation(summary = "移除继承关系")
    @DeleteMapping("/{roleId}/inherit/{parentRoleId}")
    @OperationLog(operation = "移除角色继承", module = "角色管理")
    public Result<Void> removeInheritance(@PathVariable Long roleId,
                                          @PathVariable Long parentRoleId) {
        roleInheritanceService.removeInheritance(roleId, parentRoleId);
        return Result.success();
    }
}
