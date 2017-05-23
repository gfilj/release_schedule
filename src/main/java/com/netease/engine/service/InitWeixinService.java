package com.netease.engine.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.alibaba.fastjson.JSON;
import com.netease.engine.mapper.AppSourceInfoMapper;
import com.netease.engine.model.AppSourceInfo;
import com.netease.engine.proxyip.ProxyIpClient;
import com.netease.engine.util.Constant;
import com.netease.engine.vo.ProxyIpInfo;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

/**
 * 加载微信公众号
 * 
 * @author handongming
 *
 */
@Service("initWeixinService")
public class InitWeixinService {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	private static String FIRURL = "http://weixin.sogou.com/weixin?type=1&query=";
	
	@Autowired
	private AppSourceInfoMapper appSourceInfoMapper;
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	
	@Autowired
	private ProxyIpClient proxyIpClient;	
	
	public void loadWeixin(){
		try{
			Spider spider = Constant.spiders.get(Constant.SPIDER_TYPE_WEIXIN);
			List<AppSourceInfo> postInfoList = appSourceInfoMapper.selectAll();
			for(AppSourceInfo postInfo : postInfoList){
				spider.addRequest(parseRequest(postInfo));
			}
		}catch(Exception e){
			log.error("初始化加载微信号失败 ,",e);
		}
	}
	
	public Request parseRequest(AppSourceInfo postInfo){
		String url = FIRURL + postInfo.getSourceid() + "&ie=utf8&_sug_=n&_sug_type_=";
		Request request = new Request(url);
		Map<String, Object> extras = new ConcurrentHashMap<String, Object>();
		extras.put("pageName", Constant.WEIXIN_SEARCH);
		extras.put("taskName", Constant.WEIXIN_SEARCH);
		extras.put("needDeduplication", "false");
		extras.put(Constant.REQUEST_HEADER_REFERER, "http://weixin.sogou.com/");
		request.setExtras(extras);
		request.setSourceid(postInfo.getSourceid());
		request.setPriority(postInfo.getPriority());
		
		return request;
	}
	
	//向公众号队列添加内容
	public void pushweixinsource(String sourceid,String appid,String priority){
		Jedis jedis = null;
		try {	
			jedis = pool.getResource();
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("sourceid",sourceid);
			map.put("appid", appid);
			map.put("priority", priority);
			//获取代理ip
			/*ProxyIpInfo proxyIpInfo = proxyIpClient.getProxyIpInfo("sogou");
			while(proxyIpInfo == null) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				proxyIpInfo = proxyIpClient.getProxyIpInfo("sogou");
			}
			if(StringUtils.isNotBlank(proxyIpInfo.getUsername()) && StringUtils.isNotBlank(proxyIpInfo.getPassword())){
				map.put("username", proxyIpInfo.getUsername());
				map.put("password", proxyIpInfo.getPassword());
			}
			map.put("proxyip", proxyIpInfo.getIp());
			map.put("port", String.valueOf(proxyIpInfo.getPort()));*/
			
			jedis.lpush(Constant.WEIXIN_SOURCE, JSON.toJSONString(map));
		} finally {
			pool.returnResource(jedis);
		}
	}
	
	//获取sourcid
	public String getsourceid(){
		String json = null;
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			json = jedis.rpop(Constant.WEIXIN_SOURCE);
		} finally {
			pool.returnResource(jedis);
		}
		return json;
	}
	
}
