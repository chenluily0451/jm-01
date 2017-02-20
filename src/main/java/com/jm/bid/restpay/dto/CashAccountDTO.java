package com.jm.bid.restpay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by xiangyang on 2016/12/1.
 */
@Data
public class CashAccountDTO {

    /*系统账户ID*/
    @ApiModelProperty(hidden = true)
    private String accId;

    /*银行账户No*/
    @ApiModelProperty(value = "资金账号")
    private String accNo;

    private BigDecimal sjAmount;

    @ApiModelProperty(value = "可用金额")
    private BigDecimal kyAmount;

    @ApiModelProperty(value = "冻结金额")
    private BigDecimal djAmount;

    /*1 处理中 2 成功 3 失败*/
    private Integer status;

    private String errMess;

    public boolean isProcessed() {
        if (this.getStatus() == 1) {
            return true;
        }
        return false;
    }

    public boolean isSuccess() {
        if (this.getStatus() == 2) {
            return true;
        }
        return false;
    }

    public boolean isFail() {
        if (this.getStatus() == 3) {
            return true;
        }
        return false;
    }

}
