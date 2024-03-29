package com.lundong.sync.controller;

import cn.hutool.core.util.StrUtil;
import com.lundong.sync.entity.ApprovalInstanceFormResult;
import com.lundong.sync.entity.BitableParam;
import com.lundong.sync.entity.base.SyncRecord;
import com.lundong.sync.entity.bitable.bitable.*;
import com.lundong.sync.enums.BitableTypeEnum;
import com.lundong.sync.service.BitableService;
import com.lundong.sync.service.SystemService;
import com.lundong.sync.util.SignUtil;
import com.lundong.sync.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shuangquan.chen
 * @date 2023-12-11 11:13
 */
@Slf4j
@RestController
@RequestMapping
public class BitableController {

    @Autowired
    BitableService bitableService;

    @Autowired
    SystemService systemService;

    @RequestMapping(value = "/create_voucher")
    public void createVoucher(@RequestParam("app_token") String appToken,
                              @RequestParam("table_id") String tableId,
                              @RequestParam("record_id") String recordId) throws Exception {
        BitableParam bitableParam = new BitableParam();
        bitableParam.setAppToken(appToken);
        bitableParam.setTableId(tableId);
        bitableParam.setRecordId(recordId);
        log.info("生成参数: appToken: {}, tableId: {}, recordId: {}", appToken, tableId, recordId);
        switch (BitableTypeEnum.toType(tableId)) {
            case TABLE_ID_INCOME_ESTIMATION:
                IncomeEstimation baseRecord01 = SignUtil.findBaseRecord(bitableParam, IncomeEstimation.class);
                bitableService.processBitable(baseRecord01, bitableParam);
                break;
            case TABLE_ID_CONSUMPTION_ESTIMATION:
                ConsumptionEstimation baseRecord02 = SignUtil.findBaseRecord(bitableParam, ConsumptionEstimation.class);
                bitableService.processBitable(baseRecord02, bitableParam);
                break;
            case TABLE_ID_OTHER_AMORTIZATION:
                OtherAmortization baseRecord03 = SignUtil.findBaseRecord(bitableParam, OtherAmortization.class);
                bitableService.processBitable(baseRecord03, bitableParam);
                break;
            case TABLE_ID_RENT_PROPERTY_MANAGEMENT:
                DeferRentPropertyManagement baseRecord04 = SignUtil.findBaseRecord(bitableParam, DeferRentPropertyManagement.class);
                bitableService.processBitable(baseRecord04, bitableParam);
                break;
            case TABLE_ID_RENOVATION:
                DeferRenovation baseRecord05 = SignUtil.findBaseRecord(bitableParam, DeferRenovation.class);
                bitableService.processBitable(baseRecord05, bitableParam);
                break;

        }
    }

    @RequestMapping(value = "write_off")
    public void writeOff(@RequestParam("app_token") String appToken,
                              @RequestParam("table_id") String tableId,
                              @RequestParam("record_id") String recordId) throws Exception {
        BitableParam bitableParam = new BitableParam();
        bitableParam.setAppToken(appToken);
        bitableParam.setTableId(tableId);
        bitableParam.setRecordId(recordId);
        log.info("生成参数: appToken: {}, tableId: {}, recordId: {}", appToken, tableId, recordId);
        if (BitableTypeEnum.toType(tableId) == BitableTypeEnum.TABLE_ID_INCOME_ESTIMATION) {
            IncomeEstimation baseRecord01 = SignUtil.findBaseRecord(bitableParam, IncomeEstimation.class);
            bitableService.processBitableWriteOff(baseRecord01, bitableParam);
        } else if (BitableTypeEnum.toType(tableId) == BitableTypeEnum.TABLE_ID_CONSUMPTION_ESTIMATION) {
            ConsumptionEstimation baseRecord01 = SignUtil.findBaseRecord(bitableParam, ConsumptionEstimation.class);
            bitableService.processBitableWriteOff(baseRecord01, bitableParam);
        }
    }

    @RequestMapping(value = "recommend")
    public void recommend(@RequestParam("app_token") String appToken,
                         @RequestParam("table_id") String tableId,
                         @RequestParam("record_id") String recordId) throws Exception {
        log.info("重推参数: appToken: {}, tableId: {}, recordId: {}", appToken, tableId, recordId);
        if (StrUtil.isNotEmpty(appToken) && StrUtil.isNotEmpty(tableId) && StrUtil.isNotEmpty(recordId)) {
            BitableParam bitableParam = new BitableParam();
            bitableParam.setAppToken(appToken);
            bitableParam.setTableId(tableId);
            bitableParam.setRecordId(recordId);
            SyncRecord baseRecord01 = SignUtil.findBaseRecord(bitableParam, SyncRecord.class);
            if ("已同步".equals(baseRecord01.getSyncType())) {
                log.info("已成功生成过该审批对应的凭证: {}, bitableParam: {}", baseRecord01.getInstanceCode(), bitableParam);
            } else {
                // 获取审批保存
                ApprovalInstanceFormResult result = StringUtil.instanceToFormList(baseRecord01.getInstanceCode());
                String save = systemService.processApprovalForm(result, baseRecord01.getApprovalName(),
                        baseRecord01.getInstanceCode(),
                        StringUtil.yearMonthDayHourMinuteSecondToTimestamp(baseRecord01.getInstanceOperateTime()));
                SignUtil.updateHasGenerate(save, bitableParam);
            }
        }
    }
}
