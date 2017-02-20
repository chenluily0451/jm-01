package com.jm.bid.admin.finance.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/18.
 */
@Data
public class JMSelfSystemCashAccount {
    private Long id;

    private String accId;

    private String companyId;

    private LocalDateTime createDate;

    private String createBy;
}
