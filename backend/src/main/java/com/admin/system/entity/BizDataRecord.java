package com.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务数据记录实体
 * 对应数据库表biz_data_record，存储动态业务数据的实际记录
 * 数据以JSON格式存储在dataJson字段中，结构由BizDataField定义
 */
@Data
@TableName("biz_data_record")
public class BizDataRecord {

    /** 记录ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属数据模型ID */
    private Long modelId;

    /** 数据内容（JSON格式），键为字段编码，值为字段值 */
    private String dataJson;

    /** 创建人ID */
    private Long createdBy;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新人ID */
    private Long updatedBy;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    /** 逻辑删除标记：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
