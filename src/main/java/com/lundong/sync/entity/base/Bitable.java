package com.lundong.sync.entity.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author RawChen
 * @date 2023-08-10 13:40
 */
@Data
public class Bitable {

	@JSONField(name = "费用大类")
	private String costCategory;

	@JSONField(name = "费用子类")
	private String costSubcategory;

	@JSONField(name = "所属品牌")
	private String brand;

	@JSONField(name = "摘要")
	private String summary;

	@JSONField(name = "商品信息/服务信息")
	private String goodServiceInfo;

	@JSONField(name = "税率")
	private String taxRate;

	@JSONField(name = "借方科目编码1")
	private String debitAccountCodeOne;

	@JSONField(name = "借方科目名称1")
	private String debitAccountNameOne;

	@JSONField(name = "借方核算维度1")
	private String debitAccountingDimensionOne;

	@JSONField(name = "借方科目编码2")
	private String debitAccountCodeTwo;

	@JSONField(name = "借方科目名称2")
	private String debitAccountNameTwo;

	@JSONField(name = "借方核算维度2")
	private String debitAccountingDimensionTwo;

	@JSONField(name = "贷方科目编码1")
	private String creditAccountCodeOne;

	@JSONField(name = "贷方科目名称1")
	private String creditAccountNameOne;

	@JSONField(name = "贷方核算维度1")
	private String creditAccountingDimensionOne;

	@JSONField(name = "贷方科目编码2")
	private String creditAccountCodeTwo;

	@JSONField(name = "贷方科目名称2")
	private String creditAccountNameTwo;

	@JSONField(name = "贷方核算维度2")
	private String creditAccountingDimensionTwo;

	@JSONField(name = "收入类型")
	private String incomeType;

	@JSONField(name = "客户/公司承担")
	private String customerCompanyResponsible;

	@JSONField(name = "消耗类型")
	private String consumptionType;

	@JSONField(name = "摊销项目")
	private String amortizationItems;

}
