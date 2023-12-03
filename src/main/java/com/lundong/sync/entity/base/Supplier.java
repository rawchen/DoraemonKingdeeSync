package com.lundong.sync.entity.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-29 17:22
 */
@Data
public class Supplier {

    @JSONField(name = "供应商编码")
    private String supplierCode;

    @JSONField(name = "供应商名称")
    private String supplierName;
}
