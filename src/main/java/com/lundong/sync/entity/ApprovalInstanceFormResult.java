package com.lundong.sync.entity;

import com.lundong.sync.entity.approval.ApprovalInstanceForm;
import lombok.Data;

import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2023-12-08 16:27
 */
@Data
public class ApprovalInstanceFormResult {

    private List<ApprovalInstanceForm> approvalInstanceForms;

    private ApprovalInstance approvalInstance;
}
