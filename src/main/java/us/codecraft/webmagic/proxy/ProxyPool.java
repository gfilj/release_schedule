package us.codecraft.webmagic.proxy;

import org.apache.http.HttpHost;

/**
 * 代理池
 * 
 * @author handongming
 *
 */
public interface ProxyPool {
	public void returnProxy(HttpHost host, int statusCode);

	public Proxy getProxy();

	public boolean isEnable();
}
