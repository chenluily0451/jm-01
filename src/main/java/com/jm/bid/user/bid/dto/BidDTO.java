package com.jm.bid.user.bid.dto;

import com.jm.bid.boot.persistence.BaseFilter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/21.
 */
@Data
public class BidDTO extends BaseFilter<BidDTO> {


    private Long id;

    private Long tenderId;

    private String bidCode;

    private String tenderTitle;

    private String companyId;

    private String companyName;

    private Boolean checked;

    private BigDecimal quotePrice;

    private Integer quoteAmount;

    private Integer winBidAmount;

    private LocalDateTime createDate;

    private String createBy;

}
