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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataModelService {

    private final BizDataModelMapper bizDataModelMapper;
    private final BizDataFieldMapper bizDataFieldMapper;
    private final BizDataRecordMapper bizDataRecordMapper;
    private final ObjectMapper objectMapper;

    public IPage<DataModelVO> page(DataModelQueryRequest request) {
        Page<BizDataModel> page = new Page<>(request.getPageNum(), request.getPageSize());

        if ("ASC".equalsIgnoreCase(request.getOrderDirection())) {
            page.addOrder(OrderItem.asc(request.getOrderBy()));
        } else {
            page.addOrder(OrderItem.desc(request.getOrderBy()));
        }

        LambdaQueryWrapper<BizDataModel> wrapper = new LambdaQueryWrapper<>();
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            wrapper.and(w -> w
                    .like(BizDataModel::getModelCode, request.getKeyword())
                    .or().like(BizDataModel::getModelName, request.getKeyword())
                    .or().like(BizDataModel::getDescription, request.getKeyword()));
        }
        if (request.getStatus() != null) {
            wrapper.eq(BizDataModel::getStatus, request.getStatus());
        }

        IPage<BizDataModel> modelPage = bizDataModelMapper.selectPage(page, wrapper);
        return modelPage.convert(model -> {
            List<BizDataField> fields = bizDataFieldMapper.selectList(
                    new LambdaQueryWrapper<BizDataField>()
                            .eq(BizDataField::getModelId, model.getId())
                            .orderByAsc(BizDataField::getSortOrder));
            return toModelVO(model, fields);
        });
    }

    public DataModelVO getById(Long id) {
        BizDataModel model = bizDataModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException("数据模型不存在");
        }
        List<BizDataField> fields = bizDataFieldMapper.selectList(
                new LambdaQueryWrapper<BizDataField>()
                        .eq(BizDataField::getModelId, id)
                        .orderByAsc(BizDataField::getSortOrder));
        return toModelVO(model, fields);
    }

    @Transactional
    public DataModelVO create(DataModelCreateRequest request) {
        Long count = bizDataModelMapper.selectCount(
                new LambdaQueryWrapper<BizDataModel>()
                        .eq(BizDataModel::getModelCode, request.getModelCode()));
        if (count > 0) {
            throw new BusinessException("模型编码已存在");
        }

        BizDataModel model = new BizDataModel();
        model.setModelCode(request.getModelCode());
        model.setModelName(request.getModelName());
        model.setDescription(request.getDescription());
        model.setTableName(request.getTableName());
        model.setStatus(1);
        model.setCreatedTime(LocalDateTime.now());
        model.setUpdatedTime(LocalDateTime.now());

        bizDataModelMapper.insert(model);

        if (request.getFields() != null && !request.getFields().isEmpty()) {
            createFields(model.getId(), request.getFields());
        }

        return getById(model.getId());
    }

    @Transactional
    public DataModelVO update(Long id, DataModelUpdateRequest request) {
        BizDataModel model = bizDataModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException("数据模型不存在");
        }

        if (request.getModelName() != null) {
            model.setModelName(request.getModelName());
        }
        if (request.getDescription() != null) {
            model.setDescription(request.getDescription());
        }
        model.setUpdatedTime(LocalDateTime.now());

        bizDataModelMapper.updateById(model);

        if (request.getFields() != null) {
            bizDataFieldMapper.delete(
                    new LambdaQueryWrapper<BizDataField>().eq(BizDataField::getModelId, id));
            if (!request.getFields().isEmpty()) {
                createFields(id, request.getFields());
            }
        }

        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        BizDataModel model = bizDataModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException("数据模型不存在");
        }

        bizDataModelMapper.deleteById(id);
        bizDataFieldMapper.delete(
                new LambdaQueryWrapper<BizDataField>().eq(BizDataField::getModelId, id));
        bizDataRecordMapper.delete(
                new LambdaQueryWrapper<BizDataRecord>().eq(BizDataRecord::getModelId, id));
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

    private void createFields(Long modelId, List<DataFieldRequest> fieldRequests) {
        for (DataFieldRequest fieldRequest : fieldRequests) {
            BizDataField field = new BizDataField();
            field.setModelId(modelId);
            field.setFieldCode(fieldRequest.getFieldCode());
            field.setFieldName(fieldRequest.getFieldName());
            field.setFieldType(fieldRequest.getFieldType());
            field.setRequired(fieldRequest.getRequired() != null ? fieldRequest.getRequired() : 0);
            field.setUniqueFlag(fieldRequest.getUniqueFlag() != null ? fieldRequest.getUniqueFlag() : 0);
            field.setReferenceModelId(fieldRequest.getReferenceModelId());
            field.setSortOrder(fieldRequest.getSortOrder() != null ? fieldRequest.getSortOrder() : 0);

            if (fieldRequest.getOptions() != null && !fieldRequest.getOptions().isEmpty()) {
                try {
                    field.setOptions(objectMapper.writeValueAsString(fieldRequest.getOptions()));
                } catch (JsonProcessingException e) {
                    throw new BusinessException("选项数据序列化失败");
                }
            }

            field.setCreatedTime(LocalDateTime.now());
            field.setUpdatedTime(LocalDateTime.now());
            bizDataFieldMapper.insert(field);
        }
    }

    private DataModelVO toModelVO(BizDataModel model, List<BizDataField> fields) {
        DataModelVO vo = new DataModelVO();
        vo.setId(model.getId());
        vo.setModelCode(model.getModelCode());
        vo.setModelName(model.getModelName());
        vo.setDescription(model.getDescription());
        vo.setTableName(model.getTableName());
        vo.setStatus(model.getStatus());
        vo.setCreatedTime(model.getCreatedTime());
        vo.setUpdatedTime(model.getUpdatedTime());

        if (fields != null && !fields.isEmpty()) {
            vo.setFields(fields.stream().map(this::toFieldVO).collect(Collectors.toList()));
        } else {
            vo.setFields(new ArrayList<>());
        }

        return vo;
    }

    private DataFieldVO toFieldVO(BizDataField field) {
        DataFieldVO vo = new DataFieldVO();
        vo.setId(field.getId());
        vo.setModelId(field.getModelId());
        vo.setFieldCode(field.getFieldCode());
        vo.setFieldName(field.getFieldName());
        vo.setFieldType(field.getFieldType());
        vo.setRequired(field.getRequired());
        vo.setUniqueFlag(field.getUniqueFlag());
        vo.setReferenceModelId(field.getReferenceModelId());
        vo.setSortOrder(field.getSortOrder());
        vo.setCreatedTime(field.getCreatedTime());
        vo.setUpdatedTime(field.getUpdatedTime());

        if (field.getReferenceModelId() != null) {
            BizDataModel refModel = bizDataModelMapper.selectById(field.getReferenceModelId());
            if (refModel != null) {
                vo.setReferenceModelName(refModel.getModelName());
            }
        }

        if (field.getOptions() != null && !field.getOptions().isBlank()) {
            try {
                vo.setOptions(objectMapper.readValue(field.getOptions(), new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                vo.setOptions(new ArrayList<>());
            }
        } else {
            vo.setOptions(new ArrayList<>());
        }

        return vo;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getSysUser().getId();
        }
        return null;
    }
}
