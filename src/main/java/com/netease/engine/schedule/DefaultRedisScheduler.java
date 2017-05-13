package com.netease.engine.schedule;

import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.engine.vo.SpiderBean;
import com.netease.engine.zookeeper.Curator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

/**
 * url分布式调度
 * 
 * @author handongming
 *
 */
@Component("defaultRedisScheduler")
public class DefaultRedisScheduler extends DuplicateRemovedScheduler implements
		MonitorableScheduler, DuplicateRemover {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	@Qualifier("spiderBean")
	private SpiderBean spiderBean;
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	
	@Autowired
	@Qualifier("curator")
	private Curator curator;

	@Override
	public void resetDuplicateCheck(Task task) {}

	@Override
	protected void pushWhenNoDuplicate(Request request, Task task) {
		String pageName = (String) request.getExtra("pageName");
		try {
			DefaultRedisScheduler redisScheduler = spiderBean.getRedisSchedulerMap().get(pageName);
			if(redisScheduler == null) {
				log.info("unable to match to the appropriate redisscheduler");
				return;
			}
			redisScheduler.pushWhenNoDuplicate(request, task);
		} catch(Exception e)	{
			log.error(e);
		}
	}
	
	
	@Override
	public synchronized Request poll(Task task) {
		InterProcessMutex lock = curator.getLock(getClass().getSimpleName() + "/lock", 1);
        if (lock == null) {
        	log.info("\n\n ***************unable to get the lock ***************\n\n");
            return null;
        }
		Jedis jedis = pool.getResource();
		try {
			String value = getRequest(jedis, task);
			if (StringUtils.isBlank(value))
				return null;
			return JSON.parseObject(value, Request.class);
		} finally {
			pool.returnResource(jedis);
			curator.releaseLock(lock);
		}
	}
	
	
	private String getRequest(Jedis jedis, Task task) {
		String value;
		Set<String> values = jedis.zrevrange(RedisPriorityScheduler.getZsetPlusPriorityKey(task), 0, -1);
		if (values.isEmpty()) {
			value = jedis.lpop(RedisPriorityScheduler.getQueueNoPriorityKey(task));
			if (StringUtils.isBlank(value)) {
				values = jedis.zrevrange(RedisPriorityScheduler.getZsetMinusPriorityKey(task), 0, -1);
				if (!values.isEmpty()) {
					value = values.toArray(new String[0])[0];
					jedis.zrem(RedisPriorityScheduler.getZsetMinusPriorityKey(task), value);
				}
			}
		} else {
			value = values.toArray(new String[0])[0];
			jedis.zrem(RedisPriorityScheduler.getZsetPlusPriorityKey(task), value);
		}
		return value;
	}
	
	@Override
	public int getLeftRequestsCount(Task task) {
		Jedis jedis = pool.getResource();
		try {
			Long plus_size = jedis.scard(RedisPriorityScheduler.getZsetPlusPriorityKey(task));
			Long minus_size = jedis.scard(RedisPriorityScheduler.getZsetMinusPriorityKey(task));
			Long zore_siez = jedis.llen(RedisPriorityScheduler.getQueueNoPriorityKey(task));
			return plus_size.intValue() + minus_size.intValue() + zore_siez.intValue();
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public int getTotalRequestsCount(Task task) {
		Jedis jedis = pool.getResource();
		try {
			Long plus_size = jedis.scard(RedisPriorityScheduler.getZsetPlusPriorityKey(task));
			Long minus_size = jedis.scard(RedisPriorityScheduler.getZsetMinusPriorityKey(task));
			Long zore_siez = jedis.llen(RedisPriorityScheduler.getQueueNoPriorityKey(task));
			return plus_size.intValue() + minus_size.intValue() + zore_siez.intValue();
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public boolean isDuplicate(Request request, Task task) {
		String pageName = (String) request.getExtra("pageName");
		try {
			DefaultRedisScheduler redisScheduler = spiderBean.getRedisSchedulerMap().get(pageName);
			if(redisScheduler == null) {
				log.info("\n\n ***************unable to match to the appropriate redisscheduler ***************\n request-->{} \n"+JSONObject.toJSONString(request));
				return true;
			}
			return redisScheduler.isDuplicate(request, task);			
		}catch(Exception e)	{
			log.error(e);
		}
		return false;
	}
}
