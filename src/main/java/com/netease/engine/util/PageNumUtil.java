package com.netease.engine.util;

public class PageNumUtil {
	public static int getTotalPage(int total,int pagesize){
		int page=0;
		if(total!=0&&pagesize!=0){
			page=total/pagesize;
		}
		return (page*pagesize)<total?(page+1):page;
	}
}
