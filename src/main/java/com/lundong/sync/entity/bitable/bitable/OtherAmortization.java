package com.lundong.sync.entity.bitable.bitable;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 其他摊销
 *
 * @author shuangquan.chen
 * @date 2023-12-11 15:13
 */
@Data
public class OtherAmortization {

    @JSONField(name = "生成日期")
    private String generationDate;

    @JSONField(name = "是否已生成")
    private String hasGenerate;

    @JSONField(name = "摊销项目")
    private String amortizationItems;

    @JSONField(name = "供应商")
    private String supplierName;

    @JSONField(name = "所属摊销时间")
    private String correspondingAmortizationDate;

    @JSONField(name = "摊销金额")
    private String amount;
}
