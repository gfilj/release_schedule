package com.netease.engine.util;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;


/**
 * Html 工具类。过滤非法标签，纠正html语法。
 * 
 * @author Ben Liu
 */
public final class HtmlUtil {
    private static final Pattern FLASH_IMG_PATTERN = Pattern.compile("<img(.*)flashsrc=\"([^\"]*)\"(.*)/>");
    private static final Pattern FLASH_DIV_PATTERN = Pattern.compile("<div(.*)flashsrc=\"([^\"]*)\" style=\"width:([^\"]*)px; height:([^\"]*)px\"(.*)></div>");
    private static final Whitelist DEFAULT_WHITE_LIST;
    private static final Whitelist TBLOG_WHITE_LIST;
    private static final List<Pattern> FLASH_URL_WHITE_LIST;
    // 获取img标签正则
    private static final String IMGURL_REG = "<img.*src=(.*?)[^>]*?>";
    // 获取src路径的正则
    private static final String IMGSRC_REG = "http:\"?(.*?)(\"|>|\\s+)";
    static {
        DEFAULT_WHITE_LIST = new Whitelist().addTags("blockquote", "h1", "h2", "h3", "h4", "h5", "h6", "ol", "ul", "li", "p", "font", "span", "div", "dl", "dt", "dd", "em", "b", "i", "u", "small", "strong", "a", "img", "hr", "br", "table", "thead", "tbody", "tr", "td", "object", "embed", "param")
        // 链接
                .addAttributes("a", new String[] {"href", "target"}).addProtocols("a", "href", new String[] {"ftp", "http", "https", "mailto"})
                // 图片
                .addAttributes("img", new String[] {"src", "width", "height", "flashsrc", "type", "srcpostfloor", "size", "srcordernumber"}).addProtocols("img", "src", new String[] {"http", "https"}).addProtocols("img", "flashsrc", new String[] {"http", "https"})
                // 字体
                .addAttributes("font", new String[] {"color", "face", "style"}).addAttributes("strong", "style").addAttributes("p", new String[] {"align", "style"})
                // 横线
                .addAttributes("hr", new String[] {"size", "width"})
                // 斜体
                .addAttributes("i", "style")
                // 粗体
                .addAttributes("b", "style")
                // 下划线
                .addAttributes("u", "style")
                // 引用回复
                .addAttributes("dl", "class")
                //
                .addAttributes("span", "style")
                //
                .addAttributes("blockquote", "style")
                // 表格
                .addAttributes("table", new String[] {"cellspacing", "style", "border", "width", "height"}).addAttributes("tr", "style", "width", "height").addAttributes("td", "style", "width", "height")
                // div
                .addAttributes("div", new String[] {"align"})
                // 列表
                .addAttributes("li", "style");
        //保留img标签
        TBLOG_WHITE_LIST = new Whitelist().addTags("img")
        // 图片
                .addAttributes("img", new String[] {"src"});

        List<String> flashUrls = new ArrayList<String>();
        flashUrls.add("163.com"); // 网易
        flashUrls.add("video.sina.com.cn"); // 新浪视频
        flashUrls.add("tv.sohu.com"); // hdtv搜狐高清
        flashUrls.add("v.qq.com"); // 腾讯视频
        flashUrls.add("youku.com"); // 优酷网
        flashUrls.add("tudou.com"); // 土豆网
        flashUrls.add("video.baidu.com"); // 百度视频搜索
        flashUrls.add("ku6.com"); // 酷6网
        flashUrls.add("56.com"); // 56网
        flashUrls.add("xunlei.com"); // 迅雷看看
        flashUrls.add("v.ifeng.com"); // 凤凰视频
        flashUrls.add("qiyi.com"); // 奇艺高清
        flashUrls.add("joy.cn"); // 激动网
        flashUrls.add("cntv.cn"); // 中国网络电视台
        flashUrls.add("imgo.tv"); // 芒果tv
        flashUrls.add("jstv.com"); // 江苏卫视
        flashUrls.add("umiwi.com"); // 优米网
        flashUrls.add("smgbb.cn"); // 2010tv东方宽频
        flashUrls.add("v.6.cn"); // 六间房
        flashUrls.add("tv.hao123.com"); // 电视剧排行榜
        flashUrls.add("letv.com"); // 乐视网
        flashUrls.add("pptv.com"); // PPTV
        flashUrls.add("uusee.com"); // UUsee
        flashUrls.add("haoetv.com"); // 好易网视
        flashUrls.add("pipi.cn"); // 皮皮播放器
        flashUrls.add("baofeng.com"); // 暴风影音

        FLASH_URL_WHITE_LIST = new ArrayList<Pattern>(flashUrls.size());
        for (String url : flashUrls) {
            String patternStr = "^http://(([0-9a-zA-Z_]+\\.)*)" + url.replace(".", "\\.") + "/(.*)";
            FLASH_URL_WHITE_LIST.add(Pattern.compile(patternStr));
        }

    }

