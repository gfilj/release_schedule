package com.netease.engine.listener;

import java.util.concurrent.Executors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netease.engine.task.NosDaemonThread;
import com.netease.engine.util.ApplicationContextInit;
import com.netease.engine.util.Constant;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.Scheduler;

/**
 * 系统初始化
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
			HttpClientDownloader httpClientDownloader=ApplicationContextInit.getBean("httpClientDownloader");
			Spider weixinspider = Spider.create(defaultProcess)
					.scheduler(defaultRedisScheduler).setUUID("weixin_spider")
					.setExitWhenComplete(false)
					.thread(Executors.newCachedThreadPool(), 5)
					.setDownloader(httpClientDownloader)
					.addPipeline(defaultPipeline);
			Thread weixinThread = new Thread(weixinspider, "grab-listener-thread-weixin");
			SpiderMonitor.instance().register(weixinspider);
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
			
			log.info("=======================System  initialization  end=======================");
		} catch (Exception e) {
			log.error("系统初始化失败!!", e);
			e.printStackTrace();
		}

	}

}
