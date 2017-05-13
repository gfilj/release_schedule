package com.netease.engine.util;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 时间处理
 * @author handongming
 *
 */
public class DateUtil {
	public static String TimeStamp2Date(String timestampString){ 
		Long timestamp = Long.parseLong(timestampString)*1000; 
		String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp)); 
		return date; 
	}
	
	/**
	 * 格式日期 yyyy-MM-dd HH:mm:ss
	 * @param date
	 * @return
	 */
	public static String formatDateForJson(Date date)
	{
		DateFormat format = new SimpleDateFormat("yyyyMMddHH");
		return format.format(date);
	}
	/**
	 * 格式日期 yyyy-MM-dd HH:mm:ss
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date)
	{
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}
	
	public static String formatDate(Date date,String format)
	{
		DateFormat datafFormat = new SimpleDateFormat(format);
		return datafFormat.format(date);
	}
	
	/**
	 * 格式时间 E dd MMM yyyy hh:mm:ss a
	 * @param time
	 * @return
	 */
	public static String getTime(Long time){
		Date d = new Date(time);
		Format simpleFormat = new SimpleDateFormat("E dd MMM yyyy hh:mm:ss a");
		String date = simpleFormat.format(d);
		return date;
	}
	
	/**
	 * 微博时间格式化
	 * @param date0
	 * @return
	 */
	public static String getFormulaDate(String date0) {
		String date;
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		DateFormat ddf = new SimpleDateFormat("MM月dd日 HH:mm");
		DateFormat dddf = new SimpleDateFormat("MM-dd HH:mm");
		
		String regex1 = "今天\\s\\d{2}:\\d{2}";
		String regex2 = "\\d{1,2}分钟";
		String regex3 = "\\d{2}月\\d{2}日\\s\\d{2}:\\d{2}";
		String regex4 = "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}";
		String regex5 = "\\d{2}-\\d{2}\\s\\d{2}:\\d{2}";
		
		
		Pattern p = Pattern.compile(regex1);
		Matcher m = p.matcher(date0);		
		if (m.find()) {
			date = df.format(new Date()).substring(0, 11) + m.group().substring(3);
			return date;
		} 
		
		p = Pattern.compile(regex2);
		m = p.matcher(date0);
		if (m.find()) {
			Date d = new Date();
			long t = d.getTime()-Integer.parseInt(m.group().split("分钟")[0])*60*1000;			
			date = df.format(new Date(t));
			return date;
		}
		
		p = Pattern.compile(regex3);
		m = p.matcher(date0);
		if (m.find()) {
			try {
				date = df.format(new Date()).substring(0,5) + dddf.format(ddf.parse(m.group()));
				return date;
			} catch (ParseException e) {
				e.printStackTrace();
				return "";
			}
		}
		p = Pattern.compile(regex4);
		m = p.matcher(date0);
		if (m.find()) {
			date = m.group();
			return date;
		}
		
		p=Pattern.compile(regex5);
		m=p.matcher(date0);
		if(m.find()){
			date = df.format(new Date()).substring(0,5)+date0;
			return date;
		}
		return "";
	}
	public static String formatTime(String createTime) {  
	    long msgCreateTime = Long.parseLong(createTime) * 1000L;  
	    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	    return format.format(new Date(msgCreateTime));  

	}  
	public static String formatTime(String createTime,String fromat) {  
	    long msgCreateTime = Long.parseLong(createTime) * 1000L;  
	    DateFormat format = new SimpleDateFormat(fromat);  
	    return format.format(new Date(msgCreateTime));  

	}
	public static boolean getDelayTime(Long delayTime){
		Long nowTime = System.currentTimeMillis();
		long lg = nowTime - delayTime;
		if(lg > 1000*60){
			return true;
		}
		return false;
	}

	public static Date getNextDay() {
//		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long current = System.currentTimeMillis();// 当前时间毫秒数
		long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(zero));
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return calendar.getTime();
//		Date date = calendar.getTime();
//		return format.format(date);
	}
	public static Date getNowDay() {
		long current=System.currentTimeMillis();//当前时间毫秒数
		long zero=current/(1000*3600*24)*(1000*3600*24)-TimeZone.getDefault().getRawOffset();//今天零点零分零秒的毫秒数
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(zero));
		return calendar.getTime();
	}
	public static int long2Int(long lg){
		return (int) (lg%1000000000);
	}
	//日期差
	public static int daysSub(Date fDate, Date oDate) {
	       Calendar aCalendar = Calendar.getInstance();
	       aCalendar.setTime(fDate);
	       int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
	       aCalendar.setTime(oDate);
	       int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);
	       return day2 - day1;
	
	}
}
