package com.netease.engine.vo;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import com.netease.engine.schedule.RedisPriorityScheduler;


public class SpiderBean implements InitializingBean {

	private ConcurrentHashMap<String,PageProcessor> processMap;
	private ConcurrentHashMap<String,Pipeline> pipelineMap;
	private ConcurrentHashMap<String,RedisPriorityScheduler> redisSchedulerMap;
	private Site site; 
	private int threadCount;
	private String chartSet;

	public String getChartSet() {
		return chartSet;
	}

	public void setChartSet(String chartSet) {
		this.chartSet = chartSet;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public ConcurrentHashMap<String, PageProcessor> getProcessMap() {
		return processMap;
	}

	public void setProcessMap(ConcurrentHashMap<String, PageProcessor> processMap) {
		this.processMap = processMap;
	}

	public ConcurrentHashMap<String, Pipeline> getPipelineMap() {
		return pipelineMap;
	}

	public void setPipelineMap(ConcurrentHashMap<String, Pipeline> pipelineMap) {
		this.pipelineMap = pipelineMap;
	}

	public ConcurrentHashMap<String, RedisPriorityScheduler> getRedisSchedulerMap() {
		return redisSchedulerMap;
	}

	public void setRedisSchedulerMap(
			ConcurrentHashMap<String, RedisPriorityScheduler> redisSchedulerMap) {
		this.redisSchedulerMap = redisSchedulerMap;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
	}


}
