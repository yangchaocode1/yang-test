package com.admin.system.service;

import com.admin.system.mapper.BizDataModelMapper;
import com.admin.system.mapper.SysOperationLogMapper;
import com.admin.system.mapper.SysRoleMapper;
import com.admin.system.mapper.SysUserMapper;
import com.admin.system.vo.DashboardStatsVO;
import com.admin.system.vo.OperationLogVO;
import com.admin.system.entity.SysOperationLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final BizDataModelMapper bizDataModelMapper;
    private final SysOperationLogMapper sysOperationLogMapper;

    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();
        stats.setUserCount(sysUserMapper.selectCount(null));
        stats.setRoleCount(sysRoleMapper.selectCount(null));
        stats.setDataModelCount(bizDataModelMapper.selectCount(null));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SysOperationLog::getCreatedTime, todayStart)
                .le(SysOperationLog::getCreatedTime, todayEnd);
        stats.setTodayOperationCount(sysOperationLogMapper.selectCount(wrapper));

        return stats;
    }

    public List<OperationLogVO> getRecentLogs(int limit) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysOperationLog::getCreatedTime)
                .last("LIMIT " + limit);
        List<SysOperationLog> logs = sysOperationLogMapper.selectList(wrapper);
        return logs.stream().map(this::toVO).collect(Collectors.toList());
    }

    private OperationLogVO toVO(SysOperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
    }
}
