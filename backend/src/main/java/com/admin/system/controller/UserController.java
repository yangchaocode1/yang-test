package com.admin.system.controller;

import com.admin.system.annotation.OperationLog;
import com.admin.system.common.result.Result;
import com.admin.system.dto.*;
import com.admin.system.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

/**
 * 用户管理控制器
 * 提供用户的增删改查、状态管理、密码管理、导入导出等接口
 */
@Tag(name = "用户管理", description = "用户CRUD、状态管理、密码管理、导入导出等接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    /** 用户服务，处理用户相关的业务逻辑 */
    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     * 支持关键词搜索（用户名、姓名、手机号、邮箱）、状态筛选、排序
     *
     * @param request 查询条件，含关键词、状态、分页参数、排序参数
     * @return 分页用户列表
     */
    @Operation(summary = "分页查询用户列表", description = "支持关键词搜索、状态筛选、排序")
    @GetMapping
    @OperationLog(operation = "查询用户列表", module = "用户管理")
    public Result<IPage<UserVO>> listUsers(UserQueryRequest request) {
        IPage<UserVO> page = userService.listUsers(request);
        return Result.success(page);
    }

    /**
     * 获取用户详情
     * 根据用户ID获取用户详细信息，包含角色信息
     *
     * @param id 用户ID
     * @return 用户详细信息
     */
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息，包含角色信息")
    @GetMapping("/{id}")
    @OperationLog(operation = "查询用户详情", module = "用户管理")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 创建用户
     * 创建新用户，需提供用户名、密码等信息，支持同时分配角色
     *
     * @param request 创建用户请求，包含用户名、密码、姓名、角色等
     * @return 创建后的用户信息
     */
    @Operation(summary = "创建用户", description = "创建新用户，需提供用户名、密码等信息")
    @PostMapping
    @OperationLog(operation = "创建用户", module = "用户管理")
    public Result<UserVO> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserVO user = userService.createUser(request);
        return Result.success(user);
    }

    /**
     * 更新用户
     * 更新用户信息，可修改姓名、手机号、邮箱、角色等
     *
     * @param id      用户ID
     * @param request 更新用户请求，包含需要修改的字段
     * @return 更新后的用户信息
     */
    @Operation(summary = "更新用户", description = "更新用户信息，可修改姓名、手机号、邮箱、角色等")
    @PutMapping("/{id}")
    @OperationLog(operation = "更新用户", module = "用户管理")
    public Result<UserVO> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        UserVO user = userService.updateUser(id, request);
        return Result.success(user);
    }

    /**
     * 删除用户
     * 逻辑删除用户，同时删除用户与角色的关联关系
     *
     * @param id 用户ID
     * @return 空结果
     */
    @Operation(summary = "删除用户", description = "逻辑删除用户")
    @DeleteMapping("/{id}")
    @OperationLog(operation = "删除用户", module = "用户管理")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 批量删除用户
     * 批量逻辑删除用户，同时删除用户与角色的关联关系
     *
     * @param ids 用户ID列表
     * @return 空结果
     */
    @Operation(summary = "批量删除用户", description = "批量逻辑删除用户")
    @DeleteMapping("/batch")
    @OperationLog(operation = "批量删除用户", module = "用户管理")
    public Result<Void> batchDeleteUsers(@RequestBody List<Long> ids) {
        userService.batchDeleteUsers(ids);
        return Result.success();
    }

    /**
     * 切换用户状态
     * 启用/禁用用户，禁用后该用户无法登录
     *
     * @param id 用户ID
     * @return 空结果
     */
    @Operation(summary = "切换用户状态", description = "启用/禁用用户，禁用后该用户无法登录")
    @PutMapping("/{id}/status")
    @OperationLog(operation = "切换用户状态", module = "用户管理")
    public Result<Void> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return Result.success();
    }

    /**
     * 设置账号有效期
     * 设置用户账号的过期时间，过期后用户无法登录
     *
     * @param id      用户ID
     * @param request 有效期请求，包含过期时间
     * @return 空结果
     */
    @Operation(summary = "设置账号有效期", description = "设置用户账号的过期时间")
    @PutMapping("/{id}/expire-time")
    @OperationLog(operation = "设置账号有效期", module = "用户管理")
    public Result<Void> setExpireTime(@PathVariable Long id, @RequestBody ExpireTimeRequest request) {
        userService.setExpireTime(id, request);
        return Result.success();
    }

    /**
     * 重置密码
     * 将用户密码重置为默认值123456，并标记需要下次登录修改密码
     *
     * @param id 用户ID
     * @return 空结果
     */
    @Operation(summary = "重置密码", description = "将用户密码重置为默认值123456，并要求下次登录修改密码")
    @PutMapping("/{id}/reset-password")
    @OperationLog(operation = "重置密码", module = "用户管理")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    /**
     * 修改密码
     * 用户自行修改密码，需提供旧密码和新密码
     *
     * @param id      用户ID
     * @param request 修改密码请求，包含旧密码和新密码
     * @return 空结果
     */
    @Operation(summary = "修改密码", description = "用户自行修改密码，需提供旧密码和新密码")
    @PutMapping("/{id}/change-password")
    @OperationLog(operation = "修改密码", module = "用户管理")
    public Result<Void> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return Result.success();
    }

    /**
     * 导出用户数据
     * 导出所有用户数据为Excel文件
     *
     * @param response HTTP响应，用于写入Excel文件流
     * @throws IOException 文件写入异常
     */
    @Operation(summary = "导出用户数据", description = "导出用户数据为Excel文件")
    @GetMapping("/export")
    @OperationLog(operation = "导出用户数据", module = "用户管理")
    public void exportUsers(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
        userService.exportUsers(response.getOutputStream());
    }

    /**
     * 导入用户数据
     * 从Excel文件导入用户数据，返回导入结果（成功数、失败数、失败详情）
     *
     * @param file 上传的Excel文件
     * @return 导入结果，包含总数、成功数、失败数和失败详情
     * @throws IOException 文件读取异常
     */
    @Operation(summary = "导入用户数据", description = "从Excel文件导入用户数据，返回导入结果")
    @PostMapping("/import")
    @OperationLog(operation = "导入用户数据", module = "用户管理")
    public Result<UserImportResult> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        UserImportResult result = userService.importUsers(file);
        return Result.success(result);
    }

    /**
     * 测试接口 - 获取当前登录用户信息
     * 用于测试登录状态和权限
     *
     * @return 当前登录用户信息
     */
    @Operation(summary = "测试接口 - 获取当前登录用户信息", description = "用于测试登录状态和权限")
    @GetMapping("/test/current-user")
    @OperationLog(operation = "测试获取当前用户", module = "用户管理")
    public Result<UserVO> getCurrentUser() {
        UserVO user = userService.getCurrentUser();
        return Result.success(user);
    }

    /**
     * 测试接口 - 批量创建测试用户
     * 用于生成测试数据
     *
     * @param count 要创建的测试用户数量
     * @return 创建结果
     */
    @Operation(summary = "测试接口 - 批量创建测试用户", description = "用于生成测试数据")
    @PostMapping("/test/batch-create")
    @OperationLog(operation = "批量创建测试用户", module = "用户管理")
    public Result<String> batchCreateTestUsers(@RequestParam Integer count) {
        userService.batchCreateTestUsers(count);
        return Result.success("成功创建 " + count + " 个测试用户");
    }

    /**
     * 测试 AI 审查 - 故意写出有问题的代码，验证 AI 能否审出
     */
    @Operation(summary = "测试 AI 审查 - 危险接口")
    @GetMapping("/test/dangerous")
    public Result<String> dangerousEndpoint(@RequestParam String username) {
        // 问题 1：SQL 注入风险（直接拼接 SQL）
        String sql = "SELECT * FROM user WHERE username = '" + username + "'";
        System.out.println("Executing SQL: " + sql);

        // 问题 2：硬编码密码
        String password = "admin123";

        // 问题 3：空指针风险（没判空就用）
        String upperName = username.toUpperCase();

        // 问题 4： System.out 应该用日志框架
        System.out.println("User query: " + upperName + ", pwd=" + password);

        return Result.success(upperName);
    }

}
