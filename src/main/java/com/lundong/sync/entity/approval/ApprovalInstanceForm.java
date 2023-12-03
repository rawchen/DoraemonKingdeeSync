package com.lundong.sync.entity.approval;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author RawChen
 * @date 2023-08-09 15:34
 */
@Data
public class ApprovalInstanceForm {

	/**
	 * ID
	 */
	@JSONField(name = "id")
	private String id;

	/**
	 * 自定义ID
	 */
	@JSONField(name = "custom_id")
	private String customId;

	/**
	 * 名称
	 */
	@JSONField(name = "name")
	private String name;

	/**
	 * 类型
	 */
	@JSONField(name = "type")
	private String type;

	/**
	 * 扩展
	 */
	@JSONField(name = "ext")
	private String ext;

	/**
	 * 值
	 */
	@JSONField(name = "value")
	private String value;

	/**
	 * 选项
	 */
	@JSONField(name = "option")
	private Option option;

}
