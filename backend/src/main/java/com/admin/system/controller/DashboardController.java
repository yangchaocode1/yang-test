package com.admin.system.controller;

import com.admin.system.annotation.OperationLog;
import com.admin.system.common.result.Result;
import com.admin.system.service.DashboardService;
import com.admin.system.vo.DashboardStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘控制器
 * 提供仪表盘统计数据查询接口，用于首页展示系统概览信息
 */
@Tag(name = "仪表盘", description = "仪表盘统计数据接口")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /** 仪表盘服务，处理仪表盘统计数据的业务逻辑 */
    private final DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     * 返回系统概览统计信息，包括用户总数、角色总数、数据模型总数、今日操作次数
     *
     * @return 仪表盘统计数据
     */
    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    @OperationLog(operation = "查询仪表盘统计", module = "仪表盘")
    public Result<DashboardStatsVO> getStats() {
        return Result.success(dashboardService.getStats());
    }
}
