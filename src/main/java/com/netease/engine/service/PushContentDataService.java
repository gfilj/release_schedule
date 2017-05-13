//package com.netease.engine.service;
//
//import java.io.File;
//import java.util.Date;
//import java.util.List;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import com.alibaba.fastjson.JSONObject;
//import com.netease.engine.constant.Constant;
//import com.netease.engine.mapper.ContentInfoMapper;
//import com.netease.engine.mapper.IndexInfoMapper;
//import com.netease.engine.model.ContentInfo;
//import com.netease.engine.util.DateUtil;
//
///**
// * 推送微信内容信息
// * @author handongming
// *
// */
//@Service("pushContentDataService")
//public class PushContentDataService {
//	
//	private final Log log = LogFactory.getLog(this.getClass());
//	
//	@Autowired
//	private ContentInfoMapper contentInfoMapper;
//	@Autowired
//	private IndexInfoMapper indexInfoMapper;
//	
//	public void push() {
//		List<ContentInfo> list = null;
//		try{
//			Long index = indexInfoMapper.selectIndex(Constant.WEIXIN_CONTENT_KEY);
//			if(index == null){
//				index = 0l;
//			}
//			list = contentInfoMapper.selectPushData(index,Constant.WEIXIN_CONTENT_STEP,1);			
//			StringBuilder sb = new StringBuilder();
//			
//			if(!list.isEmpty()){
//				Long stat = list.get(0).getId();
//				index = list.get(list.size()-1).getId();
//				indexInfoMapper.updateIndex(Constant.WEIXIN_CONTENT_KEY, index);
//				contentInfoMapper.updateContentStatusById(stat,index);
//				for(ContentInfo info : list){
//					sb.append(JSONObject.toJSON(info)+"\n");
//				}
//				//写文件
//				FileUtils.writeStringToFile(new File(Constant.WEIXIN_JSON_PATH
//						+ DateUtil.formatDateForJson(new Date()) +".content.json"),sb.toString(), Constant.DEFAULT_ENCODING);
//			}else{
//				//写文件
//				FileUtils.writeStringToFile(new File(Constant.WEIXIN_JSON_PATH
//						+ DateUtil.formatDateForJson(new Date()) +".content.json"),"", Constant.DEFAULT_ENCODING);
//			}
//			
//		}catch(Exception e){
//			String msg = "PushContentDataService推送信息保存失败"+JSONObject.toJSON(list);
//			e.printStackTrace();
//			log.error(msg,e);
//			throw new RuntimeException(msg,e);
//		}
//	}
//	
//}
