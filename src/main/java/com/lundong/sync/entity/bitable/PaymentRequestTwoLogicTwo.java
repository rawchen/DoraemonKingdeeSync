package com.lundong.sync.entity.bitable;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 付款申请第二张（逻辑2映射表）
 *
 * @author shuangquan.chen
 * @date 2023-11-29 11:44
 */
@Data
public class PaymentRequestTwoLogicTwo {

    @JSONField(name = "费用大类")
    private String costCategory;

    @JSONField(name = "费用子类")
    private String costSubcategory;

    @JSONField(name = "所属品牌")
    private String brand;

    @JSONField(name = "摘要")
    private String summary;

    @JSONField(name = "借方科目编码1")
    private String debitAccountCodeOne;

    @JSONField(name = "借方科目名称1")
    private String debitAccountNameOne;

    @JSONField(name = "借方核算维度1")
    private String debitAccountingDimensionOne;

    @JSONField(name = "贷方科目编码")
    private String creditAccountCode;

    @JSONField(name = "贷方科目名称")
    private String creditAccountName;

    @JSONField(name = "贷方核算维度")
    private String creditAccountingDimension;

    
}
