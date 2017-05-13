package com.netease.engine.util;

import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 二进制文件上传
 * 
 * @author handongming
 *
 */
public class CrawlUtil {
	
    private static final Logger logger = LoggerFactory.getLogger(CrawlUtil.class);
    
    Pattern SUFFIX_PATTERN = Pattern.compile("", Pattern.CASE_INSENSITIVE);
    
    public static String makeDataTag(String url) {
        return "##spider_data#" + MD5Util.calcMD5(url) + "###";
    }
    
    public static String makeSrcTag(String url) {
        return "##spider_src#" + MD5Util.calcMD5(url) + "###";
    }
    
    /**
     * 获取uri的文件名后缀
     * @param url
     * @return
     */
    public static String getSuffixFromUri(String url) {
        if(StringUtils.isBlank(url)) {
            return null;
        }
        try {
            UURI uri = UURIFactory.getInstance(url);
            String name = StringUtils.trimToEmpty(uri.getEscapedName());
            int index = name.lastIndexOf('.');
            return index != -1 ? name.substring(index + 1).toLowerCase() : null;
        } catch (URIException e) {
            logger.error("url= " + url , e);
        }
        return null;
    }
    
    /**
     * 根据二进制资源的uri生成唯一标识
     * @param url
     * @return
     */
    public static String makeResourceKey(String url) {
        url = StringUtils.trimToNull(url);
        if(url == null) {
            return null;
        }
        UURI uri;
        try {
            uri = UURIFactory.getInstance(url);
            url = uri.toCustomString();
        } catch (URIException e) {
            logger.error("url= " + url , e);
        }      
        String suffix = CrawlUtil.getSuffixFromUri(url);
        String key = MD5Util.calcMD5(url);
        if(suffix != null) {
            key = key + "." + suffix;
        }
        return key;
    }
}
