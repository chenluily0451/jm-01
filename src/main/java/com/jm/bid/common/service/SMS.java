package com.jm.bid.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by lxj on 14/11/6.
 */
@Service
public class SMS {

    private static final Logger logger = LoggerFactory.getLogger(SMS.class);

    private static final String JM_SIGN = "【晋煤集团】";

    @Value("${sms.sn}")
    private String sn;

    @Value("${sms.password}")
    private String password;

    @Value("${sms.server}")
    private String server;

    @Autowired
    private Environment env;


    /**
     * @param phone   电话
     * @param content 内容
     * @param sign    署名
     */
    public void sendSMS(String phone, String content, String sign) {
        sendSMS(phone, content);
    }

    /**
     * 发送短信
     */
    @Async
    public void sendSMS(String phone, String content) {
        SMSClient smsClient = new SMSClient(server, sn, password);
        logger.info("CODE---------" + phone + "-------" + content);

        if (env.getActiveProfiles().length == 0) {
            throw new RuntimeException("No Spring profile configured, running with default configuration");
        } else {
            logger.info("Running with Spring profile(s) : {}", Arrays.toString(env.getActiveProfiles()));
            Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
            if (activeProfiles.contains("prod")) {
                try {
                    SMSResponse smsResponse = smsClient.sendSMS(phone, content,JM_SIGN);
                    if (!smsResponse.isSuccess()) {
                        logger.error("短信发送失败,内容:{},\n号码:{}", content, phone);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("SMS_EXCEPTION -------- 短信发送失败, phone=" + phone);
                }
            }
        }


    }


}
