package com.lundong.sync.entity.kingdee;

import com.lundong.sync.entity.BitableParam;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2023-11-30 10:47
 */
@Data
@Accessors(chain = true)
public class Voucher {

    /**
     * 账簿
     */
    private String number;

    /**
     * 账期日期
     */
    private String date;

    /**
     * 业务日期
     */
    private String busDate;

    /**
     * 会计年度
     */
    private String year;

    /**
     * 期间
     */
    private String period;

    /**
     * 凭证字
     */
    private String voucherGroupId;

    /**
     * 凭证号
     */
    private String voucherGroupNumber;

    /**
     * 审核状态
     */
    private String documentStatus;

    /**
     * 凭证明细
     */
    private List<VoucherDetail> voucherDetails;

    /**
     * 飞书流程号
     */
    private String serialNumber;

    /**
     * 审批名称
     */
    private String approvalName;

    /**
     * 多维表格参数
     */
    private BitableParam bitableParam;
}
