package com.jm.bid.admin.account.service;

import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.admin.account.dto.CompanyDTO;
import com.jm.bid.admin.account.mapper.AdminCompanyMapper;
import com.jm.bid.boot.exception.BusinessException;
import com.jm.bid.boot.exception.NotFoundException;
import com.jm.bid.boot.persistence.Page;
import com.jm.bid.common.service.SMS;
import com.jm.bid.user.account.entity.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by xiangyang on 2016/11/21.
 */
@Service
@Transactional(readOnly = false)
public class AdminCompanyService {

    @Autowired
    private AdminCompanyMapper adminCompanyMapper;

    @Autowired
    private SMS sms;

    public Long countCompanyByStatus(int status) {
        return adminCompanyMapper.countCompanyByStatus(status);
    }

    public Page<Company> findAllCompany(CompanyDTO company) {
        return adminCompanyMapper.findAllCompany(company);
    }


    public Company loadByUUID(String UUID) {
        return adminCompanyMapper.loadByUUID(UUID);
    }

    public List<Company> loadAuditMsgByUUID(String UUID) {
        return adminCompanyMapper.loadAuditMsgByUUID(UUID);
    }


    @Transactional(readOnly = false)
    public void auditCompany(long companyId, String auditMsg, boolean passFlag, AdminDTO adminDTO) {
        Company company = adminCompanyMapper.loadById(companyId);
        if (company == null) {
            throw new NotFoundException();
        }
        if (company.getAuditDate() != null) {
            throw new BusinessException("该条数据已经审核!", "/admin/customer/customerList");
        }
        company.setStatus(passFlag == true ? Company.Status.approved.value : Company.Status.unApproved.value);
        company.setAuditMsg(auditMsg);
        company.setAuditDate(LocalDateTime.now());
        company.setAuditBy(adminDTO.getSecurePhone());
        adminCompanyMapper.auditCompany(company);
        if (passFlag) {
            sms.sendSMS(company.getCreateBy(), "尊敬的用户您好!贵公司资质信息已审核通过，请在我的账户--员工管理中设置组织分工，进行交易！");
        } else {
            sms.sendSMS(company.getCreateBy(), "尊敬的用户您好!贵公司资质信息未被审核通过，原因：" + auditMsg + "；请登录进行信息修改！");
        }

    }
}
