package com.admin.system.controller;

import com.admin.system.annotation.OperationLog;
import com.admin.system.common.result.Result;
import com.admin.system.dto.AuditLogQueryRequest;
import com.admin.system.dto.AuditLogVO;
import com.admin.system.service.AuditLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 审计日志管理控制器
 * 提供审计日志的分页查询和详情查询接口，支持按用户、模块、操作类型、时间范围等条件筛选
 */
@Tag(name = "审计日志管理", description = "审计日志查询接口")
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    /** 审计日志服务，处理审计日志的查询业务逻辑 */
    private final AuditLogService auditLogService;

    /**
     * 分页查询审计日志
     * 支持按用户ID、模块、操作类型、目标类型、时间范围等条件筛选
     *
     * @param userId     操作用户ID（可选）
     * @param module     操作模块（可选）
     * @param operation  操作类型（可选）
     * @param targetType 目标类型（可选）
     * @param startTime  开始时间（可选）
     * @param endTime    结束时间（可选）
     * @param pageNum    页码，默认1
     * @param pageSize   每页条数，默认10
     * @return 分页审计日志列表
     */
    @Operation(summary = "分页查询审计日志")
    @GetMapping
    @OperationLog(operation = "查询审计日志", module = "审计日志")
    public Result<Page<AuditLogVO>> pageList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 组装查询条件
        AuditLogQueryRequest request = new AuditLogQueryRequest();
        request.setUserId(userId);
        request.setModule(module);
        request.setOperation(operation);
        request.setTargetType(targetType);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        return Result.success(auditLogService.pageList(request));
    }

    /**
     * 获取审计日志详情
     * 根据ID获取审计日志的详细信息
     *
     * @param id 审计日志ID
     * @return 审计日志详情
     */
    @Operation(summary = "获取审计日志详情")
    @GetMapping("/{id}")
    @OperationLog(operation = "查询审计日志详情", module = "审计日志")
    public Result<AuditLogVO> getById(@PathVariable Long id) {
        return Result.success(auditLogService.getById(id));
    }
}
