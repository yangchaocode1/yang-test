package com.admin.system.service;

import com.admin.system.dto.OperationLogQueryRequest;
import com.admin.system.entity.SysOperationLog;
import com.admin.system.mapper.SysOperationLogMapper;
import com.admin.system.vo.OperationLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final SysOperationLogMapper sysOperationLogMapper;

    public IPage<OperationLogVO> pageQuery(OperationLogQueryRequest request) {
        Page<SysOperationLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (request.getUserId() != null) {
            wrapper.eq(SysOperationLog::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(SysOperationLog::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getOperation())) {
            wrapper.like(SysOperationLog::getOperation, request.getOperation());
        }
        if (StringUtils.hasText(request.getModule())) {
            wrapper.like(SysOperationLog::getOperation, "[" + request.getModule() + "]");
        }
        if (request.getStartTime() != null) {
            wrapper.ge(SysOperationLog::getCreatedTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(SysOperationLog::getCreatedTime, request.getEndTime());
        }
        wrapper.orderByDesc(SysOperationLog::getCreatedTime);
        IPage<SysOperationLog> logPage = sysOperationLogMapper.selectPage(page, wrapper);
        return logPage.convert(this::toVO);
    }

    public OperationLogVO getById(Long id) {
        SysOperationLog log = sysOperationLogMapper.selectById(id);
        if (log == null) {
            throw new com.admin.system.common.exception.BusinessException("日志不存在");
        }
        return toVO(log);
    }

    public void exportExcel(OperationLogQueryRequest request, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (request.getUserId() != null) {
            wrapper.eq(SysOperationLog::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(SysOperationLog::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getOperation())) {
            wrapper.like(SysOperationLog::getOperation, request.getOperation());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(SysOperationLog::getCreatedTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(SysOperationLog::getCreatedTime, request.getEndTime());
        }
        wrapper.orderByDesc(SysOperationLog::getCreatedTime);
        List<SysOperationLog> logs = sysOperationLogMapper.selectList(wrapper);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("操作日志");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "用户ID", "用户名", "操作", "方法", "参数", "结果", "IP地址", "耗时(ms)", "操作时间"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < logs.size(); i++) {
            SysOperationLog log = logs.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(log.getId());
            row.createCell(1).setCellValue(log.getUserId() != null ? log.getUserId() : 0);
            row.createCell(2).setCellValue(log.getUsername() != null ? log.getUsername() : "");
            row.createCell(3).setCellValue(log.getOperation() != null ? log.getOperation() : "");
            row.createCell(4).setCellValue(log.getMethod() != null ? log.getMethod() : "");
            row.createCell(5).setCellValue(log.getParams() != null ? log.getParams() : "");
            row.createCell(6).setCellValue(log.getResult() != null ? log.getResult() : "");
            row.createCell(7).setCellValue(log.getIpAddress() != null ? log.getIpAddress() : "");
            row.createCell(8).setCellValue(log.getDuration() != null ? log.getDuration() : 0);
            row.createCell(9).setCellValue(log.getCreatedTime() != null ? log.getCreatedTime().format(fmt) : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        String fileName = URLEncoder.encode("操作日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private OperationLogVO toVO(SysOperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
    }
}
