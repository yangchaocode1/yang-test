package com.admin.system.controller;

import com.admin.system.annotation.OperationLog;
import com.admin.system.common.result.Result;
import com.admin.system.dto.LoginRequest;
import com.admin.system.dto.LoginResponse;
import com.admin.system.dto.RefreshTokenRequest;
import com.admin.system.dto.RefreshTokenResponse;
import com.admin.system.security.LoginUser;
import com.admin.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证管理控制器
 * 提供用户登录、登出、令牌刷新、获取当前用户信息等认证相关接口
 */
@Tag(name = "认证管理", description = "登录、登出、令牌刷新等接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 认证服务，处理登录响应构建、令牌刷新等业务逻辑 */
    private final AuthService authService;

    /** Spring Security认证管理器，用于执行用户名密码认证 */
    private final AuthenticationManager authenticationManager;

    /**
     * 用户登录
     * 通过用户名和密码进行身份认证，认证成功后返回访问令牌和刷新令牌
     *
     * @param request 登录请求，包含用户名和密码
     * @return 登录响应，包含访问令牌、刷新令牌、用户角色和权限列表
     */
    @Operation(summary = "用户登录", description = "通过用户名密码登录，返回访问令牌和刷新令牌")
    @PostMapping("/login")
    @OperationLog(operation = "用户登录", module = "认证")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 使用Spring Security认证管理器执行身份认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        // 将认证信息存入安全上下文，供后续请求使用
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 获取当前登录用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 构建登录响应（含令牌、角色、权限）
        LoginResponse response = authService.buildLoginResponse(loginUser);
        return Result.success(response);
    }

    /**
     * 用户登出
     * 清除当前用户的安全上下文信息，使当前令牌失效
     *
     * @return 空结果
     */
    @Operation(summary = "用户登出", description = "清除当前用户的登录状态")
    @PostMapping("/logout")
    @OperationLog(operation = "用户登出", module = "认证")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    /**
     * 刷新令牌
     * 使用刷新令牌获取新的访问令牌和刷新令牌，实现令牌续期
     *
     * @param request 刷新令牌请求，包含刷新令牌
     * @return 新的令牌响应，包含新的访问令牌和刷新令牌
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    @OperationLog(operation = "刷新令牌", module = "认证")
    public Result<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request.getRefreshToken());
        return Result.success(response);
    }

    /**
     * 获取当前用户信息
     * 获取当前已登录用户的详细信息，包括角色和权限列表
     *
     * @return 当前用户的登录响应信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息，包括角色和权限列表")
    @GetMapping("/user-info")
    @OperationLog(operation = "获取用户信息", module = "认证")
    public Result<LoginResponse> getUserInfo() {
        LoginResponse response = authService.getUserInfo();
        return Result.success(response);
    }
}
