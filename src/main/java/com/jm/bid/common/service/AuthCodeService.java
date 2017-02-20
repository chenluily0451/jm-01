package com.jm.bid.common.service;

import com.jm.bid.boot.web.Response;
import com.jm.bid.common.entity.AuthCode;
import com.jm.bid.common.mapper.AuthCodeMapper;
import com.jm.bid.user.account.dto.UserDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Created by xiangyang on 2016/10/28.
 */
@Service
@Transactional(readOnly = true)
public class AuthCodeService {

    private static final Logger logger = LoggerFactory.getLogger(AuthCodeService.class);

    private static final char[] codeNums = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    private static final Pattern securePhoneRegex = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$");

    private static final String verifyCodeStr = "验证码为：code，请按照页面提示提交验证码，确保信息安全，切勿将验证码泄露与他人，唯一热线:0356-3662594!";

    /*一天之内发送短信验证码上线,防止短信攻击*/
    public static final int InDay_AuthCodeCount_UPPERLIMIT = 20;

    @Autowired
    private AuthCodeMapper authCodeMapper;

    @Autowired
    private SMS sms;

    /*验证码验证错误类型*/
    public enum AuthCodeErrorType {
        Auth_PhoneIllegal,
        Auth_CodeError,
        Auth_CodeExpire,
        Auth_Success,
    }

    @Transactional(readOnly = false)
    public Response sendVerifyCode(String phone, AuthCodeType type) {
        return this.sendVerifyCode(phone, type, null);
    }

    @Transactional(readOnly = false)
    public Response sendVerifyCode(String phone, AuthCodeType type, UserDTO user) {
        if (!securePhoneRegex.matcher(phone).matches()) {
            logger.error("{}发送验证码手机号格式错误!", phone);
            return Response.error("sendAuthCode_phoneIllegal");
        }
        if (authCodeMapper.countInDay(phone) == InDay_AuthCodeCount_UPPERLIMIT) {
            logger.error("{}手机验证码发送条数今天已经达到上限,请明日再试!", phone);
            return Response.error("sendAuthCode_limitation");
        }

        AuthCode authCode = authCodeMapper.findCodeWithin10Minute(phone, type);
        if (authCode == null) {
            String verifyCode = generateVerifyCode();
            authCode = new AuthCode();
            authCode.setCode(verifyCode);
            authCode.setPhone(phone);
            authCode.setUserId(user == null ? null : user.getId());
            authCode.setCreateDate(LocalDateTime.now());
            authCode.setExpireTime(LocalDateTime.now().plusMinutes(10));
            authCode.setValidated(false);
            authCode.setValidateType(type);
            authCodeMapper.addAuthCode(authCode);
            sms.sendSMS(phone, verifyCodeStr.replace("code", verifyCode));
            return Response.success();
        } else if (LocalDateTime.now().isBefore(authCode.getCreateDate().plusMinutes(2))) {
            //同类型的验证码2分钟内多次点击
            logger.error("{}手机验证码发送频率过高, 稍后再试!", authCode.getPhone());
            return Response.error("sendAuthCode_highRate");
        } else {
            //10分钟内有相同类型的验证码存在,更新创建时间。发相同的验证码
            authCode.setCreateDate(LocalDateTime.now());
            authCodeMapper.addAuthCode(authCode);
            sms.sendSMS(phone, verifyCodeStr.replace("code", authCode.getCode()));
            return Response.success();
        }
    }

    @Transactional(readOnly = false)
    public void updateVerifyCodeInvalid(String phone, String code) {
        authCodeMapper.updateAuthCodeInvalid(phone, code);
    }


    public AuthCodeErrorType validateAuthCode(String phone, AuthCodeType type, String code) {
        AuthCode v = authCodeMapper.findAuthCode(phone, code, type);
        if (StringUtils.isEmpty(phone) || !securePhoneRegex.matcher(phone).matches()) {
            return AuthCodeErrorType.Auth_PhoneIllegal;
        } else if (v == null || !v.getCode().equals(code)) {
            return AuthCodeErrorType.Auth_CodeError;
        } else if (v.getExpireTime().isBefore(LocalDateTime.now())) {
            return AuthCodeErrorType.Auth_CodeExpire;
        }
        return AuthCodeErrorType.Auth_Success;
    }

    /**
     * 生成6位随机数字符串
     */
    public static String generateVerifyCode() {
        Random random = new Random();
        String code = "";
        for (int i = 0; i < 6; i++) {
            code += codeNums[random.nextInt(10)];
        }
        return code.toString();
    }

    public boolean isValidPhone(String phone) {
        return securePhoneRegex.matcher(phone).matches();
    }

}
