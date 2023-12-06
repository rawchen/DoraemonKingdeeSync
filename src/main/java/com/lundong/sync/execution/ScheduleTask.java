package com.lundong.sync.execution;

import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.base.*;
import com.lundong.sync.entity.bitable.SecondExceptionTable;
import com.lundong.sync.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Spring Boot定时任务
 *
 * @author RawChen
 * @date 2023-12-03 17:21
 */
@Slf4j
@Component
@EnableScheduling
public class ScheduleTask {

    /**
     * 启动执行一次 && 每1小时执行一次刷新access_token
     */
    @Scheduled(fixedRate = 3600 * 1000)
//	@Scheduled(fixedRate = 30 * 1000)
    private void scheduleTask() {
        // 初始化到内存
        Constants.LIST_TABLE_01 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_01, Bitable.class);
        Constants.LIST_TABLE_02 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_02, BrandShopBusiness.class);
        Constants.LIST_TABLE_03 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_03, Account.class);
        Constants.LIST_TABLE_04 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_04, Supplier.class);
        Constants.LIST_TABLE_05 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_05, Employee.class);
        Constants.LIST_TABLE_06 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_06, Custom.class);
        Constants.LIST_TABLE_07 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_07, Department.class);
        Constants.LIST_TABLE_08 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_08, SecondExceptionTable.class);
        Constants.LIST_TABLE_09 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_09, Bitable.class);
        Constants.LIST_TABLE_10 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_10, Bitable.class);
        Constants.LIST_TABLE_11 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_11, Bitable.class);
        Constants.LIST_TABLE_12 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_12, Bitable.class);
        Constants.LIST_TABLE_13 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_13, Bitable.class);
        Constants.LIST_TABLE_14 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_14, Bitable.class);
        Constants.LIST_TABLE_15 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_15, Bitable.class);
        Constants.LIST_TABLE_16 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_16, Bitable.class);
        Constants.LIST_TABLE_17 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_17, Bitable.class);
        Constants.LIST_TABLE_18 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_18, Bitable.class);
        Constants.LIST_TABLE_19 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_19, Bitable.class);

        log.info("初始化或刷新映射表成功");
    }
}
