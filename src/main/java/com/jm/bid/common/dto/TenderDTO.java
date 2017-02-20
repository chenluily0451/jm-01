package com.jm.bid.common.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jm.bid.boot.persistence.BaseFilter;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/11/16.
 */
@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public class TenderDTO extends BaseFilter {

    @ApiModelProperty(hidden = true)
    private Long id;

    @ApiModelProperty(value = "招标标题")
    @NotEmpty
    @Length(max = 50, message = "竞买标题最多50个字符")
    private String tenderTitle;

    @ApiModelProperty(value = "招标编号")
    private String tenderCode;

    /*竞标状态 0删除  1 草稿 2 预告 3 竞价中 4 有中标-结束 5 未中标-结束*/
    @ApiModelProperty(hidden = true)
    private Integer status;

    @ApiModelProperty(value = "煤种id")
    @NotNull
    @Range(min = 1, max = 3, message = "请选择正确的煤种信息")
    private Integer coalTypeId;

    @ApiModelProperty(value = "煤矿id")
    @NotNull
    @Range(min = 1, max = 4, message = "请选择正确的矿别")
    private Integer coalZoneId;

    @ApiModelProperty(value = "发站id")
    @NotNull
    @Range(min = 1, max = 4, message = "请选择正确的发站信息")
    private Integer forwardStationId;

    @ApiModelProperty(value = "运输方式id")
    @NotNull
    @Range(min = 1, max = 2, message = "请选择正确的运输方式")
    private Integer transportModeId;

    @ApiModelProperty(value = "发售吨数")
    @NotNull
    @Range(min = 1000, max = 99999999, message = "发售吨数范围在{min}-{max}之间")
    private Integer saleAmount;

    @ApiModelProperty(value = "保证金")
    @Digits(integer = 7, fraction = 2, message = "保证金不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "保证金不能小于0{value}")
    @DecimalMax(value = "1000000", message = "保证金必须大于{value}")
    private BigDecimal margins;

    @ApiModelProperty(value = "竞买底价")
    @NotNull
    @Digits(integer = 4, fraction = 2, message = "竞买底价不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "竞买底价不能小于{value}")
    @DecimalMax(value = "1500", message = "竞买底价不能大于{value}")
    private BigDecimal saleBasePrice;

    @ApiModelProperty(value = "加价幅度")
    @NotNull
    @Range(min=1,max = 100,message = "加价幅度范围在{min}-{max}之间")
    private Integer saleIncreasePriceStep;

    @ApiModelProperty(value = "最低竞买量")
    @NotNull
    @Range(min = 1000, max = 99999999, message = "最小采购吨数范围在{min}-{max}之间")
    private Integer minimumSaleAmount;

    @ApiModelProperty(value = "结算方式")
    @NotEmpty(message = "结算方式不能为空")
    private String settlementModeStr;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "招标开始时间")
    @NotNull(message = "报价开始时间不能为空")
    private LocalDateTime tenderStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @ApiModelProperty(value = "招标截止时间")
    @NotNull(message = "报价截止时间不能为空")
    private LocalDateTime tenderEndDate;

    @ApiModelProperty(value = "招标项目")
    @Valid
    private CoalBaseDTO coalBaseDTO;

    @JsonProperty("KPIStr")
    @ApiModelProperty(value = "考核指标文本")
    @NotEmpty(message = "考核指标文本不能为空")
    private String KPIStr;

    @ApiModelProperty(value = "资质文本")
    @NotEmpty(message = "资质文本不能为空")
    private String qualificationStr;

    @ApiModelProperty(value = "保证金文本")
    @NotEmpty(message = "保证金文本不能为空")
    private String marginsStr;

    @ApiModelProperty(value = "竞买规则文本")
    @NotEmpty(message = "竞买规则文本不能为空")
    private String saleRuleStr;

    @ApiModelProperty(hidden = true)
    private LocalDateTime createDate;

    @ApiModelProperty(hidden = true)
    private String createBy;

    @ApiModelProperty(hidden = true)
    private LocalDateTime releaseDate;

    @ApiModelProperty(hidden = true)
    private long visitorsCount;

}
