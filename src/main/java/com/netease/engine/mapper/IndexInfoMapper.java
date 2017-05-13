package com.netease.engine.mapper;

import org.apache.ibatis.annotations.Param;

import com.netease.engine.model.IndexInfo;


/**
 * DAO信息映射
 * @author handongming
 *
 */
public interface IndexInfoMapper {
	
	public Long selectIndex(@Param("id")String id);
	
	public void updateIndex(@Param("id")String id,@Param("index")Long index);
	
	public void updateIndexInfo(IndexInfo indexInfo);
	
}
