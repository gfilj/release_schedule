package com.netease.engine.proxyip;

import com.alibaba.fastjson.JSONObject;
import com.netease.engine.util.Constant;
import com.netease.engine.vo.ProxyIpInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 代理池切换工具类
 * 
 * @author handongming
 *
 */
public class ProxyIpClient {
    
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	
    /**
     * 获取IP信息
     * @param key
     * @return
     */
    public ProxyIpInfo getPrivateProxyIpInfo(String key, boolean isPrivate) {
        String seqKey = isPrivate ? Constant.KEY_IP_POOL_CONSUMER_PRIVATE_PREFIX : Constant.KEY_IP_POOL_CONSUMER_PREFIX + key;
        String poolKey = isPrivate ? Constant.KEY_IP_POOL_PRIVATE_CHECKED : Constant.KEY_IP_POOL_CHECKED;
        Jedis jedis = null;
        try {
        	jedis = getJedis();
            Long i = jedis.incr(seqKey);
            String str = jedis.lindex(poolKey, i - 1);
            if (str == null && i > jedis.llen(poolKey)) {
                i = 1l;
                jedis.set(seqKey, "1");
                str = jedis.lindex(poolKey, i - 1);
            }
            if (StringUtils.isNotBlank(str)) {
                return JSONObject.parseObject(str, ProxyIpInfo.class);
            }
        } finally {
            closeJedis(jedis);
        }
        return null;
    }

    private Jedis getJedis() {
		return pool.getResource();
	}


	private void closeJedis(Jedis jedis) {
		pool.returnResourceObject(jedis);
	}
}
