package com.netease.engine.util;

import com.netease.engine.service.RedisService;

public class RedisUtil {
	
	public static RedisService redisManager = ApplicationContextInit.getBean("redisService");
	
	public static final int expireTime = 86400;

	public static final String separator = ":";		
	
	
}
