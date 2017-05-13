package us.codecraft.webmagic.downloader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.selector.Html;

/**
 * 下载虚基类
 * @author handongming
 *
 */
public abstract class AbstractDownloader implements Downloader {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	//记录抓取失败的日志
	private Logger loggererror = LoggerFactory.getLogger("HttpClientDownloaderError");
	
    /**
     * A simple method to download a url.
     *
     * @param url url
     * @return html
     */
    public Html download(String url) {
        return download(url, null);
    }

    /**
     * A simple method to download a url.
     *
     * @param url url
     * @param charset charset
     * @return html
     */
    public Html download(String url, String charset) {
        Page page = download(new Request(url), Site.me().setCharset(charset).toTask());
        return (Html) page.getHtml();
    }

    //记录下载成功日志
    protected void onSuccess(Request request) {}

    //记录下载失败日志
    protected void onError(Request request,int cycleRetryTimes) {
    	Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);
        if (cycleTriedTimesObject != null) {
            int cycleTriedTimes = (Integer) cycleTriedTimesObject;
            if (cycleTriedTimes == cycleRetryTimes) {
            	String value = JSONObject.toJSONString(request);
            	loggererror.info(value);
            }
        }
    }

    protected Page addToCycleRetry(Request request, Site site) {
    	Page page = new Page();
        Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);
        if (cycleTriedTimesObject == null) {
            page.addTargetRequest(request.putExtra(Request.CYCLE_TRIED_TIMES, 1));
        } else {
            int cycleTriedTimes = (Integer) cycleTriedTimesObject;
            cycleTriedTimes++;
            if (cycleTriedTimes >= site.getCycleRetryTimes()) {
            	String value = JSONObject.toJSONString(request);
            	loggererror.info(value);
                return null;
            }
            page.addTargetRequest(request.putExtra(Request.CYCLE_TRIED_TIMES, cycleTriedTimes));
        }
        page.setNeedCycleRetry(true);
        return page;
        
    }
}
