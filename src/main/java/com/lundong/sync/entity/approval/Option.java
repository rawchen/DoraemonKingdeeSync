package com.lundong.sync.entity.approval;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-30 18:17
 */
@Data
public class Option {

    /**
     * key
     */
    @JSONField(name = "key")
    private String key;

    /**
     * text
     */
    @JSONField(name = "text")
    private String text;
}
