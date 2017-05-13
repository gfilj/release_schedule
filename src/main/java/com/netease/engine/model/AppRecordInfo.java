package com.netease.engine.model;

import java.util.Date;

/**
 * 记录表
 * 
 * @author handongming
 *
 */
public class AppRecordInfo{
	private int id;			//自增id
	private int appid;    //抓取类型
	private String sourceid;	//源id
	private String title; //标题
	private String url;		//URL地址
	private int type;		//结果类型(1 正文 2 评论)
	private String storeid;	//nos存储id
	private Date create_time;//创建时间
	private Date update_time;//最后更新时间
	private String modifykey;//指纹去重标记
	private int status;		//状态(待确定)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getAppid() {
		return appid;
	}
	public void setAppid(int appid) {
		this.appid = appid;
	}
	public String getSourceid() {
		return sourceid;
	}
	public void setSourceid(String sourceid) {
		this.sourceid = sourceid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getStoreid() {
		return storeid;
	}
	public void setStoreid(String storeid) {
		this.storeid = storeid;
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
	public String getModifykey() {
		return modifykey;
	}
	public void setModifykey(String modifykey) {
		this.modifykey = modifykey;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

	
}
