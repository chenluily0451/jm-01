package com.jm.bid.user.account.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/11/10.
 */
@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Company {


    @ApiModelProperty(hidden = true)
    private Long id;

    /* 1 审核通过  2 审核未通过 3 待审核*/
    @ApiModelProperty(hidden = true)
    private Integer status;

    @NotNull
    @Length(min = 5, max = 50)
    @ApiModelProperty(required = true, name = "公司名称")
    private String name;

    @NotNull
    @Length(min = 5, max = 60)
    @ApiModelProperty(required = true, name = "注册地址")
    private String registerAddress;

    @NotNull
    @Length(min = 5, max = 30)
    @ApiModelProperty(required = true, name = "注册号码")
    private String registerCode;

    @NotNull
    @Length(min = 2, max = 20)
    @ApiModelProperty(required = true, name = "法人姓名")
    private String legalPerson;

    @NotNull
    @Length(min = 2, max = 20)
    @ApiModelProperty(required = true, name = "委托人姓名")
    private String proxyPerson;

    @NotNull
    @Length(min = 11, max = 14)
    @ApiModelProperty(required = true, name = "公司电话")
    private String companyPhone;

    @Length(max = 200)
    @ApiModelProperty(name = "营业执照url")
    private String businessLicensePic;

    @Length(max = 200)
    @ApiModelProperty(name = "税务登记证url")
    private String taxRegistrationPic;

    @Length(max = 200)
    @ApiModelProperty(name = "组织机构代码证url")
    private String organizationCodePic;

    @Length(max = 200)
    @ApiModelProperty(name = "开户许可证url")
    private String openingLicensePic;

    @Length(max = 200)
    @ApiModelProperty(name = "信用代码证url")
    private String creditCodePic;

    @Length(max = 200)
    @ApiModelProperty(name = "委托证url")
    private String proxyPic;

    @Length(max = 200)
    @NotEmpty(message = "身份证不能为空")
    @ApiModelProperty(readOnly = true, name = "身份证url")
    private String idCardPic;

    @ApiModelProperty(hidden = true)
    private LocalDateTime createDate;

    @ApiModelProperty(hidden = true)
    private String createBy;

    @ApiModelProperty(hidden = true)
    private String auditMsg;

    @ApiModelProperty(hidden = true)
    private LocalDateTime auditDate;

    @ApiModelProperty(hidden = true)
    private String auditBy;

    @ApiModelProperty(hidden = true)
    private String UUID;


    public enum Status {

        //审核通过
        approved(1),
        //审核未通过
        unApproved(2),
        //待审核
        checkPending(3),

        //待完善
        waitImprove;
        public int value;

        Status(int value) {
            this.value = value;
        }

        Status() {
        }
    }
}
