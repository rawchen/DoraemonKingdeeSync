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
    public final static String ACCOUNT_BOOK = "209";
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
    public final static String PAYMENT_PERSONAL_PREPAID_APPROVAL_CODE = "F5567689-781A-4AAC-995E-A58F00F92C2D";

    // 开票申请审批CODE
    public final static String INVOICING_APPLICATION_APPROVAL_CODE = "9231F104-AE34-455C-99F6-2E09CAE21A7D";

    // 发票核销审批CODE
    public final static String INVOICE_WRITE_OFF_APPROVAL_CODE = "920AF2C1-24C0-47A0-BDE0-2CB4197727CE";

    // 预提申请审批CODE
    public final static String WITHHOLDING_APPLICATION_APPROVAL_CODE = "12F8F5FC-D80B-4E63-909A-E46E0B63ED78";

    // 退款申请审批CODE
    public final static String REFUND_APPLICATION_APPROVAL_CODE = "F030BE04-16FA-434B-BA77-5361E915D4D5";

    // 审批生成凭证 多维表格APP_TOKEN
    public static final String APP_TOKEN_APPROVAL = "ItOxbgPt2a3g3Gs0helc9x4znwd";

    // 付款申请第一张（映射表）
    public static final String TABLE_01 = "tbl9LjcLRjpFESfL";
    public static List<Bitable> LIST_TABLE_01;

    // 品牌&店铺&新业务组
    public static final String TABLE_02 = "tblVMZ8nNQrb8vVX";
    public static List<BrandShopBusiness> LIST_TABLE_02;

    // 账户信息
    public static final String TABLE_03 = "tbl9dee95KQ5zZTg";
    public static List<Account> LIST_TABLE_03;

    // 供应商信息
    public static final String TABLE_04 = "tblyKayEqdCZdMDW";
    public static List<Supplier> LIST_TABLE_04;

    // 员工信息
    public static final String TABLE_05 = "tbl9SfkUBJrRAVfJ";
    public static List<Employee> LIST_TABLE_05;

    // 客户信息
    public static final String TABLE_06 = "tblxt950cQ8qdpEa";
    public static List<Custom> LIST_TABLE_06;

    // 部门信息
    public static final String TABLE_07 = "tblZU5IZQPwemJVp";
    public static List<Department> LIST_TABLE_07;

    // 付款申请&个人报销（第二张生成例外表）
    public static final String TABLE_08 = "tbliEaxNEOl3v0IM";
    public static List<SecondExceptionTable> LIST_TABLE_08;

    // 付款申请第二张（逻辑1映射表）
    public static final String TABLE_09 = "tblBbzwF3J7jrBC7";
    public static List<Bitable> LIST_TABLE_09;

    // 付款申请第二张（逻辑2映射表）
    public static final String TABLE_10 = "tblg4fQUZoYl0uu1";
    public static List<Bitable> LIST_TABLE_10;

    // 付款申请第二张（逻辑3映射表）
    public static final String TABLE_11 = "tbluUy9ueZNK3c8w";
    public static List<Bitable> LIST_TABLE_11;

    // 付款申请第二张（逻辑4映射表）
    public static final String TABLE_12 = "tblXHJKgK4JHGcCT";
    public static List<Bitable> LIST_TABLE_12;

    // 个人报销第一张（映射表）
    public static final String TABLE_13 = "tblR8Kk7me24eXvX";
    public static List<Bitable> LIST_TABLE_13;

    // 个人报销第二张（逻辑1映射表）
    public static final String TABLE_14 = "tblRwfVrgxkd9OrK";
    public static List<Bitable> LIST_TABLE_14;

    // 个人报销第二张（逻辑2映射表）
    public static final String TABLE_15 = "tbln3DOuEqpfy6gd";
    public static List<Bitable> LIST_TABLE_15;

    // 预付申请第一张（映射表）
    public static final String TABLE_16 = "tblEsN5NE7cJLhbK";
    public static List<Bitable> LIST_TABLE_16;

    // 预付申请第二张（映射表）
    public static final String TABLE_17 = "tblmLLWnuXaeMVxj";
    public static List<Bitable> LIST_TABLE_17;

    // 开票申请（逻辑1映射）
    public static final String TABLE_18 = "tbl8pq5lZnpG1YRh";
    public static List<Bitable> LIST_TABLE_18;

    // 开票申请（逻辑2映射）
    public static final String TABLE_19 = "tbl6QhBLKBUTQ5Xz";
    public static List<Bitable> LIST_TABLE_19;

    // 发票核销（逻辑1映射）
    public static final String TABLE_20 = "tbllsy5ezYUktumN";
    public static List<Bitable> LIST_TABLE_20;

    // 发票核销（逻辑2映射）
    public static final String TABLE_21 = "tblEYcufHXQVJUTQ";
    public static List<Bitable> LIST_TABLE_21;

    // 预提申请（逻辑1映射）
    public static final String TABLE_22 = "tblUUh5RMI2YLcR9";
    public static List<Bitable> LIST_TABLE_22;

    // 预提申请（逻辑2映射）
    public static final String TABLE_23 = "tblXK4tFzHnxpOoh";
    public static List<Bitable> LIST_TABLE_23;

    // 退款申请（逻辑1映射）
    public static final String TABLE_24 = "tblQhQzDEtroJDqT";
    public static List<Bitable> LIST_TABLE_24;

    // 退款申请（逻辑2映射）
    public static final String TABLE_25 = "tblntHRd0INhXiga";
    public static List<Bitable> LIST_TABLE_25;

    // 多维表格生成凭证 多维表格APP_TOKEN
    public static final String APP_TOKEN_BITABLE = "Aw02btCEVa3GuBskRUfcSNelnI8";

    // 收入暂估表
    public static final String TABLE_ID_INCOME_ESTIMATION = "tblXtdpvOXX9Jweq";

    // 消耗暂估表
    public static final String TABLE_ID_CONSUMPTION_ESTIMATION = "tblINVYVClMC2ufd";

    // 其他摊销
    public static final String TABLE_ID_OTHER_AMORTIZATION = "tblh5dSMDcS4e7PC";

    // 房租及物业
    public static final String TABLE_ID_RENT_PROPERTY_MANAGEMENT = "tblc85SDjjaGjFSe";

    // 长期待摊
    public static final String TABLE_ID_RENOVATION = "tblPl3cl2qvO9qMe";

    // 收入暂估核算维度映射表
    public static final String TABLE_26 = "tblLvtxAoUPumvHB";
    public static List<BrandShopBusiness> LIST_TABLE_26;

    // 收入暂估科目映射表
    public static final String TABLE_27 = "tblFxR0o9fnGHhxi";
    public static List<Bitable> LIST_TABLE_27;

    // 消耗暂估核算维度映射表
    public static final String TABLE_28 = "tblSsMFVVOYb0eee";
    public static List<BrandShopBusiness> LIST_TABLE_28;

    // 消耗暂估科目映射表
    public static final String TABLE_29 = "tblmTAZVjBHYucKP";
    public static List<Bitable> LIST_TABLE_29;

    // 其他摊销核算维度映射表
    public static final String TABLE_30 = "tblEP1HhHhHG8kQf";
    public static List<BrandShopBusiness> LIST_TABLE_30;

    // 其他摊销科目映射表
    public static final String TABLE_31 = "tblhv4iR8CH7NOUz";
    public static List<Bitable> LIST_TABLE_31;
}
