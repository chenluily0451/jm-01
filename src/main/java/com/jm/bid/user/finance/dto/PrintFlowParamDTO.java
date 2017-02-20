package com.jm.bid.user.finance.dto;

import com.jm.bid.boot.persistence.BaseFilter;
import com.jm.bid.restpay.dto.PrintFlowDTO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Created by xiangyang on 2016/12/21.
 */
@Data
public class PrintFlowParamDTO extends BaseFilter<PrintFlowDTO> {

    private int bankId = 1;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    //默认查询入金
    private Type tradeType =Type.C;

    /*C 入金  D 出金*/
    private Type type;

    enum Type {
        C, D;

    }

}
