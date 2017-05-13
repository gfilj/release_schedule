package com.netease.engine.model;

import java.util.Date;

public class AppProjectInfo {
	private int id;//项目id
	private String name;//项目名
	private int status; //项目状态
	private Date create_time;//创建时间
	private String create_user;//创建人
	private int push_type;//推送方式
	private String push_path; //推送地址
	private String secret_key;//校验码
	private String secret_name;//校验名
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public String getCreate_user() {
		return create_user;
	}
	public void setCreate_user(String create_user) {
		this.create_user = create_user;
	}
	public int getPush_type() {
		return push_type;
	}
	public void setPush_type(int push_type) {
		this.push_type = push_type;
	}
	public String getPush_path() {
		return push_path;
	}
	public void setPush_path(String push_path) {
		this.push_path = push_path;
	}
	public String getSecret_key() {
		return secret_key;
	}
	public void setSecret_key(String secret_key) {
		this.secret_key = secret_key;
	}
	public String getSecret_name() {
		return secret_name;
	}
	public void setSecret_name(String secret_name) {
		this.secret_name = secret_name;
	}
}
