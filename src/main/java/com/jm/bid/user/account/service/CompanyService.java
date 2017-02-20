package com.jm.bid.user.account.service;

import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.util.Ids;
import com.jm.bid.user.account.dto.UserDTO;
import com.jm.bid.user.account.entity.Company;
import com.jm.bid.user.account.mapper.CompanyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/11/14.
 */
@Service
@Transactional(readOnly = false)
public class CompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private UserService userService;

    @Transactional(readOnly = false)
    public String  registerCompany(Company company, UserDTO userDTO) {
        String UUID = Ids.randomBase62(40);
        company.setCreateDate(LocalDateTime.now());
        company.setCreateBy(userDTO.getSecurePhone());
        company.setUUID(UUID);
        company.setStatus(Company.Status.checkPending.value);
        companyMapper.addSelective(company);
        //更新用户公司ID
        userService.updateUserCompanyId(UUID, userDTO.getId());
        return UUID;
    }

    @Transactional(readOnly = false)
    public void updateCompany(Company company, UserDTO userDTO) {
        Assert.notNull(company);
        Company lastCompanyInfo =this.loadLastCompanyByUUID(userDTO.getCompanyId());
        if (company.getId()==null|| lastCompanyInfo==null || company.getId().compareTo(lastCompanyInfo.getId())==-1) {
           throw  new BusinessException("可能读取到脏数据,请刷新重新提交");
        }

        company.setCreateDate(LocalDateTime.now());
        company.setCreateBy(userDTO.getSecurePhone());
        company.setStatus(Company.Status.checkPending.value);
        company.setUUID(userDTO.getCompanyId());
        companyMapper.addSelective(company);
    }
    public Company loadLastCompanyByUUID(String UUID) {
        return companyMapper.loadByUUID(UUID);
    }


    @Transactional(readOnly = false)
    public boolean checkCompanyNameExists(String companyName) {
        return companyMapper.checkCompanyNameExists(companyName);
    }

    @Transactional(readOnly = false)
    public boolean checkCompanyNameExists2(String UUID,String companyName) {
        Assert.notNull(UUID);
        return companyMapper.checkCompanyNameExists2(UUID,companyName);
    }
}
