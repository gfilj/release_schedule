package com.netease.engine.model;

import java.util.Date;

/**
 * 需要抓取的公众号列表
 * @author handongming
 *
 */
public class AppSourceInfo {
	private long id;//自增id
	private String sourceid; //公众号ID
	private int appid;//类型(1微信)
	private String name; //公众号名字
	private Date create_time;//创建时间
	private Date update_time;//更新时间
	private int status;//公众号状态（0待抓取1已抓取-1抓取失败）
	private int priority; //优先级（大于0等于0小于0）

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSourceid() {
		return sourceid;
	}
	public void setSourceid(String sourceid) {
		this.sourceid = sourceid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
	public Date getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(Date update_time) {
		this.update_time = update_time;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getAppid() {
		return appid;
	}
	public void setAppid(int appid) {
		this.appid = appid;
	}
}
