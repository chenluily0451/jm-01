package com.jm.bid.user.account.service;

import com.jm.bid.boot.util.Digests;
import com.jm.bid.boot.util.Encodes;
import com.jm.bid.boot.util.Ids;
import com.jm.bid.common.consts.UserRole;
import com.jm.bid.common.service.SMS;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.entity.Company;
import com.jm.bid.user.account.entity.User;
import com.jm.bid.user.account.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by xiangyang on 2016/11/14.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public static final int HASH_INTERATIONS = 1024;

    public static final int SALT_SIZE = 8;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SMS sms;

    /**
     * @param securePhone
     * @return false 不存在  true 存在
     */
    public boolean securePhoneExists(String securePhone) {
        return userMapper.loadBySecurePhone(securePhone) == null ? false : true;
    }

    @Transactional(readOnly = false)
    public User registerUser(String securePhone, String plainPassword) {
        byte[] passwordSalt = Digests.generateSalt(SALT_SIZE);
        byte[] hashPassword = Digests.sha1(plainPassword.getBytes(), passwordSalt, HASH_INTERATIONS);
        User user = new User();
        user.setRole(UserRole.ADMIN);
        user.setSecurePhone(securePhone);
        user.setPassword(Encodes.encodeHex(hashPassword));
        user.setPasswordSalt(Encodes.encodeHex(passwordSalt));
        user.setCreateDate(LocalDateTime.now());
        user.setCreateBy("register");
        userMapper.addSelective(user);
        sms.sendSMS(securePhone, "尊敬的用户您好！您已完成晋煤电子商务平台的注册，请妥善保管您的账号及密码!");
        return user;
    }

    /**
     * 添加员工
     *
     * @param securePhone
     * @param userRole
     * @param adminUser
     * @return
     */
    @Transactional(readOnly = false)
    public User addEmployee(String securePhone, String userName, UserRole userRole, UserDTO adminUser) {
        byte[] passwordSalt = Digests.generateSalt(SALT_SIZE);
        String plainPassword = Ids.uuid2().substring(0, 10);
        byte[] hashPassword = Digests.sha1(plainPassword.getBytes(), passwordSalt, HASH_INTERATIONS);

        User employee = new User();
        employee.setRole(userRole);
        employee.setUserName(userName);
        employee.setSecurePhone(securePhone);
        employee.setPassword(Encodes.encodeHex(hashPassword));
        employee.setPasswordSalt(Encodes.encodeHex(passwordSalt));

        employee.setCreateDate(LocalDateTime.now());
        employee.setCompanyId(adminUser.getCompanyId());
        employee.setCreateBy(adminUser.getSecurePhone());
        userMapper.addSelective(employee);
        Company company = companyService.loadLastCompanyByUUID(adminUser.getCompanyId());
        sms.sendSMS(securePhone, "尊敬的用户您好!您已被" + company.getName() + "公司管理员分配为晋煤电子商务平台" + userRole.value +
                ",登录账号:" + securePhone + ",初始密码:" + plainPassword + "," +
                "请登录平台修改密码及业务操作，切勿泄露该信息！");
        return employee;
    }

    public List<User> loadEmployeeByCompanyId(String companyId, UserRole... userRole) {
        return userMapper.loadEmployeeByCompanyId(companyId, userRole);
    }

    @Transactional(readOnly = false)
    public void updatePassword(String securePhone, String plainPassword) {
        byte[] passwordSalt = Digests.generateSalt(SALT_SIZE);
        byte[] hashPassword = Digests.sha1(plainPassword.getBytes(), passwordSalt, HASH_INTERATIONS);

        User user = userMapper.loadBySecurePhone(securePhone);
        user.setPassword(Encodes.encodeHex(hashPassword));
        user.setPasswordSalt(Encodes.encodeHex(passwordSalt));
        userMapper.updateSelective(user);
        sms.sendSMS(securePhone, "尊敬的用户您好！您的登陆密码已重置，请妥善保管，切勿泄露!");

    }

    @Transactional(readOnly = false)
    public void updatePayPassword(String securePhone, String plainPayPassword) {
        byte[] passwordSalt = Digests.generateSalt(SALT_SIZE);
        byte[] hashPassword = Digests.sha1(plainPayPassword.getBytes(), passwordSalt, HASH_INTERATIONS);
        User user = userMapper.loadBySecurePhone(securePhone);
        user.setPayPassword(Encodes.encodeHex(hashPassword));
        user.setPayPasswordSalt(Encodes.encodeHex(passwordSalt));
        userMapper.updateSelective(user);
    }

    @Transactional(readOnly = false)
    public void forbiddenUser(long userId) {
        User user = this.loadById(userId);
        user.setActive(false);
        userMapper.updateSelective(user);
    }

    public User loadBySecurePhone(String securePhone) {
        return userMapper.loadBySecurePhone(securePhone);
    }

    public User loadById(long userId) {
        return userMapper.loadById(userId);
    }

    public boolean validPasswordEquals(String phone, String plainPassword) {
        User user = userMapper.loadBySecurePhone(phone);
        Assert.notNull(user);
        String credentials = Encodes.encodeHex(Digests.sha1(plainPassword.getBytes(), Encodes.decodeHex(user.getPasswordSalt()), HASH_INTERATIONS));
        return user.getPassword().equals(credentials);
    }

    public boolean validPayPasswordEquals(String phone, String plainPayPassword) {
        User user = userMapper.loadBySecurePhone(phone);
        Assert.notNull(user);
        if (StringUtils.isEmpty(user.getPayPassword()) || StringUtils.isEmpty(user.getPayPasswordSalt())) {
          return false;
        }
        String credentials = Encodes.encodeHex(Digests.sha1(plainPayPassword.getBytes(), Encodes.decodeHex(user.getPayPasswordSalt()), HASH_INTERATIONS));
        return user.getPayPassword().equals(credentials);
    }

    @Transactional(readOnly = false)
    public void updateUserCompanyId(String UUID, long userId) {
        userMapper.updateUserCompanyId(UUID, userId);
    }


}
