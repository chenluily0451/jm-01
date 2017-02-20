package com.jm.bid.user.account.dto;

import com.jm.bid.common.consts.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/11/14.
 */
@Data
public class UserDTO {

    private Long id;

    private UserRole role;

    private String securePhone;

    private String userName;

    private String password;

    private String passwordSalt;

    private String payPassword;

    private String payPasswordSalt;

    private String companyId;

    private String companyName;

    private boolean isActive;

    private LocalDateTime createDate;

    private String createBy;


}
