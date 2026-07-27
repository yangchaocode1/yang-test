package com.admin.system.security;

import com.admin.system.entity.SysRole;
import com.admin.system.entity.SysRoleInheritance;
import com.admin.system.entity.SysRolePermission;
import com.admin.system.entity.SysUser;
import com.admin.system.entity.SysUserRole;
import com.admin.system.mapper.SysPermissionMapper;
import com.admin.system.mapper.SysRoleInheritanceMapper;
import com.admin.system.mapper.SysRoleMapper;
import com.admin.system.mapper.SysRolePermissionMapper;
import com.admin.system.mapper.SysUserMapper;
import com.admin.system.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRoleInheritanceMapper sysRoleInheritanceMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, sysUser.getId()));
        if (userRoles.isEmpty()) {
            return new LoginUser(sysUser, List.of());
        }

        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        Set<Long> allRoleIds = collectAllRoleIds(roleIds);

        List<SysRolePermission> rolePermissions = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, allRoleIds));
        if (rolePermissions.isEmpty()) {
            return new LoginUser(sysUser, List.of());
        }

        List<Long> permissionIds = rolePermissions.stream()
                .map(SysRolePermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());

        List<String> permissionCodes = sysPermissionMapper.selectBatchIds(permissionIds).stream()
                .map(p -> p.getPermissionCode())
                .distinct()
                .collect(Collectors.toList());

        return new LoginUser(sysUser, permissionCodes);
    }

    private Set<Long> collectAllRoleIds(List<Long> roleIds) {
        Set<Long> allRoleIds = new HashSet<>(roleIds);
        Set<Long> visited = new HashSet<>(roleIds);
        List<Long> currentParentSearch = new ArrayList<>(roleIds);

        while (!currentParentSearch.isEmpty()) {
            List<SysRoleInheritance> inheritances = sysRoleInheritanceMapper.selectList(
                    new LambdaQueryWrapper<SysRoleInheritance>()
                            .in(SysRoleInheritance::getChildRoleId, currentParentSearch));
            currentParentSearch.clear();
            for (SysRoleInheritance inheritance : inheritances) {
                if (visited.add(inheritance.getParentRoleId())) {
                    allRoleIds.add(inheritance.getParentRoleId());
                    currentParentSearch.add(inheritance.getParentRoleId());
                }
            }
        }

        List<SysRole> roles = sysRoleMapper.selectBatchIds(allRoleIds);
        Set<Long> validRoleIds = roles.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .map(SysRole::getId)
                .collect(Collectors.toSet());
        allRoleIds.retainAll(validRoleIds);

        return allRoleIds;
    }
}
