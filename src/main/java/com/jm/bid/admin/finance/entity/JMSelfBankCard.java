package com.jm.bid.admin.finance.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/18.
 */
@Data
public class JMSelfBankCard {
    private Long id;

    @NotEmpty
    @Length(max = 45)
    private String cardNum;

    @NotEmpty
    @Length(max = 20)
    private String cardAccountName;

    private String childBankName;

    @NotNull
    private String childBankCode;

    private Integer status;

    private Boolean validated;

    private Integer validateCount;

    /*验证金额*/
    private BigDecimal validateAmount;

    private String tradeId;

    private String createBy;

    private LocalDateTime createDate;

    private Boolean deleted;

    private String bankMsg;

    public enum Status {

        init(1),
        processing(2),
        fail(3),
        success(4);

        public int value;

        Status(int value) {
            this.value = value;
        }
    }

}
