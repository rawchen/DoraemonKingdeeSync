package com.lundong.sync.entity.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-29 17:22
 */
@Data
public class Custom {

    @JSONField(name = "客户编码")
    private String customCode;

    @JSONField(name = "客户名称")
    private String customName;
}
