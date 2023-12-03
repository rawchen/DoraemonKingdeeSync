package com.lundong.sync.entity.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-29 17:22
 */
@Data
public class Department {

    @JSONField(name = "部门编码")
    private String departmentCode;

    @JSONField(name = "部门名称")
    private String departmentName;
}
