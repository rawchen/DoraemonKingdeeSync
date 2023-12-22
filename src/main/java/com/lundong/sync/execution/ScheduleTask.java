package com.lundong.sync.execution;

import cn.hutool.core.util.StrUtil;
import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.base.*;
import com.lundong.sync.entity.bitable.approval.SecondExceptionTable;
import com.lundong.sync.util.ArrayUtil;
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
     * 启动延迟4秒执行一次 && 每1小时执行一次
     * 获取最新的多维表格映射表记录到内存
     */
    @Scheduled(initialDelay = 4000, fixedRate = 3600 * 1000)
//	@Scheduled(fixedRate = 30 * 1000)
    private void scheduleTask() {
        // 初始化到内存
        List<Bitable> table01 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_01, Bitable.class);
        List<BrandShopBusiness> table02 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_02, BrandShopBusiness.class);
        List<Account> table03 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_03, Account.class);
        List<Supplier> table04 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_04, Supplier.class);
        List<Employee> table05 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_05, Employee.class);
        List<Custom> table06 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_06, Custom.class);
        List<Department> table07 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_07, Department.class);
        List<SecondExceptionTable> table08 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_08, SecondExceptionTable.class);
        List<Bitable> table09 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_09, Bitable.class);
        List<Bitable> table10 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_10, Bitable.class);
        List<Bitable> table11 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_11, Bitable.class);
        List<Bitable> table12 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_12, Bitable.class);
        List<Bitable> table13 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_13, Bitable.class);
        List<Bitable> table14 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_14, Bitable.class);
        List<Bitable> table15 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_15, Bitable.class);
        List<Bitable> table16 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_16, Bitable.class);
        List<Bitable> table17 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_17, Bitable.class);
        List<Bitable> table18 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_18, Bitable.class);
        List<Bitable> table19 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_19, Bitable.class);
        List<Bitable> table20 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_20, Bitable.class);
        List<Bitable> table21 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_21, Bitable.class);
        List<Bitable> table22 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_22, Bitable.class);
        List<Bitable> table23 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_23, Bitable.class);
        List<Bitable> table24 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_24, Bitable.class);
        List<Bitable> table25 = SignUtil.findBaseList(Constants.APP_TOKEN_APPROVAL, Constants.TABLE_25, Bitable.class);
        List<BrandShopBusiness> table26 = SignUtil.findBaseList(Constants.APP_TOKEN_BITABLE, Constants.TABLE_26, BrandShopBusiness.class);
        List<Bitable> table27 = SignUtil.findBaseList(Constants.APP_TOKEN_BITABLE, Constants.TABLE_27, Bitable.class);
        List<BrandShopBusiness> table28 = SignUtil.findBaseList(Constants.APP_TOKEN_BITABLE, Constants.TABLE_28, BrandShopBusiness.class);
        List<Bitable> table29 = SignUtil.findBaseList(Constants.APP_TOKEN_BITABLE, Constants.TABLE_29, Bitable.class);
        List<BrandShopBusiness> table30 = SignUtil.findBaseList(Constants.APP_TOKEN_BITABLE, Constants.TABLE_30, BrandShopBusiness.class);
        List<Bitable> table31 = SignUtil.findBaseList(Constants.APP_TOKEN_BITABLE, Constants.TABLE_31, Bitable.class);

        if (!ArrayUtil.isEmpty(table01)) Constants.LIST_TABLE_01 = table01;
        if (!ArrayUtil.isEmpty(table02)) Constants.LIST_TABLE_02 = table02;
        if (!ArrayUtil.isEmpty(table03)) Constants.LIST_TABLE_03 = table03;
        if (!ArrayUtil.isEmpty(table04)) Constants.LIST_TABLE_04 = table04;
        if (!ArrayUtil.isEmpty(table05)) Constants.LIST_TABLE_05 = table05;
        if (!ArrayUtil.isEmpty(table06)) Constants.LIST_TABLE_06 = table06;
        if (!ArrayUtil.isEmpty(table07)) Constants.LIST_TABLE_07 = table07;
        if (!ArrayUtil.isEmpty(table08)) Constants.LIST_TABLE_08 = table08;
        if (!ArrayUtil.isEmpty(table09)) Constants.LIST_TABLE_09 = table09;
        if (!ArrayUtil.isEmpty(table10)) Constants.LIST_TABLE_10 = table10;
        if (!ArrayUtil.isEmpty(table11)) Constants.LIST_TABLE_11 = table11;
        if (!ArrayUtil.isEmpty(table12)) Constants.LIST_TABLE_12 = table12;
        if (!ArrayUtil.isEmpty(table13)) Constants.LIST_TABLE_13 = table13;
        if (!ArrayUtil.isEmpty(table14)) Constants.LIST_TABLE_14 = table14;
        if (!ArrayUtil.isEmpty(table15)) Constants.LIST_TABLE_15 = table15;
        if (!ArrayUtil.isEmpty(table16)) Constants.LIST_TABLE_16 = table16;
        if (!ArrayUtil.isEmpty(table17)) Constants.LIST_TABLE_17 = table17;
        if (!ArrayUtil.isEmpty(table18)) Constants.LIST_TABLE_18 = table18;
        if (!ArrayUtil.isEmpty(table19)) Constants.LIST_TABLE_19 = table19;
        if (!ArrayUtil.isEmpty(table20)) Constants.LIST_TABLE_20 = table20;
        if (!ArrayUtil.isEmpty(table21)) Constants.LIST_TABLE_21 = table21;
        if (!ArrayUtil.isEmpty(table22)) Constants.LIST_TABLE_22 = table22;
        if (!ArrayUtil.isEmpty(table23)) Constants.LIST_TABLE_23 = table23;
        if (!ArrayUtil.isEmpty(table24)) Constants.LIST_TABLE_24 = table24;
        if (!ArrayUtil.isEmpty(table25)) Constants.LIST_TABLE_25 = table25;
        if (!ArrayUtil.isEmpty(table26)) Constants.LIST_TABLE_26 = table26;
        if (!ArrayUtil.isEmpty(table27)) Constants.LIST_TABLE_27 = table27;
        if (!ArrayUtil.isEmpty(table28)) Constants.LIST_TABLE_28 = table28;
        if (!ArrayUtil.isEmpty(table29)) Constants.LIST_TABLE_29 = table29;
        if (!ArrayUtil.isEmpty(table30)) Constants.LIST_TABLE_30 = table30;
        if (!ArrayUtil.isEmpty(table31)) Constants.LIST_TABLE_31 = table31;
        log.info("初始化或刷新映射表成功");
    }

    /**
     * 每隔10分钟刷新一个token
     */
    @Scheduled(initialDelay = 10 * 60 * 1000, fixedRate = 10 * 60 * 1000)
    private void scheduleRefreshToken() {
        log.info("重新获得一个tenant_access_token");
        String accessToken = SignUtil.getAccessToken(Constants.APP_ID_FEISHU, Constants.APP_SECRET_FEISHU);
        if (!StrUtil.isEmpty(accessToken)) {
            Constants.ACCESS_TOKEN = accessToken;
        }
    }
}
