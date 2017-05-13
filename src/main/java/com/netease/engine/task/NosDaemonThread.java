package com.netease.engine.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.netease.engine.util.Constant;

/**
 * nos上传后台线程
 * 
 * @author handongming
 *
 */
@Component
public class NosDaemonThread{
	
	@Autowired
	private NosUploadTask nosUploadTask;
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	
	private ThreadPoolExecutor threadPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(5);  
	
	public void run(){
		while(true){
			Jedis jedis = pool.getResource();
			long size = jedis.llen(Constant.NOSUPLOADKEY.getBytes());
			if (size == 0) {
				try {
					Thread.sleep(5*60*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			int activeCount=threadPool.getActiveCount();
			
			if(activeCount < threadPool.getMaximumPoolSize()){
				threadPool.execute(nosUploadTask);
			}
			
		}
	}
	
}
