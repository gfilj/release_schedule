package com.netease.engine.model;

import java.io.Serializable;

/**
 * 正文信息
 * @author handongming
 *
 */
public class ContentInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	private String title;//标题
	private String origin; //公共号名称
	private String content;//正文信息
	private String url;//原文链接
	private String time;//发布时间
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
}
