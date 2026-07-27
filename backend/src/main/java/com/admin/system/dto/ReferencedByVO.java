package com.admin.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReferencedByVO {

    private Long modelId;
    private String modelCode;
    private String modelName;
    private List<FieldReferenceInfo> fields;

    @Data
    public static class FieldReferenceInfo {
        private Long fieldId;
        private String fieldCode;
        private String fieldName;
    }
}
