package com.netease.engine.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.netease.engine.util.RedisUtil;
import com.netease.engine.util.SerializeUtil;
import com.netease.engine.util.StringUtil;

import java.util.Map.Entry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * redis工具类
 * 
 * @author handongming
 *
 */
@Service("redisService")
public class RedisService {
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	

	private Jedis getJedis() {
		return pool.getResource();
	}


	private void closeJedis(Jedis jedis) {
		pool.returnResourceObject(jedis);
	}
	
	/**
	 * 如果不存在，则 SET
	 * @param key
	 * @param value
	 * @param seconds
	 * @return
	 */
	public Long setnx(String key, String value, int seconds){
		Jedis jedis = null;
		Long res = null;
		try {
			jedis = getJedis();
			res = jedis.setnx(key, value);
			if (seconds > 0) {
				jedis.expire(key, seconds);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return res;
	}
	
	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public  String getset(String key, String value, int seconds) {
		String oldValue = "";
		Jedis jedis = null;
		try {
			jedis = getJedis();
			oldValue = jedis.getSet(key, value);
			if (seconds > 0) {
				jedis.expire(key, seconds);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 获取数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return oldValue;
	}
	
	
	/**
	 * 存储单个字符串
	 * 
	 * @param key
	 * @param value
	 * @param seconds
	 * @return
	 */
	public void set(String key, String value, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.set(key, value);
			if (seconds > 0) {
				jedis.expire(key, seconds);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
	}

	/**
	 * 将值存储到set中
	 * 
	 * @param second
	 * @param key
	 * @param value
	 */
	public void sadd(int second, String key, String... value) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.sadd(key, value);
			if (second > 0) {
				jedis.expire(key, second);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 将值存储到set中
	 * 
	 * @param second
	 * @param key
	 * @param value
	 */
	public Set<String> smembers(String key) {
		Jedis jedis = null;
		Set<String> set = null;
		try {
			jedis = getJedis();
			set = jedis.smembers(key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return set;
	}
	
	/**
	 * 将值存储到set中 (不过期)
	 * 
	 * @param key
	 * @param value
	 */
	public void sadd(String key, String... value) {
		sadd(-1, key, value);
	}

	/**
	 * 判断是否在集合中
	 */
	public Long isSadd(String key,String value){
		Jedis jedis = null;
		try {
			jedis = getJedis();
			return jedis.sadd(key, value);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			closeJedis(jedis);
		}
		return 0l;
	}
	/**
	 * 判断是否在集合中,不在集合中则添加,并设置过期时间
	 */
	public Long isSaddTime(String key,String value,int second){
		Jedis jedis = null;
		try {
			jedis = getJedis();
			return this.saddL(second, key, value);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			closeJedis(jedis);
		}
		return 0l;
	}
	/**
	 * 删除集合中值为value的元素
	 * @param setName 集合名
	 * @param value
	 * @return
	 */
	public Long srem(String setName, String value){
		Jedis jedis = null;
		try {
			jedis = getJedis();
			return this.srem(setName, value);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			closeJedis(jedis);
		}
		return 0l;
	}
	/**
	 * 将值存储到set中
	 * 
	 * @param second
	 * @param key
	 * @param value
	 */
	public Long saddL(int second, String key, String... value) {
		Long lg = 0l;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			lg = jedis.sadd(key, value);
			if (second > 0) {
				jedis.expire(key, second);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return lg;
	}
	/**
	 * 存储单个字符串 （不过期）
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, String value) {
		set(key, value, -1);
	}

	/**
	 * 存储单个对象
	 * 
	 * @param key
	 * @param obj
	 * @param seconds
	 */
	public void setObject(String key, Object obj, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.set(key.getBytes("utf-8"), SerializeUtil.serialize(obj));
			if (seconds > 0) {
				jedis.expire(key, seconds);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
	}	
	
	/**
	 * 存储单个对象（不超时）
	 * 
	 * @param key
	 * @param obj
	 */
	public  void setObject(String key, Object obj) {
		setObject(key, obj, -1);
	}

	/**
	 * 获取对象
	 * 
	 * @param key
	 */
	public  Object getObject(String key) {
		Object obj = null;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			byte[] value = jedis.get(key.getBytes("utf-8"));
			if (value != null && value.length > 0) {
				obj = SerializeUtil.deserialize(value);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 获取数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return obj;
	}

	/**
	 * 根据key取值
	 * 
	 * @param key
	 * @return
	 */
	public  String get(String key) {
		String value = "";
		Jedis jedis = null;
		try {
			jedis = getJedis();
			value = jedis.get(key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 获取数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return value;
	}
	
	/**
	 * 根据多个key 获取多个value
	 * @param keys
	 * @return
	 */
	public List<String> mget(String... keys)
	{
		List<String> value = null;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			value = jedis.mget(keys);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 获取数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return value;		
	}
	
	public String mset(String... keysvalues)
	{
		String value = null;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			value = jedis.mset(keysvalues);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 获取数据错误!", e);
		} finally {
			closeJedis(jedis);
		}		
		return value;
	}

	/**
	 * 删除值
	 * 
	 * @param key
	 * @return
	 */
	public  long delete(String... keys) {
		long value = -1;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			value = jedis.del(keys);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 删除数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return value;
	}


	/**
	 * 设置hash中的单个值
	 * 
	 * @param hkey
	 * @param field
	 * @param value
	 * @param second
	 * @return
	 */
	public  long hset(String hkey, String field, String value) {
		long result = -1;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			result = jedis.hset(hkey, field, value);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return result;
	}

	/**
	 * 设置hash值(不过期)
	 * 
	 * @param hkey
	 * @param map
	 */
	public  void hsetAll(String hkey, Map<String, String> map) {
		hsetAll(hkey, map, -1);
	}

	/**
	 * 设置hash值(不过期)
	 * 
	 * @param hkey
	 * @param map
	 */
	public  void hsetAll(String hkey, Map<String, String> map, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.hmset(hkey, map);
			if (seconds > 0) {
				jedis.expire(hkey, seconds);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
	}

	/**
	 * 从hash中获取单个数据
	 * 
	 * @param hkey
	 * @param filed
	 * @return
	 */
	public  String hget(String hkey, String filed) {
		String result = "";
		Jedis jedis = null;
		try {
			jedis = getJedis();
			result = jedis.hget(hkey, filed);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 获取数据错误!", e);
		} finally {
			closeJedis(jedis);
		}

		return result;
	}
	
	/**
	 * 从hash中获取单个数据
	 * 
	 * @param hkey
	 * @param filed
	 * @return
	 */
	public  byte[] hget(byte[] hkey, byte[] filed) {
		byte[] result = null;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			result = jedis.hget(hkey, filed);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 获取数据错误!", e);
		} finally {
			closeJedis(jedis);
		}

		return result;
	}
		
	

	/**
	 * 从hash中获取所有值
	 * 
	 * @param hkey
	 * @return
	 */
	public  Map<String, String> hgetAll(String hkey) {
		Map<String, String> result = null;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			result = jedis.hgetAll(hkey);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 获取数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return result;
	}

	/**
	 * 从hash中删除某个值
	 * 
	 * @param hkey
	 * @param field
	 * @return
	 */
	public  long hdel(String hkey, String... fields) {
		long result = -1;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			result = jedis.hdel(hkey, fields);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 删除数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return result;
	}

	/**
	 * 设置过期时间
	 * 
	 * @param key
	 * @param seconds
	 */
	public  void setExpires(String key, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			if (seconds > 0) {
				jedis.expire(key, seconds);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 设置过期时间错误!", e);
		} finally {
			closeJedis(jedis);
		}

	}

	/**
	 * 判断是否存在
	 * 
	 * @param key
	 * @return
	 */
	public  boolean exists(String key) {
		Jedis jedis = null;
		boolean result = false;
		try {
			jedis = getJedis();
			result = jedis.exists(key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return result;
	}

	/**
	 * 模糊匹配所有符合条件的key
	 * 
	 * @param keys
	 * @return
	 */
	public  String[] getKeys(String key) {
		Jedis jedis = null;
		Set<String> set = null;
		String[] result = null;
		try {
			jedis = getJedis();
			set = jedis.keys(key);
			if (set != null && set.size() > 0) {
				result = set.toArray(new String[] {});
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 错误!", e);
		} finally {
			closeJedis(jedis);
		}
		return result;
	}

	/**
	 * 清空redis缓存
	 * 
	 * @param city
	 * @param key
	 */

	public void cleanRedis(String prefix, String key) {
		if (StringUtil.isEmpty(key)) {
			key = "*";
		} else {
			key = key + RedisUtil.separator + "*";
		}
		String[] keys = this.getKeys(prefix + RedisUtil.separator
				+ key);
		if (keys != null && keys.length > 0) {
			this.delete(keys);
		}
	}

	/**
	 * 清空当前数据库缓存
	 */
	public void cleanDatebase() {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.flushDB();
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("执行cleanDatebase  方法错误!", e);
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 有序集合存储
	 * 
	 * @param key
	 * @param map
	 * @param seconds
	 */
	public void zadd(String key, Map<String, Double> map, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedis();

			jedis.zadd(key, map);

			if (seconds > 0) {
				jedis.expire(key, seconds);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis zadd error!", e);
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 有序集合存储
	 * 
	 * @param key
	 * @param map
	 * @param seconds
	 */
	public void zadd(byte[] key, Map<byte[], Double> map, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedis();

			jedis.zadd(key, map);

			if (seconds > 0) {
				jedis.expire(key, seconds);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis zadd error!", e);
		} finally {
			closeJedis(jedis);
		}
	}	
	
	
	
	/**
	 * 缓存默认时间有序集合存储
	 * 
	 * @param key
	 * @param map
	 */
	public void zadd(String key, Map<String, Double> map) {
		zadd(key, map, -1);
	}
	
	/**
	 * 缓存默认时间有序集合存储
	 * 
	 * @param key
	 * @param map
	 */
	public void zadd(byte[] key, Map<byte[], Double> map) {
		zadd(key, map, -1);
	}	
	
	
	public void zincrby(String key,String member,double increment,int seconds)
	{
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.zincrby(key, increment, member);
			if (seconds > 0) {
				jedis.expire(key, seconds);
			}			
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis zincrby error!", e);
		} finally {
			closeJedis(jedis);
		}
	}
	
	public void zincrby(String key,String member,double increment)
	{
		zincrby(key,member,increment,-1);
	}
	
	/**
	 * 使用管道方式更新
	 * @param key
	 * @param map
	 */
	public void zaddByPipelined(String key, Map<Double, String> map) {
		// 获取redis
		Jedis jedis = getJedis();
		Pipeline pipeline = null;
		try {
			 pipeline = jedis.pipelined();

			for (Entry<Double, String> entry : map.entrySet()) {

				pipeline.zadd(key, entry.getKey(), entry.getValue());
			}
			// 管道获取返回值
			pipeline.sync();

		} catch (Exception e) {
			if (pipeline != null) {
				pipeline.discard();
			}
			e.printStackTrace();
			throw new RuntimeException("redis zaddByPipelined error!", e);

		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 使用管道方式序列化更新
	 * 
	 * @param key
	 * @param map
	 */
	public void zaddObjectByPipelined(Jedis jedis, String key, Map<Double, Object> map) {
		Pipeline pipeline = null;
		try {
			pipeline = jedis.pipelined();

			for (Entry<Double, Object> entry : map.entrySet()) {

				pipeline.zadd(key.getBytes("utf-8"), entry.getKey(),
						SerializeUtil.serialize(entry.getValue()));
			}
			// 管道获取返回值
			pipeline.sync();

		} catch (Exception e) {
			if (pipeline != null) {
				pipeline.discard();
			}
			e.printStackTrace();
			throw new RuntimeException("redis zaddObjectByPipelined error!", e);

		} finally {
			closeJedis(jedis);
		}
	}
	
	
	/**
	 * 使用管道方式序列化更新
	 * 
	 * @param key
	 * @param map
	 */
	public void zaddObjectByPipelined(String key, Map<Double, Object> map) {
		// 获取redis
		Jedis jedis = getJedis();
		zaddObjectByPipelined(jedis, key, map);
	}
	
	/**
	 * 使用管道方式序列化更新
	 * 
	 * @param key
	 * @param map
	 */
	public void zaddStringByPipelined(String key, Map<Double, String> map) {
		// 获取redis
		Jedis jedis = getJedis();
		Pipeline pipeline = null;
		try {
			pipeline = jedis.pipelined();
			
			for (Entry<Double, String> entry : map.entrySet()) {
				pipeline.zadd(key, entry.getKey(), entry.getValue());
			}
			// 管道获取返回值
			pipeline.sync();
			
		} catch (Exception e) {
			if (pipeline != null) {
				pipeline.discard();
			}
			e.printStackTrace();
			throw new RuntimeException("redis zaddStringByPipelined error!", e);
			
		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 使用管道方式更新
	 * @param key 前缀
	 * @param keyField map中包含的主键字段
	 * @param list 需要更新的list
	 */
	public void hsetAllByPipelined(String prefixKey, String keyField, List<Map<String, String>> list) {
		// 获取redis
		Jedis jedis = getJedis();
		Pipeline pipeline = null;
		try {
			pipeline = jedis.pipelined();

			for (Map<String, String> map : list) {
				String hsetKey = prefixKey + RedisUtil.separator + map.get(keyField);
				pipeline.hmset(hsetKey, map);
			}
			// 管道获取返回值
			pipeline.sync();

		} catch (Exception e) {
			if (pipeline != null) {
				pipeline.discard();
			}
			e.printStackTrace();
			throw new RuntimeException("redis zaddByPipelined error!", e);

		} finally {
			closeJedis(jedis);
		}
	}
	
	/**
	 * 向队列中插入数据
	 * @param key
	 * @param value
	 * @return
	 */
	public long lpush(String key, String value){
		long result = 0;
		Jedis jedis = null;
		try {
			jedis = getJedis();
			result = jedis.lpush(key, value);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
		
		return result;
	}

	public Object lIndex(String key, long index){
		
		Jedis jedis = null;
		try {
			jedis = getJedis();
			byte[] objectbyte = jedis.lindex(key.getBytes("utf-8"), index);
			return SerializeUtil.deserialize(objectbyte);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
	}
	
	public long lLength(String key){
		Jedis jedis = null;
		try {
			jedis = getJedis();
			return jedis.llen(key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
	}
	
	public List<String> lRange(String key, int start, int end){
		Jedis jedis = null;
		try {
			jedis = getJedis();
			return jedis.lrange(key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
	}
	
	public void clearKey(String key){
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.del(key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis 存储数据错误!", e);
		} finally {
			closeJedis(jedis);
		}
	}
	
	public long rpush(String key,String... values)
	{
		Jedis jedis = null;
		long result = -1;
		try {
			jedis = getJedis();
			result = jedis.rpush(key, values);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis rpush 错误!", e);
		} finally {
			closeJedis(jedis);
		}	
		return result;
	}
	
	
	public String lpop(String key)
	{
		Jedis jedis = null;
		String result = "";
		try {
			jedis = getJedis();
			result = jedis.lpop(key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis lpop 错误!", e);
		} finally {
			closeJedis(jedis);
		}	
		return result;		
	}
	
	
	public long llen(String key)
	{
		Jedis jedis = null;
		long result = -1;
		try {
			jedis = getJedis();
			result = jedis.llen(key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis llen 错误!", e);
		} finally {
			closeJedis(jedis);
		}	
		return result;				
	}
	
	
	public long scard(String key)
	{
		Jedis jedis = null;
		long result = -1;
		try {
			jedis = getJedis();
			result = jedis.scard(key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JedisConnectionException("redis scard 错误!", e);
		} finally {
			closeJedis(jedis);
		}	
		return result;				
	}
}
