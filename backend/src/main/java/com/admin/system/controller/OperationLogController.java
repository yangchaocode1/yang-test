package com.admin.system.controller;

import com.admin.system.annotation.OperationLog;
import com.admin.system.common.result.Result;
import com.admin.system.dto.OperationLogQueryRequest;
import com.admin.system.service.DashboardService;
import com.admin.system.service.OperationLogService;
import com.admin.system.vo.OperationLogVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 操作日志管理控制器
 * 提供操作日志的分页查询、详情查询、导出Excel、最近操作日志查询等接口
 */
@Tag(name = "操作日志管理", description = "操作日志查询与导出接口")
@RestController
@RequestMapping("/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    /** 操作日志服务，处理操作日志的查询和导出业务逻辑 */
    private final OperationLogService operationLogService;

    /** 仪表盘服务，用于查询最近操作日志 */
    private final DashboardService dashboardService;

    /**
     * 分页查询操作日志
     * 支持按用户ID、用户名、操作类型、模块、时间范围等条件筛选
     *
     * @param request 查询条件，含用户ID、用户名、操作、模块、时间范围、分页参数
     * @return 分页操作日志列表
     */
    @Operation(summary = "分页查询操作日志")
    @GetMapping
    @OperationLog(operation = "查询操作日志", module = "操作日志")
    public Result<IPage<OperationLogVO>> pageQuery(OperationLogQueryRequest request) {
        return Result.success(operationLogService.pageQuery(request));
    }

    /**
     * 获取日志详情
     * 根据ID获取操作日志的详细信息
     *
     * @param id 操作日志ID
     * @return 操作日志详情
     */
    @Operation(summary = "获取日志详情")
    @GetMapping("/{id}")
    @OperationLog(operation = "查询日志详情", module = "操作日志")
    public Result<OperationLogVO> getById(@PathVariable Long id) {
        return Result.success(operationLogService.getById(id));
    }

    /**
     * 导出操作日志
     * 根据查询条件导出操作日志为Excel文件
     *
     * @param request  查询条件
     * @param response HTTP响应，用于写入Excel文件流
     * @throws IOException 文件写入异常
     */
    @Operation(summary = "导出操作日志")
    @GetMapping("/export")
    @OperationLog(operation = "导出操作日志", module = "操作日志")
    public void exportExcel(OperationLogQueryRequest request, HttpServletResponse response) throws IOException {
        operationLogService.exportExcel(request, response);
    }

    /**
     * 获取最近操作日志
     * 查询最近N条操作日志，用于仪表盘展示
     *
     * @param limit 返回条数，默认10
     * @return 最近的操作日志列表
     */
    @Operation(summary = "获取最近操作日志")
    @GetMapping("/recent")
    @OperationLog(operation = "查询最近操作日志", module = "操作日志")
    public Result<List<OperationLogVO>> getRecentLogs(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(dashboardService.getRecentLogs(limit));
    }
}
