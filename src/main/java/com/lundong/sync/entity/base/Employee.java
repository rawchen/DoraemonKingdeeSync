package com.lundong.sync.entity.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-11-29 17:22
 */
@Data
public class Employee {

    @JSONField(name = "员工编码")
    private String employeeCode;

    @JSONField(name = "员工名称")
    private String employeeName;
}
