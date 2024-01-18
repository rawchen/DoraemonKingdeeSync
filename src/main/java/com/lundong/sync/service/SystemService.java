package com.lundong.sync.service;

import com.lundong.sync.entity.ApprovalInstanceFormResult;

/**
 * @author RawChen
 * @date 2023-06-25 14:02
 */
public interface SystemService {


    /**
     * 处理审批单
     *
     * @param result
     * @param approvalCode
     * @param instanceCode
     * @param instanceOperateTime
     */
    String processApprovalForm(ApprovalInstanceFormResult result, String approvalCode, String instanceCode, String instanceOperateTime);

    /**
     * 新增多维表格日志
     *
     * @param result
     * @param save
     * @param instanceOperateTime
     */
    void insertRecordLog(ApprovalInstanceFormResult result, String save, String instanceOperateTime);
}
