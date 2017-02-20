package com.jm.bid.admin.account.dto;

import com.jm.bid.common.consts.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/11/18.
 */
@Data
public class AdminDTO {

    private Long id;

    private String userName;

    private String securePhone;

    private UserRole role;

    private String password;

    private String passwordSalt;

    private String payPassword;

    private String payPasswordSalt;

    private LocalDateTime createDate;

    private String createBy;
}
