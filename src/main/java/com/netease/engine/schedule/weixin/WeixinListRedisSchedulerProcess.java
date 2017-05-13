package com.netease.engine.schedule.weixin;

import org.springframework.stereotype.Component;
import com.netease.engine.schedule.RedisPriorityScheduler;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

/**
 * 微信列表页去重
 * 
 * @author handongming
 *
 */
@Component("weixinListRedisSchedulerProcess")
public class WeixinListRedisSchedulerProcess extends RedisPriorityScheduler {
	
	@Override
	public boolean isDuplicate(Request request, Task task) {
		return false;
	}
}
