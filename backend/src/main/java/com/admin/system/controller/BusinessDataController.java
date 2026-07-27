package com.admin.system.controller;

import com.admin.system.annotation.OperationLog;
import com.admin.system.common.result.Result;
import com.admin.system.dto.*;
import com.admin.system.service.BusinessDataService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 业务数据管理控制器
 * 提供业务数据的增删改查、批量删除、关联数据查询等接口
 * 业务数据是基于数据模型动态创建的数据记录，数据以JSON格式存储
 */
@Tag(name = "业务数据管理")
@RestController
@RequestMapping("/business-data")
@RequiredArgsConstructor
public class BusinessDataController {

    /** 业务数据服务，处理业务数据相关的业务逻辑 */
    private final BusinessDataService businessDataService;

    /**
     * 分页查询业务数据
     * 根据模型ID查询该模型下的业务数据，支持条件筛选和排序
     *
     * @param request 查询条件，含模型ID、筛选条件、分页参数、排序参数
     * @return 分页业务数据列表
     */
    @Operation(summary = "分页查询业务数据")
    @GetMapping
    @OperationLog(operation = "查询业务数据列表", module = "业务数据管理")
    public Result<IPage<BusinessDataVO>> page(BusinessDataQueryRequest request) {
        IPage<BusinessDataVO> page = businessDataService.page(request);
        return Result.success(page);
    }

    /**
     * 获取业务数据详情
     * 根据ID获取业务数据详细信息，包含模型名称和动态字段数据
     *
     * @param id 业务数据记录ID
     * @return 业务数据详情
     */
    @Operation(summary = "获取业务数据详情")
    @GetMapping("/{id}")
    @OperationLog(operation = "查询业务数据详情", module = "业务数据管理")
    public Result<BusinessDataVO> getById(@PathVariable Long id) {
        BusinessDataVO data = businessDataService.getById(id);
        return Result.success(data);
    }

    /**
     * 创建业务数据
     * 在指定模型下创建新的业务数据记录，会进行必填校验、类型校验、唯一性校验和引用校验
     *
     * @param request 创建业务数据请求，包含模型ID和动态字段数据
     * @return 创建后的业务数据信息
     */
    @Operation(summary = "创建业务数据")
    @PostMapping
    @OperationLog(operation = "创建业务数据", module = "业务数据管理")
    public Result<BusinessDataVO> create(@Valid @RequestBody BusinessDataCreateRequest request) {
        BusinessDataVO data = businessDataService.create(request);
        return Result.success(data);
    }

    /**
     * 更新业务数据
     * 更新业务数据记录，会进行必填校验、类型校验、唯一性校验和引用校验
     *
     * @param id      业务数据记录ID
     * @param request 更新业务数据请求，包含动态字段数据
     * @return 更新后的业务数据信息
     */
    @Operation(summary = "更新业务数据")
    @PutMapping("/{id}")
    @OperationLog(operation = "更新业务数据", module = "业务数据管理")
    public Result<BusinessDataVO> update(@PathVariable Long id, @RequestBody BusinessDataUpdateRequest request) {
        BusinessDataVO data = businessDataService.update(id, request);
        return Result.success(data);
    }

    /**
     * 删除业务数据
     * 根据ID删除业务数据记录
     *
     * @param id 业务数据记录ID
     * @return 空结果
     */
    @Operation(summary = "删除业务数据")
    @DeleteMapping("/{id}")
    @OperationLog(operation = "删除业务数据", module = "业务数据管理")
    public Result<Void> delete(@PathVariable Long id) {
        businessDataService.delete(id);
        return Result.success();
    }

    /**
     * 批量删除业务数据
     * 根据ID列表批量删除业务数据记录
     *
     * @param ids 业务数据记录ID列表
     * @return 空结果
     */
    @Operation(summary = "批量删除业务数据")
    @DeleteMapping("/batch")
    @OperationLog(operation = "批量删除业务数据", module = "业务数据管理")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        businessDataService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 获取数据的关联数据
     * 查询指定业务数据的引用关系，包括该数据引用的其他数据和被其他数据引用的关系
     *
     * @param id 业务数据记录ID
     * @return 关联数据列表
     */
    @Operation(summary = "获取数据的关联数据")
    @GetMapping("/{id}/references")
    @OperationLog(operation = "查询数据关联关系", module = "业务数据管理")
    public Result<List<ReferenceDataVO>> getReferences(@PathVariable Long id) {
        List<ReferenceDataVO> references = businessDataService.getReferences(id);
        return Result.success(references);
    }
}
