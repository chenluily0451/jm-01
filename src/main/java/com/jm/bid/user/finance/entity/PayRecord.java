package com.jm.bid.user.finance.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayRecord {
    private Long id;

    private Long tenderId;

    private String tenderCode;

    private String tradeId;

    /*1 发起支付 2 银行支付中  3 银行支付失败 4 银行支付成功*/
    private Integer status;

    /*资金业务类型  1 保证金  */
    private Integer type;

    private BigDecimal amount;

    private Integer bankId;

    private String payRemark;

    private Long payApplyId;

    private String payCompanyId;

    private String paySystemNum;

    private String payAccountName;

    private String payAccountNo;

    private String recSystemNum;

    private String recAccountName;

    private String recAccountNo;

    private LocalDateTime createDate;

    private String createBy;

    private Boolean deleted;

    private String bankMsg;

    public enum Type {
        /*保证金*/
        deposit(1);

        public int value;

        Type(int value) {
            this.value = value;
        }
    }

    public enum PayStatus {

        init(1),
        processing(2),
        fail(3),
        success(4);

        public int value;

        PayStatus(int value) {
            this.value = value;
        }
    }


}

