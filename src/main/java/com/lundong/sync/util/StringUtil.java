package com.lundong.sync.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lundong.sync.config.Constants;
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
            return multiply.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP) .toString();
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
}
