package com.netease.engine.mapper;

import java.util.List;

import com.netease.engine.model.AppRecordInfo;

public interface AppRecordInfoMapper {
	
	public void save(AppRecordInfo appRecordInfo);
	
	public List<AppRecordInfo> findByStatus(int status);
	
	public AppRecordInfo findByModifykey(String modifykey);
	
	public List<AppRecordInfo> findByModifykeyAndType(AppRecordInfo info);
	
	//根据文章id(指纹)和type更新
	public void updateByModifykeyAndType(AppRecordInfo appRecordInfo);
}
