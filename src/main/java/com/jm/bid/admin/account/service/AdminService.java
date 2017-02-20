package com.jm.bid.admin.account.service;

import com.jm.bid.admin.account.entity.Admin;
import com.jm.bid.admin.account.mapper.AdminMapper;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.util.Digests;
import com.jm.bid.boot.util.Encodes;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.user.account.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by xiangyang on 2016/11/21.
 */
@Service
@Transactional(readOnly = false)
public class AdminService {

    // 此处生成随机12位的salt, hash迭代2048次,和前台用户加密方式不一样
    public static final int SALT_SIZE = 12;

    public static final int HASH_INTERATIONS = 2048;

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    @Autowired
    private AdminMapper adminMapper;

    public Admin loadBySecurePhone(String securePhone) {
        return adminMapper.loadBySecurePhone(securePhone);
    }

    public boolean validPasswordEquals(String phone, String plainPassword) {
        Admin admin = adminMapper.loadBySecurePhone(phone);
        Assert.notNull(admin);
        String credentials = Encodes.encodeHex(Digests.sha1(plainPassword.getBytes(), Encodes.decodeHex(admin.getPasswordSalt()), HASH_INTERATIONS));
        return admin.getPassword().equals(credentials);
    }

    public boolean validPayPasswordEquals(String phone, String plainPayPassword) {
        Admin admin = adminMapper.loadBySecurePhone(phone);
        Assert.notNull(admin);
        String credentials = Encodes.encodeHex(Digests.sha1(plainPayPassword.getBytes(), Encodes.decodeHex(admin.getPayPasswordSalt()), HASH_INTERATIONS));
        return admin.getPayPassword().equals(credentials);
    }

    @Transactional(readOnly = false)
    public void updatePassword(String securePhone, String plainPassword) {
        byte[] passwordSalt = Digests.generateSalt(SALT_SIZE);
        byte[] hashPassword = Digests.sha1(plainPassword.getBytes(), passwordSalt, HASH_INTERATIONS);

        Admin admin = adminMapper.loadBySecurePhone(securePhone);
        admin.setPassword(Encodes.encodeHex(hashPassword));
        admin.setPasswordSalt(Encodes.encodeHex(passwordSalt));
        adminMapper.updateSelective(admin);

    }

    @Transactional(readOnly = false)
    public void updatePayPassword(String securePhone, String plainPayPassword) {
        byte[] payPasswordSalt = Digests.generateSalt(SALT_SIZE);
        byte[] hashPayPassword = Digests.sha1(plainPayPassword.getBytes(), payPasswordSalt, HASH_INTERATIONS);

        Admin admin = adminMapper.loadBySecurePhone(securePhone);
        admin.setPayPassword(Encodes.encodeHex(hashPayPassword));
        admin.setPayPasswordSalt(Encodes.encodeHex(payPasswordSalt));
        adminMapper.updateSelective(admin);

    }

    @Transactional(readOnly = false)
    public void updatePhone(String newPhone, String oldPhone) {
        Assert.notNull(newPhone);
        Assert.notNull(oldPhone);
        Admin admin = adminMapper.loadBySecurePhone(oldPhone);
        if (admin == null) {
            logger.error("{}手机号的管理员不存在!", oldPhone);
            throw new BusinessException("查无此人");
        }
        admin.setSecurePhone(newPhone);
        adminMapper.updateSelective(admin);

    }

    public List<Admin> loadAdminByRole(UserRole... userRole) {
        return adminMapper.loadAdminByRole(userRole);
    }

}
