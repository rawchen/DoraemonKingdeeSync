package com.lundong.sync.entity.bitable.bitable;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-12-11 15:14
 */
@Data
public class DeferRenovation {

    @JSONField(name = "摊销项目")
    private String amortizationItems;

    @JSONField(name = "供应商")
    private String supplierName;

    @JSONField(name = "借方入账科目编码")
    private String debitAccountCode;

    @JSONField(name = "借方入账科目名称")
    private String debitAccountName;

    @JSONField(name = "借方核算维度")
    private String debitAccountingDimension;

    @JSONField(name = "店铺编码")
    private String shopCode;

    @JSONField(name = "店铺名称")
    private String shopName;

    @JSONField(name = "部门编码")
    private String departmentCode;

    @JSONField(name = "部门名称")
    private String departmentName;

    @JSONField(name = "贷方入账科目编码")
    private String creditAccountCode;

    @JSONField(name = "贷方入账科目名称")
    private String creditAccountName;

    @JSONField(name = "贷方核算维度")
    private String creditAccountingDimension;

    @JSONField(name = "新业务组编码")
    private String businessCode;

    @JSONField(name = "新业务组名称")
    private String businessName;

    @JSONField(name = "所属摊销时间")
    private String correspondingAmortizationDate;

    @JSONField(name = "摊销金额")
    private String amount;

    @JSONField(name = "生成日期")
    private String generationDate;

    @JSONField(name = "是否已生成")
    private String hasGenerate;

}
