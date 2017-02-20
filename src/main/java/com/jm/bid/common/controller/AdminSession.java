package com.jm.bid.common.controller;

import com.jm.bid.admin.account.dto.AdminDTO;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by xiangyang on 2016/11/9.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class AdminSession {

    private AdminDTO admin;

    private String imgCode;

    public boolean login(AdminDTO admin) {
        this.admin = admin;
        return true;
    }

    public void logout() {
        this.admin = null;
    }

    public boolean isLogined() {
        return this.admin != null && this.admin.getId() != null;
    }

    public void removeImgCode() {
        this.imgCode = null;
    }
}
