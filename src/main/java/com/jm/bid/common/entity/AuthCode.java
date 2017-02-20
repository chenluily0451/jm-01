package com.jm.bid.common.entity;

import com.jm.bid.common.service.AuthCodeType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 16/7/26.
 */
@Data
public class AuthCode {

    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    private String phone;

    private String code;

    private Long userId;

    private LocalDateTime validateTime;

    private boolean validated;

    private AuthCodeType validateType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;


}
