package com.admin.system.service;

import com.admin.system.common.exception.BusinessException;
import com.admin.system.dto.*;
import com.admin.system.entity.SysRole;
import com.admin.system.entity.SysRoleInheritance;
import com.admin.system.entity.SysRolePermission;
import com.admin.system.mapper.SysRoleInheritanceMapper;
import com.admin.system.mapper.SysRoleMapper;
import com.admin.system.mapper.SysRolePermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleInheritanceService {

    private final SysRoleInheritanceMapper sysRoleInheritanceMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final AuditLogService auditLogService;

    public List<RoleVO> getInheritedRoles(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        LambdaQueryWrapper<SysRoleInheritance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleInheritance::getChildRoleId, roleId);
        List<SysRoleInheritance> inheritances = sysRoleInheritanceMapper.selectList(wrapper);

        if (inheritances.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> parentIds = inheritances.stream()
                .map(SysRoleInheritance::getParentRoleId)
                .collect(Collectors.toList());

        List<SysRole> parentRoles = sysRoleMapper.selectBatchIds(parentIds);
        return parentRoles.stream().map(this::toSimpleVO).collect(Collectors.toList());
    }

    @Transactional
    public void setInheritances(Long roleId, List<Long> parentRoleIds) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        for (Long parentRoleId : parentRoleIds) {
            SysRole parentRole = sysRoleMapper.selectById(parentRoleId);
            if (parentRole == null) {
                throw new BusinessException("父角色不存在: " + parentRoleId);
            }
            if (parentRoleId.equals(roleId)) {
                throw new BusinessException("角色不能继承自身");
            }
            if (wouldCreateCycle(roleId, parentRoleId)) {
                throw new BusinessException("设置继承关系会导致循环继承");
            }
        }

        LambdaQueryWrapper<SysRoleInheritance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleInheritance::getChildRoleId, roleId);
        List<Long> oldParentIds = sysRoleInheritanceMapper.selectList(wrapper).stream()
                .map(SysRoleInheritance::getParentRoleId)
                .collect(Collectors.toList());
        sysRoleInheritanceMapper.delete(wrapper);

        for (Long parentRoleId : parentRoleIds) {
            SysRoleInheritance inheritance = new SysRoleInheritance();
            inheritance.setParentRoleId(parentRoleId);
            inheritance.setChildRoleId(roleId);
            sysRoleInheritanceMapper.insert(inheritance);
        }

        auditLogService.log("SET_INHERITANCE", "ROLE", roleId.toString(), oldParentIds, parentRoleIds);
    }

    @Transactional
    public void removeInheritance(Long roleId, Long parentRoleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        LambdaQueryWrapper<SysRoleInheritance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleInheritance::getChildRoleId, roleId)
                .eq(SysRoleInheritance::getParentRoleId, parentRoleId);
        if (sysRoleInheritanceMapper.selectCount(wrapper) == 0) {
            throw new BusinessException("继承关系不存在");
        }
        sysRoleInheritanceMapper.delete(wrapper);

        auditLogService.log("REMOVE_INHERITANCE", "ROLE", roleId.toString(), parentRoleId, null);
    }

    public Set<Long> getAllInheritedPermissionIds(Long roleId) {
        Set<Long> visited = new HashSet<>();
        Set<Long> permissionIds = new HashSet<>();
        collectInheritedPermissions(roleId, visited, permissionIds);
        return permissionIds;
    }

    private void collectInheritedPermissions(Long roleId, Set<Long> visited, Set<Long> permissionIds) {
        if (visited.contains(roleId)) {
            return;
        }
        visited.add(roleId);

        LambdaQueryWrapper<SysRolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(SysRolePermission::getRoleId, roleId);
        List<Long> directPerms = sysRolePermissionMapper.selectList(rpWrapper).stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());
        permissionIds.addAll(directPerms);

        LambdaQueryWrapper<SysRoleInheritance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleInheritance::getChildRoleId, roleId);
        List<Long> parentRoleIds = sysRoleInheritanceMapper.selectList(wrapper).stream()
                .map(SysRoleInheritance::getParentRoleId)
                .collect(Collectors.toList());

        for (Long parentRoleId : parentRoleIds) {
            collectInheritedPermissions(parentRoleId, visited, permissionIds);
        }
    }

    private boolean wouldCreateCycle(Long roleId, Long parentRoleId) {
        Set<Long> visited = new HashSet<>();
        return isAncestor(roleId, parentRoleId, visited);
    }

    private boolean isAncestor(Long targetAncestorId, Long currentRoleId, Set<Long> visited) {
        if (visited.contains(currentRoleId)) {
            return false;
        }
        visited.add(currentRoleId);

        if (currentRoleId.equals(targetAncestorId)) {
            return true;
        }

        LambdaQueryWrapper<SysRoleInheritance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleInheritance::getChildRoleId, currentRoleId);
        List<Long> parentIds = sysRoleInheritanceMapper.selectList(wrapper).stream()
                .map(SysRoleInheritance::getParentRoleId)
                .collect(Collectors.toList());

        for (Long parentId : parentIds) {
            if (isAncestor(targetAncestorId, parentId, visited)) {
                return true;
            }
        }
        return false;
    }

    private RoleVO toSimpleVO(SysRole role) {
        return RoleVO.builder()
                .id(role.getId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .parentId(role.getParentId())
                .sortOrder(role.getSortOrder())
                .status(role.getStatus())
                .createdTime(role.getCreatedTime())
                .updatedTime(role.getUpdatedTime())
                .build();
    }
}
