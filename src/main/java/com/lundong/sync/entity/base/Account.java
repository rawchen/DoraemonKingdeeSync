package com.lundong.sync.entity.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-29 17:17
 */
@Data
public class Account {

    @JSONField(name = "飞书选项")
    private String feishuOption;

    @JSONField(name = "科目编码")
    private String accountCode;

    @JSONField(name = "科目名称")
    private String accountName;

}
