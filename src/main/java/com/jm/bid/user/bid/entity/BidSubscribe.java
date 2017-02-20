package com.jm.bid.user.bid.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BidSubscribe {
    private Long id;

    private Long tenderId;

    private String phone;

    private Boolean logined;

    private String ip;

    private LocalDateTime createDate;

    private LocalDateTime noticeDate;
}

