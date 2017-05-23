package com.netease.engine.util;

import java.io.IOException;
import java.util.List;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * cookie预处理
 * 
 * @author handongming
 *
 */
public class HttpClientUtil {
	private RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(30000)
            .setConnectTimeout(30000)
            .setConnectionRequestTimeout(30000)
            .build();
	
	private static HttpClientUtil instance = new HttpClientUtil();
	public HttpClientUtil(){}
	
	public static HttpClientUtil getInstance(){
		return instance;
	}
	
	/**
	 * 发送 get请求
	 * @param httpUrl
	 */
	public String sendHttpGet(String httpUrl,String ua,String chartSet) throws Exception {
		HttpGet httpGet = new HttpGet(httpUrl);// 创建get请求
		try {
			return sendHttpGet(httpGet,ua,chartSet);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public String sendHttpGet(String httpUrl,String chartSet) throws Exception {
		HttpGet httpGet = new HttpGet(httpUrl);// 创建get请求
		try {
			return sendHttpGet(httpGet,null,chartSet);
		} catch (Exception e) {
			throw e;
		}
	}
	
	
	/**
	 * 发送Get请求
	 * @param httpPost
	 * @return
	 */
	private String sendHttpGet(HttpGet httpGet,String ua,String chartSet) throws Exception{
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		String result = "";
		try {
			BasicCookieStore bcs=new BasicCookieStore();
			httpClient = HttpClientBuilder.create().setDefaultCookieStore(bcs).setRetryHandler(new DefaultHttpRequestRetryHandler(5,true)).build(); //默认重试3次
			httpGet.setConfig(requestConfig);
			if(ua!=null){
				httpGet.setHeader("User-Agent", ua);
			}
			
			Thread.sleep(100);
			
			response = httpClient.execute(httpGet);
			
			List<Cookie> list = bcs.getCookies();
	        if(null==list||list.isEmpty()){
	            return null;
	        }
	        
	        for(Cookie cookie : list){
	        	result += cookie.getName() + "=" + cookie.getValue() + "; ";
	        }
	        
	        return result;
		} catch (Exception e) {
			e.printStackTrace();
			httpGet.abort();  
			throw e;
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		System.out.println(HttpClientUtil.getInstance().sendHttpGet("http://localhost:8079/getTask/2", "utf-8"));
	}
	
}
