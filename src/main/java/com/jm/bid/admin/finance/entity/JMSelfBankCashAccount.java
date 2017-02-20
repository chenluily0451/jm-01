package com.jm.bid.admin.finance.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 16/7/26.
 * 晋煤资金账户
 */
@Data
public class JMSelfBankCashAccount {

    private Long id;

    private String accId;

    private String bankAccountName;

    private String bankAccountNum;

    private Integer bankId;

    /*资金账号类型 1 交易账号  2 保证金账号 */
    private Integer accountType;

    private Boolean available;

    private LocalDateTime createDate;

    private String createBy;


    public enum JMCashAccountType {
        /*一般资金账户*/
        generalAccount(1),
        /*保证金资金账户*/
        depositAccount(2);
        public int value;

        JMCashAccountType(int value) {
            this.value = value;
        }
    }
}

