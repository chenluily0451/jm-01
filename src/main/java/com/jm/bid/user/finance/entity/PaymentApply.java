package com.jm.bid.user.finance.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentApply {
    private Long id;

    private Long tenderId;

    private String tenderCode;

    private BigDecimal amount;

    private Integer quantity;

    private Integer type;

    private Boolean done;

    private String recAccountNo;

    private String companyId;

    private String companyName;

    private LocalDateTime createDate;

    private String createBy;

    private Boolean deleted;

    public enum Type {
        /*保证金*/
        deposit(1);

        public int value;

        Type(int value) {
            this.value = value;
        }
    }

}

