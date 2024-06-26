package com.lundong.sync.service.impl;

import cn.hutool.core.util.StrUtil;
import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.AccountingDimensionParam;
import com.lundong.sync.entity.BitableParam;
import com.lundong.sync.entity.base.Bitable;
import com.lundong.sync.entity.base.BrandShopBusiness;
import com.lundong.sync.entity.bitable.bitable.*;
import com.lundong.sync.entity.kingdee.AccountingDimension;
import com.lundong.sync.entity.kingdee.Voucher;
import com.lundong.sync.entity.kingdee.VoucherDetail;
import com.lundong.sync.enums.StatusFieldEnum;
import com.lundong.sync.enums.VoucherGroupIdEnum;
import com.lundong.sync.service.BitableService;
import com.lundong.sync.util.ArrayUtil;
import com.lundong.sync.util.SignUtil;
import com.lundong.sync.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shuangquan.chen
 * @date 2023-12-11 14:59
 */
@Slf4j
@Service
public class BitableServiceImpl implements BitableService {

    @Override
    public <T> void processBitable(T bitable, BitableParam bitableParam) {
        if (bitable == null) {
            log.error("bitable参数为null");
            return;
        }
        Voucher voucher = new Voucher();
        voucher.setBitableParam(bitableParam);
        if (IncomeEstimation.class.isAssignableFrom(bitable.getClass())) {
            // 收入暂估
            IncomeEstimation incomeEstimation = (IncomeEstimation) bitable;
            if ("是".equals(incomeEstimation.getHasGenerate())) {
                log.info("已生成过该凭证: {}", bitableParam);
            } else {
                voucher = new Voucher().setBitableParam(bitableParam);
                List<Integer> timeList = StringUtil.timestampToYearMonthDay(incomeEstimation.getGenerationDate());
                int year = timeList.get(0);
                int month = timeList.get(1);
                int day = timeList.get(2);
                voucher.setDate(year + "-" + month + "-" + day);
                voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                VoucherDetail j1 = new VoucherDetail();
                VoucherDetail d1 = new VoucherDetail();
                VoucherDetail d2 = new VoucherDetail();

                List<BrandShopBusiness> accountingDimensionBaseList = Constants.LIST_TABLE_26;
                List<Bitable> accountMappingBaseList = Constants.LIST_TABLE_27;
                accountingDimensionBaseList = accountingDimensionBaseList.stream().filter(n -> incomeEstimation.getDesc().equals(n.getDesc())).collect(Collectors.toList());
                accountMappingBaseList = accountMappingBaseList.stream().filter(n -> incomeEstimation.getIncomeType().equals(n.getIncomeType())).collect(Collectors.toList());
                BrandShopBusiness bitableAccountingDimension;
                Bitable bitableAccountMapping;
                if (ArrayUtil.isEmpty(accountingDimensionBaseList) || accountingDimensionBaseList.size() > 1) {
                    log.error("存在争议的核算维度映射，请检查参数是否在映射表匹配。品牌：{}, bitableParam: {}", incomeEstimation.getDesc(), bitableParam);
                    return;
                } else {
                    bitableAccountingDimension = accountingDimensionBaseList.get(0);
                }
                if (ArrayUtil.isEmpty(accountMappingBaseList) || accountMappingBaseList.size() > 1) {
                    log.error("存在争议的科目映射，请检查参数是否在映射表匹配。收入类型：{}, bitableParam: {}", incomeEstimation.getIncomeType(), bitableParam);
                    return;
                } else {
                    bitableAccountMapping = accountMappingBaseList.get(0);
                }

                // 构建
                String explanation = ("收入暂估") +
                        "&" + bitableAccountingDimension.getCustomName() +
                        "&" + incomeEstimation.getDesc() +
                        "&" + incomeEstimation.getIncomeType() +
                        "&" + incomeEstimation.getYear() + incomeEstimation.getMonth();
                j1.setExplanation(explanation);
                d1.setExplanation(explanation);
                d2.setExplanation(explanation);

                j1.setAmountFor(incomeEstimation.getAmountIncludingTaxAmount());
                j1.setDebit(incomeEstimation.getAmountIncludingTaxAmount()); // 含税金额
                d1.setCredit(incomeEstimation.getExcludingTaxAmount()); // 不含税金额
                d2.setCredit(incomeEstimation.getTaxes()); // 税金

                List<VoucherDetail> voucherDetails = new ArrayList<>();
                // 借贷方科目编码名称维度组装
                j1.setAccountId(bitableAccountMapping.getDebitAccountCodeOne());
                String debitAccountingDimensionOne = bitableAccountMapping.getDebitAccountingDimensionOne();
                VoucherDetail voucherDetailDebitOne = getAccountingDimensionParam(bitableAccountingDimension, j1, debitAccountingDimensionOne);
                voucherDetails.add(voucherDetailDebitOne);

                d1.setAccountId(bitableAccountMapping.getCreditAccountCodeOne());
                String creditAccountingDimensionOne = bitableAccountMapping.getCreditAccountingDimensionOne();
                VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(bitableAccountingDimension, d1, creditAccountingDimensionOne);
                voucherDetails.add(voucherDetailCreditOne);

                d2.setAccountId(bitableAccountMapping.getCreditAccountCodeTwo());
                String creditAccountingDimensionTwo = bitableAccountMapping.getCreditAccountingDimensionTwo();
                VoucherDetail voucherDetailCreditTwo = getAccountingDimensionParam(bitableAccountingDimension, d2, creditAccountingDimensionTwo);
                voucherDetails.add(voucherDetailCreditTwo);

                voucher.setVoucherDetails(voucherDetails);
                SignUtil.updateHasGenerate(SignUtil.saveVoucher(voucher, incomeEstimation.getGenerationDate()), bitableParam, StatusFieldEnum.CREATED.getCode());
            }
        } else if (ConsumptionEstimation.class.isAssignableFrom(bitable.getClass())) {
            // 消耗暂估
            ConsumptionEstimation consumptionEstimation = (ConsumptionEstimation) bitable;
            if ("是".equals(consumptionEstimation.getHasGenerate())) {
                log.info("已生成过该凭证: {}", bitableParam);
            } else {
                voucher = new Voucher().setBitableParam(bitableParam);
                List<Integer> timeList = StringUtil.timestampToYearMonthDay(consumptionEstimation.getGenerationDate());
                int year = timeList.get(0);
                int month = timeList.get(1);
                int day = timeList.get(2);
                voucher.setDate(year + "-" + month + "-" + day);
                voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                VoucherDetail j1 = new VoucherDetail();
                VoucherDetail j2 = new VoucherDetail();
                VoucherDetail d1 = new VoucherDetail();
                VoucherDetail d2 = new VoucherDetail();

                List<BrandShopBusiness> accountingDimensionBaseList = Constants.LIST_TABLE_28;
                List<Bitable> accountMappingBaseList = Constants.LIST_TABLE_29;
                accountingDimensionBaseList = accountingDimensionBaseList.stream().filter(
                        n -> consumptionEstimation.getShopCode().equals(n.getShopCode())).collect(Collectors.toList());
                accountMappingBaseList = accountMappingBaseList.stream().filter(
                        n -> consumptionEstimation.getConsumptionType().equals(
                                n.getConsumptionType()) && consumptionEstimation.getCustomerCompanyResponsible().equals(
                                        n.getCustomerCompanyResponsible())).collect(Collectors.toList());
                BrandShopBusiness bitableAccountingDimension;
                Bitable bitableAccountMapping;
                if (ArrayUtil.isEmpty(accountingDimensionBaseList) || accountingDimensionBaseList.size() > 1) {
                    log.error("存在争议的核算维度映射，请检查参数是否在映射表匹配。店铺编码：{}, bitableParam: {}", consumptionEstimation.getShopCode(), bitableParam);
                    return;
                } else {
                    bitableAccountingDimension = accountingDimensionBaseList.get(0);
                }
                if (ArrayUtil.isEmpty(accountMappingBaseList) || accountMappingBaseList.size() > 1) {
                    log.error("存在争议的科目映射，请检查参数是否在映射表匹配。消耗类型：{}, 客户/公司承担：{}, bitableParam: {}",
                            consumptionEstimation.getConsumptionType(), consumptionEstimation.getCustomerCompanyResponsible(), bitableParam);
                    return;
                } else {
                    bitableAccountMapping = accountMappingBaseList.get(0);
                }

                // 构建
                String explanation = ("千川消耗") +
                        "&" + consumptionEstimation.getSupplierName() +
                        "&" + StringUtil.subBusinessName(bitableAccountingDimension.getBusinessName()) +
                        "&" + year + StringUtil.placeholderTwo(month);
                j1.setExplanation(explanation);
                j2.setExplanation(explanation);
                d1.setExplanation(explanation);
                d2.setExplanation(explanation);

                j1.setAmountFor(consumptionEstimation.getAmountIncludingTaxAmount());
                j1.setDebit(consumptionEstimation.getAmountIncludingTaxAmount()); // 含税金额
                d1.setCredit(consumptionEstimation.getAmountIncludingTaxAmount()); // 含税金额
                j2.setDebit(StringUtil.keepTwoDecimalPlaces(consumptionEstimation.getExcludingTaxAmount())); // 不含税金额
                d2.setCredit(StringUtil.keepTwoDecimalPlaces(consumptionEstimation.getExcludingTaxAmount())); // 不含税金额

                List<VoucherDetail> voucherDetails = new ArrayList<>();
                // 借贷方科目编码名称维度组装
                j1.setAccountId(bitableAccountMapping.getDebitAccountCodeOne());
                String debitAccountingDimensionOne = bitableAccountMapping.getDebitAccountingDimensionOne();
                VoucherDetail voucherDetailDebitOne = getAccountingDimensionParam(bitableAccountingDimension, j1, debitAccountingDimensionOne, consumptionEstimation.getSupplierCode());
                voucherDetails.add(voucherDetailDebitOne);

                d1.setAccountId(bitableAccountMapping.getCreditAccountCodeOne());
                String creditAccountingDimensionOne = bitableAccountMapping.getCreditAccountingDimensionOne();
                VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(bitableAccountingDimension, d1, creditAccountingDimensionOne, consumptionEstimation.getSupplierCode());
                voucherDetails.add(voucherDetailCreditOne);

                j2.setAccountId(bitableAccountMapping.getDebitAccountCodeTwo());
                String debitAccountingDimensionTwo = bitableAccountMapping.getDebitAccountingDimensionTwo();
                VoucherDetail voucherDetailDebitTwo = getAccountingDimensionParam(bitableAccountingDimension, j2, debitAccountingDimensionTwo, consumptionEstimation.getSupplierCode());
                voucherDetails.add(voucherDetailDebitTwo);

                d2.setAccountId(bitableAccountMapping.getCreditAccountCodeTwo());
                String creditAccountingDimensionTwo = bitableAccountMapping.getCreditAccountingDimensionTwo();
                VoucherDetail voucherDetailCreditTwo = getAccountingDimensionParam(bitableAccountingDimension, d2, creditAccountingDimensionTwo, consumptionEstimation.getSupplierCode());
                voucherDetails.add(voucherDetailCreditTwo);

                voucher.setVoucherDetails(voucherDetails);
                SignUtil.updateHasGenerate(SignUtil.saveVoucher(voucher, consumptionEstimation.getGenerationDate()), bitableParam, StatusFieldEnum.CREATED.getCode());
            }
        } else if (OtherAmortization.class.isAssignableFrom(bitable.getClass())) {
            // 其他摊销
            OtherAmortization otherAmortization = (OtherAmortization) bitable;
            if ("是".equals(otherAmortization.getHasGenerate())) {
                log.info("已生成过该凭证: {}", bitableParam);
            } else {
                voucher = new Voucher().setBitableParam(bitableParam);
                List<Integer> timeList = StringUtil.timestampToYearMonthDay(otherAmortization.getGenerationDate());
                int year = timeList.get(0);
                int month = timeList.get(1);
                int day = timeList.get(2);
                voucher.setDate(year + "-" + month + "-" + day);
                voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                VoucherDetail j1 = new VoucherDetail();
                VoucherDetail d1 = new VoucherDetail();

                List<BrandShopBusiness> accountingDimensionBaseList = Constants.LIST_TABLE_30;
                List<Bitable> accountMappingBaseList = Constants.LIST_TABLE_31;
                accountingDimensionBaseList = accountingDimensionBaseList.stream().filter(
                        n -> otherAmortization.getAmortizationItems().equals(n.getAmortizationItems())).collect(Collectors.toList());
                accountMappingBaseList = accountMappingBaseList.stream().filter(
                        n -> otherAmortization.getAmortizationItems().equals(n.getAmortizationItems())).collect(Collectors.toList());
                BrandShopBusiness bitableAccountingDimension;
                Bitable bitableAccountMapping;
                if (ArrayUtil.isEmpty(accountingDimensionBaseList) || accountingDimensionBaseList.size() > 1) {
                    log.error("存在争议的核算维度映射，请检查参数是否在映射表匹配。摊销项目：{}, bitableParam: {}", otherAmortization.getAmortizationItems(), bitableParam);
                    return;
                } else {
                    bitableAccountingDimension = accountingDimensionBaseList.get(0);
                }
                if (ArrayUtil.isEmpty(accountMappingBaseList) || accountMappingBaseList.size() > 1) {
                    log.error("存在争议的科目映射，请检查参数是否在映射表匹配。摊销项目：{}, bitableParam: {}", otherAmortization.getAmortizationItems(), bitableParam);
                    return;
                } else {
                    bitableAccountMapping = accountMappingBaseList.get(0);
                }

                // 构建
                String explanation = ("摊销") +
                        "&" + otherAmortization.getSupplierName() +
                        "&" + otherAmortization.getAmortizationItems() +
                        "&" + StringUtil.placeholderTwo(StringUtil.timestampToYearMonthDay(otherAmortization.getCorrespondingAmortizationDate()).get(0)) +
                        StringUtil.placeholderTwo(StringUtil.timestampToYearMonthDay(otherAmortization.getCorrespondingAmortizationDate()).get(1));
                j1.setExplanation(explanation);
                d1.setExplanation(explanation);

                j1.setAmountFor(otherAmortization.getAmount());
                j1.setDebit(otherAmortization.getAmount());
                d1.setCredit(otherAmortization.getAmount());

                List<VoucherDetail> voucherDetails = new ArrayList<>();
                // 借贷方科目编码名称维度组装
                j1.setAccountId(bitableAccountMapping.getDebitAccountCodeOne());
                String debitAccountingDimensionOne = bitableAccountMapping.getDebitAccountingDimensionOne();
                VoucherDetail voucherDetailDebitOne = getAccountingDimensionParam(bitableAccountingDimension, j1, debitAccountingDimensionOne);
                voucherDetails.add(voucherDetailDebitOne);

                d1.setAccountId(bitableAccountMapping.getCreditAccountCodeOne());
                String creditAccountingDimensionOne = bitableAccountMapping.getCreditAccountingDimensionOne();
                VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(bitableAccountingDimension, d1, creditAccountingDimensionOne);
                voucherDetails.add(voucherDetailCreditOne);

                voucher.setVoucherDetails(voucherDetails);
                SignUtil.updateHasGenerate(SignUtil.saveVoucher(voucher, otherAmortization.getGenerationDate()), bitableParam, StatusFieldEnum.CREATED.getCode());
            }
        } else if (DeferRentPropertyManagement.class.isAssignableFrom(bitable.getClass())) {
            // 房租及物业-待摊
            DeferRentPropertyManagement defer = (DeferRentPropertyManagement) bitable;
            if ("是".equals(defer.getHasGenerate())) {
                log.info("已生成过该凭证: {}", bitableParam);
            } else {
                voucher = new Voucher().setBitableParam(bitableParam);
                List<Integer> timeList = StringUtil.timestampToYearMonthDay(defer.getGenerationDate());
                int year = timeList.get(0);
                int month = timeList.get(1);
                int day = timeList.get(2);
                voucher.setDate(year + "-" + month + "-" + day);
                voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                VoucherDetail j1 = new VoucherDetail();
                VoucherDetail d1 = new VoucherDetail();

                // 构建
                String explanation = ("摊销") +
                        "&" + defer.getSupplierName() +
                        "&" + defer.getAmortizationItems() +
                        "&" + StringUtil.subShopName(defer.getShopName()) +
                        "&" + StringUtil.placeholderTwo(StringUtil.timestampToYearMonthDay(defer.getCorrespondingAmortizationDate()).get(0)) +
                        StringUtil.placeholderTwo(StringUtil.timestampToYearMonthDay(defer.getCorrespondingAmortizationDate()).get(1));
                j1.setExplanation(explanation);
                d1.setExplanation(explanation);

                j1.setAmountFor(defer.getAmount());
                j1.setDebit(defer.getAmount());
                d1.setCredit(defer.getAmount());

                List<VoucherDetail> voucherDetails = new ArrayList<>();
                // 借贷方科目编码名称维度组装
                j1.setAccountId(defer.getDebitAccountCode());
                j1.setAccountingDimension(getAccountingDimensionNotMapping(defer, defer.getDebitAccountingDimension()));
                voucherDetails.add(j1);

                d1.setAccountId(defer.getCreditAccountCode());
                d1.setAccountingDimension(getAccountingDimensionNotMapping(defer, defer.getCreditAccountingDimension()));
                voucherDetails.add(d1);

                voucher.setVoucherDetails(voucherDetails);
                SignUtil.updateHasGenerate(SignUtil.saveVoucher(voucher, defer.getGenerationDate()), bitableParam, StatusFieldEnum.CREATED.getCode());
            }
        } else if (DeferRenovation.class.isAssignableFrom(bitable.getClass())) {
            // 长期待摊-装修
            DeferRenovation defer = (DeferRenovation) bitable;
            if ("是".equals(defer.getHasGenerate())) {
                log.info("已生成过该凭证: {}", bitableParam);
            } else {
                voucher = new Voucher().setBitableParam(bitableParam);
                List<Integer> timeList = StringUtil.timestampToYearMonthDay(defer.getGenerationDate());
                int year = timeList.get(0);
                int month = timeList.get(1);
                int day = timeList.get(2);
                voucher.setDate(year + "-" + month + "-" + day);
                voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                VoucherDetail j1 = new VoucherDetail();
                VoucherDetail d1 = new VoucherDetail();

                // 构建
                String explanation = ("摊销") +
                        "&" + defer.getSupplierName() +
                        "&" + defer.getAmortizationItems() +
                        "&" + StringUtil.subShopName(defer.getShopName()) +
                        "&" + StringUtil.placeholderTwo(StringUtil.timestampToYearMonthDay(defer.getCorrespondingAmortizationDate()).get(0)) +
                        StringUtil.placeholderTwo(StringUtil.timestampToYearMonthDay(defer.getCorrespondingAmortizationDate()).get(1));
                j1.setExplanation(explanation);
                d1.setExplanation(explanation);

                j1.setAmountFor(defer.getAmount());
                j1.setDebit(defer.getAmount());
                d1.setCredit(defer.getAmount());

                List<VoucherDetail> voucherDetails = new ArrayList<>();
                // 借贷方科目编码名称维度组装
                j1.setAccountId(defer.getDebitAccountCode());
                j1.setAccountingDimension(getAccountingDimensionNotMappingTwo(defer, defer.getDebitAccountingDimension()));
                voucherDetails.add(j1);

                d1.setAccountId(defer.getCreditAccountCode());
                d1.setAccountingDimension(getAccountingDimensionNotMappingTwo(defer, defer.getCreditAccountingDimension()));
                voucherDetails.add(d1);

                voucher.setVoucherDetails(voucherDetails);
                SignUtil.updateHasGenerate(SignUtil.saveVoucher(voucher, defer.getGenerationDate()), bitableParam, StatusFieldEnum.CREATED.getCode());
            }
        }
    }

