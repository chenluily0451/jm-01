package com.jm.bid.user.finance.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/12.
 *  系统资金账户--->关联多个不同银行资金账户
 */
@Data
public class SystemCashAccount {
    private Long id;

    private String accId;

    private String companyId;

    private LocalDateTime createDate;

    private String createBy;
}
