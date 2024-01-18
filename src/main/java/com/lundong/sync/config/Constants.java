package com.lundong.sync.config;

import com.lundong.sync.entity.base.*;
import com.lundong.sync.entity.bitable.approval.SecondExceptionTable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;

/**
 * @author RawChen
 * @date 2023-06-25 14:02
 */
@EnableConfigurationProperties
public class Constants {

    public static TaskQueueExample queue = new TaskQueueExample(1000);

    public static String ACCESS_TOKEN = "";

    // 飞书自建应用 App ID
    public final static String APP_ID_FEISHU = "cli_a45ee3c70cbxxxx";

    // 飞书自建应用 App Secret
    public final static String APP_SECRET_FEISHU = "6MkitjVSFb9ER6lGrKJXBRyYdxxxxxx";

    // 飞书自建应用订阅事件 Encrypt Key
    public final static String ENCRYPT_KEY = "";

    // 飞书自建应用订阅事件 Verification Token
    public final static String VERIFICATION_TOKEN = "PESUdUW5tJfuISZFNy7U4dDuSMxxxxxx";

    // 金蝶云星空网址
    public final static String KINGDEE_API = "http://xxxx/k3cloud";
    public final static String ACCT_ID = "xx";
    public final static String USERNAME = "xx";
    public final static String PASSWORD = "xx";

//	// 金蝶云星空网址
//	public final static String KINGDEE_API = "http://192.168.110.223/k3cloud";
//	public final static String ACCT_ID = "642427270e9f87";
//	public final static String USERNAME = "Administrator";
//	public final static String PASSWORD = "Admin123456.";

    // 账套号
    public final static String ACCOUNT_BOOK = "201";
    public final static String VOUCHER_DOCUMENT_STATUS = "A";

    // 固定科目编码：其他应付款_个人
    public final static String ACCOUNT_CODE_EMPLOYEE = "2181.01";

    // 登录 ValidateUser
    public static final String KINGDEE_LOGIN = "/Kingdee.BOS.WebApi.ServicesStub.AuthService.ValidateUser.common.kdsvc";

    // 增、改 Save
    public final static String KINGDEE_SAVE = "/Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.Save.common.kdsvc";

    // 删 Delete
    public final static String KINGDEE_DELETE = "/Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.Delete.common.kdsvc";

    // 单据查询 View
    public final static String KINGDEE_VIEW = "/Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.View.common.kdsvc";

    // 列表查询 ExecuteBillQuery
    public final static String KINGDEE_QUERY = "/Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.ExecuteBillQuery.common.kdsvc";

    // 付款申请&个人报销&预付申请审批CODE
    public final static String PAYMENT_PERSONAL_PREPAID_APPROVAL_CODE = "D21ABB18-F620-4974-8254-1E1CD6BEBE1E";

    // 开票申请审批CODE
    public final static String INVOICING_APPLICATION_APPROVAL_CODE = "BED066C1-8E14-49DE-B603-583644694639";

    // 发票核销审批CODE
    public final static String INVOICE_WRITE_OFF_APPROVAL_CODE = "0DF19FE2-A617-41CB-9360-5AD35A9129C6";

    // 预提申请审批CODE
    public final static String WITHHOLDING_APPLICATION_APPROVAL_CODE = "307AE7F2-8C43-4080-85FB-F5CB2CF56C74";

    // 退款申请审批CODE
    public final static String REFUND_APPLICATION_APPROVAL_CODE = "DDCE15D6-E889-4EFD-911A-AB65A8B464AB";

    // 审批生成凭证 多维表格APP_TOKEN
    public static final String APP_TOKEN_APPROVAL = "SFJ1bu5GJaT2CSse8QUcEIb0nRh";

    // 付款申请第一张（映射表）
    public static final String TABLE_01 = "tblT8MTY4HdHMgzc";
    public static List<Bitable> LIST_TABLE_01;

    // 品牌&店铺&新业务组
    public static final String TABLE_02 = "tblsxGFY1YHJ9zXy";
    public static List<BrandShopBusiness> LIST_TABLE_02;

    // 账户信息
    public static final String TABLE_03 = "tblAJviRNBAlX30v";
    public static List<Account> LIST_TABLE_03;

    // 供应商信息
    public static final String TABLE_04 = "tbl3ryysGsr13sI4";
    public static List<Supplier> LIST_TABLE_04;

    // 员工信息
    public static final String TABLE_05 = "tbl43aqGSiLW1Tnh";
    public static List<Employee> LIST_TABLE_05;

    // 客户信息
    public static final String TABLE_06 = "tblOC8tr7fqMl7Oe";
    public static List<Custom> LIST_TABLE_06;

    // 部门信息
    public static final String TABLE_07 = "tbl0HNjK1p4qVFXH";
    public static List<Department> LIST_TABLE_07;

    // 付款申请&个人报销（第二张生成例外表）
    public static final String TABLE_08 = "tblZwpq9z0Mws5jR";
    public static List<SecondExceptionTable> LIST_TABLE_08;

