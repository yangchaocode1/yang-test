package com.admin.system.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class DataModelUpdateRequest {

    private String modelName;

    private String description;

    @Valid
    private List<DataFieldRequest> fields;
}
