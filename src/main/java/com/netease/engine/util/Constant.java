package com.netease.engine.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import us.codecraft.webmagic.Spider;

public class Constant {

	public static Map<String,Spider> spiders = new HashMap<String,Spider>();
	
	public static final String SPIDER_TYPE_WEIXIN="spiderTypeWeixin";
	
	//-------------------评论redis前缀-----------------------------------------
	
	public static final Map<String,String> WEIXINMAP = new ConcurrentHashMap<String,String>();
	
	public static final String WEIXIN_SEARCH = "weixin_search";
	
	public static final String WEIXIN_LIST = "weixin_list";
	
	public static final String WEIXIN_CONTENT = "weixin_content";
	
	public static final String WEIXIN_COMMENT = "weixin_comment";
	
	public static final String WEIXIN_CONTENT_PIPELINE = "weixin_content_pi";
	
	public static final String DELAYTIME = "delayTime";
	
	public static final String REQUEST_HEADER_REFERER = "requestHeaderReferer";
	
	public static final String REQUEST_HEADER_COOKIES = "requestHeaderCookies";
	
    public static final String KEY_IP_POOL_CHECKED = "ip:pool:checked";
    
    public static final String KEY_IP_POOL_CONSUMER_PREFIX = "ip:pool:consumer:";
    
    public static final String KEY_IP_POOL_PRIVATE_CHECKED = "ip:pool:private:checked";
    
    public static final String KEY_IP_POOL_CONSUMER_PRIVATE_PREFIX = "ip:pool:consumer:private:";
	
	public static final String KEY_COOKIE_POOL = "queue_cookie";
	
  	public static final String NOSUPLOADKEY = "nos_upload_key";
  	
  	public static final String WEIXIN_SOURCE="weixin_source";
	
}
