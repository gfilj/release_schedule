package com.netease.engine.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.netease.engine.task.NosDaemonThread;
import com.netease.engine.util.ApplicationContextInit;
import com.netease.engine.util.Constant;
import com.netease.engine.util.HttpClientFactory;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.Scheduler;

/**
 * 绯荤粺鍒濆鍖�
 * 
 * @author handongming
 *
 */
public class InitListener implements ServletContextListener {

	private final Log log = LogFactory.getLog(this.getClass());

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			log.info("=======================System initialization start=======================");

			PageProcessor defaultProcess = ApplicationContextInit.getBean("defaultProcess");
			Scheduler defaultRedisScheduler = ApplicationContextInit.getBean("defaultRedisScheduler");
			Pipeline defaultPipeline = ApplicationContextInit.getBean("defaultPipeline");
			HttpClientDownloader httpClientDownloader = ApplicationContextInit.getBean("httpClientDownloader");
			final Spider weixinspider = Spider.create(defaultProcess)
					.scheduler(defaultRedisScheduler)
					.setUUID("weixin_spider")
					.setExitWhenComplete(false)
					.thread(Executors.newCachedThreadPool(), 5)
					.setDownloader(httpClientDownloader)
					.addPipeline(defaultPipeline);
			Thread weixinThread = new Thread(weixinspider, "grab-listener-thread-weixin");
			// SpiderMonitor.instance().register(weixinspider);
			weixinThread.start();
			Constant.spiders.put(Constant.SPIDER_TYPE_WEIXIN, weixinspider);

			final NosDaemonThread nosDaemonThread = ApplicationContextInit.getBean("nosDaemonThread");
			Thread nosupload = new Thread(new Runnable() {
				@Override
				public void run() {
					nosDaemonThread.run();
				}
			});
			nosupload.start();

			Thread thread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(1000);
							int threadAlive = weixinspider.getThreadPool().getThreadAlive();
							log.info("threadPool size " + threadAlive);
							if (threadAlive <= 3) {
								String taskStr = HttpClientFactory.runGetMethod("http://localhost:8079/getTask/2");

								log.info(String.format("get task %s ", taskStr));
								List<Request> parseArray = JSON.parseArray(taskStr, Request.class);
								for (Request request : parseArray) {
									log.info("weixin list Request:" + request);
									weixinspider.addRequest(request);
								}
							}else{
								Thread.sleep(1000);
							}
						} catch (Exception e) {
							log.info("parse2 info error" + e);
						}
					}
				}
			});
			thread.start();
			log.info("=======================System  initialization  end=======================");
		} catch (Exception e) {
			log.error("绯荤粺鍒濆鍖栧け璐�!", e);
			e.printStackTrace();
		}

	}

}
