package com.admin.system.service;

import com.admin.system.common.exception.BusinessException;
import com.admin.system.dto.*;
import com.admin.system.entity.BizDataField;
import com.admin.system.entity.BizDataModel;
import com.admin.system.entity.BizDataRecord;
import com.admin.system.mapper.BizDataFieldMapper;
import com.admin.system.mapper.BizDataModelMapper;
import com.admin.system.mapper.BizDataRecordMapper;
import com.admin.system.security.LoginUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessDataService {

    private final BizDataRecordMapper bizDataRecordMapper;
    private final BizDataModelMapper bizDataModelMapper;
    private final BizDataFieldMapper bizDataFieldMapper;
    private final ObjectMapper objectMapper;

    public IPage<BusinessDataVO> page(BusinessDataQueryRequest request) {
        if (request.getModelId() == null) {
            throw new BusinessException("模型ID不能为空");
        }

        BizDataModel model = bizDataModelMapper.selectById(request.getModelId());
        if (model == null) {
            throw new BusinessException("数据模型不存在");
        }

        List<BizDataField> fields = bizDataFieldMapper.selectList(
                new LambdaQueryWrapper<BizDataField>()
                        .eq(BizDataField::getModelId, request.getModelId())
                        .orderByAsc(BizDataField::getSortOrder));

        Page<BizDataRecord> page = new Page<>(request.getPageNum(), request.getPageSize());

        if ("ASC".equalsIgnoreCase(request.getOrderDirection())) {
            page.addOrder(OrderItem.asc(request.getOrderBy()));
        } else {
            page.addOrder(OrderItem.desc(request.getOrderBy()));
        }

        LambdaQueryWrapper<BizDataRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDataRecord::getModelId, request.getModelId());

        IPage<BizDataRecord> recordPage = bizDataRecordMapper.selectPage(page, wrapper);

        if (request.getConditions() != null && !request.getConditions().isEmpty()) {
            List<BusinessDataVO> filteredRecords = recordPage.getRecords().stream()
                    .map(record -> toBusinessDataVO(record, model, fields))
                    .filter(vo -> matchesConditions(vo.getData(), request.getConditions(), fields))
                    .collect(Collectors.toList());

            Page<BusinessDataVO> resultPage = new Page<>(request.getPageNum(), request.getPageSize());
            resultPage.setTotal(filteredRecords.size());
            resultPage.setRecords(filteredRecords);
            return resultPage;
        }

        return recordPage.convert(record -> toBusinessDataVO(record, model, fields));
    }

    public BusinessDataVO getById(Long id) {
        BizDataRecord record = bizDataRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("业务数据不存在");
        }

        BizDataModel model = bizDataModelMapper.selectById(record.getModelId());
        if (model == null) {
            throw new BusinessException("数据模型不存在");
        }

        List<BizDataField> fields = bizDataFieldMapper.selectList(
                new LambdaQueryWrapper<BizDataField>()
                        .eq(BizDataField::getModelId, record.getModelId())
                        .orderByAsc(BizDataField::getSortOrder));

        return toBusinessDataVO(record, model, fields);
    }

    @Transactional
    public BusinessDataVO create(BusinessDataCreateRequest request) {
        BizDataModel model = bizDataModelMapper.selectById(request.getModelId());
        if (model == null) {
            throw new BusinessException("数据模型不存在");
        }

        List<BizDataField> fields = bizDataFieldMapper.selectList(
                new LambdaQueryWrapper<BizDataField>()
                        .eq(BizDataField::getModelId, request.getModelId())
                        .orderByAsc(BizDataField::getSortOrder));

        Map<String, Object> data = request.getData() != null ? request.getData() : new HashMap<>();

        validateRequired(fields, data);
        validateFieldType(fields, data);
        validateUnique(request.getModelId(), fields, data, null);
        validateReference(fields, data);

        BizDataRecord record = new BizDataRecord();
        record.setModelId(request.getModelId());
        record.setCreatedBy(getCurrentUserId());
        record.setCreatedTime(LocalDateTime.now());
        record.setUpdatedBy(getCurrentUserId());
        record.setUpdatedTime(LocalDateTime.now());

        try {
            record.setDataJson(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据序列化失败");
        }

        bizDataRecordMapper.insert(record);

        return getById(record.getId());
    }

    @Transactional
    public BusinessDataVO update(Long id, BusinessDataUpdateRequest request) {
        BizDataRecord record = bizDataRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("业务数据不存在");
        }

        List<BizDataField> fields = bizDataFieldMapper.selectList(
                new LambdaQueryWrapper<BizDataField>()
                        .eq(BizDataField::getModelId, record.getModelId())
                        .orderByAsc(BizDataField::getSortOrder));

        Map<String, Object> data = request.getData() != null ? request.getData() : new HashMap<>();

        validateRequired(fields, data);
        validateFieldType(fields, data);
        validateUnique(record.getModelId(), fields, data, id);
        validateReference(fields, data);

        record.setUpdatedBy(getCurrentUserId());
        record.setUpdatedTime(LocalDateTime.now());

        try {
            record.setDataJson(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据序列化失败");
        }

        bizDataRecordMapper.updateById(record);

        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        BizDataRecord record = bizDataRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("业务数据不存在");
        }
        bizDataRecordMapper.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的数据");
        }
        bizDataRecordMapper.deleteBatchIds(ids);
    }

    public List<ReferenceDataVO> getReferences(Long id) {
        BizDataRecord record = bizDataRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("业务数据不存在");
        }

        List<BizDataField> fields = bizDataFieldMapper.selectList(
                new LambdaQueryWrapper<BizDataField>()
                        .eq(BizDataField::getModelId, record.getModelId()));

        List<ReferenceDataVO> references = new ArrayList<>();

        List<BizDataField> referenceFields = fields.stream()
                .filter(f -> "REFERENCE".equals(f.getFieldType()) && f.getReferenceModelId() != null)
                .collect(Collectors.toList());

        Map<String, Object> data = parseDataJson(record.getDataJson());

        for (BizDataField field : referenceFields) {
            Object refIdObj = data.get(field.getFieldCode());
            if (refIdObj != null) {
                Long refId = null;
                if (refIdObj instanceof Number) {
                    refId = ((Number) refIdObj).longValue();
                } else if (refIdObj instanceof String) {
                    try {
                        refId = Long.parseLong((String) refIdObj);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }

                if (refId != null) {
                    BizDataRecord refRecord = bizDataRecordMapper.selectById(refId);
                    if (refRecord != null) {
                        BizDataModel refModel = bizDataModelMapper.selectById(refRecord.getModelId());
                        if (refModel != null) {
                            ReferenceDataVO refVO = new ReferenceDataVO();
                            refVO.setId(refRecord.getId());
                            refVO.setModelId(refModel.getId());
                            refVO.setModelName(refModel.getModelName());
                            refVO.setDisplayValue(getDisplayValue(refRecord, refModel));
                            refVO.setData(parseDataJson(refRecord.getDataJson()));
                            references.add(refVO);
                        }
                    }
                }
            }
        }

        List<BizDataField> allReferenceFields = bizDataFieldMapper.selectList(
                new LambdaQueryWrapper<BizDataField>()
                        .eq(BizDataField::getReferenceModelId, record.getModelId()));

        for (BizDataField refField : allReferenceFields) {
            List<BizDataRecord> refRecords = bizDataRecordMapper.selectList(
                    new LambdaQueryWrapper<BizDataRecord>()
                            .eq(BizDataRecord::getModelId, refField.getModelId()));

            for (BizDataRecord refRecord : refRecords) {
                Map<String, Object> refData = parseDataJson(refRecord.getDataJson());
                Object refIdObj = refData.get(refField.getFieldCode());
                if (refIdObj != null) {
                    Long refId = null;
                    if (refIdObj instanceof Number) {
                        refId = ((Number) refIdObj).longValue();
                    } else if (refIdObj instanceof String) {
                        try {
                            refId = Long.parseLong((String) refIdObj);
                        } catch (NumberFormatException e) {
                            continue;
                        }
                    }

                    if (id.equals(refId)) {
                        BizDataModel refModel = bizDataModelMapper.selectById(refRecord.getModelId());
                        if (refModel != null) {
                            ReferenceDataVO refVO = new ReferenceDataVO();
                            refVO.setId(refRecord.getId());
                            refVO.setModelId(refModel.getId());
                            refVO.setModelName(refModel.getModelName());
                            refVO.setDisplayValue(getDisplayValue(refRecord, refModel));
                            refVO.setData(refData);
                            references.add(refVO);
                        }
                    }
                }
            }
        }

        return references;
    }

    public List<ReferencedByVO> getReferencedBy(Long modelId) {
        BizDataModel model = bizDataModelMapper.selectById(modelId);
        if (model == null) {
            throw new BusinessException("数据模型不存在");
        }

        List<BizDataField> referenceFields = bizDataFieldMapper.selectList(
                new LambdaQueryWrapper<BizDataField>()
                        .eq(BizDataField::getReferenceModelId, modelId));

        if (referenceFields.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> modelIds = referenceFields.stream()
                .map(BizDataField::getModelId)
                .distinct()
                .collect(Collectors.toList());

        List<BizDataModel> models = bizDataModelMapper.selectBatchIds(modelIds);

        return models.stream().map(m -> {
            ReferencedByVO vo = new ReferencedByVO();
            vo.setModelId(m.getId());
            vo.setModelCode(m.getModelCode());
            vo.setModelName(m.getModelName());

            List<ReferencedByVO.FieldReferenceInfo> fieldInfos = referenceFields.stream()
                    .filter(f -> f.getModelId().equals(m.getId()))
                    .map(f -> {
                        ReferencedByVO.FieldReferenceInfo info = new ReferencedByVO.FieldReferenceInfo();
                        info.setFieldId(f.getId());
                        info.setFieldCode(f.getFieldCode());
                        info.setFieldName(f.getFieldName());
                        return info;
                    })
                    .collect(Collectors.toList());
            vo.setFields(fieldInfos);

            return vo;
        }).collect(Collectors.toList());
    }

    private void validateRequired(List<BizDataField> fields, Map<String, Object> data) {
        for (BizDataField field : fields) {
            if (field.getRequired() != null && field.getRequired() == 1) {
                Object value = data.get(field.getFieldCode());
                if (value == null || (value instanceof String && ((String) value).isBlank())) {
                    throw new BusinessException("字段[" + field.getFieldName() + "]不能为空");
                }
            }
        }
    }

    private void validateUnique(Long modelId, List<BizDataField> fields, Map<String, Object> data, Long excludeId) {
        List<BizDataField> uniqueFields = fields.stream()
                .filter(f -> f.getUniqueFlag() != null && f.getUniqueFlag() == 1)
                .collect(Collectors.toList());

        for (BizDataField field : uniqueFields) {
            Object value = data.get(field.getFieldCode());
            if (value != null) {
                List<BizDataRecord> records = bizDataRecordMapper.selectList(
                        new LambdaQueryWrapper<BizDataRecord>()
                                .eq(BizDataRecord::getModelId, modelId));

                for (BizDataRecord record : records) {
                    if (excludeId != null && record.getId().equals(excludeId)) {
                        continue;
                    }

                    Map<String, Object> recordData = parseDataJson(record.getDataJson());
                    Object recordValue = recordData.get(field.getFieldCode());

                    if (value.equals(recordValue)) {
                        throw new BusinessException("字段[" + field.getFieldName() + "]的值已存在");
                    }
                }
            }
        }
    }

    private void validateFieldType(List<BizDataField> fields, Map<String, Object> data) {
        for (BizDataField field : fields) {
            Object value = data.get(field.getFieldCode());
            if (value == null || (value instanceof String && ((String) value).isBlank())) {
                continue;
            }

            String fieldType = field.getFieldType();
            String strValue = value.toString();

            switch (fieldType) {
                case "NUMBER":
                    try {
                        Double.parseDouble(strValue);
                    } catch (NumberFormatException e) {
                        throw new BusinessException("字段[" + field.getFieldName() + "]必须是数字");
                    }
                    break;
                case "DATE":
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        java.time.LocalDate.parse(strValue, formatter);
                    } catch (Exception e) {
                        throw new BusinessException("字段[" + field.getFieldName() + "]日期格式不正确，应为yyyy-MM-dd");
                    }
                    break;
                case "SELECT":
                    if (field.getOptions() != null && !field.getOptions().isBlank()) {
                        try {
                            List<String> options = objectMapper.readValue(field.getOptions(), new TypeReference<List<String>>() {});
                            if (!options.contains(strValue)) {
                                throw new BusinessException("字段[" + field.getFieldName() + "]的值不在选项范围内");
                            }
                        } catch (JsonProcessingException e) {
                            throw new BusinessException("字段[" + field.getFieldName() + "]选项配置错误");
                        }
                    }
                    break;
            }
        }
    }

    private void validateReference(List<BizDataField> fields, Map<String, Object> data) {
        List<BizDataField> referenceFields = fields.stream()
                .filter(f -> "REFERENCE".equals(f.getFieldType()) && f.getReferenceModelId() != null)
                .collect(Collectors.toList());

        for (BizDataField field : referenceFields) {
            Object value = data.get(field.getFieldCode());
            if (value != null) {
                Long refId = null;
                if (value instanceof Number) {
                    refId = ((Number) value).longValue();
                } else if (value instanceof String) {
                    try {
                        refId = Long.parseLong((String) value);
                    } catch (NumberFormatException e) {
                        throw new BusinessException("字段[" + field.getFieldName() + "]引用ID格式不正确");
                    }
                }

                if (refId != null) {
                    BizDataRecord refRecord = bizDataRecordMapper.selectById(refId);
                    if (refRecord == null) {
                        throw new BusinessException("字段[" + field.getFieldName() + "]引用的数据不存在");
                    }
                    if (!refRecord.getModelId().equals(field.getReferenceModelId())) {
                        throw new BusinessException("字段[" + field.getFieldName() + "]引用的数据类型不匹配");
                    }
                }
            }
        }
    }

    private boolean matchesConditions(Map<String, Object> data, List<QueryCondition> conditions, List<BizDataField> fields) {
        for (QueryCondition condition : conditions) {
            Object value = data.get(condition.getFieldCode());
            String condValue = condition.getValue();

            if (value == null) {
                return false;
            }

            String strValue = value.toString();
            String operator = condition.getOperator();

            switch (operator) {
                case "EQ":
                    if (!strValue.equals(condValue)) {
                        return false;
                    }
                    break;
                case "NE":
                    if (strValue.equals(condValue)) {
                        return false;
                    }
                    break;
                case "LIKE":
                    if (!strValue.contains(condValue)) {
                        return false;
                    }
                    break;
                case "GT":
                    if (strValue.compareTo(condValue) <= 0) {
                        return false;
                    }
                    break;
                case "GE":
                    if (strValue.compareTo(condValue) < 0) {
                        return false;
                    }
                    break;
                case "LT":
                    if (strValue.compareTo(condValue) >= 0) {
                        return false;
                    }
                    break;
                case "LE":
                    if (strValue.compareTo(condValue) > 0) {
                        return false;
                    }
                    break;
                case "BETWEEN":
                    if (condition.getValueTo() == null) {
                        return false;
                    }
                    if (strValue.compareTo(condValue) < 0 || strValue.compareTo(condition.getValueTo()) > 0) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private BusinessDataVO toBusinessDataVO(BizDataRecord record, BizDataModel model, List<BizDataField> fields) {
        BusinessDataVO vo = new BusinessDataVO();
        vo.setId(record.getId());
        vo.setModelId(record.getModelId());
        vo.setModelName(model.getModelName());
        vo.setData(parseDataJson(record.getDataJson()));
        vo.setCreatedTime(record.getCreatedTime());
        vo.setUpdatedTime(record.getUpdatedTime());
        return vo;
    }

    private Map<String, Object> parseDataJson(String dataJson) {
        if (dataJson == null || dataJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private String getDisplayValue(BizDataRecord record, BizDataModel model) {
        Map<String, Object> data = parseDataJson(record.getDataJson());
        if (data.isEmpty()) {
            return model.getModelName() + "-" + record.getId();
        }

        List<BizDataField> fields = bizDataFieldMapper.selectList(
                new LambdaQueryWrapper<BizDataField>()
                        .eq(BizDataField::getModelId, model.getId())
                        .orderByAsc(BizDataField::getSortOrder));

        if (!fields.isEmpty()) {
            BizDataField firstField = fields.get(0);
            Object value = data.get(firstField.getFieldCode());
            if (value != null) {
                return value.toString();
            }
        }

        return model.getModelName() + "-" + record.getId();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getSysUser().getId();
        }
        return null;
    }
}
