package com.jm.bid.common.service;

import com.jm.bid.boot.util.Ids;
import com.jm.bid.common.entity.FindPwdLog;
import com.jm.bid.common.mapper.FindPwdLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/15.
 */
@Service
public class FindPwdLogService {

    @Autowired
    private FindPwdLogMapper findPwdLogMapper;

    /**
     * 返回uuid
     *
     * @param securePhone
     * @return
     */
    @Transactional(readOnly = false)
    public String addFindPwdLog(String securePhone) {
        FindPwdLog log = new FindPwdLog();
        String UUId = Ids.randomBase62(32);
        log.setSecurePhone(securePhone);
        log.setUUID(UUId);
        log.setCreateDate(LocalDateTime.now());
        log.setSuccess(false);
        findPwdLogMapper.add(log);
        return UUId;
    }

    public FindPwdLog loadFindPwdLogByUUId(String uuid) {
        return findPwdLogMapper.loadByUUId(uuid);
    }

    @Transactional(readOnly = false)
    public void updateFindPwdLogSuccess(String uuid) {
        findPwdLogMapper.updateSuccess(uuid);
    }
}
