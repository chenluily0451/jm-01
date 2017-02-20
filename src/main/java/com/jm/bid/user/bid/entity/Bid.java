package com.jm.bid.user.bid.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Bid {
    private Long id;

    private Long tenderId;

    private String companyId;

    private String companyName;

    private Boolean checked;

    private BigDecimal quotePrice;

    private Integer quoteAmount;

    private Integer winBidAmount;

    private LocalDateTime createDate;

    private String createBy;
}

