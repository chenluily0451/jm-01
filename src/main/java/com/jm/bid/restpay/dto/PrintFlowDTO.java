package com.jm.bid.restpay.dto;

import com.jm.bid.boot.persistence.BaseFilter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/20.
 */
@Data
public class PrintFlowDTO extends BaseFilter<PrintFlowDTO> {


    private String accountNm;

    private String subAccNo;

    private String tranType;

    private String loanFlag;

    private String tellerNo;

    private String accountNo;

    private String verifyCode;

    private BigDecimal tranAmt;

    private BigDecimal pdgAmt;

    private String tranSeqNo;

    private BigDecimal accBalAmt;

    private String tranTime;

    private String accBnkNm;

    private String memo;

    private String tranDate;

    private String tradeId;

    private LocalDateTime createDate;

    public String getTradeId() {
        return this.memo.substring(this.memo.lastIndexOf("|") + 1);
    }

    public String getMemo() {
        return this.memo.startsWith("|") ? this.memo.substring(0, this.memo.lastIndexOf("|")) : this.memo;
    }

    public LocalDateTime getCreateDate() {
        Integer year = Integer.parseInt(this.getTranDate().substring(0, 4));
        Integer month = Integer.parseInt(this.getTranDate().substring(4, 6));
        Integer day = Integer.parseInt(this.getTranDate().substring(6, 8));

        Integer hour = Integer.parseInt(this.getTranTime().substring(0, 2));
        Integer minutes = Integer.parseInt(this.getTranTime().substring(2, 4));
        Integer second = Integer.parseInt(this.getTranTime().substring(4, 6));

        return LocalDateTime.of(year, month, day, hour, minutes, second);
    }


}
