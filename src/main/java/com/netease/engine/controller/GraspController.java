package com.netease.engine.controller;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import com.netease.engine.mapper.AppSourceInfoMapper;
import com.netease.engine.model.AppSourceInfo;
import com.netease.engine.service.InitWeixinService;
import com.netease.engine.task.CleanContentQueueTask;
import com.netease.engine.util.Constant;
import com.netease.engine.util.StringUtil;
import com.netease.engine.util.kafkaConsumer;
import com.netease.engine.util.kafkaProducer;

/**
 * 微信公众号抓取
 * 
 * @author handongming
 *
 */
@Controller
public class GraspController {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private InitWeixinService initWeixinService;
	@Autowired
	private CleanContentQueueTask cleanContentQueueTask;
	@Autowired
	private AppSourceInfoMapper appSourceInfoMapper;
	
	/**
	 * 抓取微信公众号
	 * @param httpRequest
	 * @return
	 */
	@RequestMapping(value ="/grasp_weixin.action", method = RequestMethod.GET)
	@ResponseBody
	public String grasp_weixin(HttpServletRequest httpRequest){  
		log.info("=======================grasp task start=======================");
		initWeixinService.loadWeixin();
		log.info("=======================grasp  task  end=======================");
		return "";
	} 

	/**
	 * 清理内容队列
	 * @param httpRequest
	 * @return
	 */
	@RequestMapping(value ="/clean.action", method = RequestMethod.GET)
	@ResponseBody
	public String clean(HttpServletRequest httpRequest){  
		log.info("=======================grasp task start=======================");
		cleanContentQueueTask.run();
		log.info("=======================grasp  task  end=======================");
		return "";
	} 
	
	/**
	 * kafka-consumer
	 * @param httpRequest
	 * @return
	 */
	@RequestMapping(value ="/kafka_consumer.action", method = RequestMethod.GET)
	@ResponseBody
	public String consumer(HttpServletRequest httpRequest){  
		log.info("start");
		new kafkaConsumer("kafka").start();
		log.info("end");
		return "";
	} 
	
	/**
	 * kafka-producer
	 * @param httpRequest
	 * @return
	 */
	@RequestMapping(value ="/kafka_producer.action", method = RequestMethod.GET)
	@ResponseBody
	public String producer(HttpServletRequest httpRequest){  
		log.info("start");
		new kafkaProducer("kafka").start();
		log.info("end");
		return "";
	} 
	
	
	/**
	 * 向队列中添加公众号
	 */
	@RequestMapping(value ="/pushsourceid.action", method = RequestMethod.GET)
	@ResponseBody
	public String pushsourceid(HttpServletRequest httpRequest){
		List<AppSourceInfo> listasi = appSourceInfoMapper.selectAll();
		for(AppSourceInfo asi : listasi){
			initWeixinService.pushweixinsource(asi.getSourceid(),String.valueOf(asi.getAppid()),String.valueOf(asi.getPriority()));
		}
		return "success";
	}
	
	/**
	 * 从队列中取公众号
	 */
	@RequestMapping(value ="/getsourceid.action", method = RequestMethod.GET)
	@ResponseBody
	public String getsourceid(HttpServletRequest httpRequest){
		String sourceidjson=initWeixinService.getsourceid();
		if(sourceidjson==null||StringUtil.isBlank(sourceidjson)){
			return "{\"msg\":\"emtyp\"}";
		}
		return sourceidjson;
	}
	
	
	/**
 	 *接收公众号列表链接
	 */
	@RequestMapping(value ="/pushlistpage.action", method = RequestMethod.POST)
	@ResponseBody
	public String pushweixinlist(HttpServletRequest httpRequest){
		String sourceid=httpRequest.getParameter("sourceid");
		String weixinURL=httpRequest.getParameter("url");
		String appid=httpRequest.getParameter("appid");
		String priority=httpRequest.getParameter("priority");
		
		if(!StringUtil.isBlank(weixinURL)&&weixinURL.startsWith("http://mp")){
			//添加抓去任务
			Spider spider = Constant.spiders.get(Constant.SPIDER_TYPE_WEIXIN);
			Request request = new Request(weixinURL.replace("&amp;", "&").replace("\n", "").trim());
			ConcurrentHashMap<String, Object> extras = new ConcurrentHashMap<String, Object>();
			extras.put("pageName", Constant.WEIXIN_LIST);
			extras.put("taskName", Constant.WEIXIN_LIST);
			//extras.put(Constant.REQUEST_HEADER_REFERER, "");
			request.setExtras(extras);
			request.setSourceid(sourceid);
			request.setAppid(Integer.valueOf(appid));
			request.setPriority(Integer.valueOf(priority) + 1);
			spider.addRequest(request);
		}else{
			initWeixinService.pushweixinsource(sourceid,appid,priority);
		}
		return "success";
	}
}
