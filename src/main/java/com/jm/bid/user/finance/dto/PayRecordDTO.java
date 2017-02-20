package com.jm.bid.user.finance.dto;

import com.jm.bid.boot.persistence.BaseFilter;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/20.
 */
@Data
public class PayRecordDTO extends BaseFilter<PayRecordDTO> {

    private String cashAccountNo;

    private Integer type;

    private Integer status;

    private LocalDateTime createStartDate;

    private LocalDateTime createEndDate;

    private String companyId;

}
