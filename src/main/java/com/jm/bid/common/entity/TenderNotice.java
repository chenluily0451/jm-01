package com.jm.bid.common.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by xiangyang on 2016/12/21.
 */
@Data
public class TenderNotice {
    private Long id;


    @NotNull(message = "招标标题不能为空")
    private Long tenderId;

    private String tenderCode;

    private String tenderTitle;


    @NotNull(message = "公告名称不能为空")
    @Length(max = 50)
    private String noticeName;

    @NotNull(message = "公告内容不能为空")
    private String contentStr;

    // 1 中标公告   0 流标公告
    private boolean hasWinBid;

    @NotNull(message = "公告开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate noticeStartDate;

    @NotNull(message = "公告截止时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate noticeEndDate;


    //公告状态 1 发布  2 编辑
    private Integer status;

    @NotNull(message = "监督电话不能为空")
    @Length(max = 20)
    private String servicePhone;

    @NotNull(message = "监督部门不能为空")
    @Length(min = 2, max = 50)
    private String serviceDepartment;

    private String createBy;

    private LocalDateTime createDate;

    //最后发布时间
    private LocalDateTime lastUpdateDate;

}
