package com.jm.bid.common.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/5.
 */
@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public class Tender {
    private Long id;

    /*招标标题*/
    private String tenderTitle;

    /*招标编号*/
    private String tenderCode;

    /*竞标状态 0 删除 1 草稿 2 预告 3 竞价中 4 有中标-结束 5 未中标-结束*/
    private Integer status;

    /*煤种id*/
    private Integer coalTypeId;

    /*煤矿id*/
    private Integer coalZoneId;

    /*发站id*/
    private Integer forwardStationId;

    /*运输方式id*/
    private Integer transportModeId;

    /*发售吨数*/
    private Integer saleAmount;

    /*保证金*/
    private BigDecimal margins;

    /*竞买底价*/
    private BigDecimal saleBasePrice;

    /*加价幅度*/
    private Integer saleIncreasePriceStep;

    /*最低竞买量*/
    private Integer minimumSaleAmount;

    /*结算方式*/
    private String settlementModeStr;

    /*招标开始时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime tenderStartDate;

    /*招标截止时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime tenderEndDate;

    /*招标项目*/
    private String projectStr;

    /*考核指标*/
    @JsonProperty("KPIStr")
    private String KPIStr;

    /*资质*/
    private String qualificationStr;

    /*保证金*/
    private String marginsStr;

    /*竞买规则*/
    private String saleRuleStr;

    private LocalDateTime createDate;

    private LocalDateTime releaseDate;

    private String createBy;

    //浏览人数
    private long visitorsCount;

    public enum Status {

        predict(2),
        processing(3),
        fail(4),
        success(5);

        public int value;

        Status(int value) {
            this.value = value;
        }
    }
}
