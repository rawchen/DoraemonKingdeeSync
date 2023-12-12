package com.lundong.sync.entity;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-12-12 18:20
 */
@Data
public class BitableParam {

    @Alias("app_token")
    private String appToken;

    @Alias("table_id")
    private String tableId;

    @Alias("record_id")
    private String recordId;

}
