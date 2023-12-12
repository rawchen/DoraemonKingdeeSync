package com.lundong.sync.entity.bitable.approval;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 付款申请&个人报销（第二张生成例外表）
 *
 * @author shuangquan.chen
 * @date 2023-11-29 15:40
 */
@Data
public class SecondExceptionTable {

    @JSONField(name = "费用子类")
    private String costSubcategory;
}
