package com.jm.bid.user.account.mapper;


import com.jm.bid.common.consts.UserRole;
import com.jm.bid.user.account.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {


    int addSelective(User user);

    User loadById(Long id);

    User loadBySecurePhone(String securePhone);

    int updateSelective(User record);

    int updateUserCompanyId(@Param("UUID") String UUID, @Param("userId") long userId);

    List<User> loadEmployeeByCompanyId(@Param("companyId") String companyId, @Param("userRoles") UserRole... userRole);


}