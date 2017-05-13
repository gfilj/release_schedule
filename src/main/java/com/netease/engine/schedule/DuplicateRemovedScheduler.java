package com.netease.engine.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import redis.clients.jedis.JedisPool;
import com.netease.engine.vo.SpiderBean;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

/**
 * 去重调度管理器
 * 
 * @author handongming
 *
 */
public abstract class DuplicateRemovedScheduler implements Scheduler {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
	@Qualifier("spiderBean")
	private SpiderBean spiderBean;
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	

    @Override
    public void push(Request request, Task task) {
        logger.info("get a candidate url {}\t", request.getUrl());
        if (!isDuplicate(request, task) || shouldReserved(request)) {
            logger.info("push to queue {}\t", request.getUrl());
            pushWhenNoDuplicate(request, task);
        }
    }
    
    
	public boolean isDuplicate(Request request, Task task) {
		String pageName = (String) request.getExtra("pageName");
		try {
			DefaultRedisScheduler redisScheduler = spiderBean.getRedisSchedulerMap().get(pageName);
			if(redisScheduler == null) {
				logger.info("unable to match to the appropriate redisscheduler");
				return true;
			}
			redisScheduler.isDuplicate(request, task);			
		}catch(Exception e)	{
			logger.error("match redisscheduler error \t", e);
		}
		return false;
	}

    protected boolean shouldReserved(Request request) {
        return request.getExtra(Request.CYCLE_TRIED_TIMES) != null;
    }

    protected void pushWhenNoDuplicate(Request request, Task task) {

    }
}
