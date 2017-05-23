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
			//			Thread thread = new Thread(new Runnable() {
//				public void run() {
//					while (true) {
//						try {
//							Thread.sleep(1000);
//
//							int threadAlive = weixinspider.getThreadPool().getThreadAlive();
//							log.info("threadPool size " + threadAlive);
//							if (threadAlive <= 3) {
//								try {
//									String taskStr = HttpClientFactory.runGetMethod("http://localhost:8079/getTask/2");
//
//									log.info(String.format("get task %s ", taskStr));
//									List<HashMap> taskArr = JSON.parseArray(taskStr, HashMap.class);
//									for (int i = 0; i < taskArr.size(); i++) {
//										HashMap taskObj = (HashMap) taskArr.get(i);
//										String sourceid = (String) taskObj.get("sourceId");
//										int priority = (int) taskObj.get("priority");
//										String url = "http://weixin.sogou.com/weixin?type=1&query=" + sourceid
//												+ "&ie=utf8&_sug_=n&_sug_type_=";
//										log.info("weixin searchUrl:" + url);
//										Request request = new Request(url);
//										Map<String, Object> extras = new ConcurrentHashMap<String, Object>();
//										extras.put("pageName", Constant.WEIXIN_SEARCH);
//										extras.put("taskName", Constant.WEIXIN_SEARCH);
//										extras.put("needDeduplication", "false");
//										extras.put(Constant.REQUEST_HEADER_REFERER, "http://weixin.sogou.com/");
//										request.setExtras(extras);
//										request.setSourceid(sourceid);
//										request.setPriority(priority);
//										request.setWhether_proxy(true);
//										weixinspider.addRequest(request);
//									}
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//							}
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}
//			});
//			thread.start();
			log.info("=======================System  initialization  end=======================");
		} catch (Exception e) {
			log.error("系统初始化失败!!", e);
			e.printStackTrace();
		}

	}

}
