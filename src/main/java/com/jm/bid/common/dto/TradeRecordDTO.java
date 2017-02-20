package com.jm.bid.common.dto;

import com.jm.bid.boot.persistence.BaseFilter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Created by xiangyang on 2016/12/26.
 */
@Data
public class TradeRecordDTO extends BaseFilter {
    private Long id;

    private Long tenderId;

    private String tenderCode;

    private String tradeId;

    private Integer status;

    //保证金
    private Integer type;

    //资金类型 : 保证金缴纳  保证金退款
    private Integer cashType;


    //对方资金账号
    private String bankAccountNum;

    private String payAccountNo;

    private String recAccountNo;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createEndDate;

    public String getBankAccountNum() {
        return StringUtils.trimToNull(this.bankAccountNum);
    }

    public String getPayAccountNo(){
        return StringUtils.trimToNull(this.payAccountNo);
    }

}
