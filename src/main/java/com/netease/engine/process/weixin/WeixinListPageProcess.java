package com.netease.engine.process.weixin;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.engine.service.RedisService;
import com.netease.engine.util.Constant;
import com.netease.engine.util.DateUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

/**
 * 公众号列表页
 * 
 * @author handongming
 *
 */
@Component("weixinListPageProcess")
public class WeixinListPageProcess extends AbstractProcess {
	private static final String HTTPHEAD = "http://mp.weixin.qq.com";
	private static final String CONTENTLIST = "var\\s+msgList\\s+=\\s+\\{(.*?)\\};";
	
	@Autowired
	private RedisService redisService;

	@Override
	public void process(Page page) {
		try {
			page.getResultItems().setSkip(true);
			String contList = page.getHtml().regex(CONTENTLIST).toString();
			if (contList != null && !"".equals(contList)) {
				contList= "{" + contList + "}";
				JSONObject jsonObject = JSON.parseObject(contList);
				JSONArray list = jsonObject.getJSONArray("list");
				if(list != null && !list.isEmpty()){
					JSONObject obj = list.getJSONObject(0);
					JSONObject cmi = obj.getJSONObject("comm_msg_info");
					String today = DateUtil.formatTime(cmi.getString("datetime"),"yyyy-MM-dd");
					String nowDay = DateUtil.formatDate(new Date(), "yyyy-MM-dd");
					if(nowDay.equals(today)){
						JSONObject amei = obj.getJSONObject("app_msg_ext_info");
						String contentUrl = amei.get("content_url").toString();
						String title = amei.get("title").toString();
						page.addTargetRequest(getRequest(contentUrl,title, page));
						JSONArray jsonArray = amei.getJSONArray("multi_app_msg_item_list");
						if (jsonArray != null && !jsonArray.isEmpty()) {
							for (int i = 0; i < jsonArray.size(); i++) {
								JSONObject subJson = jsonArray.getJSONObject(i);
								String subUrl = subJson.getString("content_url");
								String subTitle = subJson.get("title").toString();
								page.addTargetRequest(getRequest(subUrl, subTitle, page));
							} 
						}
					}
				}
				
				
				/*
				 * 取全列表
				 * for (int j = 0; j < list.size(); j++) {
					JSONObject obj = list.getJSONObject(j);
					JSONObject amei = obj.getJSONObject("app_msg_ext_info");
					String contentUrl = amei.get("content_url").toString();
					String title = amei.get("title").toString();
					page.addTargetRequest(getRequest(contentUrl,title, page));
					JSONArray jsonArray = amei.getJSONArray("multi_app_msg_item_list");
					if (jsonArray != null && !jsonArray.isEmpty()) {
						for (int i = 0; i < jsonArray.size(); i++) {
							JSONObject subJson = jsonArray.getJSONObject(i);
							String subUrl = subJson.getString("content_url");
							String subTitle = subJson.get("title").toString();
							page.addTargetRequest(getRequest(subUrl, subTitle, page));
						} 
					}
				}*/
			}
		} catch (Exception e) {
			String msg = "weixinListPageProcess process  解析失败    url="
					+ page.getRequest().getUrl() + "  ,页面内容  = "
					+ page.getRawText();
			log.error(msg, e);
		}
	}

	public Request getRequest(String url,String title, Page page) {
		Request request = new Request();
		if (url != null && !"".equals(url)) {
			String contentUrl = url.replace("amp;", "").replace("\\", "");
			if (!contentUrl.startsWith(HTTPHEAD)) {
				contentUrl = HTTPHEAD + contentUrl;
			}
			ConcurrentHashMap<String, Object> extras = new ConcurrentHashMap<String, Object>();
			extras.put("pageName", Constant.WEIXIN_CONTENT);
			extras.put("taskName", Constant.WEIXIN_CONTENT);
			extras.put(Constant.REQUEST_HEADER_REFERER, page.getUrl().toString());
			request.setExtras(extras);
			request.setWhether_proxy(false);
			request.setUrl(contentUrl.replace("&amp;", "&"));
			title = Matcher.quoteReplacement(title);
			request.setTitle(title.replace("&nbsp;", " "));
			request.setSourceid(page.getRequest().getSourceid());
			request.setAppid(page.getRequest().getAppid());
			request.setPriority(page.getRequest().getPriority() + 1);
			
		}
		return request;
	}
}
