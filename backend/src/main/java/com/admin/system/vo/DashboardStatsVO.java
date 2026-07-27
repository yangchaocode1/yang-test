package com.admin.system.vo;

import lombok.Data;

@Data
public class DashboardStatsVO {

    private Long userCount;

    private Long roleCount;

    private Long dataModelCount;

    private Long todayOperationCount;
}
