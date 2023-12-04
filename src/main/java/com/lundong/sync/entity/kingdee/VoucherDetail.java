package com.lundong.sync.entity.kingdee;

import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-30 11:48
 */
@Data
public class VoucherDetail {

    /**
     * 摘要
     */
    private String explanation;

    /**
     * 科目编码
     */
    private String accountId;

    /**
     * 币别
     */
    private String currencyId;

    /**
     * 汇率类型
     */
    private String exchangeRateType;

    /**
     * 借方金额
     */
    private String debit;

    /**
     * 贷方金额
     */
    private String credit;

    /**
     * 原币金额
     */
    private String amountFor;

    /**
     * 核算维度
     */
    private AccountingDimension accountingDimension;
}
