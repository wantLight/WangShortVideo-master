package com.neepu.enums;

/**
 * 视频状态：0、待审核
 * 1、发布成功
 * 2、禁止播放，管理员操作
 */
public enum VideoStatusEnum {
	//待审核
	AUDIT(0),
	// 发布成功
	SUCCESS(1),
	FORBID(2);		// 禁止播放，管理员操作
	
	public final int value;
	
	VideoStatusEnum(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
}
