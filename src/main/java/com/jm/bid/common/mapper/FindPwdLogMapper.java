package com.jm.bid.common.mapper;


import com.jm.bid.common.entity.FindPwdLog;

public interface FindPwdLogMapper {


    int add(FindPwdLog log);

    FindPwdLog loadByUUId(String UUID);

    int updateSuccess(String UUID);

}