    /**
     * 生成审批单并处理状态（冲销）
     *
     * @param bitable
     * @param bitableParam
     * @param <T>
     */
    @Override
    public <T> void processBitableWriteOff(T bitable, BitableParam bitableParam) {
        if (bitable == null) {
            log.error("bitable参数为null");
            return;
        }
        if (IncomeEstimation.class.isAssignableFrom(bitable.getClass())) {
            // 收入暂估冲销
            IncomeEstimation incomeEstimation = (IncomeEstimation) bitable;
            if ("是".equals(incomeEstimation.getHasWriteOff())) {
                log.info("已生成过该凭证: {}", bitableParam);
            } else {
                Voucher voucher = new Voucher().setBitableParam(bitableParam);
                List<Integer> timeList = StringUtil.timestampToYearMonthDay(incomeEstimation.getWriteOffDate());
                int year = timeList.get(0);
                int month = timeList.get(1);
                int day = timeList.get(2);
                voucher.setDate(year + "-" + month + "-" + day);
                voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                VoucherDetail j1 = new VoucherDetail();
                VoucherDetail d1 = new VoucherDetail();
                VoucherDetail d2 = new VoucherDetail();

                List<BrandShopBusiness> accountingDimensionBaseList = Constants.LIST_TABLE_26;
                List<Bitable> accountMappingBaseList = Constants.LIST_TABLE_27;
                accountingDimensionBaseList = accountingDimensionBaseList.stream().filter(n -> incomeEstimation.getDesc().equals(n.getDesc())).collect(Collectors.toList());
                accountMappingBaseList = accountMappingBaseList.stream().filter(n -> incomeEstimation.getIncomeType().equals(n.getIncomeType())).collect(Collectors.toList());
                BrandShopBusiness bitableAccountingDimension;
                Bitable bitableAccountMapping;
                if (ArrayUtil.isEmpty(accountingDimensionBaseList) || accountingDimensionBaseList.size() > 1) {
                    log.error("存在争议的核算维度映射，请检查参数是否在映射表匹配。品牌：{}, bitableParam: {}", incomeEstimation.getDesc(), bitableParam);
                    return;
                } else {
                    bitableAccountingDimension = accountingDimensionBaseList.get(0);
                }
                if (ArrayUtil.isEmpty(accountMappingBaseList) || accountMappingBaseList.size() > 1) {
                    log.error("存在争议的科目映射，请检查参数是否在映射表匹配。收入类型：{}, bitableParam: {}", incomeEstimation.getIncomeType(), bitableParam);
                    return;
                } else {
                    bitableAccountMapping = accountMappingBaseList.get(0);
                }

                // 构建
                String explanation = ("冲销收入暂估") +
                        "&" + bitableAccountingDimension.getCustomName() +
                        "&" + incomeEstimation.getDesc() +
                        "&" + incomeEstimation.getIncomeType() +
                        "&" + incomeEstimation.getYear() + incomeEstimation.getMonth();
                j1.setExplanation(explanation);
                d1.setExplanation(explanation);
                d2.setExplanation(explanation);
                j1.setAmountFor(StringUtil.negate(incomeEstimation.getAmountIncludingTaxAmount()));
                j1.setDebit(StringUtil.negate(incomeEstimation.getAmountIncludingTaxAmount())); // 含税金额
                d1.setCredit(StringUtil.negate(incomeEstimation.getExcludingTaxAmount())); // 不含税金额
                d2.setCredit(StringUtil.negate(incomeEstimation.getTaxes())); // 税金

                List<VoucherDetail> voucherDetails = new ArrayList<>();
                // 借贷方科目编码名称维度组装
                j1.setAccountId(bitableAccountMapping.getDebitAccountCodeOne());
                String debitAccountingDimensionOne = bitableAccountMapping.getDebitAccountingDimensionOne();
                VoucherDetail voucherDetailDebitOne = getAccountingDimensionParam(bitableAccountingDimension, j1, debitAccountingDimensionOne);
                voucherDetails.add(voucherDetailDebitOne);

                d1.setAccountId(bitableAccountMapping.getCreditAccountCodeOne());
                String creditAccountingDimensionOne = bitableAccountMapping.getCreditAccountingDimensionOne();
                VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(bitableAccountingDimension, d1, creditAccountingDimensionOne);
                voucherDetails.add(voucherDetailCreditOne);

                d2.setAccountId(bitableAccountMapping.getCreditAccountCodeTwo());
                String creditAccountingDimensionTwo = bitableAccountMapping.getCreditAccountingDimensionTwo();
                VoucherDetail voucherDetailCreditTwo = getAccountingDimensionParam(bitableAccountingDimension, d2, creditAccountingDimensionTwo);
                voucherDetails.add(voucherDetailCreditTwo);

                voucher.setVoucherDetails(voucherDetails);
                SignUtil.updateHasGenerate(SignUtil.saveVoucher(voucher, incomeEstimation.getWriteOffDate()), bitableParam, StatusFieldEnum.WRITE_OFF.getCode());
            }
        } else if (ConsumptionEstimation.class.isAssignableFrom(bitable.getClass())) {
            // 消耗暂估冲销
            ConsumptionEstimation consumptionEstimation = (ConsumptionEstimation) bitable;
            if ("是".equals(consumptionEstimation.getHasWriteOff())) {
                log.info("已生成过该凭证: {}", bitableParam);
            } else {
                Voucher voucher = new Voucher().setBitableParam(bitableParam);
                List<Integer> timeList = StringUtil.timestampToYearMonthDay(consumptionEstimation.getWriteOffDate());
                int year = timeList.get(0);
                int month = timeList.get(1);
                int day = timeList.get(2);
                voucher.setDate(year + "-" + month + "-" + day);
                voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                VoucherDetail j1 = new VoucherDetail();
                VoucherDetail j2 = new VoucherDetail();
                VoucherDetail d1 = new VoucherDetail();
                VoucherDetail d2 = new VoucherDetail();

                List<BrandShopBusiness> accountingDimensionBaseList = Constants.LIST_TABLE_28;
                List<Bitable> accountMappingBaseList = Constants.LIST_TABLE_29;
                accountingDimensionBaseList = accountingDimensionBaseList.stream().filter(
                        n -> consumptionEstimation.getShopCode().equals(n.getShopCode())).collect(Collectors.toList());
                accountMappingBaseList = accountMappingBaseList.stream().filter(
                        n -> consumptionEstimation.getConsumptionType().equals(
                                n.getConsumptionType()) && consumptionEstimation.getCustomerCompanyResponsible().equals(
                                n.getCustomerCompanyResponsible())).collect(Collectors.toList());
                BrandShopBusiness bitableAccountingDimension;
                Bitable bitableAccountMapping;
                if (ArrayUtil.isEmpty(accountingDimensionBaseList) || accountingDimensionBaseList.size() > 1) {
                    log.error("存在争议的核算维度映射，请检查参数是否在映射表匹配。店铺编码：{}, bitableParam: {}", consumptionEstimation.getShopCode(), bitableParam);
                    return;
                } else {
                    bitableAccountingDimension = accountingDimensionBaseList.get(0);
                }
                if (ArrayUtil.isEmpty(accountMappingBaseList) || accountMappingBaseList.size() > 1) {
                    log.error("存在争议的科目映射，请检查参数是否在映射表匹配。消耗类型：{}, 客户/公司承担：{}, bitableParam: {}",
                            consumptionEstimation.getConsumptionType(), consumptionEstimation.getCustomerCompanyResponsible(), bitableParam);
                    return;
                } else {
                    bitableAccountMapping = accountMappingBaseList.get(0);
                }

                // 构建
                List<Integer> timeListGen = StringUtil.timestampToYearMonthDay(consumptionEstimation.getGenerationDate());
                String explanation = ("冲销消耗暂估") +
                        "&" + consumptionEstimation.getSupplierName() +
                        "&" + StringUtil.subBusinessName(bitableAccountingDimension.getBusinessName()) +
                        "&" + timeListGen.get(0) + StringUtil.placeholderTwo(timeList.get(1));
                j1.setExplanation(explanation);
                j2.setExplanation(explanation);
                d1.setExplanation(explanation);
                d2.setExplanation(explanation);

                j1.setAmountFor(StringUtil.negate(consumptionEstimation.getAmountIncludingTaxAmount()));
                j1.setDebit(StringUtil.negate(consumptionEstimation.getAmountIncludingTaxAmount())); // 含税金额
                d1.setCredit(StringUtil.negate(consumptionEstimation.getAmountIncludingTaxAmount())); // 含税金额
                j2.setDebit(StringUtil.negate(StringUtil.keepTwoDecimalPlaces(consumptionEstimation.getExcludingTaxAmount()))); // 不含税金额
                d2.setCredit(StringUtil.negate(StringUtil.keepTwoDecimalPlaces(consumptionEstimation.getExcludingTaxAmount()))); // 不含税金额

                List<VoucherDetail> voucherDetails = new ArrayList<>();
                // 借贷方科目编码名称维度组装
                j1.setAccountId(bitableAccountMapping.getDebitAccountCodeOne());
                String debitAccountingDimensionOne = bitableAccountMapping.getDebitAccountingDimensionOne();
                VoucherDetail voucherDetailDebitOne = getAccountingDimensionParam(bitableAccountingDimension, j1, debitAccountingDimensionOne, consumptionEstimation.getSupplierCode());
                voucherDetails.add(voucherDetailDebitOne);

                d1.setAccountId(bitableAccountMapping.getCreditAccountCodeOne());
                String creditAccountingDimensionOne = bitableAccountMapping.getCreditAccountingDimensionOne();
                VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(bitableAccountingDimension, d1, creditAccountingDimensionOne, consumptionEstimation.getSupplierCode());
                voucherDetails.add(voucherDetailCreditOne);

                j2.setAccountId(bitableAccountMapping.getDebitAccountCodeTwo());
                String debitAccountingDimensionTwo = bitableAccountMapping.getDebitAccountingDimensionTwo();
                VoucherDetail voucherDetailDebitTwo = getAccountingDimensionParam(bitableAccountingDimension, j2, debitAccountingDimensionTwo, consumptionEstimation.getSupplierCode());
                voucherDetails.add(voucherDetailDebitTwo);

                d2.setAccountId(bitableAccountMapping.getCreditAccountCodeTwo());
                String creditAccountingDimensionTwo = bitableAccountMapping.getCreditAccountingDimensionTwo();
                VoucherDetail voucherDetailCreditTwo = getAccountingDimensionParam(bitableAccountingDimension, d2, creditAccountingDimensionTwo, consumptionEstimation.getSupplierCode());
                voucherDetails.add(voucherDetailCreditTwo);

                voucher.setVoucherDetails(voucherDetails);
                SignUtil.updateHasGenerate(SignUtil.saveVoucher(voucher, consumptionEstimation.getWriteOffDate()), bitableParam, StatusFieldEnum.WRITE_OFF.getCode());
            }
        }
    }

