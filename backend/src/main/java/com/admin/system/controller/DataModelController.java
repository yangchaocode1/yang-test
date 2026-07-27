package com.admin.system.controller;

import com.admin.system.annotation.OperationLog;
import com.admin.system.common.result.Result;
import com.admin.system.dto.*;
import com.admin.system.service.DataModelService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据模型管理控制器
 * 提供数据模型的增删改查、引用关系查询等接口
 * 数据模型定义了业务数据的结构（包含字段定义），是动态业务数据管理的基础
 */
@Tag(name = "数据模型管理")
@RestController
@RequestMapping("/data-models")
@RequiredArgsConstructor
public class DataModelController {

    /** 数据模型服务，处理数据模型相关的业务逻辑 */
    private final DataModelService dataModelService;

    /**
     * 分页查询数据模型
     * 支持关键词搜索（模型编码、模型名称、描述）、状态筛选、排序
     *
     * @param request 查询条件，含关键词、状态、分页参数、排序参数
     * @return 分页数据模型列表（含字段定义）
     */
    @Operation(summary = "分页查询数据模型")
    @GetMapping
    @OperationLog(operation = "查询数据模型列表", module = "数据模型管理")
    public Result<IPage<DataModelVO>> page(DataModelQueryRequest request) {
        IPage<DataModelVO> page = dataModelService.page(request);
        return Result.success(page);
    }

    /**
     * 获取数据模型详情
     * 根据ID获取数据模型详细信息，包含所有字段定义
     *
     * @param id 数据模型ID
     * @return 数据模型详情（含字段列表）
     */
    @Operation(summary = "获取数据模型详情")
    @GetMapping("/{id}")
    @OperationLog(operation = "查询数据模型详情", module = "数据模型管理")
    public Result<DataModelVO> getById(@PathVariable Long id) {
        DataModelVO model = dataModelService.getById(id);
        return Result.success(model);
    }

    /**
     * 创建数据模型
     * 创建新的数据模型，可同时定义字段列表
     *
     * @param request 创建数据模型请求，包含模型编码、名称、表名、字段列表等
     * @return 创建后的数据模型信息
     */
    @Operation(summary = "创建数据模型")
    @PostMapping
    @OperationLog(operation = "创建数据模型", module = "数据模型管理")
    public Result<DataModelVO> create(@Valid @RequestBody DataModelCreateRequest request) {
        DataModelVO model = dataModelService.create(request);
        return Result.success(model);
    }

    /**
     * 更新数据模型
     * 更新数据模型信息，可修改名称、描述、字段列表（字段为全量替换）
     *
     * @param id      数据模型ID
     * @param request 更新数据模型请求
     * @return 更新后的数据模型信息
     */
    @Operation(summary = "更新数据模型")
    @PutMapping("/{id}")
    @OperationLog(operation = "更新数据模型", module = "数据模型管理")
    public Result<DataModelVO> update(@PathVariable Long id, @RequestBody DataModelUpdateRequest request) {
        DataModelVO model = dataModelService.update(id, request);
        return Result.success(model);
    }

    /**
     * 删除数据模型
     * 删除数据模型，同时删除关联的字段定义和数据记录
     *
     * @param id 数据模型ID
     * @return 空结果
     */
    @Operation(summary = "删除数据模型")
    @DeleteMapping("/{id}")
    @OperationLog(operation = "删除数据模型", module = "数据模型管理")
    public Result<Void> delete(@PathVariable Long id) {
        dataModelService.delete(id);
        return Result.success();
    }

    /**
     * 获取引用该模型的其他模型列表
     * 查询哪些数据模型的字段引用了当前模型（引用类型字段指向当前模型）
     *
     * @param modelId 数据模型ID
     * @return 引用该模型的模型列表，包含引用字段信息
     */
    @Operation(summary = "获取引用该模型的其他模型列表")
    @GetMapping("/{modelId}/referenced-by")
    @OperationLog(operation = "查询模型引用关系", module = "数据模型管理")
    public Result<List<ReferencedByVO>> getReferencedBy(@PathVariable Long modelId) {
        List<ReferencedByVO> list = dataModelService.getReferencedBy(modelId);
        return Result.success(list);
    }
}
