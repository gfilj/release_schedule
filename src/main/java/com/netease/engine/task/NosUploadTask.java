package com.netease.engine.task;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import com.alibaba.fastjson.JSON;
import com.netease.cloud.services.nos.transfer.model.UploadResult;
import com.netease.engine.mapper.AppImageInfoMapper;
import com.netease.engine.mapper.AppRecordInfoMapper;
import com.netease.engine.model.AppRecordInfo;
import com.netease.engine.model.ContentInfo;
import com.netease.engine.service.nos.impl.NosServiceImpl;
import com.netease.engine.util.Constant;
import com.netease.engine.util.CrawlUtil;
import com.netease.engine.util.FileTransferUtil;
import com.netease.engine.util.MD5Util;
import com.netease.engine.util.SerializeUtil;
import com.netease.engine.util.StringUtil;
import com.netease.engine.vo.NosContent;

/**
 * 图片文件，内容上传
 * 
 * @author handongming
 *
 */

public class NosUploadTask implements Runnable{
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private AppRecordInfoMapper appRecordInfoMapper;
	
	@Autowired
	private AppImageInfoMapper appImageInfoMapper;

	@Autowired
	private NosServiceImpl nosServiceImpl;
	
	@Autowired
	@Qualifier("jedisPool")
	private JedisPool pool;	
	
	public NosUploadTask(){
	}
	
	private int picDownloadLoopCount;
	
	public int getPicDownloadLoopCount() {
		return picDownloadLoopCount;
	}

	public void setPicDownloadLoopCount(int picDownloadLoopCount) {
		this.picDownloadLoopCount = picDownloadLoopCount;
	}

	@Override
	public void run() {
    	Jedis jedis = pool.getResource();
		try {
			NosContent nosContent = null;
            String key = null;
            
			byte[] contentbyte = jedis.rpop(Constant.NOSUPLOADKEY.getBytes());
			if (contentbyte == null) {
				return;
            } else {
            	nosServiceImpl.init();
            	nosContent = (NosContent)SerializeUtil.deserialize(contentbyte);
            	ContentInfo info = nosContent.getInfo();
            	String content = info.getContent();
            	String modifykey = null;
            	HashMap<String, String> tagUrl = nosContent.getTagURL();
            	Set<String> keySet = tagUrl.keySet(); 
            	for(String url : keySet){
            		String tag = tagUrl.get(url);
            		String suffix = CrawlUtil.getSuffixFromUri(url);
            		byte[] bytes = null;
            		for(int i=0; i<picDownloadLoopCount; i++){//循环下载
            			try {
    						bytes = FileTransferUtil.getByteForURL(url);
    						if(bytes!=null&&bytes.length>0){
    							break;
    						}
    					} catch (Exception e) {
    						logger.error("getByteForURL error {}", e);
    						continue;
    					}
            		}
                    String name = CrawlUtil.makeResourceKey(url);
                    key = nosServiceImpl.generatePublicUrl(name);
                    nosServiceImpl.putObject(name, new ByteArrayInputStream(bytes), nosServiceImpl.updateContentType(name, null, null, suffix));
            		content = content.replace(tag, key);
            	}
            	
            	info.setContent(content);
            	String url = info.getUrl();
    			UploadResult uploadResult = nosServiceImpl.putText(nosServiceImpl.key(), JSON.toJSONString(info));
    			String storeid = uploadResult.getKey();
    			AppRecordInfo appRecordInfo = new AppRecordInfo();
    			appRecordInfo.setAppid(nosContent.getAppId());
    			modifykey = MD5Util.calcMD5(nosContent.getSourceId() + info.getTitle());
    			appRecordInfo.setModifykey(modifykey);
    			appRecordInfo.setTitle(StringUtil.filterEmoji(info.getTitle()));
    			appRecordInfo.setSourceid(nosContent.getSourceId());
    			appRecordInfo.setStatus(0);
    			appRecordInfo.setStoreid(storeid);
    			appRecordInfo.setType(1);
    			appRecordInfo.setUrl(url);
    			appRecordInfo.setCreate_time(new Date());
    			appRecordInfo.setUpdate_time(new Date());
    			appRecordInfoMapper.save(appRecordInfo);
            }
		} catch(Exception e){
			logger.error("nos upload content error {}", e);
		} finally {
			pool.returnResource(jedis);
		}
    }

}
