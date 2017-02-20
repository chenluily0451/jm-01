package com.jm.bid.admin.account.entity;

import com.jm.bid.common.consts.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/11/9.
 */
@Data
public class Admin {


    private Long id;

    private String userName;

    private String securePhone;

    private UserRole role;

    private boolean isActive;

    private String password;

    private String passwordSalt;

    private String payPassword;

    private String payPasswordSalt;

    private LocalDateTime createDate;

    private String createBy;
}
