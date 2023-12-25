package com.lundong.sync.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 收入暂估修改状态枚举
 *
 * @author RawChen
 * @date 2023-08-02 15:12
 */
@Getter
@AllArgsConstructor
public enum StatusFieldEnum {
	CREATED         ("CREATED", 		"生成",   1),
	WRITE_OFF       ("WRITE_OFF", 	    "冲销",   2);

	private String type;
	private String desc;
	private Integer code;

	public static StatusFieldEnum getType(String dataTypeCode) {
		for (StatusFieldEnum enums : StatusFieldEnum.values()) {
			if (enums.getType().equals(dataTypeCode)) {
				return enums;
			}
		}
		return null;
	}
}
