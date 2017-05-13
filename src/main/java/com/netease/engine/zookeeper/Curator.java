package com.netease.engine.zookeeper;
import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分布式锁
 * @author handongming
 *
 */
public class Curator {
	
    private static final Logger log = LoggerFactory.getLogger(Curator.class);

    private String zookeeper;

    private String lockPathPrefix="/data/iread/source/lock";

    private CuratorFramework client;
    
    public Curator(String zookeeper) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(zookeeper, retryPolicy);
        client.start();
    }
    
    public Curator() {}

    /**
     * 获取锁。返回不为null表示成功获取到锁，用完之后需要调用releaseLock方法释放
     * @param relativePath 锁的相对路径，Not start with '/'
     * @param waitSeconds 等待秒数
     * @return 未获取到锁返回null
     */
    public InterProcessMutex getLock(String relativePath, int waitSeconds) {
        InterProcessMutex lock = new InterProcessMutex(client, lockPathPrefix + relativePath);
        try {
            if (lock.acquire(waitSeconds, TimeUnit.SECONDS)) {
                return lock;
            }
        } catch (Exception e) {
            log.error("get lock error", e);
        }
        releaseLock(lock);
        return null;
    }

    /**
     * 释放锁
     */
    public void releaseLock(InterProcessMutex lock) {
        if (lock != null && lock.isAcquiredInThisProcess()) {
            try {
                lock.release();
            } catch (Exception e) {
                log.error("release lock error", e);
            }
        }
    }

	public String getZookeeper() {
		return zookeeper;
	}

	public void setZookeeper(String zookeeper) {
		this.zookeeper = zookeeper;
	}
    
}