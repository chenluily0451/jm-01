package com.jm.bid;

import com.jm.bid.admin.account.service.AdminService;
import com.jm.bid.boot.util.Digests;
import com.jm.bid.boot.util.Encodes;

/**
 * Created by xiangyang on 2016/11/21.
 */
public class ApplicationTest {


    public static void main(String[] args) {


        String plainPassword = "yimei180";

        byte[] passwordSalt = Digests.generateSalt(AdminService.SALT_SIZE);

        byte[] hashPassword = Digests.sha1(plainPassword.getBytes(), passwordSalt, AdminService.HASH_INTERATIONS);

        System.out.println("-----------------登陆密码-------------------");

        System.out.println("password:  " + Encodes.encodeHex(hashPassword));
        System.out.println("passwordSalt:  " + Encodes.encodeHex(passwordSalt));

        System.out.println("-----------------支付密码-------------------");
        byte[] payPasswordSalt = Digests.generateSalt(AdminService.SALT_SIZE);
        byte[] payHashPassword = Digests.sha1(plainPassword.getBytes(), payPasswordSalt, AdminService.HASH_INTERATIONS);

        System.out.println("payPassword:" + Encodes.encodeHex(payHashPassword));
        System.out.println("payPasswordSalt:" + Encodes.encodeHex(payPasswordSalt));
        //添加admin用户sql
        /**
         * insert into jm_admin (userName, securePhone, password, passwordSalt, payPassword, payPasswordSalt, status, createDate, createBy)value ('admin2','18103912341','5e63da634094de3575c6798b2d4afed6bd70e998','27eb13a57863988b','5e63da634094de3575c6798b2d4afed6bd70e998','27eb13a57863988b',1,now(),'system');
         */

    }
}
