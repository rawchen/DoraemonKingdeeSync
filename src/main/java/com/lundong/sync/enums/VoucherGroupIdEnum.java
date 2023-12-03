package com.lundong.sync.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author shuangquan.chen
 * @date 2023-12-01 10:14
 */
@Getter
@AllArgsConstructor
public enum VoucherGroupIdEnum {

    PRE001				("PRE001",			"记"),
    PRE002				("PRE002",			"收"),
    PRE003				("PRE003",			"付"),
    PRE004				("PRE004",			"转");

    private String type;
    private String desc;

    public static VoucherGroupIdEnum getType(String voucherGroupId) {
        for (VoucherGroupIdEnum enums : VoucherGroupIdEnum.values()) {
            if (enums.getType().equals(voucherGroupId)) {
                return enums;
            }
        }
        return null;
    }

}
