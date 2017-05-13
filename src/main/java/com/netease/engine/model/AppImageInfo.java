package com.netease.engine.model;

import java.util.Date;

/**
 * 图片信息
 * 
 * @author handongming
 *
 */
public class AppImageInfo {

	private long id;

	private String url;// 图片原始链接

	private long fingerprintId; // url的指纹id

	private String storeId; // 存储于文件系统的唯一标识符nos

	private Date create_time; // 创建时间

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getFingerprintId() {
		return fingerprintId;
	}

	public void setFingerprintId(long fingerprintId) {
		this.fingerprintId = fingerprintId;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
	
}
