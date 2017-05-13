package com.netease.engine.process.weixin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.netease.engine.vo.SpiderBean;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

@Service("abstractProcess")
public abstract class AbstractProcess implements PageProcessor {

	protected final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	@Qualifier("spiderBean")
	private SpiderBean spiderBean;

	@Override
	public Site getSite() {
		return spiderBean.getSite();
	}

}
