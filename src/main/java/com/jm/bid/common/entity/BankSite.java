package com.jm.bid.common.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xiangyang on 16/5/16.
 */

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE)
public class BankSite implements Serializable {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String provinceName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer provinceCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cityName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer cityCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer bankCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String bankName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String childBankCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String childBankName;

}
