package com.lundong.sync.service;

import com.lundong.sync.entity.approval.ApprovalInstanceForm;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author RawChen
 * @date 2023-06-25 14:02
 */
public interface SystemService {


    /**
     * 处理审批单
     *
     * @param forms
     * @param approvalCode
     */
    void processApprovalForm(List<ApprovalInstanceForm> forms, LocalDateTime operateTime, String approvalCode, String serialNumber);
}
