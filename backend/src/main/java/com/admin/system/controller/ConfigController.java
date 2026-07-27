package com.admin.system.controller;

import com.admin.system.annotation.OperationLog;
import com.admin.system.common.result.Result;
import com.admin.system.dto.ConfigCreateRequest;
import com.admin.system.dto.ConfigQueryRequest;
import com.admin.system.dto.ConfigUpdateRequest;
import com.admin.system.service.ConfigService;
import com.admin.system.vo.ConfigVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统配置管理控制器
 * 提供系统配置的增删改查、按类型分组查询与批量保存等接口
 * 系统配置支持按类型（UI、SECURITY、NOTIFICATION）分组管理
 */
@Tag(name = "系统配置管理", description = "系统配置的增删改查接口")
@RestController
@RequestMapping("/configs")
@RequiredArgsConstructor
public class ConfigController {

    /** 配置服务，处理系统配置相关的业务逻辑 */
    private final ConfigService configService;

    /**
     * 分页查询配置列表
     * 支持按配置类型筛选和关键词搜索（配置键、描述）
     *
     * @param request 查询条件，含配置类型、关键词、分页参数
     * @return 分页配置列表
     */
    @Operation(summary = "分页查询配置列表")
    @GetMapping
    @OperationLog(operation = "查询配置列表", module = "系统配置")
    public Result<IPage<ConfigVO>> pageQuery(ConfigQueryRequest request) {
        return Result.success(configService.pageQuery(request));
    }

    /**
     * 获取配置详情
     * 根据配置ID获取配置详细信息
     *
     * @param id 配置ID
     * @return 配置详情
     */
    @Operation(summary = "获取配置详情")
    @GetMapping("/{id}")
    @OperationLog(operation = "查询配置详情", module = "系统配置")
    public Result<ConfigVO> getById(@PathVariable Long id) {
        return Result.success(configService.getById(id));
    }

    /**
     * 根据key获取配置值
     * 通过配置键查询对应的配置信息
     *
     * @param configKey 配置键
     * @return 配置详情
     */
    @Operation(summary = "根据key获取配置值")
    @GetMapping("/key/{configKey}")
    @OperationLog(operation = "根据key查询配置", module = "系统配置")
    public Result<ConfigVO> getByKey(@PathVariable String configKey) {
        return Result.success(configService.getByKey(configKey));
    }

    /**
     * 创建配置
     * 创建新的系统配置项
     *
     * @param request 创建配置请求，包含配置键、配置值、配置类型、描述
     * @return 创建后的配置信息
     */
    @Operation(summary = "创建配置")
    @PostMapping
    @OperationLog(operation = "创建配置", module = "系统配置")
    public Result<ConfigVO> create(@Valid @RequestBody ConfigCreateRequest request) {
        return Result.success(configService.create(request));
    }

    /**
     * 更新配置
     * 更新系统配置项，可修改配置值、配置类型、描述
     *
     * @param id      配置ID
     * @param request 更新配置请求
     * @return 更新后的配置信息
     */
    @Operation(summary = "更新配置")
    @PutMapping("/{id}")
    @OperationLog(operation = "更新配置", module = "系统配置")
    public Result<ConfigVO> update(@PathVariable Long id, @Valid @RequestBody ConfigUpdateRequest request) {
        return Result.success(configService.update(id, request));
    }

    /**
     * 删除配置
     * 根据ID删除系统配置项
     *
     * @param id 配置ID
     * @return 空结果
     */
    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    @OperationLog(operation = "删除配置", module = "系统配置")
    public Result<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return Result.success();
    }

    /**
     * 根据类型获取配置分组
     * 查询指定类型的所有配置，返回键值对映射
     *
     * @param type 配置类型（如UI、SECURITY、NOTIFICATION）
     * @return 配置键值对映射
     */
    @Operation(summary = "根据类型获取配置分组")
    @GetMapping("/group/{type}")
    @OperationLog(operation = "查询配置分组", module = "系统配置")
    public Result<Map<String, String>> getConfigsByType(@PathVariable String type) {
        return Result.success(configService.getConfigsByType(type));
    }

    /**
     * 批量保存配置分组
     * 按类型批量保存配置，已存在的配置更新值，不存在的配置新建
     *
     * @param type      配置类型
     * @param configMap 配置键值对映射
     * @return 空结果
     */
    @Operation(summary = "批量保存配置分组")
    @PostMapping("/group/{type}")
    @OperationLog(operation = "批量保存配置分组", module = "系统配置")
    public Result<Void> saveConfigsByType(@PathVariable String type, @RequestBody Map<String, String> configMap) {
        configService.saveConfigsByType(type, configMap);
        return Result.success();
    }
}
