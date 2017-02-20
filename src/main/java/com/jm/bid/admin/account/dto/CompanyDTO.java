package com.jm.bid.admin.account.dto;

import com.jm.bid.boot.persistence.BaseFilter;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by xiangyang on 2016/11/23.
 */
@Data
public class CompanyDTO extends BaseFilter {

    private String name;

    private Integer status;

    public String getName() {
        return StringUtils.trimToNull(name);
    }
}
