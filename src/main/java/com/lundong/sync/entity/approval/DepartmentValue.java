package com.lundong.sync.entity.approval;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-12-03 10:59
 */
@Data
public class DepartmentValue {

    /**
     * name
     */
    @JSONField(name = "name")
    private String name;

    /**
     * openId
     */
    @JSONField(name = "open_id")
    private String openId;

}