    // 付款申请第二张（逻辑1映射表）
    public static final String TABLE_09 = "tblBDWo5f1RNcufB";
    public static List<Bitable> LIST_TABLE_09;

    // 付款申请第二张（逻辑2映射表）
    public static final String TABLE_10 = "tbl9q5onR4WLRSoV";
    public static List<Bitable> LIST_TABLE_10;

    // 付款申请第二张（逻辑3映射表）
    public static final String TABLE_11 = "tblUrxaxRBcPjRy8";
    public static List<Bitable> LIST_TABLE_11;

    // 付款申请第二张（逻辑4映射表）
    public static final String TABLE_12 = "tblZNF32Zjxr2E7H";
    public static List<Bitable> LIST_TABLE_12;

    // 个人报销第一张（映射表）
    public static final String TABLE_13 = "tbl0Z7XeAv6P9UVQ";
    public static List<Bitable> LIST_TABLE_13;

    // 个人报销第二张（逻辑1映射表）
    public static final String TABLE_14 = "tbliAHwhnxdL2BWu";
    public static List<Bitable> LIST_TABLE_14;

    // 个人报销第二张（逻辑2映射表）
    public static final String TABLE_15 = "tblQCuOINHiAHwSe";
    public static List<Bitable> LIST_TABLE_15;

    // 预付申请第一张（映射表）
    public static final String TABLE_16 = "tblkcysXhD6up6pt";
    public static List<Bitable> LIST_TABLE_16;

    // 预付申请第二张（映射表）
    public static final String TABLE_17 = "tblCpj6FNHPi3Sry";
    public static List<Bitable> LIST_TABLE_17;

    // 开票申请（逻辑1映射）
    public static final String TABLE_18 = "tblHa7vT8ZiHC1Wb";
    public static List<Bitable> LIST_TABLE_18;

    // 开票申请（逻辑2映射）
    public static final String TABLE_19 = "tblhSNdB8wypH7Em";
    public static List<Bitable> LIST_TABLE_19;

    // 发票核销（逻辑1映射）
    public static final String TABLE_20 = "tblSCDsu0y9qyH25";
    public static List<Bitable> LIST_TABLE_20;

    // 发票核销（逻辑2映射）
    public static final String TABLE_21 = "tblsh1xEQ10DxGqS";
    public static List<Bitable> LIST_TABLE_21;

    // 预提申请（逻辑1映射）
    public static final String TABLE_22 = "tbljXJsxJBLyYX4a";
    public static List<Bitable> LIST_TABLE_22;

    // 预提申请（逻辑2映射）
    public static final String TABLE_23 = "tblIPcoQRvtUZCNK";
    public static List<Bitable> LIST_TABLE_23;

    // 退款申请（逻辑1映射）
    public static final String TABLE_24 = "tblgxPxqjL1Z2T3Q";
    public static List<Bitable> LIST_TABLE_24;

    // 退款申请（逻辑2映射）
    public static final String TABLE_25 = "tblK7IwEXEWlodjz";
    public static List<Bitable> LIST_TABLE_25;

    // 多维表格生成凭证 多维表格APP_TOKEN
    public static final String APP_TOKEN_BITABLE = "U32HbX7sSaatdtsmGeGcPDhVnEe";

    // 收入暂估表
    public static final String TABLE_ID_INCOME_ESTIMATION = "tbl9aoq5K3U1dIpf";

    // 消耗暂估表
    public static final String TABLE_ID_CONSUMPTION_ESTIMATION = "tbl5wGnCQmRLJDRV";

    // 其他摊销
    public static final String TABLE_ID_OTHER_AMORTIZATION = "tbltM3j9bl4HOA7W";

    // 房租及物业
    public static final String TABLE_ID_RENT_PROPERTY_MANAGEMENT = "tblX2vhFkIgzNBYK";

    // 长期待摊
    public static final String TABLE_ID_RENOVATION = "tblcdp0SIJJ7cpGz";

    // 收入暂估核算维度映射表
    public static final String TABLE_26 = "tbllsic2H7dbuGoS";
    public static List<BrandShopBusiness> LIST_TABLE_26;

    // 收入暂估科目映射表
    public static final String TABLE_27 = "tblyMBFYqdYBzACx";
    public static List<Bitable> LIST_TABLE_27;

    // 消耗暂估核算维度映射表
    public static final String TABLE_28 = "tblwurA588w5ODvP";
    public static List<BrandShopBusiness> LIST_TABLE_28;

    // 消耗暂估科目映射表
    public static final String TABLE_29 = "tblmyQuqTDQKjGKD";
    public static List<Bitable> LIST_TABLE_29;

    // 其他摊销核算维度映射表
    public static final String TABLE_30 = "tblfPkHl7m031ZKr";
    public static List<BrandShopBusiness> LIST_TABLE_30;

    // 其他摊销科目映射表
    public static final String TABLE_31 = "tblvQvFQJ8gNM7lW";
    public static List<Bitable> LIST_TABLE_31;
}
