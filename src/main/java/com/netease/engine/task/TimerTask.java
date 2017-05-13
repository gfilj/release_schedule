package com.netease.engine.task;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.netease.engine.mapper.AppSourceInfoMapper;
import com.netease.engine.model.AppSourceInfo;
import com.netease.engine.service.InitWeixinService;

public class TimerTask {
	
	@Autowired	
	@Qualifier("initWeixinService")
	private InitWeixinService initWeixinService;
	
	@Autowired
	private AppSourceInfoMapper appSourceInfoMapper;
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * 微信推送
	 */
	public void grasp_weixin(){  
		log.info("重新抓取微信数据开始");
		initWeixinService.loadWeixin();
		log.info("重新抓取微信数据完毕");
	} 
	
	/**
	 * 向队列中添加公众号
	 */
	public void push_sourceid(){  
		log.info("向队列中添加公众号开始");
		List<AppSourceInfo> listasi = appSourceInfoMapper.selectAll();
		for(AppSourceInfo asi : listasi){
			initWeixinService.pushweixinsource(asi.getSourceid(),String.valueOf(asi.getAppid()),String.valueOf(asi.getPriority()));
		}
		log.info("向队列中添加公众号完毕");
	} 
	
}
