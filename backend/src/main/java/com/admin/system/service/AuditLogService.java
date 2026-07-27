package com.admin.system.service;

import com.admin.system.dto.AuditLogQueryRequest;
import com.admin.system.dto.AuditLogVO;
import com.admin.system.entity.SysAuditLog;
import com.admin.system.mapper.SysAuditLogMapper;
import com.admin.system.security.LoginUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final SysAuditLogMapper sysAuditLogMapper;
    private final ObjectMapper objectMapper;

    public Page<AuditLogVO> pageList(AuditLogQueryRequest request) {
        Page<SysAuditLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<>();

        if (request.getUserId() != null) {
            wrapper.eq(SysAuditLog::getUserId, request.getUserId());
        }
        if (request.getModule() != null && !request.getModule().isEmpty()) {
            wrapper.eq(SysAuditLog::getModule, request.getModule());
        }
        if (request.getOperation() != null && !request.getOperation().isEmpty()) {
            wrapper.eq(SysAuditLog::getOperation, request.getOperation());
        }
        if (request.getTargetType() != null && !request.getTargetType().isEmpty()) {
            wrapper.eq(SysAuditLog::getTargetType, request.getTargetType());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(SysAuditLog::getCreatedTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(SysAuditLog::getCreatedTime, request.getEndTime());
        }
        wrapper.orderByDesc(SysAuditLog::getCreatedTime);

        Page<SysAuditLog> logPage = sysAuditLogMapper.selectPage(page, wrapper);
        Page<AuditLogVO> voPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        voPage.setRecords(logPage.getRecords().stream().map(this::toVO).collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    public AuditLogVO getById(Long id) {
        SysAuditLog auditLog = sysAuditLogMapper.selectById(id);
        if (auditLog == null) {
            throw new com.admin.system.common.exception.BusinessException("审计日志不存在");
        }
        return toVO(auditLog);
    }

    @Async("logExecutor")
    public void log(String operation, String targetType, String targetId, Object oldValue, Object newValue) {
        try {
            SysAuditLog auditLog = new SysAuditLog();
            auditLog.setOperation(operation);
            auditLog.setModule(targetType);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                auditLog.setUserId(loginUser.getSysUser().getId());
                auditLog.setUsername(loginUser.getUsername());
            }

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(request.getRemoteAddr());
            }

            if (oldValue != null) {
                auditLog.setOldValue(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                auditLog.setNewValue(objectMapper.writeValueAsString(newValue));
            }

            sysAuditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
        }
    }

    private AuditLogVO toVO(SysAuditLog auditLog) {
        return AuditLogVO.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .operation(auditLog.getOperation())
                .module(auditLog.getModule())
                .targetType(auditLog.getTargetType())
                .targetId(auditLog.getTargetId())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .ipAddress(auditLog.getIpAddress())
                .createdTime(auditLog.getCreatedTime())
                .build();
    }
}
