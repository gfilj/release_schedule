package us.codecraft.webmagic.downloader;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.netease.engine.util.Constant;
import com.netease.proxyip.DataType;
import com.netease.proxyip.ProxyIpInfo;
import com.netease.proxyip.ProxyIpService;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.thrift.TException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.HttpConstant;
import us.codecraft.webmagic.utils.UrlUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.Set;

/**
 * http下载
 * 
 * @author handongming
 *
 */
@ThreadSafe
@Component
public class HttpClientDownloader extends AbstractDownloader {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();
	@Value("${contentSleepMax}")
	private int contentSleepMax;
	@Value("${contentSleepMin}")
    private int contentSleepMin;
	@Value("${listSleepMax}")
	private int listSleepMax;
	@Value("${listSleepMin}")
	private int listSleepMin;
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	
	@Autowired
	private ProxyIpService proxyIpService;
    
    private Jedis getJedis() {
		return pool.getResource();
	}


	private void closeJedis(Jedis jedis) {
		pool.returnResourceObject(jedis);
	}
	
    
    
    /**
     * 获取Cookie
     * @return
     */
    public String getCookie() {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            String value = jedis.lpop(Constant.KEY_COOKIE_POOL);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	closeJedis(jedis);
        }
        return null;
    }


    /**
     * 归还Cookie
     */
    public void giveBackCookie(String cookie) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.rpush(Constant.KEY_COOKIE_POOL, cookie);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeJedis(jedis);
        }
    }

	@Override
	public Page download(Request request, Task task) {
		Site site = null;
		String cookie = null;
		ProxyIpInfo proxyIpInfo = null;
		request.putExtra(Request.ERROR, "");
		if (task != null) {
			site = task.getSite();
		}
		Set<Integer> acceptStatCode;
		String charset = null;
		if (site != null) {
			acceptStatCode = site.getAcceptStatCode();
			charset = site.getCharset();
		} else {
			acceptStatCode = Sets.newHashSet(200);
		}
		CloseableHttpResponse httpResponse = null;
		CloseableHttpClient httpClient = null;
		RequestConfig requestConfig = null;
		HttpGet httpGet = new HttpGet(request.getUrl());
		int statusCode = 0;
		try {
			//针对不同的task休眠随机时间
			String taskName = (String) request.getExtra("taskName");
			if(Constant.WEIXIN_CONTENT.equals(taskName)){
		        Random random = new Random();
		        int s = random.nextInt(contentSleepMax)%(contentSleepMax-contentSleepMin+1) + contentSleepMin;
		        try {
					Thread.sleep(s*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else if(Constant.WEIXIN_SEARCH.equals(taskName)){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else if(Constant.WEIXIN_LIST.equals(taskName)){
		        Random random = new Random();
		        int s = random.nextInt(listSleepMax)%(listSleepMax-listSleepMin+1) + listSleepMin;
		        try {
					Thread.sleep(s*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//获取代理和cookie信息
			int size = site.getUserAgent().size();
		    String ua = site.getUserAgent().get((int)(Math.random())*size);
			try {
				proxyIpInfo = proxyIpService.getNext("sogou",DataType.NE_VPS1);
			} catch (TException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		    
			if(request.isWhether_proxy()){
				try {
					proxyIpInfo = proxyIpService.getNext("sogou",DataType.NE_VPS1);
				} catch (TException e1) {
					e1.printStackTrace();
				}
				while(proxyIpInfo == null) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						proxyIpInfo = proxyIpService.getNext("sogou",DataType.NE_VPS1);
					} catch (TException e) {
						e.printStackTrace();
					}
				}

				request.putExtra(Request.PROXY, proxyIpInfo);
				if(StringUtils.isNotBlank(proxyIpInfo.getUsername()) && StringUtils.isNotBlank(proxyIpInfo.getPassword())){
					String headerKey = "Proxy-Authorization";
		            String headerValue = "Basic " + Base64.encodeBase64String((proxyIpInfo.getUsername() +":" + proxyIpInfo.getPassword()).getBytes());
		            httpGet.setHeader(headerKey, headerValue);
				}
			
//				if(Constant.WEIXIN_SEARCH.equals(request.getExtra("taskName"))){
//					cookie = getCookie();
//					while(cookie==null) {
//						try {
//							Thread.sleep(5000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//						cookie = getCookie();
//					}
//				}
				
				request.putExtra("cookie", cookie);
				httpClient = HttpClientBuilder.create().setProxy(new HttpHost(proxyIpInfo.getIp(),proxyIpInfo.getPort())).setRetryHandler(new DefaultHttpRequestRetryHandler(1, true)).build();
				requestConfig = RequestConfig.custom()
	            .setSocketTimeout(site.getTimeOut())
	            .setConnectTimeout(site.getTimeOut())
	            .setConnectionRequestTimeout(site.getTimeOut())
	            .setProxy(new HttpHost(proxyIpInfo.getIp(),proxyIpInfo.getPort()))
	            .build();
			}else{
				request.putExtra(Request.PROXY, null);
				request.putExtra("cookie", null);
				httpClient = HttpClientBuilder.create().setRetryHandler(new DefaultHttpRequestRetryHandler(1, true)).build();
				requestConfig = RequestConfig.custom()
	            .setSocketTimeout(site.getTimeOut())
	            .setConnectTimeout(site.getTimeOut())
	            .setConnectionRequestTimeout(site.getTimeOut())
	            .build();
			}
			
			logger.info("\n\n ***************downloading page ***************\n url-->{} \n request-->{} \n", request.getUrl(),JSONObject.toJSONString(request));
			
			httpGet.setConfig(requestConfig);
			httpGet.setHeader("Cookie", cookie);
//			httpGet.setHeader("Referer", (String)request.getExtras().get(Constant.REQUEST_HEADER_REFERER));
			
			httpGet.setHeader("User-Agent", ua);
			httpResponse = httpClient.execute(httpGet);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			
			request.putExtra(Request.STATUS_CODE, statusCode);
			Page page = null;
			if (statusAccept(acceptStatCode, statusCode)) {
				//处理page页面
				page = handleResponse(request, charset, httpResponse, task);
				String is_error = page.getHtml().regex("(<span[^>]*\\s+class=['\"]s1['\"]>您的访问出错了</span>)").toString();
				if(is_error!=null){
					request.putExtra(Request.ERROR, "您的访问出错了");
					return null;
				}else{
					is_error = page.getHtml().regex("(<title>请输入验证码 </title>)").toString();
					if(is_error!=null){
						request.putExtra(Request.ERROR, "请输入验证码");
						return null;
					}
					is_error = page.getHtml().regex("(<script[^>]*\\s+src=['\"]static/js/antispider\\.min\\.js\\?v=2['\"]></script>)").toString();
					if(is_error!=null){
						request.putExtra(Request.ERROR, "失效cookie");
						return null;
					}
					if(cookie!=null&&!"".equals(cookie)){
						giveBackCookie(cookie);
					}
					if(request.getExtra("taskName").equals(Constant.WEIXIN_CONTENT)){
						request.setWhether_deposited(true);
					}
					onSuccess(request);
					return page;
				}
			} else {
				if(cookie!=null&&!"".equals(cookie)){
					giveBackCookie(cookie);
				}
				if(request.getExtra("taskName").equals(Constant.WEIXIN_CONTENT)){
					request.setWhether_deposited(true);
				}
				request.putExtra(Request.ERROR, "访问受限,状态码为"+statusCode);
				return null;
			}
		} catch (IOException e) {
			if(cookie!=null&&!"".equals(cookie)){
				giveBackCookie(cookie);
			}
			if(e instanceof ConnectException){
				request.putExtra(Request.ERROR, "建立代理连接失败");
				return null;
			}
			if (site.getCycleRetryTimes() > 0) {
				return addToCycleRetry(request, site);
			}
			return null;
		} finally {
			request.putExtra(Request.STATUS_CODE, statusCode);
			try {
				if(httpGet!=null){
					httpGet.abort();
				}
				if (httpResponse != null) {
					httpResponse.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				logger.info("close response fail", e);
			}
		}
	}
	
	
	@Override
	public void setThread(int thread) {
		httpClientGenerator.setPoolSize(thread);
	}

	public static boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
		return acceptStatCode.contains(statusCode);
	}

	protected RequestBuilder selectRequestMethod(Request request) {
		String method = request.getMethod();
		if (method == null || method.equalsIgnoreCase(HttpConstant.Method.GET)) {
			return RequestBuilder.get();
		} else if (method.equalsIgnoreCase(HttpConstant.Method.POST)) {
			RequestBuilder requestBuilder = RequestBuilder.post();
			NameValuePair[] nameValuePair = (NameValuePair[]) request.getExtra("nameValuePair");
			if (nameValuePair != null && nameValuePair.length > 0) {
				requestBuilder.addParameters(nameValuePair);
			}
			return requestBuilder;
		} else if (method.equalsIgnoreCase(HttpConstant.Method.HEAD)) {
			return RequestBuilder.head();
		} else if (method.equalsIgnoreCase(HttpConstant.Method.PUT)) {
			return RequestBuilder.put();
		} else if (method.equalsIgnoreCase(HttpConstant.Method.DELETE)) {
			return RequestBuilder.delete();
		} else if (method.equalsIgnoreCase(HttpConstant.Method.TRACE)) {
			return RequestBuilder.trace();
		}
		throw new IllegalArgumentException("Illegal HTTP Method " + method);
	}

	protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
		String content = getContent(charset, httpResponse);
		Page page = new Page();
		page.setRawText(content);
		page.setUrl(new PlainText(request.getUrl()));
		page.setRequest(request);
		page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
		return page;
	}

	protected String getContent(String charset, HttpResponse httpResponse)
			throws IOException {
		if (charset == null) {
			byte[] contentBytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
			String htmlCharset = getHtmlCharset(httpResponse, contentBytes);
			if (htmlCharset != null) {
				return new String(contentBytes, htmlCharset);
			} else {
				logger.info("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
				return new String(contentBytes);
			}
		} else {
			return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
		}
	}

	protected String getHtmlCharset(HttpResponse httpResponse, byte[] contentBytes) throws IOException {
		String charset;
		String value = httpResponse.getEntity().getContentType().getValue();
		charset = UrlUtils.getCharset(value);
		if (StringUtils.isNotBlank(charset)) {
			logger.debug("Auto get charset: {}", charset);
			return charset;
		}
		Charset defaultCharset = Charset.defaultCharset();
		String content = new String(contentBytes, defaultCharset.name());
		if (StringUtils.isNotEmpty(content)) {
			Document document = Jsoup.parse(content);
			Elements links = document.select("meta");
			for (Element link : links) {
				String metaContent = link.attr("content");
				String metaCharset = link.attr("charset");
				if (metaContent.indexOf("charset") != -1) {
					metaContent = metaContent.substring(metaContent.indexOf("charset"), metaContent.length());
					charset = metaContent.split("=")[1];
					break;
				}
				else if (StringUtils.isNotEmpty(metaCharset)) {
					charset = metaCharset;
					break;
				}
			}
		}
		logger.debug("Auto get charset: {}", charset);
		return charset;
	}
}
