package com.lundong.sync.entity.kingdee;

import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-12-01 16:52
 */
@Data
public class AccountingDimension {

    /**
     * 资产类别
     */
    private String fflex10;

    /**
     * 供应商
     */
    private String fflex4;

    /**
     * 部门
     */
    private String fflex5;

    /**
     * 费用项目
     */
    private String fflex9;

    /**
     * 客户
     */
    private String fflex6;

    /**
     * 员工
     */
    private String fflex7;

    /**
     * 组织机构
     */
    private String fflex11;

    /**
     * 物料
     */
    private String fflex8;

    /**
     * 物料分组
     */
    private String fflex12;

    /**
     * 客户分组
     */
    private String fflex13;

    /**
     * 店铺
     */
    private String ff100002;

    /**
     * 费用类型
     */
    private String ff100003;

    /**
     * 项目档案
     */
    private String ff100004;

    /**
     * 新业务组
     */
    private String ff100005;

}
