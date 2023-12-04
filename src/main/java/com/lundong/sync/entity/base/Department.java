package com.lundong.sync.entity.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-29 17:22
 */
@Data
public class Department {

    @JSONField(name = "飞书部门")
    private String feishuDepartmentName;

    @JSONField(name = "部门编码")
    private String departmentCode;

    @JSONField(name = "金蝶部门")
    private String departmentName;
}
