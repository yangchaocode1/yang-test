package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务数据模型实体
 * 对应数据库表biz_data_model，定义动态业务数据的结构
 * 每个模型包含多个字段定义（BizDataField），可创建多条数据记录（BizDataRecord）
 */
@Data
@TableName("biz_data_model")
public class BizDataModel {

    /** 模型ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模型编码，唯一标识 */
    private String modelCode;

    /** 模型名称 */
    private String modelName;

    /** 模型描述 */
    private String description;

    /** 对应的数据库表名 */
    private String tableName;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    /** 逻辑删除标记：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
