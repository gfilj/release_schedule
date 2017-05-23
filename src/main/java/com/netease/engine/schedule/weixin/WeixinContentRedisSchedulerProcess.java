package com.netease.engine.schedule.weixin;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.alibaba.fastjson.JSON;
import com.netease.engine.schedule.RedisPriorityScheduler;
import com.netease.engine.util.MD5Util;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

/**
 * 微信正文
 * 
 * @author handongming
 *
 */
@Component("weixinContentRedisSchedulerProcess")
public class WeixinContentRedisSchedulerProcess extends RedisPriorityScheduler {
	
	private static final String CONTENT_PREFIX = "content_";
	public static final String CLEAN_CONTENT_QUEUE="clean_content_queue";
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	
	@Override
	public boolean isDuplicate(Request request, Task task) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			/*if(request.isWhether_deposited())
				return false;*/
			String modifykey = MD5Util.calcMD5(request.getSourceid() + "_" + request.getTitle());
			request.putExtra("updatetime", new Date());
			long count = (long)jedis.hsetnx(getContentKey(task), modifykey,JSON.toJSONString(request));
			if(count > 0L){
				jedis.zadd(CLEAN_CONTENT_QUEUE, System.currentTimeMillis(), getContentKey(task) + "@" + modifykey);
				return false;
			}else{
				return true;
			}
		} finally {
			pool.returnResource(jedis);
		}
	}
	
	protected String getContentKey(Task task) {
		return CONTENT_PREFIX + task.getUUID();
	}
}
