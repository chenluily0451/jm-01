package com.jm.bid.admin.account.mapper;

import com.jm.bid.admin.account.entity.Admin;
import com.jm.bid.common.consts.UserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by xiangyang on 2016/11/21.
 */
public interface AdminMapper {

    int add(Admin admin);

    int addSelective(Admin admin);

    Admin loadById(Long id);

    Admin loadBySecurePhone(String securePhone);

    int updateSelective(Admin admin);

    List<Admin> loadAdminByRole(@Param("userRoles") UserRole... roles);

}
