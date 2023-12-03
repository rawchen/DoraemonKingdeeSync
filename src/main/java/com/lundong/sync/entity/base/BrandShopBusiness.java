package com.lundong.sync.entity.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 品牌&店铺&新业务组
 *
 * @author shuangquan.chen
 * @date 2023-11-29 17:04
 */
@Data
public class BrandShopBusiness {

    @JSONField(name = "所属品牌")
    private String brand;

    @JSONField(name = "店铺编码")
    private String shopCode;

    @JSONField(name = "店铺名称")
    private String shopName;

    @JSONField(name = "新业务组编码")
    private String businessCode;

    @JSONField(name = "新业务组名称")
    private String businessName;

}
