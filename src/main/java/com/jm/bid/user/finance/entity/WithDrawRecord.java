package com.jm.bid.user.finance.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithDrawRecord {
    private Long id;

    private String companyId;

    private String tradeId;

    private BigDecimal amount;

    private Integer status;

    private Long bankCardId;

    private String createBy;

    private LocalDateTime createDate;

    private String remark;

    private String bankMsg;

    private Boolean deleted;

    public enum WithDrawStatus {

        init(1),
        processing(2),
        fail(3),
        success(4);

        public int value;

        WithDrawStatus(int value) {
            this.value = value;
        }
    }
}

