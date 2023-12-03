package com.lundong.sync.entity.approval;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-12-03 10:59
 */
@Data
public class AccountValue {

    /**
     * 收款账户名称
     */
    @JSONField(name = "widgetAccountName")
    private String widgetAccountName;

    /**
     * 收款账户编码
     */
    @JSONField(name = "widgetAccountNumber")
    private String widgetAccountNumber;

}
