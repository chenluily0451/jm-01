package com.jm.bid.admin.tender.dto;

import com.jm.bid.boot.persistence.BaseFilter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Created by xiangyang on 2016/12/21.
 */
@Data
public class TenderNoticeDTO extends BaseFilter<TenderNoticeDTO> {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createEndDate;

    private String noticeName;

    private Integer status;

    public String getNotice(){
       return StringUtils.trimToNull(this.noticeName);
    }


}
