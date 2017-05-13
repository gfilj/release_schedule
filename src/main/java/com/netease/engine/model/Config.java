package com.netease.engine.model;

/**
 * 配置信息
 * 
 * @author handongming
 *
 */
public class Config {

	private String cycleRetry;
	
	private String delayTime;
	
	protected String accessKey;
	
    protected String secretKey;
    
    protected String bucketName;
	

	public String getCycleRetry() {
		return cycleRetry;
	}

	public void setCycleRetry(String cycleRetry) {
		this.cycleRetry = cycleRetry;
	}

	public String getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(String delayTime) {
		this.delayTime = delayTime;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	
}
