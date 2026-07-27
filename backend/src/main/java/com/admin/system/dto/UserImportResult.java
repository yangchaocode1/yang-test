package com.admin.system.dto;

import lombok.Data;

@Data
public class UserImportResult {

    private int totalCount;
    private int successCount;
    private int failCount;
    private java.util.List<FailDetail> failDetails;

    @Data
    public static class FailDetail {
        private int rowIndex;
        private String username;
        private String reason;

        public FailDetail(int rowIndex, String username, String reason) {
            this.rowIndex = rowIndex;
            this.username = username;
            this.reason = reason;
        }
    }
}
