package com.jm.bid.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/11/15.
 */
@Data
public class FindPwdLog {
    private Long id;
    private String securePhone;
    private String UUID;
    private Boolean success;
    private LocalDateTime createDate;
}
