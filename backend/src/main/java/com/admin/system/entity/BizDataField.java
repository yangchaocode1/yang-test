package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务数据字段实体
 * 对应数据库表biz_data_field，定义数据模型中的字段
 * 支持多种字段类型：TEXT、NUMBER、DATE、SELECT、REFERENCE等
 */
@Data
@TableName("biz_data_field")
public class BizDataField {

    /** 字段ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属数据模型ID */
    private Long modelId;

    /** 字段编码，如name、age等 */
    private String fieldCode;

    /** 字段名称，如"姓名"、"年龄"等 */
    private String fieldName;

    /** 字段类型：TEXT-文本，NUMBER-数字，DATE-日期，SELECT-选择，REFERENCE-引用 */
    private String fieldType;

    /** 是否必填：0-否，1-是 */
    private Integer required;

    /** 是否唯一：0-否，1-是 */
    private Integer uniqueFlag;

    /** 引用的数据模型ID（字段类型为REFERENCE时使用） */
    private Long referenceModelId;

    /** 排序序号 */
    private Integer sortOrder;

    /** 选项列表（JSON格式，字段类型为SELECT时使用） */
    private String options;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
