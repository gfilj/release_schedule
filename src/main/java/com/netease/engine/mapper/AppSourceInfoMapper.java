package com.netease.engine.mapper;

import java.util.List;

import com.netease.engine.model.AppSourceInfo;

public interface AppSourceInfoMapper {
	
	public List<AppSourceInfo> selectAll();
	
	public void delete(AppSourceInfo info);
	
	
	public AppSourceInfo getAppSourceInfoById(String id);

}
