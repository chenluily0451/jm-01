package com.jm.bid.admin.finance.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RefundRecord {
    private Long id;

    private Long tenderId;

    private String tenderCode;

    private String payTradeId;

    private String tradeId;

    private Integer status;

    private Integer type;

    /*true 系统自动退款    false 需要人工确认手动退款 */
    private Boolean auto;

    private Boolean fulledRefund;

    private String paySystemNum;

    private String payAccountName;

    private String payAccountNo;


    private String recSystemNum;

    private String recAccountName;

    private String recAccountNo;

    /*支付总金额*/
    private BigDecimal payTotalAmount;

    /*退款金额*/
    private BigDecimal refundAmount;

    private LocalDateTime createDate;

    private String createBy;

    private Boolean deleted;

    private String bankMsg;

    private String payRemark;

    public enum Type {
        /*一般资金账户*/
        deposit(1);

        public int value;

        Type(int value) {
            this.value = value;
        }
    }

    public enum RefundStatus {

        //需要财务确认
        waitTrigger(0),
        init(1),
        processing(2),
        fail(3),
        success(4);

        public int value;

        RefundStatus(int value) {
            this.value = value;
        }
    }
}

