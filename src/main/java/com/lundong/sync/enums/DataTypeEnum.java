package com.lundong.sync.enums;

import com.lundong.sync.config.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 单据类型枚举
 *
 * @author RawChen
 * @date 2023-08-02 15:12
 */
@Getter
@AllArgsConstructor
public enum DataTypeEnum {
	PAYMENT_PERSONAL_PREPAID("PAYMENT_REQUEST", 		"付款申请&个人报销&预付申请",   Constants.PAYMENT_PERSONAL_PREPAID_APPROVAL_CODE),
	INVOICING_APPLICATION	("INVOICING_APPLICATION", 	"开票申请",                  Constants.INVOICING_APPLICATION_APPROVAL_CODE),
	INVOICE_WRITE_OFF	    ("INVOICE_WRITE_OFF", 	    "发票核销",                  Constants.INVOICE_WRITE_OFF_APPROVAL_CODE),
	WITHHOLDING_APPLICATION	("WITHHOLDING_APPLICATION", "预提申请",                  Constants.WITHHOLDING_APPLICATION_APPROVAL_CODE),
	REFUND_APPLICATION	    ("REFUND_APPLICATION", 	    "退款申请",                  Constants.REFUND_APPLICATION_APPROVAL_CODE);

	private String type;
	private String desc;
	private String code;

	public static DataTypeEnum getType(String dataTypeCode) {
		for (DataTypeEnum enums : DataTypeEnum.values()) {
			if (enums.getType().equals(dataTypeCode)) {
				return enums;
			}
		}
		return null;
	}

	public static DataTypeEnum toType(String code) {
		return Stream.of(DataTypeEnum.values()) .filter(p -> p.getCode().equals(code)).findAny().orElse(null);
	}
}
