package com.lundong.sync.entity.bitable.bitable;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 消耗暂估表
 *
 * @author shuangquan.chen
 * @date 2023-12-11 15:13
 */
@Data
public class ConsumptionEstimation {

    @JSONField(name = "店铺编码")
    private String shopCode;

    @JSONField(name = "店铺")
    private String shopName;

    @JSONField(name = "客户/公司承担")
    private String customerCompanyResponsible;

    @JSONField(name = "供应商编码")
    private String supplierCode;

    @JSONField(name = "供应商")
    private String supplierName;

    @JSONField(name = "消耗类型")
    private String consumptionType;

    @JSONField(name = "税率")
    private String taxRate;

    @JSONField(name = "含税金额")
    private String amountIncludingTaxAmount;

    @JSONField(name = "不含税金额")
    private String excludingTaxAmount;

    @JSONField(name = "生成日期")
    private String generationDate;

    @JSONField(name = "是否已生成")
    private String hasGenerate;
}
