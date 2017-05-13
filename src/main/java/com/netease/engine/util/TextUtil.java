package com.netease.engine.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 文本处理
 * @author handongming
 *
 */
public class TextUtil {
	
	/**
	 * 测试字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean isNull(String str) {
		return (null == str || 0 == str.length());
	}

	/**
	 * 补充html换行符
	 * @param str
	 * @return
	 */
	public static String subString(String str) {
		if (isNull(str)) {
			return "";
		} else {
			str = str.substring(0, 50);
			str = str.replaceAll("\r\n", "，");
			str = str.replace("<p>", "");
			str = str.replace("</p>", "");
			str = str.replace("　　", "");
			str += "...";
			return str;
		}
	}

	/**
	 * 从字符串转换成整形
	 * @param str
	 * @return
	 */
	public static int String2Int(String str) {
		if(str == null || "".equals(str))
			return 0;
		try {
			int value = Integer.valueOf(str);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	public static Long String2Long(String str) {
		if(str == null || "".equals(str))
			return 0l;
		try {
			Long value = Long.valueOf(str);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return 0l;
		}
	}
	/**
	 * 将中文进行编码成UTF8
	 * @param str
	 * @return
	 */
	public static String toUTF8(String str) {
		String str2 = "";
		if (!isNull(str)) {
			try {
				str2 = URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return str2;
	}
	/**
	 * 将UTF-8转码
	 * @param str
	 * @return
	 */
	public static String fromUTF8(String str) {
		String str2 = "";
		if (!isNull(str)) {
			try {
				str2 = URLDecoder.decode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return str2;
	}
	/**
	 * 通过url传的参数，将其字符串格式转化为utf8
	 * @param str
	 * @return
	 */
	public static String httpToUTF8(String str){
		String str2 = "";
		if (!isNull(str)) {
			try {
				str2 = new String(str.getBytes("ISO-8859-1"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return str2;		
	}
}
