package com.lundong.sync.entity.bitable.approval;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-29 16:03
 */
@Data
public class PersonalReimbursementOne {

    @JSONField(name = "费用大类")
    private String costCategory;

    @JSONField(name = "费用子类")
    private String costSubcategory;

    @JSONField(name = "摘要")
    private String summary;

    @JSONField(name = "科目编码")
    private String accountCode;

    @JSONField(name = "科目名称")
    private String accountName;

    @JSONField(name = "字段1")
    private String fieldOne;

    @JSONField(name = "字段2")
    private String fieldTwo;

}
