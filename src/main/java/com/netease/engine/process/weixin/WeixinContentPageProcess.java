package com.netease.engine.process.weixin;

import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.netease.engine.model.ContentInfo;
import com.netease.engine.service.RedisService;
import us.codecraft.webmagic.Page;

/**
 * 微信正文
 * 
 * @author handongming
 *
 */
@Component("weixinContentPageProcess")
public class WeixinContentPageProcess extends AbstractProcess {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private RedisService redisService;

	@Override
	public void process(Page page) {
		try{
			String is_error = page.getHtml().regex("(<div[^>]*\\s+style=['\"]global_error_msg warn['\"]>*\\s+操作过于频繁，请稍后再试。*\\s+</div>)").toString();
			if(is_error!=null){
				page.addTargetRequest(page.getRequest());
				return;
			}
			page.getResultItems().setSkip(false);
			String title = page.getHtml().xpath("//title/text()").toString().replace("&nbsp;", " ").replaceAll("\u00A0", "");//文章标题
			title = Matcher.quoteReplacement(title);
			String post_name = page.getHtml().xpath("//a[@id='post-user']/text()").toString().replace("&nbsp;", " ").replaceAll("\u00A0", "");//公众号名字
			post_name = Matcher.quoteReplacement(post_name);
			String content = page.getHtml().xpath("//div[@id='js_content']").toString();//文章内容
			content = Matcher.quoteReplacement(content);
			String pub_time = page.getHtml().xpath("//em[@id='post-date']/text()").toString();//发布时间
			ContentInfo contentInfo = new ContentInfo();
			contentInfo.setTitle(title);
			contentInfo.setContent(content);
			contentInfo.setOrigin(post_name);
			contentInfo.setUrl(page.getUrl().toString());
			contentInfo.setTime(pub_time);
			page.putField("content_info", contentInfo);	
			page.putField("priority", page.getRequest().getPriority());
			page.putField("url", page.getRequest().getUrl());
		}catch(Exception e){
			String msg = "weixinContentPageProcess process  解析失败    url="
					+page.getRequest().getUrl() + "  ,页面内容  = " 
					+ page.getRawText();
			log.error(msg, e);
		}
	}
}
