package com.netease.engine.process.weixin;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.netease.engine.util.Constant;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.Html;

/**
 * 微信搜索入口
 * 
 * @author handongming
 *
 */
@Component("weixinSearchPageProcess")
public class WeixinSearchPageProcess extends AbstractProcess {

	private static final String SEARCH_REGEX_PRE = "<a target=\"_blank\" uigs=\"main_toweixin_account_image_0\" href=\"([\\s\\S]*?)\">[\\s\\S]*?";
	private static final String SEARCH_REGEX_END = "</label>";
	
	@Override
	public void process(Page page) {
		try {
			page.getResultItems().setSkip(true);
			String sourceid = page.getRequest().getSourceid();
			Html html = page.getHtml();
			String page_list = html.regex(SEARCH_REGEX_PRE + sourceid +SEARCH_REGEX_END).toString();
			if(page_list != null){
				Request request = new Request(page_list.replace("&amp;", "&"));
				ConcurrentHashMap<String, Object> extras = new ConcurrentHashMap<String, Object>();
				extras.put("pageName", Constant.WEIXIN_LIST);
				extras.put("taskName", Constant.WEIXIN_LIST);
				extras.put(Constant.REQUEST_HEADER_REFERER, page.getUrl().toString());
				request.setExtras(extras);
				request.setSourceid(sourceid);
				request.setAppid(page.getRequest().getAppid());
				request.setPriority(page.getRequest().getPriority() + 1);
				
				page.addTargetRequest(request);
			}else{
				throw new RuntimeException("解析失败");
			}
		} catch (Exception e) {
			String msg = "weixinSearchPageProcess process  解析失败    url="
					+ page.getRequest().getUrl() + "  ,页面内容  = "
					+ page.getRawText();
			log.error(msg, e);
		}

	}
}
