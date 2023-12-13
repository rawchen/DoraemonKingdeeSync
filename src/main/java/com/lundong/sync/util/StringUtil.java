package com.lundong.sync.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.ApprovalInstance;
import com.lundong.sync.entity.ApprovalInstanceFormResult;
import com.lundong.sync.entity.approval.AccountValue;
import com.lundong.sync.entity.approval.ApprovalInstanceForm;
import com.lundong.sync.entity.approval.DepartmentValue;
import com.lundong.sync.entity.base.Account;
import com.lundong.sync.entity.kingdee.AccountingDimension;
import com.lundong.sync.entity.kingdee.VoucherDetail;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author RawChen
 * @date 2023-06-26 10:21
 */
@Slf4j
public class StringUtil {

    public static String convertUrl(String url) {
        if (StrUtil.isEmpty(url)) {
            return "";
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * null转为空
     *
     * @param str
     * @return
     */
    public static String nullIsEmpty(String str) {
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }

    public static String getValueByCustomId(List<ApprovalInstanceForm> forms, String customId) {
        for (ApprovalInstanceForm form : forms) {
            if (customId.equals(form.getCustomId())) {
                return form.getValue();
            }
        }
        return "";
    }

    public static String getValueByName(List<ApprovalInstanceForm> forms, String name) {
        for (ApprovalInstanceForm form : forms) {
            if (name.equals(form.getName())) {
                // 审批表单取值时统一使用英文括号问题
                if (!StrUtil.isEmpty(form.getValue())) {
                    if (form.getValue().contains("（")) {
                        form.setValue(form.getValue().replaceAll("（", "("));
                    }
                    if (form.getValue().contains("）")) {
                        form.setValue(form.getValue().replaceAll("）", ")"));
                    }
                }
                return form.getValue();
            }
        }
        return "";
    }

    public static String getEmployValueByName(List<ApprovalInstanceForm> forms, String name) {
        try {
            for (ApprovalInstanceForm form : forms) {
                if (name.equals(form.getName())) {
                    List<String> ids = JSONObject.parseArray(form.getValue(), String.class);
                    return ids.get(0);
                }
            }
        } catch (Exception e) {
            log.error("转换员工ID列表时候格式化错误");
            return "";
        }
        return "";
    }

    public static List<List<ApprovalInstanceForm>> getFormDetails(List<ApprovalInstanceForm> forms, String detailName) {
        List<List<ApprovalInstanceForm>> approvalList = new ArrayList<>();
        for (ApprovalInstanceForm form : forms) {
            if (detailName.equals(form.getName())) {
                try {
                    // [[{"":""},{"":""}],[{"":""},{"":""},{"":""}]]
                    List<String> array = JSONObject.parseArray(form.getValue(), String.class);
                    for (int i = 0; i < array.size(); i++) {
                        List<ApprovalInstanceForm> approvalInstanceDetailForms =
                                JSONObject.parseArray(array.get(i), ApprovalInstanceForm.class);
                        approvalList.add(approvalInstanceDetailForms);
                    }
                    return approvalList;
                } catch (Exception e) {
                    log.error("审批单据明细转换异常: ", e);
                    log.error("审批单据明细转换文本: {}", form.getValue());
                    return Collections.emptyList();
                }
            }
        }
        return Collections.emptyList();
    }

    public static String getDepartmentName(List<ApprovalInstanceForm> forms, String name) {
        for (ApprovalInstanceForm form : forms) {
            if (name.equals(form.getName())) {
                try {
                    List<DepartmentValue> departments = JSONObject.parseArray(form.getValue(), DepartmentValue.class);
                    if (ArrayUtil.isEmpty(departments)) {
                        log.error("审批部门转换为空: {}", form.getValue());
                        return "";
                    } else {
                        return departments.get(0).getName();
                    }
                } catch (Exception e) {
                    log.error("审批部门转换异常: {}", form.getValue());
                    return "";
                }
            }
        }
        return "";
    }

    /**
     * null字段转为空字符串
     *
     * @param accountingDimension
     */
    public static void setFieldEmpty(AccountingDimension accountingDimension) {
        try {
            Class<?> clazz = accountingDimension.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value;
                value = field.get(accountingDimension);
                if (value == null) {
                    field.set(accountingDimension, "");
                }
            }
        } catch (IllegalAccessException e) {
            log.error("转换异常: ", e);
        }
    }


    public static String getAccountCodeOldVersion(List<ApprovalInstanceForm> forms, String accountFeishuName) {
        for (ApprovalInstanceForm form : forms) {
            if (accountFeishuName.equals(form.getName())) {
                try {
                    AccountValue accountValue = JSONObject.parseObject(form.getValue(), AccountValue.class);
                    if (accountValue == null) {
                        log.error("收款账户转换为空: {}", form.getValue());
                        return "";
                    } else {
                        List<Account> accounts = Constants.LIST_TABLE_03;
                        for (Account account : accounts) {
                            if (account.getFeishuOption().equals(accountValue.getWidgetAccountName())) {
                                return account.getAccountCode();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("收款账户转换异常: {}", form.getValue());
                    return "";
                }
                break;
            }
        }
        return "";
    }

    public static String getAccountCode(List<ApprovalInstanceForm> forms, String accountFeishuName) {
        for (ApprovalInstanceForm form : forms) {
            if (accountFeishuName.equals(form.getName())) {
                String feishuValue = form.getValue();
                List<Account> accounts = Constants.LIST_TABLE_03;
                for (Account account : accounts) {
                    if (account.getFeishuOption().equals(feishuValue)) {
                        return account.getAccountCode();
                    }
                }
                break;
            }
        }
        log.error("付款银行转换为空: {}", JSONObject.toJSONString(forms));
        return "";
    }

    public static String calculateIncludeTax(String amount, String taxRate) {
        if (StrUtil.isEmpty(taxRate) || StrUtil.isEmpty(amount)) {
            log.error("税率或金额为空");
            return "0";
        } else {
            if ("0%".equals(taxRate)) {
                return amount;
            }
            BigDecimal amountBigDecimal = new BigDecimal(amount);
            if (taxRate.endsWith("%")) {
                taxRate = taxRate.substring(0, taxRate.length() - 1);
            }
            // 类型转换后计算 金额/(1+税率)
            BigDecimal percentTaxRate = new BigDecimal(taxRate);
            BigDecimal divide = percentTaxRate.divide(new BigDecimal("100"));
            BigDecimal add = divide.add(new BigDecimal("1"));
            BigDecimal result = amountBigDecimal.divide(add, 2, RoundingMode.HALF_UP);
            return result.toString();
        }
    }

    public static String calculateIncludeTaxTwo(String amount, String taxRate) {
        if (StrUtil.isEmpty(taxRate) || StrUtil.isEmpty(amount)) {
            log.error("税率或金额为空");
            return "0";
        } else {
            if ("0%".equals(taxRate)) {
                return amount;
            }
            BigDecimal amountBigDecimal = new BigDecimal(amount);
            if (taxRate.endsWith("%")) {
                taxRate = taxRate.substring(0, taxRate.length() - 1);
            }
            // 类型转换后计算 金额/(1+税率)*税率
            BigDecimal percentTaxRate = new BigDecimal(taxRate);
            BigDecimal divide = percentTaxRate.divide(new BigDecimal("100"));
            BigDecimal add = divide.add(new BigDecimal("1"));

            BigDecimal result = amountBigDecimal.divide(add, 5, RoundingMode.HALF_UP);
            BigDecimal multiply = result.multiply(divide);
            return multiply.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP).toString();
        }
    }

    public static void replaceNullFieldToEmpty(List<VoucherDetail> voucherDetails) {
        for (VoucherDetail voucherDetail : voucherDetails) {
            if (voucherDetail.getAccountingDimension() == null) {
                voucherDetail.setAccountingDimension(new AccountingDimension());
            }
            setFieldEmpty(voucherDetail.getAccountingDimension());
        }
    }

    public static <T> void bracketReplace(T testEntity) {
        try {
            Class<?> clazz = testEntity.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                if ("costSubcategory".equals(name)
                        || "costCategory".equals(name)
                        || "brand".equals(name)
                        || "shopName".equals(name)
                        || "businessName".equals(name)
                        || "customName".equals(name)
                        || "feishuDepartmentName".equals(name)
                        || "employeeName".equals(name)
                        || "supplierName".equals(name)
                ) {
                    if (field.get(testEntity) != null && ((String) field.get(testEntity)).contains("（")) {
                        field.set(testEntity, ((String) field.get(testEntity)).replaceAll("（", "("));
                    }
                    if (field.get(testEntity) != null && ((String) field.get(testEntity)).contains("）")) {
                        field.set(testEntity, ((String) field.get(testEntity)).replaceAll("）", ")"));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            log.error("转换异常: ", e);
        }
    }

    public static String getExplanationName(String typeName, String serialNumber, List<ApprovalInstanceForm> forms, List<ApprovalInstanceForm> formDetails) {
        if (StrUtil.isEmpty(typeName) || StrUtil.isEmpty(serialNumber) || ArrayUtil.isEmpty(formDetails) || ArrayUtil.isEmpty(forms)) {
            log.error("参数为空: {},{},{},{}", typeName, serialNumber, forms, formDetails);
            return "";
        }
        // 申请类别&收款人（单位）全称&所属品牌&费用归属年份费用归属月份&飞书流程号&费用大类&费用子类&品牌核销&备注
        String summary = StringUtil.getValueByName(formDetails, "品牌是否核销");
        String explanation = typeName +
                "&" + StringUtil.getValueByName(forms, "收款人（单位）全称") +
                "&" + StringUtil.getValueByName(formDetails, "所属品牌") +
                "&" + StringUtil.getValueByName(formDetails, "费用归属年份") + StringUtil.getValueByName(formDetails, "费用归属月份") +
                "&" + serialNumber +
                "&" + StringUtil.getValueByName(formDetails, "费用大类") +
                "&" + StringUtil.getValueByName(formDetails, "费用子类") +
                ("是".equals(summary) ? "&品牌核销" : "") +
                "&" + StringUtil.getValueByName(formDetails, "备注");
        return explanation;
    }

    public static String matchTaxAmountValueByName(List<ApprovalInstanceForm> formDetails, String name) {
        for (ApprovalInstanceForm form : formDetails) {
            if (form.getName().contains(name)) {
                return form.getValue();
            }
        }
        return "0";
    }

    public static String subtractAmount(String valueOne, String valueTwo) {
        BigDecimal bigDecimalOne = new BigDecimal(valueOne);
        BigDecimal bigDecimalTwo = new BigDecimal(valueTwo);
        return bigDecimalOne.subtract(bigDecimalTwo).toString();
    }

    public static ApprovalInstanceFormResult instanceToFormList(String instanceCode) {
        ApprovalInstanceFormResult result = new ApprovalInstanceFormResult();
        ApprovalInstance approvalInstance = SignUtil.approvalInstanceDetail(instanceCode);
        List<ApprovalInstanceForm> approvalInstanceForms;
        try {
            if (approvalInstance != null) {
                result.setApprovalInstance(approvalInstance);
                approvalInstanceForms = JSONObject.parseArray(approvalInstance.getForm(), ApprovalInstanceForm.class);
                if (ArrayUtil.isEmpty(approvalInstanceForms)) {
                    log.info("实例Form解析字段列表为空");
                    result.setApprovalInstanceForms(Collections.emptyList());
                }
                result.setApprovalInstanceForms(approvalInstanceForms);
            }
            return result;
        } catch (Exception e) {
            log.error("审批单据转换异常: ", e);
            result.setApprovalInstanceForms(Collections.emptyList());
            return result;
        }
    }

    public static List<String> getInstanceCodeList(List<ApprovalInstanceForm> forms, String name) {
        for (ApprovalInstanceForm form : forms) {
            if (name.equals(form.getName())) {
                try {
                    List<String> departments = JSONObject.parseArray(form.getValue(), String.class);
                    if (ArrayUtil.isEmpty(departments)) {
                        log.error("审批中引用审批列表转换为空: {}", form.getValue());
                        return Collections.emptyList();
                    } else {
                        return departments;
                    }
                } catch (Exception e) {
                    log.error("审批中引用审批列表转换异常: {}", form.getValue());
                    return Collections.emptyList();
                }
            }
        }
        return Collections.emptyList();
    }

    public static int getCurrentSelectNumber(String selectString) {

        try {
            if (selectString.startsWith("明细")) {
                return Integer.parseInt(selectString.substring(2)) - 1;
            } else {
                log.error("审批设置的选择项目标题开头不为明细，请设置如下: 明细x");
                return -1;
            }
        } catch (Exception e) {
            log.error("强制转换Int异常");
            return -1;
        }
    }

    public static String positiveNumber(String amount) {
        if (StrUtil.isEmpty(amount)) {
            return "";
        }
        if (amount.startsWith("-")) {
            return amount.substring(1);
        }
        return "";
    }

    public static List<Integer> timestampToYearMonthDay(String generationDate) {
        long timestamp = Long.parseLong(generationDate);
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.ofHours(8));
        // 获取年份
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        List<Integer> result = new ArrayList<>();
        result.add(year);
        result.add(month);
        result.add(day);
        return result;
    }

    public static <T> void clearSpecialSymbols(T testEntity) {
        try {
            Class<?> clazz = testEntity.getClass();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getType() == String.class) {
                    if (field.get(testEntity) != null) {
                        String temp = (String) field.get(testEntity);
                        String result = temp.replace(" ", "").replace("\n", "").replace("\r", "").replace(" ", "");
                        field.set(testEntity, result);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            log.error("转换异常: ", e);
        }
    }

    public static String placeholderTwo(int month) {
        if (month >= 1 && month <= 9) {
            return "0" + month;
        } else {
            return String.valueOf(month);
        }
    }

    public static String subBusinessName(String businessName) {
        if (StrUtil.isEmpty(businessName)) {
            return "";
        }
        if (businessName.contains("-")) {
            return businessName.substring(businessName.lastIndexOf("-") + 1);
        } else {
            return businessName;
        }
    }

    public static String keepTwoDecimalPlaces(String excludingTaxAmount) {
        if (StrUtil.isEmpty(excludingTaxAmount)) {
            return "0";
        }
        BigDecimal bigDecimal = new BigDecimal(excludingTaxAmount);
        return bigDecimal.divide(BigDecimal.ONE, 2 , RoundingMode.HALF_UP).toString();
    }

    public static String subShopName(String shopName) {
        if (StrUtil.isEmpty(shopName)) {
            return "";
        }
        if (shopName.contains("-")) {
            return shopName.substring(0, shopName.indexOf("-"));
        } else {
            return shopName;
        }
    }
}
