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
public enum BitableTypeEnum {
	TABLE_ID_INCOME_ESTIMATION          ("TABLE_ID_INCOME_ESTIMATION", 		    "收入暂估",   Constants.TABLE_ID_INCOME_ESTIMATION),
	TABLE_ID_CONSUMPTION_ESTIMATION	    ("TABLE_ID_CONSUMPTION_ESTIMATION", 	"消耗暂估",   Constants.TABLE_ID_CONSUMPTION_ESTIMATION),
	TABLE_ID_OTHER_AMORTIZATION	        ("TABLE_ID_OTHER_AMORTIZATION", 	    "其他摊销",   Constants.TABLE_ID_OTHER_AMORTIZATION),
	TABLE_ID_RENT_PROPERTY_MANAGEMENT	("TABLE_ID_RENT_PROPERTY_MANAGEMENT",   "房租及物业", Constants.TABLE_ID_RENT_PROPERTY_MANAGEMENT),
	TABLE_ID_RENOVATION	                ("TABLE_ID_RENOVATION", 	            "长期待摊",   Constants.TABLE_ID_RENOVATION);

	private String type;
	private String desc;
	private String code;

	public static BitableTypeEnum getType(String dataTypeCode) {
		for (BitableTypeEnum enums : BitableTypeEnum.values()) {
			if (enums.getType().equals(dataTypeCode)) {
				return enums;
			}
		}
		return null;
	}

	public static BitableTypeEnum toType(String code) {
		return Stream.of(BitableTypeEnum.values()) .filter(p -> p.getCode().equals(code)).findAny().orElse(null);
	}
}
