package com.jm.bid.user.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2017/1/16.
 */
@Data
public class PaymentApplyDTO {
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

    private LocalDateTime tenderStartDate;

    private Integer payStatus;


}