    /**
     * Constructor
     */
    private HtmlUtil() {}

    /**
     * 过滤非法标签，纠正html语法。
     * 
     * @param html Html content
     * @return Filtered html content
     */
    public static String filterHtml(String html) {
        return Jsoup.clean(html, DEFAULT_WHITE_LIST);
    }

    /**
     * 为微博过滤内容
     * 
     * @param html html content
     * @return Filtered html content
     */
    public static String filterHtmlForTblog(String html) {
    	String pureHtml = html.replaceAll("&nbsp;", "");
    	StringBuffer sf = new StringBuffer();
    	for (char c : pureHtml.toCharArray()){  
            if (Character.isISOControl(c)){  
                sf.append("\\")  
                      .append(Integer.toOctalString(c));         
            }else{  
                sf.append(c);  
            }  
        }
    	pureHtml=sf.toString().replace("\\", "");
        String content=HtmlUtils.htmlUnescape(Jsoup.clean(pureHtml, TBLOG_WHITE_LIST));
        return content;
    }

    /**
     * 获取html内容中的纯文本
     * 
     * @param html html content
     * @return text
     */
    public static String toText(String html) {
        if (!StringUtils.hasLength(html)) {
            return html;
        }
        return Jsoup.clean(html, Whitelist.none()).replace("&nbsp;", "").replace("&quot;", "");
    }

    /**
     * 获取html内容中的纯文本(过滤掉
     * <dl class=".*quote.*">
     * </dl>
     * ) 目前只过滤顶层的bbs_show_content_quote
     * 
     * @param html html content
     * @return text
     */
    public static String getTextWithoutQuote(String html) {
        String pureHtml = html.replaceAll("<dl class=\"bbs_show_content_quote\">[\\s\\S]*</dl>", "");
        return Jsoup.clean(pureHtml, Whitelist.none());
    }

