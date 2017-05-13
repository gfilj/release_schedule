package com.netease.engine.schedule.weixin;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.alibaba.fastjson.JSON;
import com.netease.engine.schedule.RedisPriorityScheduler;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

/**
 * 微信搜索入口
 * 
 * @author handongming
 *
 */
@Component("weixinSearchRedisSchedulerProcess")
public class WeixinSearchRedisSchedulerProcess extends RedisPriorityScheduler {
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	
	@Override
	public boolean isDuplicate(Request request, Task task) {
		Jedis jedis = pool.getResource();
		try {
			if (request.getPriority() > 0){
				Set<String> values = jedis.zrevrange(getZsetPlusPriorityKey(task), 0, -1);
				if(values.contains(JSON.toJSONString(request)))
					return true;
				else 
					return false;
			}
			else if (request.getPriority() < 0){
				Set<String> values = jedis.zrevrange(getZsetMinusPriorityKey(task), 0, -1);
				if(values.contains(JSON.toJSONString(request)))
					return true;
				else 
					return false;
			}
			else{
				boolean isDuplicate =  jedis.sismember(getQueueNoPriorityKey(task), JSON.toJSONString(request));
				return isDuplicate;
			}
		} finally {
			pool.returnResource(jedis);
		}
	}
}
