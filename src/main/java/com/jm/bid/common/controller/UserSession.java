package com.jm.bid.common.controller;

import com.jm.bid.user.account.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by xiangyang on 2016/11/9.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class UserSession {

    private UserDTO user;

    private String imgCode;

    public boolean login(UserDTO user) {
        this.user = user;
        return true;
    }

    public void logout() {
        this.user = null;
    }

    public boolean isLogined() {
        return this.user != null && this.user.getId() != null;
    }

    public void removeImgCode() {
        this.imgCode = null;
    }

}
