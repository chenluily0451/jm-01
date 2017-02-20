package com.jm.bid.user.finance.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 16/7/26.
 * 银行资金账户
 */
@Data
public class BankCashAccount {

    private Long id;

    private String accId;

    private String bankAccountName;

    private String bankAccountNum;

    private Integer bankId;

    /*账号类型 1 普通交易账户*/
    private Integer accountType;

    private Boolean available;

    private LocalDateTime createDate;

    private String createBy;

    public enum AccountType {
        /*一般资金账户*/
        generalAccount(1);

        public int value;

        AccountType(int value) {
            this.value = value;
        }
    }

}