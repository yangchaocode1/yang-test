package com.admin.system.service;

import com.admin.system.common.exception.BusinessException;
import com.admin.system.dto.*;
import com.admin.system.entity.SysRole;
import com.admin.system.entity.SysRolePermission;
import com.admin.system.mapper.SysRoleMapper;
import com.admin.system.mapper.SysRolePermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务
 * 处理角色的增删改查、权限分配等业务逻辑，操作时记录审计日志
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    /** 角色Mapper，用于角色表的数据库操作 */
    private final SysRoleMapper sysRoleMapper;

    /** 角色-权限关联Mapper，用于角色权限关系的数据库操作 */
    private final SysRolePermissionMapper sysRolePermissionMapper;

    /** 审计日志服务，用于记录角色操作审计日志 */
    private final AuditLogService auditLogService;

    /**
     * 分页查询角色列表
     * 支持关键词搜索（角色编码、角色名称）、状态筛选
     *
     * @param request 查询条件，含关键词、状态、分页参数
     * @return 分页角色视图对象列表
     */
    public Page<RoleVO> pageList(RoleQueryRequest request) {
        Page<SysRole> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, request.getKeyword())
                    .or().like(SysRole::getRoleName, request.getKeyword()));
        }
        if (request.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, request.getStatus());
        }
        wrapper.orderByAsc(SysRole::getSortOrder).orderByDesc(SysRole::getCreatedTime);
        Page<SysRole> rolePage = sysRoleMapper.selectPage(page, wrapper);

        Page<RoleVO> voPage = new Page<>(rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal());
        voPage.setRecords(rolePage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    /**
     * 根据ID获取角色详情
     *
     * @param id 角色ID
     * @return 角色视图对象
     * @throws BusinessException 角色不存在时抛出
     */
    public RoleVO getById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return toVO(role);
    }

    /**
     * 创建角色
     * 校验角色编码唯一性后创建角色，并记录审计日志
     *
     * @param request 创建角色请求
     * @return 创建后的角色视图对象
     * @throws BusinessException 角色编码已存在时抛出
     */
    @Transactional
    public RoleVO create(RoleCreateRequest request) {
        // 校验角色编码唯一性
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, request.getRoleCode());
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setParentId(request.getParentId());
        role.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        role.setStatus(request.getStatus());
        sysRoleMapper.insert(role);

        // 记录审计日志
        auditLogService.log("CREATE", "ROLE", role.getId().toString(), null, role);
        return toVO(role);
    }

    /**
     * 更新角色
     * 更新角色信息，并记录审计日志
     *
     * @param id      角色ID
     * @param request 更新角色请求
     * @return 更新后的角色视图对象
     * @throws BusinessException 角色不存在时抛出
     */
    @Transactional
    public RoleVO update(Long id, RoleUpdateRequest request) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 保存旧值用于审计日志
        SysRole oldValue = new SysRole();
        oldValue.setRoleName(role.getRoleName());
        oldValue.setDescription(role.getDescription());
        oldValue.setParentId(role.getParentId());
        oldValue.setSortOrder(role.getSortOrder());
        oldValue.setStatus(role.getStatus());

        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setParentId(request.getParentId());
        role.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : role.getSortOrder());
        role.setStatus(request.getStatus() != null ? request.getStatus() : role.getStatus());
        sysRoleMapper.updateById(role);

        // 记录审计日志
        auditLogService.log("UPDATE", "ROLE", id.toString(), oldValue, role);
        return toVO(role);
    }

    /**
     * 删除角色
     * 删除角色及角色与权限的关联关系，并记录审计日志
     *
     * @param id 角色ID
     * @throws BusinessException 角色不存在时抛出
     */
    @Transactional
    public void delete(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除角色
        sysRoleMapper.deleteById(id);
        // 删除角色权限关联
        LambdaQueryWrapper<SysRolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(SysRolePermission::getRoleId, id);
        sysRolePermissionMapper.delete(rpWrapper);

        // 记录审计日志
        auditLogService.log("DELETE", "ROLE", id.toString(), role, null);
    }

    /**
     * 获取所有启用状态的角色列表（不分页）
     * 用于下拉选择等场景
     *
     * @return 角色视图对象列表
     */
    public List<RoleVO> listAll() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1).orderByAsc(SysRole::getSortOrder);
        List<SysRole> roles = sysRoleMapper.selectList(wrapper);
        return roles.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 获取角色已分配的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     * @throws BusinessException 角色不存在时抛出
     */
    public List<Long> getPermissionIds(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        return sysRolePermissionMapper.selectList(wrapper).stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());
    }

    /**
     * 为角色分配权限
     * 替换角色当前的权限列表为新的权限列表，并记录审计日志
     *
     * @param roleId        角色ID
     * @param permissionIds 新的权限ID列表
     * @throws BusinessException 角色不存在时抛出
     */
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 查询旧的权限ID列表用于审计日志
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        List<Long> oldPermissionIds = sysRolePermissionMapper.selectList(wrapper).stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 先删除旧关联，再保存新关联
        sysRolePermissionMapper.delete(wrapper);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                sysRolePermissionMapper.insert(rp);
            }
        }

        // 记录审计日志
        auditLogService.log("ASSIGN_PERMISSIONS", "ROLE", roleId.toString(),
                oldPermissionIds, permissionIds);
    }

    /**
     * 将角色实体转换为角色视图对象
     * 包含角色基本信息、父角色名称、已分配的权限ID列表
     *
     * @param role 角色实体
     * @return 角色视图对象
     */
    private RoleVO toVO(SysRole role) {
        // 查询父角色名称
        String parentName = null;
        if (role.getParentId() != null) {
            SysRole parent = sysRoleMapper.selectById(role.getParentId());
            if (parent != null) {
                parentName = parent.getRoleName();
            }
        }

        // 查询角色已分配的权限ID列表
        LambdaQueryWrapper<SysRolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(SysRolePermission::getRoleId, role.getId());
        List<Long> permissionIds = sysRolePermissionMapper.selectList(rpWrapper).stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());

        return RoleVO.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .parentId(role.getParentId())
                .parentName(parentName)
                .sortOrder(role.getSortOrder())
                .status(role.getStatus())
                .permissionIds(permissionIds)
                .createdTime(role.getCreatedTime())
                .updatedTime(role.getUpdatedTime())
                .build();
    }
}
