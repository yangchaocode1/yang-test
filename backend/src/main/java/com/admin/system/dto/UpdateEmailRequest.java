package com.admin.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改邮箱请求
 * 仅允许更新邮箱字段，避免误传其他字段
 */
@Data
public class UpdateEmailRequest {

    /** 新邮箱地址 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
