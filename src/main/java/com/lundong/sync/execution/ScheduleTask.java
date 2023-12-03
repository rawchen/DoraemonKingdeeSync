package com.lundong.sync.execution;

import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.base.*;
import com.lundong.sync.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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
        List<Bitable> table01 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_01, Bitable.class);
        Constants.LIST_TABLE_01 = table01;

        List<BrandShopBusiness> table02 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_02, BrandShopBusiness.class);
        Constants.LIST_TABLE_02 = table02;

        List<Account> table03 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_03, Account.class);
        Constants.LIST_TABLE_03 = table03;

        List<Supplier> table04 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_04, Supplier.class);
        Constants.LIST_TABLE_04 = table04;

        List<Employee> table05 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_05, Employee.class);
        Constants.LIST_TABLE_05 = table05;

        List<Custom> table06 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_06, Custom.class);
        Constants.LIST_TABLE_06 = table06;

        List<Department> table07 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_07, Department.class);
        Constants.LIST_TABLE_07 = table07;
        log.info("初始化或刷新映射表成功");
    }
}
