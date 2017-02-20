package com.jm.bid.common.mapper;

import com.jm.bid.common.entity.AuthCode;
import com.jm.bid.common.service.AuthCodeType;
import org.apache.ibatis.annotations.Param;

/**
 * Created by xiangyang on 16/7/26.
 */
public interface AuthCodeMapper {

    int addAuthCode(AuthCode authCode);


    AuthCode findCodeWithin10Minute(@Param("phone") String phone, @Param("type") AuthCodeType type);


    AuthCode findAuthCode(@Param("phone") String phone, @Param("code") String code, @Param("type") AuthCodeType type);

    int countInDay(@Param("phone") String phone);

    int updateAuthCodeInvalid(@Param("phone") String phone, @Param("code") String code);

}
