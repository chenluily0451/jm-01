package com.jm.bid.common.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by xiangyang on 16/8/23.
 */
@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public class CoalBaseDTO {


    @NotNull(message = "热值NCV不能为空")
    @Range(min = 1, max = 7500, message = "热值范围在{min}-{max}之间")
    public Integer NCV;

    @NotNull(message = "热值NCV2不能为空")
    @Range(min = 1, max = 7500, message = "热值范围在{min}-{max}之间")
    public Integer NCV2;

    /*空干基挥发分*/
    @Digits(integer = 2, fraction = 2, message = "空干基挥发分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "空干基挥发分不包括{value}")
    @DecimalMax(value = "50", message = "空干基挥发分不能大于{value}")
    public BigDecimal ADV;

    @Digits(integer = 2, fraction = 2, message = "空干基挥发分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "空干基挥发分不包括{value}")
    @DecimalMax(value = "50", message = "空干基挥发分不能大于{value}")
    public BigDecimal ADV2;

    /*收到基硫分*/
    @Digits(integer = 2, fraction = 2, message = "收到基硫分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "收到基硫分不包括{value}")
    @DecimalMax(value = "10", inclusive = false, message = "收到基硫分不能大于{value}")
    public BigDecimal RS;

    @Digits(integer = 2, fraction = 2, message = "收到基硫分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "收到基硫分不包括{value}")
    @DecimalMax(value = "10", inclusive = false, message = "收到基硫分不能大于{value}")
    public BigDecimal RS2;

    /*全水*/
    @Digits(integer = 2, fraction = 2, message = "全水分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "全水分不包括{value}")
    @DecimalMax(value = "50", message = "全水分不能大于{value}")
    public BigDecimal TM;

    @Digits(integer = 2, fraction = 2, message = "全水分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "全水分不包括{value}")
    @DecimalMax(value = "50", message = "全水分不能大于{value}")
    public BigDecimal TM2;

    /*收到基挥发*/
    @Digits(integer = 2, fraction = 2, message = "收到基挥发分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "收到基挥发分不包括{value}")
    @DecimalMax(value = "50", message = "收到基挥发分不能大于{value}")
    public BigDecimal RV;

    @Digits(integer = 2, fraction = 2, message = "收到基挥发分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "收到基挥发分不包括{value}")
    @DecimalMax(value = "50", message = "收到基挥发分不能大于{value}")
    public BigDecimal RV2;


    /*空干基硫分*/
    @Digits(integer = 2, fraction = 2, message = "空干基硫分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "空干基硫分不包括{value}")
    @DecimalMax(value = "10", message = "空干基硫分不能大于{value}")
    public BigDecimal ADS;

    @Digits(integer = 2, fraction = 2, message = "空干基硫分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "空干基硫分不包括{value}")
    @DecimalMax(value = "10", message = "空干基硫分不能大于{value}")
    public BigDecimal ADS2;


    /*内水*/
    @Digits(integer = 2, fraction = 2, message = "内水分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "内水分不包括{value}")
    @DecimalMax(value = "50", message = "内水分不能大于{value}")
    public BigDecimal IM;

    @Digits(integer = 2, fraction = 2, message = "内水分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "内水分不包括{value}")
    @DecimalMax(value = "50", message = "内水分不能大于{value}")
    public BigDecimal IM2;


    /*灰分*/
    @Digits(integer = 2, fraction = 2, message = "灰分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "灰分不包括{value}")
    @DecimalMax(value = "50", message = "灰分不能大于{value}")
    public BigDecimal ASH;

    @Digits(integer = 2, fraction = 2, message = "灰分不能>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "灰分不包括{value}")
    @DecimalMax(value = "50", message = "灰分不能大于{value}")
    public BigDecimal ASH2;


    /*G值*/
    @Range(min = 1, max = 100, message = "G值必须是{min}-{max}之间的整数")
    public Integer GV;

    @Range(min = 1, max = 100, message = "G值必须是{min}-{max}之间的整数")
    public Integer GV2;

    /*Y值*/

    @Range(min = 1, max = 100, message = "Y值必须是{min}-{max}之间的整数")
    public Integer YV;

    @Range(min = 1, max = 100, message = "Y值必须是{min}-{max}之间的整数")
    public Integer YV2;


    /*固定碳*/
    @Range(min = 1, max = 100, message = "固定碳必须是{min}-{max}之间的整数")
    public Integer FC;

    @Range(min = 1, max = 100, message = "固定碳必须是{min}-{max}之间的整数")
    public Integer FC2;

    /*灰熔点*/
    @Range(min = 900, max = 1800, message = "灰熔点必须是{min}-{max}之间的整数")
    public Integer AFT;

    @Range(min = 900, max = 1800, message = "灰熔点必须是{min}-{max}之间的整数")
    public Integer AFT2;


    /*焦渣特征*/
    @Range(min = 1, max = 100, message = "焦渣特征必须是{min}-{max}之间的整数")
    public Integer CRC;

    @Range(min = 1, max = 100, message = "焦渣特征必须是{min}-{max}之间的整数")
    public Integer CRC2;


    /*哈市可磨*/
    @Range(min = 1, max = 100, message = "哈氏可磨必须是{min}-${max}之间的整数")
    public Integer HGI;

    @Range(min = 1, max = 100, message = "哈氏可磨必须是{min}-${max}之间的整数")
    public Integer HGI2;


    /*干燥无灰基挥发分*/
    @Digits(integer = 2, fraction = 2, message = "干燥无灰基挥发分>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "干燥无灰基挥发分不包括{value}")
    @DecimalMax(value = "50", message = "内水分不能大于{value}")
    public BigDecimal VDAF;

    @Digits(integer = 2, fraction = 2, message = "干燥无灰基挥发分>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "干燥无灰基挥发分不包括{value}")
    @DecimalMax(value = "50", message = "干燥无灰基挥发分不能大于{value}")
    public BigDecimal VDAF2;


    /*干燥基硫分*/
    @Digits(integer = 2, fraction = 2, message = "干燥基硫分>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "干燥基硫分不包括{value}")
    @DecimalMax(value = "50", message = "干燥基硫分不能大于{value}")
    public BigDecimal STD;

    /*干燥基硫分*/
    @Digits(integer = 2, fraction = 2, message = "干燥基硫分>{fraction}位小数")
    @DecimalMin(value = "0", inclusive = false, message = "干燥基硫分不包括{value}")
    @DecimalMax(value = "50", message = "干燥基硫分不能大于{value}")
    public BigDecimal STD2;

    /*颗粒度*/
    public String  PS;


}
