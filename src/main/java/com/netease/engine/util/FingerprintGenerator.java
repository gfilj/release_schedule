package com.netease.engine.util;

/**
 * 根据路径生成指纹
 * 
 * @author handongming
 *
 */
public class FingerprintGenerator {
	
	public static final String COLON_SLASH_SLASH = "://";


    public static long urlFingerprint(CharSequence uri) {
        String url = uri.toString();
        int index = url.indexOf(COLON_SLASH_SLASH);
        if (index > 0) {
            index = url.indexOf('/', index + COLON_SLASH_SLASH.length());
        }
        CharSequence hostPlusScheme = (index == -1)? url: url.subSequence(0, index);
        long tmp = FPGenerator.std24.fp(hostPlusScheme);
        return tmp | (FPGenerator.std40.fp(url) >>> 24);
    }
    
    public static long makeFingerprint(CharSequence str) {
    	return FPGenerator.std64.fp(str);
    }
}
