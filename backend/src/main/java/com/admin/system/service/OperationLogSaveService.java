package com.admin.system.service;

import com.admin.system.entity.SysOperationLog;
import com.admin.system.mapper.SysOperationLogMapper;
import com.admin.system.security.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogSaveService {

    private final SysOperationLogMapper sysOperationLogMapper;
    private final ObjectMapper objectMapper;

    @Async("logExecutor")
    public void saveLog(ProceedingJoinPoint point, String operation, String module,
                        Object result, Throwable exception, long duration) {
        try {
            SysOperationLog logEntity = new SysOperationLog();
            logEntity.setOperation(module != null && !module.isEmpty() ? "[" + module + "]" + operation : operation);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
                LoginUser loginUser = (LoginUser) authentication.getPrincipal();
                logEntity.setUserId(loginUser.getSysUser().getId());
                logEntity.setUsername(loginUser.getUsername());
            }

            MethodSignature signature = (MethodSignature) point.getSignature();
            String className = point.getTarget().getClass().getName();
            String methodName = signature.getName();
            logEntity.setMethod(className + "." + methodName);

            String[] paramNames = signature.getParameterNames();
            Object[] args = point.getArgs();
            if (paramNames != null && args != null) {
                Map<String, Object> params = new HashMap<>();
                for (int i = 0; i < paramNames.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof MultipartFile) {
                        continue;
                    }
                    params.put(paramNames[i], arg);
                }
                logEntity.setParams(objectMapper.writeValueAsString(params));
            }

            if (exception == null && result != null) {
                String resultStr = objectMapper.writeValueAsString(result);
                if (resultStr.length() > 2000) {
                    resultStr = resultStr.substring(0, 2000) + "...";
                }
                logEntity.setResult(resultStr);
            } else if (exception != null) {
                logEntity.setResult("异常: " + exception.getMessage());
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntity.setIpAddress(getIpAddress(request));
            }

            logEntity.setDuration(duration);
            logEntity.setCreatedTime(LocalDateTime.now());
            sysOperationLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
