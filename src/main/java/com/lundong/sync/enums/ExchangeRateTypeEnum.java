package com.lundong.sync.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 汇率体系
 *
 * @author shuangquan.chen
 * @date 2023-12-01 10:28
 */
@Getter
@AllArgsConstructor
public enum ExchangeRateTypeEnum {

    HLTX01_SYS  ("HLTX01_SYS",  "固定汇率"),
    HLTX02_SYS  ("HLTX02_SYS",  "即期汇率"),
    HLTX03_SYS  ("HLTX03_SYS",  "预算汇率");

    private String type;
    private String desc;

    public static VoucherGroupIdEnum getType(String exchangeRateType) {
        for (VoucherGroupIdEnum enums : VoucherGroupIdEnum.values()) {
            if (enums.getType().equals(exchangeRateType)) {
                return enums;
            }
        }
        return null;
    }

}
