package com.netease.engine.pipeline;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.netease.engine.vo.SpiderBean;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

@Component("defaultPipeline")
public class DefaultPipeline implements Pipeline{
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	@Qualifier("spiderBean")
	private SpiderBean spiderBean;	
	
	@Override
	public void process(ResultItems resultItems, Task task) {
		String pageName = (String) resultItems.getRequest().getExtra("pageName");
		try {
			Pipeline pipeline = spiderBean.getPipelineMap().get(pageName);
			if(pipeline==null) {
				log.info("\n\n ***************unable to match to the appropriate pipeline ***************\n request-->{} \n" + JSONObject.toJSONString(resultItems.getRequest()));
				return;
			}
			pipeline.process(resultItems, task);			
		}catch(Exception e)	{
			String msg = e.getMessage() + "   url is "+resultItems.getRequest().getUrl();
			e.printStackTrace();
			log.error(msg, e);
		}

	}

}
