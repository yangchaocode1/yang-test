package com.admin.system.service;

import com.admin.system.common.exception.BusinessException;
import com.admin.system.dto.LoginResponse;
import com.admin.system.dto.RefreshTokenResponse;
import com.admin.system.entity.SysRole;
import com.admin.system.entity.SysUserRole;
import com.admin.system.mapper.SysRoleMapper;
import com.admin.system.mapper.SysUserRoleMapper;
import com.admin.system.security.JwtUtil;
import com.admin.system.security.LoginUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务
 * 处理用户登录响应构建、令牌刷新、获取当前用户信息等认证相关业务逻辑
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /** JWT工具类，用于生成和验证令牌 */
    private final JwtUtil jwtUtil;

    /** 用户-角色关联Mapper，用于查询用户角色关系 */
    private final SysUserRoleMapper sysUserRoleMapper;

    /** 角色Mapper，用于查询角色信息 */
    private final SysRoleMapper sysRoleMapper;

    /**
     * 用户登出
     * 清除安全上下文中的认证信息
     */
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 刷新令牌
     * 验证刷新令牌的有效性，若有效则生成新的访问令牌和刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的令牌响应，包含新的访问令牌和刷新令牌
     * @throws BusinessException 刷新令牌无效或已过期时抛出
     */
    public RefreshTokenResponse refreshToken(String refreshToken) {
        // 验证刷新令牌是否有效
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(401, "刷新令牌无效或已过期");
        }
        // 从令牌中提取用户名
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        // 生成新的访问令牌和刷新令牌
        String newAccessToken = jwtUtil.generateAccessToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);
        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .build();
    }

    /**
     * 获取当前登录用户信息
     * 从安全上下文中获取当前认证用户，构建登录响应
     *
     * @return 当前用户的登录响应信息
     * @throws BusinessException 用户未登录时抛出
     */
    public LoginResponse getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new BusinessException(401, "用户未登录");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return buildLoginResponse(loginUser);
    }

    /**
     * 构建登录响应
     * 根据登录用户信息构建包含令牌、角色、权限的登录响应
     *
     * @param loginUser 已认证的登录用户
     * @return 登录响应，包含访问令牌、刷新令牌、角色编码列表、权限编码列表
     */
    public LoginResponse buildLoginResponse(LoginUser loginUser) {
        // 查询用户关联的角色ID列表
        List<Long> roleIds = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, loginUser.getSysUser().getId()))
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        // 根据角色ID列表查询角色编码
        List<String> roleCodes = List.of();
        if (!roleIds.isEmpty()) {
            roleCodes = sysRoleMapper.selectBatchIds(roleIds).stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.toList());
        }

        // 生成访问令牌和刷新令牌
        String accessToken = jwtUtil.generateAccessToken(loginUser.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(loginUser.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .username(loginUser.getUsername())
                .roles(roleCodes)
                .permissions(loginUser.getPermissionCodes())
                .build();
    }
}
