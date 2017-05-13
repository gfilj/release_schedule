package com.netease.engine.schedule;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

/**
 * url调度
 * 
 * @author handongming
 *
 */
public interface Scheduler {

	public void push(Request request, Task task);

	public Request poll(Task task);

}
