package com.lundong.sync.entity.bitable.bitable;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 收入暂估表
 *
 * @author shuangquan.chen
 * @date 2023-12-11 14:01
 */
@Data
public class IncomeEstimation {

    @JSONField(name = "年")
    private String year;

    @JSONField(name = "月")
    private String month;

    @JSONField(name = "品牌")
    private String desc;

    @JSONField(name = "收入类型")
    private String incomeType;

    @JSONField(name = "不含税金额")
    private String excludingTaxAmount;

    @JSONField(name = "税率")
    private String taxRate;

    @JSONField(name = "含税金额")
    private String amountIncludingTaxAmount;

    @JSONField(name = "税金")
    private String taxes;

    @JSONField(name = "生成日期")
    private String generationDate;

    @JSONField(name = "是否已生成")
    private String hasGenerate;

    @JSONField(name = "是否已冲销")
    private String hasWriteOff;

    @JSONField(name = "冲销日期")
    private String writeOffDate;
}
