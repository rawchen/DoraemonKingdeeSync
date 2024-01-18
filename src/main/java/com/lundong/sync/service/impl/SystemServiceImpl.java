package com.lundong.sync.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.AccountingDimensionParam;
import com.lundong.sync.entity.ApprovalInstanceFormResult;
import com.lundong.sync.entity.approval.ApprovalInstanceForm;
import com.lundong.sync.entity.base.*;
import com.lundong.sync.entity.bitable.approval.SecondExceptionTable;
import com.lundong.sync.entity.kingdee.AccountingDimension;
import com.lundong.sync.entity.kingdee.Voucher;
import com.lundong.sync.entity.kingdee.VoucherDetail;
import com.lundong.sync.enums.DataTypeEnum;
import com.lundong.sync.enums.VoucherGroupIdEnum;
import com.lundong.sync.service.SystemService;
import com.lundong.sync.util.ArrayUtil;
import com.lundong.sync.util.SignUtil;
import com.lundong.sync.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author RawChen
 * @date 2023-06-25 14:02
 */
@Slf4j
@Service
public class SystemServiceImpl implements SystemService {


    /**
     * 处理审批单
     *
     * @param approvalCode        审批CODE
     * @param instanceCode        审批实例CODE
     * @param instanceOperateTime 操作时间时间戳13位
     */
    @Override
    public String processApprovalForm(ApprovalInstanceFormResult approvalInstanceFormResult, String approvalCode, String instanceCode, String instanceOperateTime) {
        String resultString = "";
        boolean formAllVoucher = true;

        if (approvalInstanceFormResult.getApprovalInstance() == null | ArrayUtil.isEmpty(approvalInstanceFormResult.getApprovalInstanceForms())) {
            return "获取审批实例为空";
        }

        long timestamp = Long.parseLong(instanceOperateTime);
        LocalDateTime operateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

        List<ApprovalInstanceForm> forms = approvalInstanceFormResult.getApprovalInstanceForms();
        String serialNumber = approvalInstanceFormResult.getApprovalInstance().getSerialNumber();

        if (ArrayUtil.isEmpty(forms)) {
            return "审批实例单据列表为空";
        }
        Voucher voucher = new Voucher();
        voucher.setSerialNumber(serialNumber);
        voucher.setApprovalName(DataTypeEnum.toType(approvalCode).getDesc());
        int year = operateTime.getYear();
        int month = operateTime.getMonthValue();
        int day = operateTime.getDayOfMonth();
        switch (DataTypeEnum.toType(approvalCode)) {
            case PAYMENT_PERSONAL_PREPAID:
                // 解析申请人为员工
                String employeeName = SignUtil.getFeishuUserName(StringUtil.getEmployValueByName(forms, "申请人"));
                // 付款申请&个人报销&预付申请同一个审批判断是哪一类
                // 获取审批中每个字段
                String approvalTypeName = StringUtil.getValueByName(forms, "申请类别");
                if ("付款申请".equals(approvalTypeName)) {
                    // 第一张生成逻辑（付款申请）
                    voucher = new Voucher().setSerialNumber(serialNumber).setApprovalName("付款申请第一张");
//                    voucher.setBusDate(year + "-" + month + "-" + day);
                    voucher.setDate(year + "-" + month + "-" + day);
                    voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE003.getType());
                    List<VoucherDetail> voucherDetails = new ArrayList<>();
                    List<List<ApprovalInstanceForm>> formListDetails = StringUtil.getFormDetails(forms, "付款明细");
                    if (ArrayUtil.isEmpty(formListDetails)) {
                        log.error("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                        return "付款申请审批第一张生成审批明细列表为空";
                    }
                    for (List<ApprovalInstanceForm> formDetails : formListDetails) {
                        VoucherDetail j = new VoucherDetail();
                        // 摘要（根据品牌是否核销不同）
                        String explanation = StringUtil.getExplanationName("付款申请", serialNumber, forms, formDetails);
                        j.setExplanation(explanation);

                        // 科目编码
                        String brandType;
                        if (!"总体管理".equals(StringUtil.getValueByName(formDetails, "所属品牌"))) {
                            brandType = "Other";
                        } else {
                            brandType = "总体管理";
                        }
                        List<Bitable> baseList = Constants.LIST_TABLE_01;
                        List<Bitable> bitableList = baseList.stream().filter(n -> StringUtil.getValueByName(formDetails, "费用大类").equals(n.getCostCategory())
                                && StringUtil.getValueByName(formDetails, "费用子类").equals(n.getCostSubcategory())
                                && brandType.equals(n.getBrand())
                        ).collect(Collectors.toList());
                        if (ArrayUtil.isEmpty(bitableList) || bitableList.size() > 1) {
                            log.error("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                    StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                        } else {
                            j.setAccountId(bitableList.get(0).getDebitAccountCodeOne());
                            // 映射借方核算维度1
                            // 根据借方核算维度文本转核算维度实体（赋好字段代码）
                            String debitAccountingDimensionOne = bitableList.get(0).getDebitAccountingDimensionOne();
                            getAccountingDimensionParam(forms, employeeName, formDetails, j, debitAccountingDimensionOne);
                        }
                        j.setDebit(StringUtil.getValueByName(formDetails, "金额"));
                        voucherDetails.add(j);
                    }
                    // 以上为通过明细获取的多个借方，以下为一个贷方
                    VoucherDetail d = new VoucherDetail();
                    String publicExplanation;
                    publicExplanation = "付款申请" + "&" + StringUtil.getValueByName(forms, "收款人（单位）全称") + "&" + serialNumber;
                    // 借贷如果都一条贷方摘要取借方的摘要
                    if (voucherDetails.size() > 1) {
                        d.setExplanation(publicExplanation);
                    } else {
                        d.setExplanation(voucherDetails.get(0).getExplanation());
                    }
                    d.setAccountId(StringUtil.getAccountCode(forms, "付款银行"));
                    d.setCredit(StringUtil.getValueByName(forms, "金额汇总"));
                    voucherDetails.add(d);
                    voucher.setVoucherDetails(voucherDetails);
                    String save01 = SignUtil.saveVoucher(voucher);
                    if (!"success".equals(save01)) {
                        formAllVoucher = false;
                        resultString += StrUtil.format("付款申请第一张凭证生成错误：【{}】 ", save01);
                    }

                    // 第二张生成逻辑（付款申请）
                    Voucher voucherTwo = new Voucher().setSerialNumber(serialNumber).setApprovalName("付款申请第二张");
                    voucherTwo.setDate(year + "-" + month + "-" + day);
                    voucherTwo.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                    List<VoucherDetail> voucherTwoDetails = new ArrayList<>();
                    // 判断子类是否属于以下项
                    for (List<ApprovalInstanceForm> formDetails : formListDetails) {
                        String costSubcategory = StringUtil.getValueByName(formDetails, "费用子类");
                        List<SecondExceptionTable> secondExceptionTables = Constants.LIST_TABLE_08;
                        List<String> secondExceptionTableStringList = secondExceptionTables.stream().map(SecondExceptionTable::getCostSubcategory).collect(Collectors.toList());
                        List<Bitable> listTable;
                        int genLogicType;
                        // 包含子类且固定资产/长期待摊
                        if (secondExceptionTableStringList.contains(costSubcategory) && "是".equals(StringUtil.getValueByName(formDetails, "固定资产/长期待摊"))) {
                            // 跳过该明细的借贷凭证列表
                            continue;
                        } else {
                            String summary = StringUtil.getValueByName(formDetails, "品牌是否核销");
                            String isTicketArrived = StringUtil.getValueByName(formDetails, "是否已到票");
                            // 判断品牌是否核销
                            if ("是".equals(summary)) {
                                // 判断是否到票
                                if ("是".equals(isTicketArrived)) {
                                    listTable = Constants.LIST_TABLE_09;
                                    genLogicType = 1;
                                } else {
                                    listTable = Constants.LIST_TABLE_10;
                                    genLogicType = 2;
                                }
                            } else {
                                if ("是".equals(isTicketArrived)) {
                                    listTable = Constants.LIST_TABLE_11;
                                    genLogicType = 3;
                                } else {
                                    listTable = Constants.LIST_TABLE_12;
                                    genLogicType = 4;
                                }
                            }
                        }
                        String brandType;
                        if (!"总体管理".equals(StringUtil.getValueByName(formDetails, "所属品牌"))) {
                            brandType = "Other";
                        } else {
                            brandType = "总体管理";
                        }
                        Bitable bitable;
                        List<Bitable> bitableList = listTable.stream().filter(n -> StringUtil.getValueByName(formDetails, "费用大类").equals(n.getCostCategory())
                                && StringUtil.getValueByName(formDetails, "费用子类").equals(n.getCostSubcategory())
                                && brandType.equals(n.getBrand())
                        ).collect(Collectors.toList());
                        if (ArrayUtil.isEmpty(bitableList) || bitableList.size() > 1) {
                            log.error("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                    StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                            return StrUtil.format("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                    StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                        } else {
                            bitable = bitableList.get(0);
                            String summary = bitable.getSummary();
                            // 摘要为空跳过该明细的借贷凭证列表
                            if (StrUtil.isBlank(summary)) {
                                continue;
                            }
                        }

                        VoucherDetail j1 = new VoucherDetail();
                        VoucherDetail j2 = new VoucherDetail();
                        VoucherDetail d1 = new VoucherDetail();
                        String explanation = (genLogicType == 1 || genLogicType == 3 ? "确认成本" : "暂估成本") +
                                "&" + StringUtil.getValueByName(forms, "收款人（单位）全称") +
                                "&" + StringUtil.getValueByName(formDetails, "所属品牌") +
                                "&" + StringUtil.getValueByName(formDetails, "费用归属年份") + StringUtil.getValueByName(formDetails, "费用归属月份") +
                                "&" + serialNumber +
                                "&" + StringUtil.getValueByName(formDetails, "费用大类") +
                                "&" + StringUtil.getValueByName(formDetails, "费用子类") + (genLogicType == 1 || genLogicType == 2 ? "&品牌核销" : "") +
                                "&" + StringUtil.getValueByName(formDetails, "备注");
                        j1.setExplanation(explanation);
                        j2.setExplanation(explanation);
                        d1.setExplanation(explanation);

                        j1.setAmountFor(StringUtil.getValueByName(formDetails, "金额"));
                        String debitAmount = StringUtil.calculateIncludeTax(StringUtil.getValueByName(formDetails, "金额"), StringUtil.getValueByName(formDetails, "税率"));
                        j1.setDebit(debitAmount);
                        String debitAmountTwo = StringUtil.calculateIncludeTaxTwo(StringUtil.getValueByName(formDetails, "金额"), StringUtil.getValueByName(formDetails, "税率"));
                        if (bitable.getDebitAccountCodeTwo() != null) {
                            j2.setDebit(debitAmountTwo);
                            d1.setCredit(StringUtil.getValueByName(formDetails, "金额"));
                        } else {
                            j2.setDebit("0");
                            d1.setCredit(debitAmount);
                        }

                        // 借贷方科目编码名称维度组装
                        j1.setAccountId(bitable.getDebitAccountCodeOne());
                        String debitAccountingDimensionOne = bitable.getDebitAccountingDimensionOne();
                        VoucherDetail voucherDetailDebitOne = getAccountingDimensionParam(forms, employeeName, formDetails, j1, debitAccountingDimensionOne);
                        voucherTwoDetails.add(voucherDetailDebitOne);
                        // 判断是否有第二张借（因为到票所以产生税金）
                        if (bitable.getDebitAccountCodeTwo() != null) { // 等同于 genLogicType == 1 || genLogicType == 3
                            j2.setAccountId(bitable.getDebitAccountCodeTwo());
                            String debitAccountingDimensionTwo = bitable.getDebitAccountingDimensionTwo();
                            VoucherDetail voucherDetailDebitTwo = getAccountingDimensionParam(forms, employeeName, formDetails, j2, debitAccountingDimensionTwo);
                            voucherTwoDetails.add(voucherDetailDebitTwo);
                        }
                        d1.setAccountId(bitable.getCreditAccountCodeOne());
                        String creditAccountingDimensionOne = bitable.getCreditAccountingDimensionOne();
                        VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(forms, employeeName, formDetails, d1, creditAccountingDimensionOne);
                        voucherTwoDetails.add(voucherDetailCreditOne);
                    }

                    if (!voucherTwoDetails.isEmpty()) {
                        voucherTwo.setVoucherDetails(voucherTwoDetails);
                        String save02 = SignUtil.saveVoucher(voucherTwo);
                        if (!"success".equals(save02)) {
                            formAllVoucher = false;
                            resultString += StrUtil.format("付款申请第二张凭证生成错误：【{}】 ", save02);
                        }
                    }
                } else if ("个人报销".equals(approvalTypeName)) {
                    // 第一张生成逻辑（个人报销）
                    voucher = new Voucher().setSerialNumber(serialNumber).setApprovalName("个人报销第一张");
                    voucher.setDate(year + "-" + month + "-" + day);
                    voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE003.getType());
                    List<VoucherDetail> voucherDetails = new ArrayList<>();
                    List<List<ApprovalInstanceForm>> formListDetails = StringUtil.getFormDetails(forms, "付款明细");
                    if (ArrayUtil.isEmpty(formListDetails)) {
                        log.error("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                        return StrUtil.format("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                    }
                    for (List<ApprovalInstanceForm> formDetails : formListDetails) {
                        VoucherDetail j = new VoucherDetail();
                        // 摘要（根据品牌是否核销不同）
                        String explanation = StringUtil.getExplanationName("个人报销", serialNumber, forms, formDetails);
                        j.setExplanation(explanation);

                        // 科目编码、核算维度（借方）
                        j.setAccountId(Constants.ACCOUNT_CODE_EMPLOYEE);
                        AccountingDimension accountingDimension = new AccountingDimension();
                        List<Employee> employees = Constants.LIST_TABLE_05;
                        for (Employee employee : employees) {
                            if (StringUtil.getValueByName(forms, "收款人（单位）全称").equals(employee.getEmployeeName())) {
                                accountingDimension.setFflex7(employee.getEmployeeCode());
                                break;
                            }
                        }
                        if (StrUtil.isEmpty(accountingDimension.getFflex7())) {
                            for (Employee employee : employees) {
                                if ("个人报销".equals(employee.getEmployeeName())) {
                                    accountingDimension.setFflex7(employee.getEmployeeCode());
                                    break;
                                }
                            }
                        }
                        if (StrUtil.isEmpty(accountingDimension.getFflex7())) {
                            log.error("个人报销第一张生成时映射表找不到名称为个人报销的核算维度：{}", StringUtil.getValueByName(forms, "收款人（单位）全称"));
                            return StrUtil.format("个人报销第一张生成时映射表找不到名称为个人报销的核算维度：{}", StringUtil.getValueByName(forms, "收款人（单位）全称"));
                        }
                        StringUtil.setFieldEmpty(accountingDimension);
                        j.setAccountingDimension(accountingDimension);
                        j.setAmountFor(StringUtil.getValueByName(formDetails, "金额"));
                        j.setDebit(StringUtil.getValueByName(formDetails, "金额"));
                        voucherDetails.add(j);
                    }
                    // 以上为通过明细获取的多个借方，以下为一个贷方
                    VoucherDetail d = new VoucherDetail();
                    String publicExplanation = "";
                    publicExplanation = "个人报销" + "&" + StringUtil.getValueByName(forms, "收款人（单位）全称") + "&" + serialNumber;
                    // 借贷如果都一条贷方摘要取借方的摘要
                    if (voucherDetails.size() > 1) {
                        d.setExplanation(publicExplanation);
                    } else {
                        d.setExplanation(voucherDetails.get(0).getExplanation());
                    }
                    d.setAccountId(StringUtil.getAccountCode(forms, "付款银行"));
                    d.setCredit(StringUtil.getValueByName(forms, "金额汇总"));
                    voucherDetails.add(d);
                    voucher.setVoucherDetails(voucherDetails);
                    String save01 = SignUtil.saveVoucher(voucher);
                    if (!"success".equals(save01)) {
                        formAllVoucher = false;
                        resultString += StrUtil.format("个人报销第一张凭证生成错误：【{}】 ", save01);
                    }


                    // 第二张生成逻辑（个人报销）
                    Voucher voucherTwo = new Voucher().setSerialNumber(serialNumber).setApprovalName("个人报销第二张");
                    voucherTwo.setDate(year + "-" + month + "-" + day);
                    voucherTwo.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                    List<VoucherDetail> voucherTwoDetails = new ArrayList<>();
                    for (List<ApprovalInstanceForm> formDetails : formListDetails) {
                        String costSubcategory = StringUtil.getValueByName(formDetails, "费用子类");
                        List<SecondExceptionTable> secondExceptionTables = Constants.LIST_TABLE_08;
                        List<String> secondExceptionTableStringList = secondExceptionTables.stream().map(SecondExceptionTable::getCostSubcategory).collect(Collectors.toList());
                        List<Bitable> listTable;
                        int genLogicType;
                        // 包含子类且固定资产/长期待摊
                        if (secondExceptionTableStringList.contains(costSubcategory) && "是".equals(StringUtil.getValueByName(formDetails, "固定资产/长期待摊"))) {
                            // 跳过该明细的借贷凭证列表
                            continue;
                        } else {
                            String summary = StringUtil.getValueByName(formDetails, "品牌是否核销");
                            // 判断品牌是否核销
                            if ("是".equals(summary)) {
                                listTable = Constants.LIST_TABLE_14;
                                genLogicType = 1;
                            } else {
                                listTable = Constants.LIST_TABLE_15;
                                genLogicType = 2;
                            }
                        }
                        String brandType;
                        if (!"总体管理".equals(StringUtil.getValueByName(formDetails, "所属品牌"))) {
                            brandType = "Other";
                        } else {
                            brandType = "总体管理";
                        }
                        Bitable bitable;
                        List<Bitable> bitableList = listTable.stream().filter(n -> StringUtil.getValueByName(formDetails, "费用大类").equals(n.getCostCategory())
                                && StringUtil.getValueByName(formDetails, "费用子类").equals(n.getCostSubcategory())
                                && brandType.equals(n.getBrand())
                        ).collect(Collectors.toList());
                        if (ArrayUtil.isEmpty(bitableList) || bitableList.size() > 1) {
                            log.error("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                    StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                            return StrUtil.format("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                    StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                        } else {
                            bitable = bitableList.get(0);
                            String summary = bitable.getSummary();
                            // 摘要为空跳过该明细的借贷凭证列表
                            if (StrUtil.isBlank(summary)) {
                                continue;
                            }
                        }

                        VoucherDetail j1 = new VoucherDetail();
                        VoucherDetail j2 = new VoucherDetail();
                        VoucherDetail d1 = new VoucherDetail();
                        String explanation = (genLogicType == 1 ? "确认成本" : "确认费用") +
                                "&" + StringUtil.getValueByName(forms, "收款人（单位）全称") +
                                "&" + StringUtil.getValueByName(formDetails, "所属品牌") +
                                "&" + StringUtil.getValueByName(formDetails, "费用归属年份") + StringUtil.getValueByName(formDetails, "费用归属月份") +
                                "&" + serialNumber +
                                "&" + StringUtil.getValueByName(formDetails, "费用大类") +
                                "&" + StringUtil.getValueByName(formDetails, "费用子类") + (genLogicType == 1 ? "&品牌核销" : "") +
                                "&" + StringUtil.getValueByName(formDetails, "备注");
                        j1.setExplanation(explanation);
                        j2.setExplanation(explanation);
                        d1.setExplanation(explanation);

                        j1.setAmountFor(StringUtil.getValueByName(formDetails, "金额"));
                        String debitAmount = StringUtil.calculateIncludeTax(StringUtil.getValueByName(formDetails, "金额"), StringUtil.getValueByName(formDetails, "税率"));
                        j1.setDebit(debitAmount);
                        String debitAmountTwo = StringUtil.calculateIncludeTaxTwo(StringUtil.getValueByName(formDetails, "金额"), StringUtil.getValueByName(formDetails, "税率"));
                        j2.setDebit(debitAmountTwo);
                        d1.setCredit(StringUtil.getValueByName(formDetails, "金额"));

                        // 借贷方科目编码名称维度组装
                        j1.setAccountId(bitable.getDebitAccountCodeOne());
                        String debitAccountingDimensionOne = bitable.getDebitAccountingDimensionOne();
                        VoucherDetail voucherDetailDebitOne = getAccountingDimensionParam(forms, StringUtil.getValueByName(forms, "收款人（单位）全称"), formDetails, j1, debitAccountingDimensionOne);
                        voucherTwoDetails.add(voucherDetailDebitOne);

                        j2.setAccountId(bitable.getDebitAccountCodeTwo());
                        String debitAccountingDimensionTwo = bitable.getDebitAccountingDimensionTwo();
                        VoucherDetail voucherDetailDebitTwo = getAccountingDimensionParam(forms, StringUtil.getValueByName(forms, "收款人（单位）全称"), formDetails, j2, debitAccountingDimensionTwo);
                        voucherTwoDetails.add(voucherDetailDebitTwo);

                        d1.setAccountId(bitable.getCreditAccountCodeOne());
                        String creditAccountingDimensionOne = bitable.getCreditAccountingDimensionOne();
                        VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(forms, StringUtil.getValueByName(forms, "收款人（单位）全称"), formDetails, d1, creditAccountingDimensionOne);
                        voucherTwoDetails.add(voucherDetailCreditOne);
                    }

                    if (!voucherTwoDetails.isEmpty()) {
                        voucherTwo.setVoucherDetails(voucherTwoDetails);
                        String save02 = SignUtil.saveVoucher(voucherTwo);
                        if (!"success".equals(save02)) {
                            formAllVoucher = false;
                            resultString += StrUtil.format("个人报销第二张凭证生成错误：【{}】 ", save02);
                        }
                    }

                } else if ("预付申请".equals(approvalTypeName)) {
                    // 第一张生成逻辑（预付申请）
                    voucher = new Voucher().setSerialNumber(serialNumber).setApprovalName("预付申请第一张");
                    voucher.setDate(year + "-" + month + "-" + day);
                    voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE003.getType());
                    List<VoucherDetail> voucherDetails = new ArrayList<>();
                    List<List<ApprovalInstanceForm>> formListDetails = StringUtil.getFormDetails(forms, "明细");
                    if (ArrayUtil.isEmpty(formListDetails)) {
                        log.error("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                        return StrUtil.format("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                    }
                    for (List<ApprovalInstanceForm> formDetails : formListDetails) {
                        VoucherDetail j = new VoucherDetail();
                        // 摘要（根据品牌是否核销不同）
                        String explanation = StringUtil.getExplanationName("预付申请", serialNumber, forms, formDetails);
                        j.setExplanation(explanation);

                        // 科目编码、核算维度（借方）
                        j.setAccountId(Constants.ACCOUNT_CODE_EMPLOYEE);
                        String brandType;
                        if (!"总体管理".equals(StringUtil.getValueByName(formDetails, "所属品牌"))) {
                            brandType = "Other";
                        } else {
                            brandType = "总体管理";
                        }
                        List<Bitable> baseList = Constants.LIST_TABLE_16;
                        List<Bitable> bitableList = baseList.stream().filter(n -> StringUtil.getValueByName(formDetails, "费用大类").equals(n.getCostCategory())
                                && StringUtil.getValueByName(formDetails, "费用子类").equals(n.getCostSubcategory())
                                && brandType.equals(n.getBrand())
                        ).collect(Collectors.toList());
                        if (ArrayUtil.isEmpty(bitableList) || bitableList.size() > 1) {
                            log.error("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                    StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                        } else {
                            j.setAccountId(bitableList.get(0).getDebitAccountCodeOne());
                            // 映射借方核算维度1
                            // 根据借方核算维度文本转核算维度实体（赋好字段代码）
                            String debitAccountingDimensionOne = bitableList.get(0).getDebitAccountingDimensionOne();
                            getAccountingDimensionParam(forms, StringUtil.getValueByName(forms, "收款人（单位）全称"), formDetails, j, debitAccountingDimensionOne);
                        }
                        j.setAmountFor(StringUtil.getValueByName(formDetails, "金额"));
                        j.setDebit(StringUtil.getValueByName(formDetails, "金额"));
                        voucherDetails.add(j);
                    }
                    // 以上为通过明细获取的多个借方，以下为一个贷方
                    VoucherDetail d = new VoucherDetail();
                    String publicExplanation = "";
                    publicExplanation = "预付申请" + "&" + StringUtil.getValueByName(forms, "收款人（单位）全称") + "&" + serialNumber;
                    // 借贷如果都一条贷方摘要取借方的摘要
                    if (voucherDetails.size() > 1) {
                        d.setExplanation(publicExplanation);
                    } else {
                        d.setExplanation(voucherDetails.get(0).getExplanation());
                    }
                    d.setAccountId(StringUtil.getAccountCode(forms, "付款银行"));
                    d.setCredit(StringUtil.getValueByName(forms, "金额汇总"));
                    voucherDetails.add(d);
                    voucher.setVoucherDetails(voucherDetails);
                    String save01 = SignUtil.saveVoucher(voucher);
                    if (!"success".equals(save01)) {
                        formAllVoucher = false;
                        resultString += StrUtil.format("预付申请第一张凭证生成错误：【{}】 ", save01);
                    }


                    // 第二张生成逻辑（预付申请）
                    Voucher voucherTwo = new Voucher().setSerialNumber(serialNumber).setApprovalName("预付申请第二张");
                    voucherTwo.setDate(year + "-" + month + "-" + day);
                    voucherTwo.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                    List<VoucherDetail> voucherTwoDetails = new ArrayList<>();
                    for (List<ApprovalInstanceForm> formDetails : formListDetails) {
                        List<Bitable> listTable = Constants.LIST_TABLE_17;
                        // 包含子类且固定资产/长期待摊
                        if ("否".equals(StringUtil.getValueByName(formDetails, "是否已到票"))) {
                            // 跳过该明细的借贷凭证列表
                            continue;
                        }
                        String brandType;
                        if (!"总体管理".equals(StringUtil.getValueByName(formDetails, "所属品牌"))) {
                            brandType = "Other";
                        } else {
                            brandType = "总体管理";
                        }
                        Bitable bitable;
                        List<Bitable> bitableList = listTable.stream().filter(n -> StringUtil.getValueByName(formDetails, "费用大类").equals(n.getCostCategory())
                                && StringUtil.getValueByName(formDetails, "费用子类").equals(n.getCostSubcategory())
                                && brandType.equals(n.getBrand())
                        ).collect(Collectors.toList());
                        if (ArrayUtil.isEmpty(bitableList) || bitableList.size() > 1) {
                            log.error("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                    StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                            return StrUtil.format("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                    StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                        } else {
                            bitable = bitableList.get(0);
                            String summary = bitable.getSummary();
                            // 摘要为空跳过该明细的借贷凭证列表
                            if (StrUtil.isBlank(summary)) {
                                continue;
                            }
                        }
                        VoucherDetail j1 = new VoucherDetail();
                        VoucherDetail j2 = new VoucherDetail();
                        VoucherDetail d1 = new VoucherDetail();
                        String explanation = ("收票") +
                                "&" + StringUtil.getValueByName(forms, "收款人（单位）全称") +
                                "&" + StringUtil.getValueByName(formDetails, "所属品牌") +
                                "&" + StringUtil.getValueByName(formDetails, "费用归属年份") + StringUtil.getValueByName(formDetails, "费用归属月份") +
                                "&" + serialNumber +
                                "&" + StringUtil.getValueByName(formDetails, "费用大类") +
                                "&" + StringUtil.getValueByName(formDetails, "费用子类") +
                                "&" + StringUtil.getValueByName(formDetails, "备注");
                        j1.setExplanation(explanation);
                        j2.setExplanation(explanation);
                        d1.setExplanation(explanation);

                        j1.setAmountFor(StringUtil.getValueByName(formDetails, "金额"));
                        String debitAmount = StringUtil.calculateIncludeTax(StringUtil.getValueByName(formDetails, "金额"), StringUtil.getValueByName(formDetails, "税率"));
                        j1.setDebit(debitAmount);
                        String debitAmountTwo = StringUtil.calculateIncludeTaxTwo(StringUtil.getValueByName(formDetails, "金额"), StringUtil.getValueByName(formDetails, "税率"));
                        j2.setDebit(debitAmountTwo);
                        d1.setCredit(StringUtil.getValueByName(formDetails, "金额"));

                        // 借贷方科目编码名称维度组装
                        j1.setAccountId(bitable.getDebitAccountCodeOne());
                        String debitAccountingDimensionOne = bitable.getDebitAccountingDimensionOne();
                        VoucherDetail voucherDetailDebitOne = getAccountingDimensionParam(forms, StringUtil.getValueByName(forms, "收款人（单位）全称"), formDetails, j1, debitAccountingDimensionOne);
                        voucherTwoDetails.add(voucherDetailDebitOne);

                        j2.setAccountId(bitable.getDebitAccountCodeTwo());
                        String debitAccountingDimensionTwo = bitable.getDebitAccountingDimensionTwo();
                        VoucherDetail voucherDetailDebitTwo = getAccountingDimensionParam(forms, StringUtil.getValueByName(forms, "收款人（单位）全称"), formDetails, j2, debitAccountingDimensionTwo);
                        voucherTwoDetails.add(voucherDetailDebitTwo);

                        d1.setAccountId(bitable.getCreditAccountCodeOne());
                        String creditAccountingDimensionOne = bitable.getCreditAccountingDimensionOne();
                        VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(forms, StringUtil.getValueByName(forms, "收款人（单位）全称"), formDetails, d1, creditAccountingDimensionOne);
                        voucherTwoDetails.add(voucherDetailCreditOne);
                    }
                    if (!voucherTwoDetails.isEmpty()) {
                        voucherTwo.setVoucherDetails(voucherTwoDetails);
                        String save02 = SignUtil.saveVoucher(voucherTwo);
                        if (!"success".equals(save02)) {
                            formAllVoucher = false;
                            resultString += StrUtil.format("预付申请第二张凭证生成错误：【{}】 ", save02);
                        }
                    }
                }
                break;
            case INVOICING_APPLICATION:
                // 开票申请
                List<List<ApprovalInstanceForm>> formListDetails = StringUtil.getFormDetails(forms, "申请明细");
                if (ArrayUtil.isEmpty(formListDetails)) {
                    log.error("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                    return StrUtil.format("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                }
                voucher = new Voucher().setSerialNumber(serialNumber).setApprovalName("开票申请");
                voucher.setDate(year + "-" + month + "-" + day);
                voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                List<VoucherDetail> voucherDetails = new ArrayList<>();
                for (List<ApprovalInstanceForm> formDetails : formListDetails) {
                    List<Bitable> listTable;
                    // 预收款走逻辑一或二
                    String prepaidFee = StringUtil.getValueByName(forms, "预收款（是/否）");
                    if ("是".equals(prepaidFee)) {
                        listTable = Constants.LIST_TABLE_18;
                    } else {
                        listTable = Constants.LIST_TABLE_19;
                    }
                    Bitable bitable;
                    List<Bitable> bitableList = listTable.stream().filter(n -> StringUtil.getValueByName(formDetails, "商品信息/服务信息").equals(n.getGoodServiceInfo())
                            && StringUtil.getValueByName(formDetails, "税率(%)").equals(n.getTaxRate())
                    ).collect(Collectors.toList());
                    if (ArrayUtil.isEmpty(bitableList) || bitableList.size() > 1) {
                        log.error("存在争议的科目编码，请检查参数是否在映射表匹配。商品信息/服务信息：{} 税率：{}，审批名称：{} 飞书流程号：{}",
                                StringUtil.getValueByName(formDetails, "商品信息/服务信息"), StringUtil.getValueByName(formDetails, "税率(%)"), voucher.getApprovalName(), voucher.getSerialNumber());
                        return StrUtil.format("存在争议的科目编码，请检查参数是否在映射表匹配。商品信息/服务信息：{} 税率：{}，审批名称：{} 飞书流程号：{}",
                                StringUtil.getValueByName(formDetails, "商品信息/服务信息"), StringUtil.getValueByName(formDetails, "税率(%)"), voucher.getApprovalName(), voucher.getSerialNumber());
                    } else {
                        bitable = bitableList.get(0);
                        String summary = bitable.getSummary();
                        // 摘要为空跳过该明细的借贷凭证列表
                        if (StrUtil.isBlank(summary)) {
                            continue;
                        }
                    }
                    VoucherDetail j1 = new VoucherDetail();
                    VoucherDetail d1 = new VoucherDetail();
                    VoucherDetail d2 = new VoucherDetail();
                    String explanation = ("收入确认") +
                            "&" + StringUtil.getValueByName(forms, "开票公司") +
                            "&" + StringUtil.getValueByName(forms, "所属品牌") +
                            "&" + StringUtil.getValueByName(formDetails, "归属年份") + StringUtil.getValueByName(formDetails, "归属月份") +
                            "&" + StringUtil.getValueByName(formDetails, "商品信息/服务信息") +
                            "&" + serialNumber;
                    j1.setExplanation(explanation);
                    d1.setExplanation(explanation);
                    d2.setExplanation(explanation);

                    String taxRate = StringUtil.getValueByName(formDetails, "税率(%)");
                    String debitAmount = StringUtil.getValueByName(formDetails, "不含税金额（" + taxRate + "）");
                    if (StrUtil.isEmpty(debitAmount)) {
                        debitAmount = "0";
                    }
                    String debitAmountTwo = StringUtil.getValueByName(formDetails, "开票金额（含税）");
                    // 不含税金额
                    j1.setDebit(debitAmountTwo);//100
                    j1.setAmountFor(debitAmountTwo);
                    // 开票金额（含税） - 不含税金额
                    d1.setCredit(debitAmount);//94.34
                    // 开票金额（含税）
                    d2.setCredit(StringUtil.subtractAmount(debitAmountTwo, debitAmount));//5.64

                    // 借贷方科目编码名称维度组装
                    j1.setAccountId(bitable.getDebitAccountCodeOne());
                    String debitAccountingDimensionOne = bitable.getDebitAccountingDimensionOne();
                    VoucherDetail voucherDetailDebitOne = getAccountingDimensionParam(forms, null, formDetails, j1, debitAccountingDimensionOne);
                    voucherDetails.add(voucherDetailDebitOne);

                    d1.setAccountId(bitable.getCreditAccountCodeOne());
                    String creditAccountingDimensionOne = bitable.getCreditAccountingDimensionOne();
                    VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(forms, null, formDetails, d1, creditAccountingDimensionOne);
                    voucherDetails.add(voucherDetailCreditOne);

                    d2.setAccountId(bitable.getCreditAccountCodeTwo());
                    String creditAccountingDimensionTwo = bitable.getCreditAccountingDimensionTwo();
                    VoucherDetail voucherDetailCreditTwo = getAccountingDimensionParam(forms, null, formDetails, d2, creditAccountingDimensionTwo);
                    voucherDetails.add(voucherDetailCreditTwo);
                }
                if (!voucherDetails.isEmpty()) {
                    voucher.setVoucherDetails(voucherDetails);
                    String save = SignUtil.saveVoucher(voucher);
                    if (!"success".equals(save)) {
                        formAllVoucher = false;
                        resultString += StrUtil.format("开票申请凭证生成错误：【{}】 ", save);
                    }
                }

                break;
            case INVOICE_WRITE_OFF:
                // 发票核销
                String referenceInstanceCode;
                List<String> instanceCodeList = StringUtil.getInstanceCodeList(forms, "核销发票");
                String selectString = StringUtil.getValueByName(forms, "付款明细选择");
                int number = StringUtil.getCurrentSelectNumber(selectString);
                if (number == -1) {
                    return "审批设置的选择项目标题开头不为明细，请设置如下: 明细x";
                } else {
                    referenceInstanceCode = instanceCodeList.get(0);
                }
                ApprovalInstanceFormResult result = StringUtil.instanceToFormList(referenceInstanceCode);
                if (result.getApprovalInstance() == null || ArrayUtil.isEmpty(result.getApprovalInstanceForms())) {
                    log.error("审批实例转化后实例为空或者字段列表为空");
                    return "审批实例转化后实例为空或者字段列表为空";
                }
                List<ApprovalInstanceForm> invoiceWriteOffForms = result.getApprovalInstanceForms();
                formListDetails = StringUtil.getFormDetails(invoiceWriteOffForms, "明细");
                if (ArrayUtil.isEmpty(formListDetails)) {
                    log.error("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(invoiceWriteOffForms));
                    return StrUtil.format("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(invoiceWriteOffForms));
                }
                if (number > formListDetails.size()) {
                    log.error("选择的明细索引超出了该引用单据明细列表");
                    return "选择的明细索引超出了该引用单据明细列表";
                }
                List<ApprovalInstanceForm> formDetails = formListDetails.get(number);
                Voucher voucherInvoiceWriteOff = new Voucher().setSerialNumber(serialNumber).setApprovalName("发票核销");
                voucherInvoiceWriteOff.setDate(year + "-" + month + "-" + day);
                voucherInvoiceWriteOff.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                List<VoucherDetail> voucherInvoiceWriteOffVoucherDetails = new ArrayList<>();
//                for (List<ApprovalInstanceForm> formDetails : formListDetails) {
//                }
                List<Bitable> listTable;
                // 判断是否核销逻辑一或二
                String prepaidFee = StringUtil.getValueByName(formDetails, "品牌是否核销");
                if ("是".equals(prepaidFee)) {
                    listTable = Constants.LIST_TABLE_20;
                } else {
                    listTable = Constants.LIST_TABLE_21;
                }

                // 科目编码
                String brandType;
                if (!"总体管理".equals(StringUtil.getValueByName(formDetails, "所属品牌"))) {
                    brandType = "Other";
                } else {
                    brandType = "总体管理";
                }
                List<Bitable> bitableList = listTable.stream().filter(n -> StringUtil.getValueByName(formDetails, "费用大类").equals(n.getCostCategory())
                        && StringUtil.getValueByName(formDetails, "费用子类").equals(n.getCostSubcategory())
                        && brandType.equals(n.getBrand())
                ).collect(Collectors.toList());
                Bitable bitable;
//                List<Bitable> bitableList = listTable.stream().filter(n -> StringUtil.getValueByName(formDetails, "商品信息/服务信息").equals(n.getGoodServiceInfo())
//                        && StringUtil.getValueByName(formDetails, "税率(%)").equals(n.getTaxRate())
//                ).collect(Collectors.toList());
                if (ArrayUtil.isEmpty(bitableList) || bitableList.size() > 1) {
                    log.error("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                            StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                    return StrUtil.format("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                            StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                } else {
                    bitable = bitableList.get(0);
                    String summary = bitable.getSummary();
                    // 摘要为空跳过该明细的借贷凭证列表
                    if (StrUtil.isBlank(summary)) {
                        log.error("摘要为空。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                        return StrUtil.format("摘要为空。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                    }
                }
                VoucherDetail j1 = new VoucherDetail();
                VoucherDetail j2 = new VoucherDetail();
                VoucherDetail d1 = new VoucherDetail();
                String explanation = ("确认成本") +
                        "&" + StringUtil.getValueByName(invoiceWriteOffForms, "收款人（单位）全称") +
                        "&" + StringUtil.getValueByName(formDetails, "所属品牌") +
                        "&" + StringUtil.getValueByName(formDetails, "费用归属年份") + StringUtil.getValueByName(formDetails, "费用归属月份") +
                        "&" + serialNumber +
                        "&" + StringUtil.getValueByName(formDetails, "费用大类") +
                        "&" + StringUtil.getValueByName(formDetails, "费用子类") + ("是".equals(prepaidFee) ? "&品牌核销" : "") +
                        "&" + StringUtil.getValueByName(forms, "备注");
                j1.setExplanation(explanation);
                j2.setExplanation(explanation);
                d1.setExplanation(explanation);


                String debitAmount = StringUtil.calculateIncludeTax(StringUtil.getValueByName(forms, "核销金额"), StringUtil.getValueByName(forms, "税率"));
                j1.setDebit(debitAmount);
                j1.setAmountFor(debitAmount);
                String debitAmountTwo = StringUtil.calculateIncludeTaxTwo(StringUtil.getValueByName(forms, "核销金额"), StringUtil.getValueByName(forms, "税率"));
                j2.setDebit(debitAmountTwo);
                d1.setCredit(StringUtil.getValueByName(forms, "核销金额"));

                // 借贷方科目编码名称维度组装
                j1.setAccountId(bitable.getDebitAccountCodeOne());
                String debitAccountingDimensionOne = bitable.getDebitAccountingDimensionOne();
                VoucherDetail voucherDetailDebitOne = getAccountingDimensionParam(invoiceWriteOffForms, null, formDetails, j1, debitAccountingDimensionOne);
                voucherInvoiceWriteOffVoucherDetails.add(voucherDetailDebitOne);

                j2.setAccountId(bitable.getDebitAccountCodeTwo());
                String debitAccountingDimensionTwo = bitable.getDebitAccountingDimensionTwo();
                VoucherDetail voucherDetailDebitTwo = getAccountingDimensionParam(invoiceWriteOffForms, null, formDetails, j2, debitAccountingDimensionTwo);
                voucherInvoiceWriteOffVoucherDetails.add(voucherDetailDebitTwo);

                d1.setAccountId(bitable.getCreditAccountCodeOne());
                String creditAccountingDimensionOne = bitable.getCreditAccountingDimensionOne();
                VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(invoiceWriteOffForms, null, formDetails, d1, creditAccountingDimensionOne);
                voucherInvoiceWriteOffVoucherDetails.add(voucherDetailCreditOne);
                voucherInvoiceWriteOff.setVoucherDetails(voucherInvoiceWriteOffVoucherDetails);
                String save = SignUtil.saveVoucher(voucherInvoiceWriteOff);
                if (!"success".equals(save)) {
                    formAllVoucher = false;
                    resultString += StrUtil.format("发票核销凭证生成错误：【{}】 ", save);
                }
                break;
            case WITHHOLDING_APPLICATION:
                // 预提申请
                List<List<ApprovalInstanceForm>> whaFormListDetails = StringUtil.getFormDetails(forms, "明细");
                if (ArrayUtil.isEmpty(whaFormListDetails)) {
                    log.error("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                    return StrUtil.format("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                }
                Voucher whaVoucher = new Voucher().setSerialNumber(serialNumber).setApprovalName("预提申请");
                whaVoucher.setDate(year + "-" + month + "-" + day);
                whaVoucher.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                List<VoucherDetail> whaVoucherDetails = new ArrayList<>();
                for (List<ApprovalInstanceForm> whaFormDetails : whaFormListDetails) {
                    List<Bitable> whaListTable;
                    // 预收款走逻辑一或二
                    String whaPrepaidFee = StringUtil.getValueByName(whaFormDetails, "品牌核销");
                    if ("是".equals(whaPrepaidFee)) {
                        whaListTable = Constants.LIST_TABLE_22;
                    } else {
                        whaListTable = Constants.LIST_TABLE_23;
                    }
                    // 科目编码
                    String whaBrandType;
                    if (!"总体管理".equals(StringUtil.getValueByName(whaFormDetails, "所属品牌"))) {
                        whaBrandType = "Other";
                    } else {
                        whaBrandType = "总体管理";
                    }
                    Bitable whaBitable;
                    List<Bitable> whaBitableList = whaListTable.stream().filter(n -> StringUtil.getValueByName(whaFormDetails, "费用大类").equals(n.getCostCategory())
                            && StringUtil.getValueByName(whaFormDetails, "费用子类").equals(n.getCostSubcategory())
                            && whaBrandType.equals(n.getBrand())
                    ).collect(Collectors.toList());
                    if (ArrayUtil.isEmpty(whaBitableList) || whaBitableList.size() > 1) {
                        log.error("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                StringUtil.getValueByName(whaFormDetails, "费用大类"), StringUtil.getValueByName(whaFormDetails, "费用子类"), StringUtil.getValueByName(whaFormDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                        return StrUtil.format("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                StringUtil.getValueByName(whaFormDetails, "费用大类"), StringUtil.getValueByName(whaFormDetails, "费用子类"), StringUtil.getValueByName(whaFormDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                    } else {
                        whaBitable = whaBitableList.get(0);
                        String summary = whaBitable.getSummary();
                        // 摘要为空跳过该明细的借贷凭证列表
                        if (StrUtil.isBlank(summary)) {
                            continue;
                        }
                    }
                    VoucherDetail whaj1 = new VoucherDetail();
                    VoucherDetail whad1 = new VoucherDetail();
                    String whaExplanation = ("暂估成本") +
                            "&" + StringUtil.getValueByName(whaFormDetails, "收款人名字/单位") +
                            "&" + StringUtil.getValueByName(whaFormDetails, "所属品牌") +
                            "&" + StringUtil.getValueByName(whaFormDetails, "费用归属年份") + StringUtil.getValueByName(whaFormDetails, "费用归属月份") +
                            "&" + serialNumber +
                            "&" + StringUtil.getValueByName(whaFormDetails, "费用大类") +
                            "&" + StringUtil.getValueByName(whaFormDetails, "费用子类") + ("是".equals(whaPrepaidFee) ? "&品牌核销" : "") +
                            "&" + StringUtil.getValueByName(whaFormDetails, "申请明细");
                    whaj1.setExplanation(whaExplanation);
                    whad1.setExplanation(whaExplanation);

                    String invoiceType = StringUtil.getValueByName(whaFormDetails, "发票类型");
                    String whaAmount = "";
                    if ("专票".equals(invoiceType)) {
                        whaAmount = StringUtil.calculateIncludeTax(StringUtil.getValueByName(whaFormDetails, "付款金额"), StringUtil.getValueByName(whaFormDetails, "税率"));
                    } else if ("普票".equals(invoiceType)) {
                        whaAmount = StringUtil.getValueByName(whaFormDetails, "付款金额");
                    }
                    // 不含税金额
                    whaj1.setDebit(whaAmount);
                    whaj1.setAmountFor(whaAmount);
                    whad1.setCredit(whaAmount);

                    // 借贷方科目编码名称维度组装
                    whaj1.setAccountId(whaBitable.getDebitAccountCodeOne());
                    String whaDebitAccountingDimensionOne = whaBitable.getDebitAccountingDimensionOne();
                    VoucherDetail whaVoucherDetailDebitOne = getAccountingDimensionParam(forms, null, whaFormDetails, whaj1, whaDebitAccountingDimensionOne);
                    whaVoucherDetails.add(whaVoucherDetailDebitOne);

                    whad1.setAccountId(whaBitable.getCreditAccountCodeOne());
                    String whaCreditAccountingDimensionOne = whaBitable.getCreditAccountingDimensionOne();
                    VoucherDetail whaVoucherDetailCreditOne = getAccountingDimensionParam(forms, null, whaFormDetails, whad1, whaCreditAccountingDimensionOne);
                    whaVoucherDetails.add(whaVoucherDetailCreditOne);
                }
                if (!whaVoucherDetails.isEmpty()) {
                    whaVoucher.setVoucherDetails(whaVoucherDetails);
                    String save01 = SignUtil.saveVoucher(whaVoucher);
                    if (!"success".equals(save01)) {
                        formAllVoucher = false;
                        resultString += StrUtil.format("预提申请凭证生成错误：【{}】 ", save01);
                    }
                }
                break;
            case REFUND_APPLICATION:
                // 退款申请
                List<List<ApprovalInstanceForm>> raFormListDetails = StringUtil.getFormDetails(forms, "申请明细");
                if (ArrayUtil.isEmpty(raFormListDetails)) {
                    log.error("审批申请明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                    return StrUtil.format("审批申请明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                }
                Voucher raVoucher = new Voucher().setSerialNumber(serialNumber).setApprovalName("退款申请");
                raVoucher.setDate(year + "-" + month + "-" + day);
                raVoucher.setVoucherGroupId(VoucherGroupIdEnum.PRE004.getType());
                List<VoucherDetail> raVoucherDetails = new ArrayList<>();

                for (List<ApprovalInstanceForm> raFormDetails : raFormListDetails) {

                    List<Bitable> raListTable;
                    // 预收款走逻辑一或二
                    String isFixedAssets = StringUtil.getValueByName(raFormDetails, "是否为固定资产");
                    if ("是".equals(isFixedAssets)) {
                        continue;
                    }

                    String raPrepaidFee = StringUtil.getValueByName(raFormDetails, "品牌核销");
                    if ("是".equals(raPrepaidFee)) {
                        raListTable = Constants.LIST_TABLE_24;
                    } else {
                        raListTable = Constants.LIST_TABLE_25;
                    }
                    // 科目编码
                    String raBrandType;
                    if (!"总体管理".equals(StringUtil.getValueByName(raFormDetails, "所属品牌"))) {
                        raBrandType = "Other";
                    } else {
                        raBrandType = "总体管理";
                    }
                    Bitable raBitable;
                    List<Bitable> raBitableList = raListTable.stream().filter(n -> StringUtil.getValueByName(raFormDetails, "费用大类").equals(n.getCostCategory())
                            && StringUtil.getValueByName(raFormDetails, "费用子类").equals(n.getCostSubcategory())
                            && raBrandType.equals(n.getBrand())
                    ).collect(Collectors.toList());
                    if (ArrayUtil.isEmpty(raBitableList) || raBitableList.size() > 1) {
                        log.error("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                StringUtil.getValueByName(raFormDetails, "费用大类"), StringUtil.getValueByName(raFormDetails, "费用子类"), StringUtil.getValueByName(raFormDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                        return StrUtil.format("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                StringUtil.getValueByName(raFormDetails, "费用大类"), StringUtil.getValueByName(raFormDetails, "费用子类"), StringUtil.getValueByName(raFormDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                    } else {
                        raBitable = raBitableList.get(0);
                        String summary = raBitable.getSummary();
                        // 摘要为空直接退出
                        if (StrUtil.isBlank(summary)) {
                            return StrUtil.format("摘要为空。费用大类：{} 费用子类：{} 所属品牌：{}，审批名称：{} 飞书流程号：{}",
                                    StringUtil.getValueByName(raFormDetails, "费用大类"), StringUtil.getValueByName(raFormDetails, "费用子类"), StringUtil.getValueByName(raFormDetails, "所属品牌"), voucher.getApprovalName(), voucher.getSerialNumber());
                        }
                    }
                    VoucherDetail raj1 = new VoucherDetail();
                    VoucherDetail rad1 = new VoucherDetail();
                    String raExplanation = ("暂估成本") +
                            "&" + StringUtil.getValueByName(raFormDetails, "退款公司") +
                            "&" + StringUtil.getValueByName(raFormDetails, "所属品牌") +
                            "&" + StringUtil.getValueByName(raFormDetails, "所属年份") + StringUtil.getValueByName(raFormDetails, "所属月份") +
                            "&" + serialNumber +
                            "&" + StringUtil.getValueByName(raFormDetails, "费用大类") +
                            "&" + StringUtil.getValueByName(raFormDetails, "费用子类") + ("是".equals(raPrepaidFee) ? "&品牌核销" : "") +
                            "&" + StringUtil.getValueByName(raFormDetails, "备注");
                    raj1.setExplanation(raExplanation);
                    rad1.setExplanation(raExplanation);

                    String raAmount = StringUtil.calculateIncludeTax(StringUtil.getValueByName(raFormDetails, "退款金额"), StringUtil.getValueByName(raFormDetails, "税率"));
                    // 不含税金额
                    raj1.setDebit(raAmount);
                    raj1.setAmountFor(raAmount);
                    rad1.setCredit(raAmount);

                    // 借贷方科目编码名称维度组装
                    raj1.setAccountId(raBitable.getDebitAccountCodeOne());
                    String raDebitAccountingDimensionOne = raBitable.getDebitAccountingDimensionOne();
                    VoucherDetail raVoucherDetailDebitOne = getAccountingDimensionParam(forms, null, raFormDetails, raj1, raDebitAccountingDimensionOne);
                    raVoucherDetails.add(raVoucherDetailDebitOne);

                    rad1.setAccountId(raBitable.getCreditAccountCodeOne());
                    String raCreditAccountingDimensionOne = raBitable.getCreditAccountingDimensionOne();
                    VoucherDetail raVoucherDetailCreditOne = getAccountingDimensionParam(forms, null, raFormDetails, rad1, raCreditAccountingDimensionOne);
                    raVoucherDetails.add(raVoucherDetailCreditOne);
                }
                raVoucher.setVoucherDetails(raVoucherDetails);
                if (ArrayUtil.isEmpty(raVoucherDetails)) {
                    log.error("申请明细列表为空，请检查raVoucherDetails列表: {}", JSONObject.toJSONString(raVoucherDetails));
                    return StrUtil.format("申请明细列表为空，请检查raVoucherDetails列表: {}", JSONObject.toJSONString(raVoucherDetails));
                }
                String save01 = SignUtil.saveVoucher(raVoucher);
                if (!"success".equals(save01)) {
                    formAllVoucher = false;
                    resultString += StrUtil.format("退款申请凭证生成错误：【{}】 ", save01);
                }
                break;
        }
        if (formAllVoucher) {
            return "success";
        } else {
            return resultString;
        }
    }

    @Override
    public void insertRecordLog(ApprovalInstanceFormResult result, String save, String instanceOperateTime) {
        if (result.getApprovalInstance() == null | ArrayUtil.isEmpty(result.getApprovalInstanceForms())) {
            log.error("insertRecordLog方法参数审批实例或FORM解析为空");
        }
        SyncRecord record = new SyncRecord();
        record.setSerialNumber(result.getApprovalInstance().getSerialNumber());
        record.setInstanceCode(result.getApprovalInstance().getInstanceCode());
        record.setApprovalName(result.getApprovalInstance().getApprovalName());
        record.setInstanceOperateTime(StringUtil.timestampToYearMonthDayHourMinuteSecond(instanceOperateTime));
        record.setSyncType("success".equals(save) ? "已同步" : "同步失败");
        record.setErrorInfo("success".equals(save) ? "" : save);
        JSONObject body = new JSONObject();
        body.put("fields", JSONObject.toJSON(record));
        String itemJson = body.toJSONString();
        // 处理字段为中文
        itemJson = StringUtil.processChineseTitleOrder(itemJson);
        SignUtil.insertRecord(itemJson, Constants.APP_TOKEN_APPROVAL, Constants.TABLE_32);
        log.info("json: {}", StringUtil.subLog(itemJson));
    }

    /**
     * 组装核算维度参数
     *
     * @param forms               审批实例表单列表
     * @param employeeName        员工姓名
     * @param formDetails         审批实例表单明细列表
     * @param voucherDetail       金蝶凭证明细详情
     * @param accountingDimension 核算维度文本
     */
    private VoucherDetail getAccountingDimensionParam(List<ApprovalInstanceForm> forms, String employeeName,
                                                      List<ApprovalInstanceForm> formDetails, VoucherDetail voucherDetail,
                                                      String accountingDimension) {
        AccountingDimensionParam param = new AccountingDimensionParam();
        param.setAccountingDimension(accountingDimension);
        String brand = StringUtil.getValueByName(formDetails, "所属品牌");
        if (StrUtil.isEmpty(brand)) {
            brand = StringUtil.getValueByName(forms, "所属品牌");
        }
        param.setBrand(brand);
        param.setDepartment(StringUtil.getDepartmentName(forms, "部门"));
        param.setEmployee(employeeName);
        String supplierOrCustomName = StringUtil.getValueByName(forms, "收款人（单位）全称");
        if (StrUtil.isEmpty(supplierOrCustomName)) {
            supplierOrCustomName = StringUtil.getValueByName(formDetails, "收款人名字/单位");
            if (StrUtil.isEmpty(supplierOrCustomName)) {
                supplierOrCustomName = StringUtil.getValueByName(forms, "开票公司");
                if (StrUtil.isEmpty(supplierOrCustomName)) {
                    supplierOrCustomName = StringUtil.getValueByName(formDetails, "退款公司");
                }
            }
        }
        param.setSupplierOrCustomName(supplierOrCustomName);
        AccountingDimension accountingDimensionDebitTwo = getAccountingDimension(param);
        voucherDetail.setAccountingDimension(accountingDimensionDebitTwo);
        return voucherDetail;
    }

    /**
     * 分割&字符后查找并设置核算维度
     *
     * @param accountingDimensionParam
     * @return
     */
    public AccountingDimension getAccountingDimension(AccountingDimensionParam accountingDimensionParam) {
        AccountingDimension accountingDimension = new AccountingDimension();

        List<BrandShopBusiness> baseList = Constants.LIST_TABLE_02;
        if (StrUtil.isEmpty(accountingDimensionParam.getAccountingDimension())) {
            return new AccountingDimension();
        }
        // 分割&品牌
        String[] accountingDimensionArr = accountingDimensionParam.getAccountingDimension().split("&");
        // 遍历核算维度
        for (String s : accountingDimensionArr) {
            if ("店铺".equals(s) || "新业务组".equals(s)) {
                if (StrUtil.isEmpty(accountingDimensionParam.getBrand())) {
                    log.error("查找核算维度映射编码时识别店铺新业务组的品牌参数为空");
                    return new AccountingDimension();
                } else {
                    if ("店铺".equals(s)) {
                        for (BrandShopBusiness brandShopBusiness : baseList) {
                            if (accountingDimensionParam.getBrand().equals(brandShopBusiness.getBrand())) {
                                accountingDimension.setFf100002(brandShopBusiness.getShopCode());
                                break;
                            }
                        }
                    } else {
                        for (BrandShopBusiness brandShopBusiness : baseList) {
                            if (accountingDimensionParam.getBrand().equals(brandShopBusiness.getBrand())) {
                                accountingDimension.setFf100005(brandShopBusiness.getBusinessCode());
                                break;
                            }
                        }
                    }
                }
            } else if ("供应商".equals(s)) {
                List<Supplier> suppliers = Constants.LIST_TABLE_04;
                for (Supplier supplier : suppliers) {
                    if (accountingDimensionParam.getSupplierOrCustomName().equals(supplier.getSupplierName())) {
                        accountingDimension.setFflex4(supplier.getSupplierCode());
                        break;
                    }
                }
            } else if ("客户".equals(s)) {
                List<Custom> customs = Constants.LIST_TABLE_06;
                for (Custom custom : customs) {
                    if (accountingDimensionParam.getSupplierOrCustomName().equals(custom.getCustomName())) {
                        accountingDimension.setFflex6(custom.getCustomCode());
                        break;
                    }
                }
            } else if ("部门".equals(s)) {
                List<Department> departments = Constants.LIST_TABLE_07;
                for (Department department : departments) {
                    if (accountingDimensionParam.getDepartment().equals(department.getFeishuDepartmentName())) {
                        accountingDimension.setFflex5(department.getDepartmentCode());
                        break;
                    }
                }
            } else if ("员工".equals(s)) {
                if (StrUtil.isEmpty(accountingDimensionParam.getEmployee())) {
                    log.error("核算维度中有员工维度但是比较的员工参数为空");
                    break;
                }
                List<Employee> employees = Constants.LIST_TABLE_05;
                for (Employee employee : employees) {
                    if (accountingDimensionParam.getEmployee().equals(employee.getEmployeeName())) {
                        accountingDimension.setFflex7(employee.getEmployeeCode());
                        break;
                    }
                }
                if (StrUtil.isEmpty(accountingDimension.getFflex7())) {
                    for (Employee employee : employees) {
                        if ("个人报销".equals(employee.getEmployeeName())) {
                            accountingDimension.setFflex7(employee.getEmployeeCode());
                            break;
                        }
                    }
                }
                if (StrUtil.isEmpty(accountingDimension.getFflex7())) {
                    log.error("映射表找不到名称为个人报销的核算维度");
                }
            }
        }
        StringUtil.setFieldEmpty(accountingDimension);
        return accountingDimension;
    }
}
