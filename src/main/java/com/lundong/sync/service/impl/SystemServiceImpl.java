package com.lundong.sync.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.AccountingDimensionParam;
import com.lundong.sync.entity.approval.ApprovalInstanceForm;
import com.lundong.sync.entity.base.*;
import com.lundong.sync.entity.bitable.SecondExceptionTable;
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

import java.time.LocalDateTime;
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
     * @param forms        审批表单列表
     * @param operateTime  审批状态变更操作时间
     * @param approvalCode 审批CODE
     * @param serialNumber 飞书审批编号
     */
    @Override
    public void processApprovalForm(List<ApprovalInstanceForm> forms, LocalDateTime operateTime, String approvalCode, String serialNumber) {
        if (ArrayUtil.isEmpty(forms)) {
            return;
        }
        // 解析申请人为员工
        String employeeName = SignUtil.getFeishuUserName(StringUtil.getEmployValueByName(forms, "申请人"));
        int year = operateTime.getYear();
        int month = operateTime.getMonthValue();
        int day = operateTime.getDayOfMonth();
        switch (DataTypeEnum.toType(approvalCode)) {
            case PAYMENT_PERSONAL_PREPAID:
                // 付款申请&个人报销&预付申请同一个审批判断是哪一类
                // 获取审批中每个字段
                String approvalTypeName = StringUtil.getValueByName(forms, "申请类别");
                if ("付款申请".equals(approvalTypeName)) {
                    // 第一张生成逻辑
                    Voucher voucher = new Voucher();
//                    voucher.setBusDate(year + "-" + month + "-" + day);
                    voucher.setDate(year + "-" + month + "-" + day);
                    voucher.setVoucherGroupId(VoucherGroupIdEnum.PRE003.getType());
                    List<VoucherDetail> voucherDetails = new ArrayList<>();
                    List<List<ApprovalInstanceForm>> formListDetails = StringUtil.getFormDetails(forms, "明细");
                    if (ArrayUtil.isEmpty(formListDetails)) {
                        log.error("审批明细列表为空，请检查forms列表: {}", JSONObject.toJSONString(forms));
                        return;
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
                        System.out.println(bitableList.size());
                        if (ArrayUtil.isEmpty(bitableList) || bitableList.size() > 1) {
                            log.error("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}",
                                    StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"));
                        } else {
                            j.setAccountId(bitableList.get(0).getDebitAccountCodeOne());
                            // 映射借方核算维度1
                            // 根据借方核算维度文本转核算维度实体（赋好字段代码）
                            String debitAccountingDimensionOne = bitableList.get(0).getDebitAccountingDimensionOne();
                            AccountingDimensionParam accountingDimensionParam = new AccountingDimensionParam();
                            accountingDimensionParam.setAccountingDimension(debitAccountingDimensionOne);
                            accountingDimensionParam.setBrand(StringUtil.getValueByName(formDetails, "所属品牌"));
                            accountingDimensionParam.setDepartment(StringUtil.getDepartmentName(forms, "部门"));
                            accountingDimensionParam.setEmployee(employeeName);
                            accountingDimensionParam.setSupplierOrCustomName(StringUtil.getValueByName(forms, "收款人（单位）全称"));
                            AccountingDimension accountingDimension = getAccountingDimension(accountingDimensionParam);
                            j.setAccountingDimension(accountingDimension);
                        }
                        j.setDebit(StringUtil.getValueByName(formDetails, "金额"));
                        voucherDetails.add(j);
                    }
                    // 以上为通过明细获取的多个借方，以下为一个贷方
                    VoucherDetail d = new VoucherDetail();
                    String publicExplanation = "";
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
                    SignUtil.saveVoucher(voucher);


                    // 第二张生成逻辑
                    Voucher voucherTwo = new Voucher();
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
                        System.out.println(bitableList);
                        System.out.println(bitableList.size());
                        if (ArrayUtil.isEmpty(bitableList) || bitableList.size() > 1) {
                            log.error("存在争议的科目编码，请检查参数是否在映射表匹配。费用大类：{} 费用子类：{} 所属品牌：{}",
                                    StringUtil.getValueByName(formDetails, "费用大类"), StringUtil.getValueByName(formDetails, "费用子类"), StringUtil.getValueByName(formDetails, "所属品牌"));
                            return;
                        } else {
                            bitable = bitableList.get(0);
                            String summary = bitable.getSummary();
                            // 摘要为空跳过该明细的借贷凭证列表
                            if (StrUtil.isEmpty(summary)) {
                                continue;
                            }
                        }

                        VoucherDetail j1 = new VoucherDetail();
                        VoucherDetail j2 = new VoucherDetail();
                        VoucherDetail d1 = new VoucherDetail();
                        String explanation = (genLogicType == 2 ? "暂估成本" : "确认成本") +
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
                        j2.setDebit(debitAmountTwo);
                        d1.setCredit(StringUtil.getValueByName(formDetails, "金额"));

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
                        } else {
                            j1.setDebit(StringUtil.getValueByName(formDetails, "金额"));
                        }
                        d1.setAccountId(bitable.getCreditAccountCodeOne());
                        String creditAccountingDimensionOne = bitable.getCreditAccountingDimensionOne();
                        VoucherDetail voucherDetailCreditOne = getAccountingDimensionParam(forms, employeeName, formDetails, d1, creditAccountingDimensionOne);
                        voucherTwoDetails.add(voucherDetailCreditOne);
                    }

                    if (!voucherTwoDetails.isEmpty()) {
                        voucherTwo.setVoucherDetails(voucherTwoDetails);
                        SignUtil.saveVoucher(voucherTwo);
                    }
                } else if ("个人报销".equals(approvalTypeName)) {

                } else if ("预付申请".equals(approvalTypeName)) {

                }
                break;
            case INVOICING_APPLICATION:
                // 开票申请
                break;
            case INVOICE_WRITE_OFF:
                // 发票核销
                break;
            case WITHHOLDING_APPLICATION:
                // 预提申请
                break;
            case REFUND_APPLICATION:
                // 退款申请
                break;
        }
    }

    /**
     * 组装核算维度参数
     *
     * @param forms                         审批实例表单列表
     * @param employeeName                  员工姓名
     * @param formDetails                   审批实例表单明细列表
     * @param voucherDetail                 金蝶凭证明细详情
     * @param accountingDimension           核算维度文本
     */
    private VoucherDetail getAccountingDimensionParam(List<ApprovalInstanceForm> forms, String employeeName,
                                             List<ApprovalInstanceForm> formDetails, VoucherDetail voucherDetail,
                                             String accountingDimension) {
        AccountingDimensionParam param = new AccountingDimensionParam();
        param.setAccountingDimension(accountingDimension);
        param.setBrand(StringUtil.getValueByName(formDetails, "所属品牌"));
        param.setDepartment(StringUtil.getDepartmentName(forms, "部门"));
        param.setEmployee(employeeName);
        param.setSupplierOrCustomName(StringUtil.getValueByName(forms, "收款人（单位）全称"));
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
            } else if ("用户".equals(s)) {
                List<Employee> employees = Constants.LIST_TABLE_05;
                for (Employee employee : employees) {
                    if (accountingDimensionParam.getEmployee().equals(employee.getEmployeeName())) {
                        accountingDimension.setFflex7(employee.getEmployeeCode());
                        break;
                    }
                }
            }
        }
        StringUtil.setFieldEmpty(accountingDimension);
        return accountingDimension;
    }
}
