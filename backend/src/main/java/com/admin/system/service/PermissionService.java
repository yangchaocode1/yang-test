package com.admin.system.service;

import com.admin.system.common.exception.BusinessException;
import com.admin.system.dto.*;
import com.admin.system.entity.SysPermission;
import com.admin.system.mapper.SysPermissionMapper;
import com.admin.system.mapper.SysRolePermissionMapper;
import com.admin.system.entity.SysRolePermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务
 * 处理权限的增删改查、权限树构建等业务逻辑，操作时记录审计日志
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    /** 权限Mapper，用于权限表的数据库操作 */
    private final SysPermissionMapper sysPermissionMapper;

    /** 角色-权限关联Mapper，用于角色权限关系的数据库操作 */
    private final SysRolePermissionMapper sysRolePermissionMapper;

    /** 审计日志服务，用于记录权限操作审计日志 */
    private final AuditLogService auditLogService;

    /**
     * 获取权限树形结构
     * 查询所有启用状态的权限，按父子关系组装为树形结构
     *
     * @return 权限树形列表
     */
    public List<PermissionVO> tree() {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getStatus, 1).orderByAsc(SysPermission::getSortOrder);
        List<SysPermission> all = sysPermissionMapper.selectList(wrapper);
        List<PermissionVO> voList = all.stream().map(this::toVO).collect(Collectors.toList());
        return buildTree(voList);
    }

    /**
     * 分页查询权限列表
     * 按排序和创建时间排序
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页权限视图对象列表
     */
    public Page<PermissionVO> pageList(Integer pageNum, Integer pageSize) {
        Page<SysPermission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysPermission::getSortOrder).orderByDesc(SysPermission::getCreatedTime);
        Page<SysPermission> permPage = sysPermissionMapper.selectPage(page, wrapper);

        Page<PermissionVO> voPage = new Page<>(permPage.getCurrent(), permPage.getSize(), permPage.getTotal());
        voPage.setRecords(permPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    /**
     * 创建权限
     * 校验权限编码唯一性后创建权限，并记录审计日志
     *
     * @param request 创建权限请求
     * @return 创建后的权限视图对象
     * @throws BusinessException 权限编码已存在时抛出
     */
    @Transactional
    public PermissionVO create(PermissionCreateRequest request) {
        // 校验权限编码唯一性
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getPermissionCode, request.getPermissionCode());
        if (sysPermissionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("权限编码已存在");
        }

        SysPermission perm = new SysPermission();
        perm.setPermissionCode(request.getPermissionCode());
        perm.setPermissionName(request.getPermissionName());
        perm.setPermissionType(request.getPermissionType());
        perm.setParentId(request.getParentId());
        perm.setPath(request.getPath());
        perm.setIcon(request.getIcon());
        perm.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        perm.setStatus(request.getStatus());
        sysPermissionMapper.insert(perm);

        // 记录审计日志
        auditLogService.log("CREATE", "PERMISSION", perm.getId().toString(), null, perm);
        return toVO(perm);
    }

    /**
     * 更新权限
     * 更新权限信息，并记录审计日志
     *
     * @param id      权限ID
     * @param request 更新权限请求
     * @return 更新后的权限视图对象
     * @throws BusinessException 权限不存在时抛出
     */
    @Transactional
    public PermissionVO update(Long id, PermissionUpdateRequest request) {
        SysPermission perm = sysPermissionMapper.selectById(id);
        if (perm == null) {
            throw new BusinessException("权限不存在");
        }

        // 保存旧值用于审计日志
        SysPermission oldValue = new SysPermission();
        oldValue.setPermissionName(perm.getPermissionName());
        oldValue.setPermissionType(perm.getPermissionType());
        oldValue.setParentId(perm.getParentId());
        oldValue.setPath(perm.getPath());
        oldValue.setIcon(perm.getIcon());
        oldValue.setSortOrder(perm.getSortOrder());
        oldValue.setStatus(perm.getStatus());

        perm.setPermissionName(request.getPermissionName());
        if (request.getPermissionType() != null) {
            perm.setPermissionType(request.getPermissionType());
        }
        if (request.getParentId() != null) {
            perm.setParentId(request.getParentId());
        }
        perm.setPath(request.getPath());
        perm.setIcon(request.getIcon());
        if (request.getSortOrder() != null) {
            perm.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            perm.setStatus(request.getStatus());
        }
        sysPermissionMapper.updateById(perm);

        // 记录审计日志
        auditLogService.log("UPDATE", "PERMISSION", id.toString(), oldValue, perm);
        return toVO(perm);
    }

    /**
     * 删除权限
     * 若存在子权限则不允许删除，同时清除角色与该权限的关联关系，并记录审计日志
     *
     * @param id 权限ID
     * @throws BusinessException 权限不存在或存在子权限时抛出
     */
    @Transactional
    public void delete(Long id) {
        SysPermission perm = sysPermissionMapper.selectById(id);
        if (perm == null) {
            throw new BusinessException("权限不存在");
        }

        // 检查是否存在子权限
        LambdaQueryWrapper<SysPermission> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(SysPermission::getParentId, id);
        if (sysPermissionMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException("存在子权限，无法删除");
        }

        // 删除角色与该权限的关联关系
        LambdaQueryWrapper<SysRolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(SysRolePermission::getPermissionId, id);
        sysRolePermissionMapper.delete(rpWrapper);

        sysPermissionMapper.deleteById(id);

        // 记录审计日志
        auditLogService.log("DELETE", "PERMISSION", id.toString(), perm, null);
    }

    /**
     * 将权限实体转换为权限视图对象
     *
     * @param perm 权限实体
     * @return 权限视图对象
     */
    private PermissionVO toVO(SysPermission perm) {
        return PermissionVO.builder()
                .id(perm.getId())
                .permissionCode(perm.getPermissionCode())
                .permissionName(perm.getPermissionName())
                .permissionType(perm.getPermissionType())
                .parentId(perm.getParentId())
                .path(perm.getPath())
                .icon(perm.getIcon())
                .sortOrder(perm.getSortOrder())
                .status(perm.getStatus())
                .build();
    }

    /**
     * 构建权限树形结构
     * 将扁平的权限列表按parentId组装为树形结构
     *
     * @param all 所有权限视图对象列表
     * @return 树形结构的根节点列表
     */
    private List<PermissionVO> buildTree(List<PermissionVO> all) {
        // 按parentId分组，构建子节点映射
        Map<Long, List<PermissionVO>> childrenMap = all.stream()
                .filter(vo -> vo.getParentId() != null)
                .collect(Collectors.groupingBy(PermissionVO::getParentId));

        // 筛选根节点（parentId为null的节点）
        List<PermissionVO> roots = all.stream()
                .filter(vo -> vo.getParentId() == null)
                .collect(Collectors.toList());

        // 递归填充子节点
        for (PermissionVO root : roots) {
            fillChildren(root, childrenMap);
        }
        return roots;
    }

    /**
     * 递归填充子节点
     *
     * @param parent      父节点
     * @param childrenMap 子节点映射（key为父节点ID）
     */
    private void fillChildren(PermissionVO parent, Map<Long, List<PermissionVO>> childrenMap) {
        List<PermissionVO> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setChildren(children);
        for (PermissionVO child : children) {
            fillChildren(child, childrenMap);
        }
    }
}
