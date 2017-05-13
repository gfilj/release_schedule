package com.netease.engine.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.processor.PageProcessor;

import com.alibaba.fastjson.JSONObject;
import com.netease.engine.process.weixin.AbstractProcess;
import com.netease.engine.vo.SpiderBean;

/**
 * 默认process
 * 
 * @author handongming
 *
 */
@Service("defaultProcess")
public class DefaultProcess extends AbstractProcess {

	@Autowired
	@Qualifier("spiderBean")
	private SpiderBean spiderBean;

	@Override
	public void process(Page page) {
		String pageName = (String) page.getRequest().getExtra("pageName");
		try {
			PageProcessor pageProcess = spiderBean.getProcessMap().get(pageName);
			if (pageProcess == null) {
				log.info("\n\n ***************unable to match to the appropriate process ***************\n request-->{} \n"+JSONObject.toJSONString(page.getRequest()));
				return;
			}
			pageProcess.process(page);
		} catch (Exception e) {
			String msg = "process  解析失败    url=" + page.getRequest().getUrl();
			log.error(msg, e);
			e.printStackTrace();
		}
	}

}