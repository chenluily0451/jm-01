package com.jm.bid.boot.persistence;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiangyang on 16/8/30.
 */
@Data
public class BaseFilter<T> {
    private Integer pageNo = 1;

    private Integer pageSize = 10;

    @ApiModelProperty(hidden = true)
    private Integer totalRecord;

    @ApiModelProperty(hidden = true)
    private Integer totalPage;

    @ApiModelProperty(hidden = true)
    private List<T> results;


    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo <= 0 ? 1 : pageNo;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageNo <= 0 ? 10 : pageSize;
    }


    public void setTotalRecord(Integer totalRecord) {
        this.totalRecord = totalRecord;
        int totalPage = totalRecord % pageSize == 0 ? totalRecord / pageSize : totalRecord / pageSize + 1;
        if(totalPage>0) {
            this.pageNo = this.getPageNo() > totalPage ? totalPage : this.getPageNo();
        }
        this.setTotalPage(totalPage);
    }

}
