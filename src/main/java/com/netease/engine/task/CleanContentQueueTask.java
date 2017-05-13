package com.netease.engine.task;

import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Request;

import com.alibaba.fastjson.JSON;
import com.netease.engine.schedule.weixin.WeixinContentRedisSchedulerProcess;
import com.netease.engine.util.DateUtil;

/**
 * 内容队列去重定时任务
 * 
 * @author handongming
 *
 */
public class CleanContentQueueTask {
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	
	private int day;
	
	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public void run(){
		Jedis jedis = pool.getResource();
		boolean flag = true;
		try {
			while(flag){
				Set<String> cleanKey = jedis.zrange(WeixinContentRedisSchedulerProcess.CLEAN_CONTENT_QUEUE, 0, 0);
				if(cleanKey != null && !cleanKey.isEmpty()){
					for(String key : cleanKey){
						String[] keys = key.split("@");
						String taskKey = keys[0];
						String modifykey = keys[1];
						String value = jedis.hget(taskKey, modifykey);
						Request request = JSON.parseObject(value, Request.class);
						Object updatetimeobj = request.getExtra("updatetime");
						if(updatetimeobj != null){
							long timestamp = (long) updatetimeobj;
							Date updatetime = new Date(timestamp);
							if(DateUtil.daysSub(updatetime, new Date()) >= day){
								jedis.hdel(taskKey, modifykey);
								jedis.zrem(WeixinContentRedisSchedulerProcess.CLEAN_CONTENT_QUEUE, key);
							}else{
								flag = false;
							}
						}
					}
				}else{
					flag=false;
				}
			}
		} finally {
			pool.returnResource(jedis);
		}
	}
}
