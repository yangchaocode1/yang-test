package com.admin.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpireTimeRequest {

    private LocalDateTime expireTime;
}
