package com.lundong.sync.controller;

import com.lundong.sync.entity.bitable.bitable.*;
import com.lundong.sync.enums.BitableTypeEnum;
import com.lundong.sync.service.BitableService;
import com.lundong.sync.util.SignUtil;
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

    @RequestMapping(value = "/create_voucher")
    public void createVoucher(@RequestParam("app_token") String appToken,
                              @RequestParam("table_id") String tableId,
                              @RequestParam("record_id") String recordId) throws Exception {
        log.info("生成参数: appToken: {}, tableId: {}, recordId: {}", appToken, tableId, recordId);
        switch (BitableTypeEnum.toType(tableId)) {
            case TABLE_ID_INCOME_ESTIMATION:
                IncomeEstimation baseRecord01 = SignUtil.findBaseRecord(appToken, tableId, recordId, IncomeEstimation.class);
                bitableService.processBitable(baseRecord01, appToken, tableId, recordId);
                break;
            case TABLE_ID_CONSUMPTION_ESTIMATION:
                ConsumptionEstimation baseRecord02 = SignUtil.findBaseRecord(appToken, tableId, recordId, ConsumptionEstimation.class);
                bitableService.processBitable(baseRecord02, appToken, tableId, recordId);
                break;
            case TABLE_ID_OTHER_AMORTIZATION:
                OtherAmortization baseRecord03 = SignUtil.findBaseRecord(appToken, tableId, recordId, OtherAmortization.class);
                bitableService.processBitable(baseRecord03, appToken, tableId, recordId);
                break;
            case TABLE_ID_RENT_PROPERTY_MANAGEMENT:
                RentPropertyManagement baseRecord04 = SignUtil.findBaseRecord(appToken, tableId, recordId, RentPropertyManagement.class);
                bitableService.processBitable(baseRecord04, appToken, tableId, recordId);
                break;
            case TABLE_ID_RENOVATION:
                Renovation baseRecord05 = SignUtil.findBaseRecord(appToken, tableId, recordId, Renovation.class);
                bitableService.processBitable(baseRecord05, appToken, tableId, recordId);
                break;

        }
    }
}