    /**
     * 判断帖子里是否包含图片
     * 
     * @param html html
     * @return boolean
     */
    public static boolean hasImage(String html) {
        if (StringUtils.hasLength(html)) {
            Matcher matcher = Pattern.compile(IMGURL_REG).matcher(html);
            while (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断帖子里的图片
     * 
     * @param html html
     * @return boolean
     */
    public static String getImage(String html) {
        Matcher matcher = Pattern.compile(IMGURL_REG).matcher(html);
        List<String> listImgUrl = new ArrayList<String>();
        while (matcher.find()) {
            listImgUrl.add(matcher.group());
        }
        System.out.println(listImgUrl.size());
        for (String image : listImgUrl) {
            Matcher matchera = Pattern.compile(IMGSRC_REG).matcher(image);
            while (matchera.find()) {
                return matchera.group().substring(0, matchera.group().length() - 1);
            }
        }
        return "";
    }

    /**
     * 归一化Flash元素，归一化之后的Flash元素格式为： <div flashsrc="http://aaaaa.swf" class="flash-img" width="480"
     * height="400" /> 使用div而不使用img是为了防止这些img成为图集中的噪音图片
     * 
     * @param s 归一化之前的帖子内容html
     * @return 归一化之后的帖子内容html
     */
    public static String normalizeFlashElement(String s) {
        Matcher matcher = FLASH_IMG_PATTERN.matcher(s);
        while (matcher.find()) {
            String flash = matcher.group(0);
            String src = matcher.group(2);
            String arg = matcher.group(1) + matcher.group(3);

            String height = getAttribute(arg, "height");
            String width = getAttribute(arg, "width");

            if (height == null || new Integer(height) < 100 || new Integer(height) > 750) {
                height = "400";
            }
            if (width == null || new Integer(width) < 100 || new Integer(width) > 750) {
                width = "480";
            }

            s = s.replace(flash, doNormalize(src, width, height));
        }
        return s;
    }

    private static String doNormalize(String src, String width, String height) {
        // 检查白名单
        boolean isInWhiteList = false;
        for (Pattern pattern : FLASH_URL_WHITE_LIST) {
            if (pattern.matcher(src).matches()) {
                isInWhiteList = true;
                break;
            }
        }

        if (!isInWhiteList) {
            return "";
        }

        StringBuffer buf = new StringBuffer();
        buf.append("<div flashsrc=\"");
        buf.append(src);
        buf.append("\" style=\"width:");
        buf.append(width);
        buf.append("px; height:");
        buf.append(height);
        buf.append("px");
        buf.append("\" class=\"flash-img\" >");
        buf.append("</div>");
        return buf.toString();
    }

    /**
     * 将div表示的Flash转换成Img节点，用来进行帖子编辑的显示
     * 
     * @param s 转换之前的帖子内容
     * @return 转换之后的帖子内容
     */
    public static String convertToImageNode(String s) {
        Matcher matcher = FLASH_DIV_PATTERN.matcher(s);
        while (matcher.find()) {
            String flash = matcher.group(0);
            String src = matcher.group(2);
            String width = matcher.group(3);
            String height = matcher.group(4);
            s = s.replace(flash, doConvert(src, width, height));
        }
        return s;
    }

    private static String doConvert(String src, String width, String height) {
        StringBuffer buf = new StringBuffer();
        buf.append("<img flashsrc=\"");
        buf.append(src);
        buf.append("\" width=\"");
        buf.append(width);
        buf.append("\" height=\"");
        buf.append(height);
        buf.append("\" class=\"flash-img\" src=\"http://img1.cache.netease.com/auto/projects/club/v1.1/editor/blank.gif\" />");
        return buf.toString();
    }

    private static String getAttribute(String str, String attribute) {
        int start = str.indexOf(attribute + "=\"");
        if (start > -1) {
            start += attribute.length() + 2;
            int end = str.indexOf("\"", start);
            return str.substring(start, end);
        }
        return null;
    }
    /**
     * 获取微信正文(保留img标签)
     * @param html html content
     * @return text
     */
    public static String toTextImg(String html) {
        if (!StringUtils.hasLength(html)) {
            return html;
        }
        return Jsoup.clean(html, TBLOG_WHITE_LIST).replaceAll("&nbsp;", "");
    }
    // /**
    // * 从html里提取图片地址
    // *
    // * @param html html
    // * @return list
    // */
    // public static List<ImageVo> getImageSrc(String html) {
    // List<ImageVo> imageVos = new ArrayList<ImageVo>();
    // Matcher matcher = Pattern.compile(
    // "<(?i)img(.[^<]*)src=\"((?!http://mimg\\.163\\.com/)[^\"]*)((.[^<]*)size=\"([^\"]*)){0,1}").matcher(
    // html);
    // while (matcher.find()) {
    // ImageVo imageVo = new ImageVo();
    // imageVo.setName(matcher.group(2));
    // imageVo.setSize(matcher.group(5));
    // imageVos.add(imageVo);
    // }
    // return imageVos;
    // }
}
