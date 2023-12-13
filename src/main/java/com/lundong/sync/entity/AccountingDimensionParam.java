package com.lundong.sync.entity;

import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-12-01 18:33
 */
@Data
public class AccountingDimensionParam {

    /**
     * 核算维度（拆分&）
     */
    private String accountingDimension;

    /**
     * 所属品牌（能查到店铺和新业务组）
     */
    private String brand;

    /**
     * 供应商或客户名称
     */
    private String supplierOrCustomName;

    /**
     * 供应商代码
     */
    private String supplierCode;

    /**
     * 员工
     */
    private String employee;

    /**
     * 部门
     */
    private String department;
}