    private AccountingDimension getAccountingDimensionNotMapping(DeferRentPropertyManagement defer, String accountingDimensionStr) {
        if (defer == null || StrUtil.isEmpty(accountingDimensionStr)) {
            return new AccountingDimension();
        }
        AccountingDimension accountingDimension = new AccountingDimension();
        // 分割&品牌
        String[] accountingDimensionArr = accountingDimensionStr.split("&");
        // 遍历核算维度
        for (String s : accountingDimensionArr) {
            if ("店铺".equals(s)) {
                accountingDimension.setFf100002(defer.getShopCode());
            } else if ("新业务组".equals(s)) {
                accountingDimension.setFf100005(defer.getBusinessCode());
            } else if ("项目档案".equals(s)) {
                accountingDimension.setFf100004(defer.getProjectArchiveCode());
            } else if ("部门".equals(s)) {
                accountingDimension.setFflex5(defer.getDepartmentCode());
            }
        }
        StringUtil.setFieldEmpty(accountingDimension);
        return accountingDimension;
    }

    private AccountingDimension getAccountingDimensionNotMappingTwo(DeferRenovation defer, String accountingDimensionStr) {
        if (defer == null || StrUtil.isEmpty(accountingDimensionStr)) {
            return new AccountingDimension();
        }
        AccountingDimension accountingDimension = new AccountingDimension();
        // 分割&品牌
        String[] accountingDimensionArr = accountingDimensionStr.split("&");
        // 遍历核算维度
        for (String s : accountingDimensionArr) {
            if ("店铺".equals(s)) {
                accountingDimension.setFf100002(defer.getShopCode());
            } else if ("新业务组".equals(s)) {
                accountingDimension.setFf100005(defer.getBusinessCode());
            } else if ("部门".equals(s)) {
                accountingDimension.setFflex5(defer.getDepartmentCode());
            }
        }
        StringUtil.setFieldEmpty(accountingDimension);
        return accountingDimension;
    }

