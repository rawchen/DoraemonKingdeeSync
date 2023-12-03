package com.lundong.sync.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author shuangquan.chen
 * @date 2023-12-01 10:20
 */
@Getter
@AllArgsConstructor
public enum CurrencyIdEnum {

    PRE001				("PRE001",			"人民币"),
    PRE002				("PRE002",			"香港元"),
    PRE003				("PRE003",			"欧元"),
    PRE004				("PRE004",			"日本日圆"),
    PRE005				("PRE005",			"新台币元"),
    PRE006				("PRE006",			"英镑"),
    PRE007				("PRE007",			"美元");

    private String type;
    private String desc;

    public static VoucherGroupIdEnum getType(String currencyId) {
        for (VoucherGroupIdEnum enums : VoucherGroupIdEnum.values()) {
            if (enums.getType().equals(currencyId)) {
                return enums;
            }
        }
        return null;
    }

}
