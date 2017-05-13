package com.netease.engine.pipeline.weixin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.alibaba.fastjson.JSONObject;
import com.netease.engine.mapper.AppRecordInfoMapper;
import com.netease.engine.model.ContentInfo;
import com.netease.engine.service.nos.impl.NosServiceImpl;
import com.netease.engine.util.Constant;
import com.netease.engine.util.CrawlUtil;
import com.netease.engine.util.SerializeUtil;
import com.netease.engine.vo.NosContent;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * 正文信息保存
 * 
 * @author handongming
 *
 */
@Component("weixinContentPipeline")
public class WeixinContentPipeline implements Pipeline {

	private final Log log = LogFactory.getLog(this.getClass());
	
	//记录抓取成功的日志 
	private Logger loggsuccess = LoggerFactory.getLogger("HttpClientDownloaderSuccess");
	
	@Autowired
	private AppRecordInfoMapper appRecordInfoMapper;

	@Autowired
	private NosServiceImpl nosServiceImpl;
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	
	@Override
	public void process(ResultItems resultItems, Task task) {
		Pattern datasrcPattern = Pattern.compile("<img[^>]*\\s+data-src\\s*=\\s*['\"]([^>]*?)['\"][^>]*/?>");
		Pattern srcPattern = Pattern.compile("<img[^>]*\\s+src\\s*=\\s*['\"]([^>]*?)['\"][^>]*/?>");
		String modifykey = null;
		try {
			if(resultItems.get("priority") == null)
				return;
			Jedis jedis = pool.getResource();
			try {
				ContentInfo info = (ContentInfo) resultItems.get("content_info");
				String content = info.getContent();
				
				Matcher datasrcMatcher=datasrcPattern.matcher(content);
				
				NosContent nosContent = new NosContent();
				while(datasrcMatcher.find()){
					String uri = datasrcMatcher.group(1);
					String tag = CrawlUtil.makeDataTag(uri);
					content=content.replace(datasrcMatcher.group(0), "<img src=\""+tag+"\" />");
					nosContent.putTagURL(uri, tag);
				}
				
				Matcher srcMatcher=srcPattern.matcher(content);
				while(srcMatcher.find()){
					String uri = srcMatcher.group(1);
					if(!uri.startsWith("#")){
						String tag = CrawlUtil.makeSrcTag(uri);
						content=content.replace(srcMatcher.group(0), "<img src=\""+tag+"\" />");
						nosContent.putTagURL(uri, tag);
					}
				}
				
				info.setContent(content);
				nosContent.setInfo(info);
				nosContent.setAppId(resultItems.getRequest().getAppid());
				nosContent.setSourceId(resultItems.getRequest().getSourceid());
				jedis.lpush(Constant.NOSUPLOADKEY.getBytes(), SerializeUtil.serialize(nosContent));
				
				//记录抓取成功的日志
				String value = JSONObject.toJSONString(resultItems.getRequest());
				loggsuccess.info(value);
			} finally {
				pool.returnResource(jedis);
			}
		} catch (Exception e) {
			log.error("save weixincontent error {}" + modifykey + "\t", e);
		}
	}

}
