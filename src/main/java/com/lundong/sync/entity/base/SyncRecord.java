package com.lundong.sync.entity.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2024-01-09 15:41
 */
@Data
public class SyncRecord {

    @JSONField(name = "飞书申请编号")
    private String serialNumber;

    @JSONField(name = "审批实例ID")
    private String instanceCode;

    @JSONField(name = "审批名称")
    private String approvalName;

    @JSONField(name = "审批操作日期")
    private String instanceOperateTime;

    @JSONField(name = "同步状态")
    private String syncType;

    @JSONField(name = "错误信息")
    private String errorInfo;

    @JSONField(name = "重试状态")
    private String hasGenerate;

    @JSONField(name = "重试错误信息")
    private String retryErrorInfo;

    @JSONField(name = "重试日期")
    private String generationDate;

}
