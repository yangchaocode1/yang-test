package com.admin.system.controller;

import com.admin.system.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查控制器
 * 提供应用健康状态检查接口，用于监控和负载均衡健康探测
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查接口
     * 返回应用的健康状态、应用名称和版本号
     *
     * @return 健康状态信息，包含status、application、version
     */
    @GetMapping
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
                "status", "UP",
                "application", "admin-system",
                "version", "1.0.2",
                "deployTime", java.time.LocalDateTime.now().toString()
        ));
    }
}
