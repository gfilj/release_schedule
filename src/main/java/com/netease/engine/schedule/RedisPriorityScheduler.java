package com.netease.engine.schedule;

import com.alibaba.fastjson.JSON;
import com.netease.engine.zookeeper.Curator;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

import java.util.Set;

/**
 * redis添加优先级调度
 * 
 * @author handongming
 *
 */
@Component("redisPriorityScheduler")
public class RedisPriorityScheduler extends DefaultRedisScheduler {

	private static final String ZSET_PREFIX = "zset_";

	private static final String QUEUE_PREFIX = "queue_";

	private static final String NO_PRIORITY_SUFFIX = "_zore";

	private static final String PLUS_PRIORITY_SUFFIX = "_plus";

	private static final String MINUS_PRIORITY_SUFFIX = "_minus";
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	@Autowired
	@Qualifier("curator")
	private Curator curator;
	
	@Override
	protected void pushWhenNoDuplicate(Request request, Task task) {
		Jedis jedis = null;
		try {
			jedis=pool.getResource();
			String value = JSON.toJSONString(request);
			if (request.getPriority() > 0)
				jedis.zadd(getZsetPlusPriorityKey(task), request.getPriority(), value);
			else if (request.getPriority() < 0)
				jedis.zadd(getZsetMinusPriorityKey(task), request.getPriority(), value);
			else
				jedis.lpush(getQueueNoPriorityKey(task), value);
		}catch(Exception e){
			log.info("\n\n ***************unable to get the redis ***************\n\n",e);
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public synchronized Request poll(Task task) {
		Jedis jedis = null;
		try {
			jedis=pool.getResource();
			String value = getRequest(jedis, task);
			if (StringUtils.isBlank(value))
				return null;
			return JSON.parseObject(value, Request.class);
		}catch(Exception e){
			log.info("\n\n ***************unable to get the redis ***************\n\n",e);
			return null;
		} finally {
			pool.returnResource(jedis);
		}
	}

	private String getRequest(Jedis jedis, Task task) {
		InterProcessMutex lock = curator.getLock(getClass().getSimpleName() + "/lock", 1);
        if (lock == null) {
        	log.info("\n\n ***************unable to get the lock ***************\n\n");
            return null;
        }
        String value;
        try{
    		Set<String> values = jedis.zrevrange(getZsetPlusPriorityKey(task), 0, -1);
    		if (values.isEmpty()) {
    			value = jedis.lpop(getQueueNoPriorityKey(task));
    			if (StringUtils.isBlank(value)) {
    				values = jedis.zrevrange(getZsetMinusPriorityKey(task), 0, -1);
    				if (!values.isEmpty()) {
    					value = values.toArray(new String[0])[0];
    					jedis.zrem(getZsetMinusPriorityKey(task), value);
    				}
    			}
    		} else {
    			value = values.toArray(new String[0])[0];
    			jedis.zrem(getZsetPlusPriorityKey(task), value);
    		}
        }catch(Exception e){
        	log.info("\n\n ***************unable to get the getRequest redis ***************\n\n",e);
        	return null;
        }finally{
        	curator.releaseLock(lock);
        }
		return value;
	}

	public static String getZsetPlusPriorityKey(Task task) {
		return ZSET_PREFIX + task.getUUID() + PLUS_PRIORITY_SUFFIX;
	}

	public static String getQueueNoPriorityKey(Task task) {
		return QUEUE_PREFIX + task.getUUID() + NO_PRIORITY_SUFFIX;
	}

	public static String getZsetMinusPriorityKey(Task task) {
		return ZSET_PREFIX + task.getUUID() + MINUS_PRIORITY_SUFFIX;
	}

}