    private VoucherDetail getAccountingDimensionParam(BrandShopBusiness bitableAccountingDimension, VoucherDetail voucherDetail, String accountingDimension) {
        return getAccountingDimensionParam(bitableAccountingDimension, voucherDetail, accountingDimension, null);
    }

    /**
     * 组装核算维度参数
     *
     * @param bitableAccountingDimension    收入暂估核算维度映射表数据
     * @param voucherDetail                 金蝶凭证明细详情
     * @param accountingDimension           核算维度文本
     */
    private VoucherDetail getAccountingDimensionParam(BrandShopBusiness bitableAccountingDimension, VoucherDetail voucherDetail, String accountingDimension, String supplierCode) {
        AccountingDimensionParam param = new AccountingDimensionParam();
        param.setAccountingDimension(accountingDimension);
        param.setSupplierCode(supplierCode);
        AccountingDimension accountingDimensionDebitTwo = getAccountingDimension(param, bitableAccountingDimension);
        voucherDetail.setAccountingDimension(accountingDimensionDebitTwo);
        return voucherDetail;
    }

    /**
     * 分割&字符后查找并设置核算维度
     *
     * @param accountingDimensionParam
     * @return
     */
    public AccountingDimension getAccountingDimension(AccountingDimensionParam accountingDimensionParam, BrandShopBusiness bitableAccountingDimension) {
        AccountingDimension accountingDimension = new AccountingDimension();
        if (StrUtil.isEmpty(accountingDimensionParam.getAccountingDimension())) {
            return new AccountingDimension();
        }
        // 分割&品牌
        String[] accountingDimensionArr = accountingDimensionParam.getAccountingDimension().split("&");
        // 遍历核算维度
        for (String s : accountingDimensionArr) {
            if ("店铺".equals(s) || "新业务组".equals(s)) {
                if ("店铺".equals(s)) {
                    accountingDimension.setFf100002(bitableAccountingDimension.getShopCode());
                } else {
                    accountingDimension.setFf100005(bitableAccountingDimension.getBusinessCode());
                }
            } else if ("客户".equals(s)) {
                accountingDimension.setFflex6(bitableAccountingDimension.getCustomCode());
            } else if ("供应商".equals(s)) {
                accountingDimension.setFflex4(accountingDimensionParam.getSupplierCode());
            } else if ("部门".equals(s)) {
                accountingDimension.setFflex5(bitableAccountingDimension.getDepartmentCode());
            }
        }
        StringUtil.setFieldEmpty(accountingDimension);
        return accountingDimension;
    }
}
