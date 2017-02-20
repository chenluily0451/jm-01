package com.jm.bid.admin.account.mapper;

import com.jm.bid.admin.account.dto.CompanyDTO;
import com.jm.bid.boot.persistence.Page;
import com.jm.bid.user.account.entity.Company;

import java.util.List;

/**
 * Created by xiangyang on 2016/11/21.
 */
public interface AdminCompanyMapper {

    int auditCompany(Company record);

    Company loadById(long companyId);

    Company loadByUUID(String UUID);

    List<Company> loadAuditMsgByUUID(String UUID);

    long countCompanyByStatus(int status);

    Page<Company> findAllCompany(CompanyDTO companyDTO);

}
