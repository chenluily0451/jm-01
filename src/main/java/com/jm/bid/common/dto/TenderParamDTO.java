package com.jm.bid.common.dto;

import com.jm.bid.boot.persistence.BaseFilter;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Created by xiangyang on 2016/11/16.
 */
@Data
public class TenderParamDTO extends BaseFilter {

    private Long id;

    @ApiModelProperty(value = "招标标题")
    private String tenderTitle;

    @ApiModelProperty(value = "招标编号")
    private String tenderCode;

    /*竞标状态 0删除  1 草稿 2 预告 3 竞价中 4 有中标-结束 5 未中标-结束*/
    @ApiModelProperty(value = "招标状态", notes = "0删除  1 草稿 2 预告 3 竞价中 4 有中标-结束 5 未中标-结束")
    private Integer status;

    @ApiModelProperty(value = "煤种id")
    private Integer coalTypeId;

    @ApiModelProperty(value = "煤矿id")
    private Integer coalZoneId;

    @ApiModelProperty(value = "发站id")
    private Integer forwardStationId;

    @ApiModelProperty(value = "运输方式id")
    private Integer transportModeId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "创建开始时间")
    private LocalDate createStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "创建截止时间")
    private LocalDate createEndDate;


}
