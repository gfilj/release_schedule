package com.netease.engine.util;

import com.netease.cloud.ClientException;
import com.netease.cloud.ServiceException;
import com.netease.cloud.auth.BasicCredentials;
import com.netease.cloud.auth.Credentials;
import com.netease.cloud.services.nos.NosClient;
import com.netease.cloud.services.nos.model.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Date;

import javax.imageio.ImageIO;

/**
 * 
 * @author handongming
 *
 */
public class NosUtil {
	
//    // 公开桶
    public final static String PUBLIC_BUCKET_NAME = "house-public-cdn";
//    // 私有桶
    public final static String PRIVATE_BUCKET_NAME = "";
//    // NOS Host
    private final static String NOS_HOST_URL = "http://nos.netease.com";
    // 访问凭证ID
    private final static String ACCESS_KEY = "e439bdf145564f80a8d582e7ebdc4dd4";
    // 访问凭证秘钥
    private final static String SECRET_KEY = "db106eb26d1c4981831e7c052e952ace";
    
    private final static String PREFIX_NAME = "clawler-";
    
    private static Log logger = LogFactory.getLog(NosUtil.class);

    /**
     * 构造客户端
     *
     * @param bucketName
     * @param bDedup
     * @return
     */
    private static NosClient newNosClient(String bucketName, Boolean bDedup) {
        Credentials credentials = new BasicCredentials(ACCESS_KEY, SECRET_KEY);
        NosClient nosClient = new NosClient(credentials);
        nosClient.setBucketDedup(bucketName, bDedup ? "Enabled" : "Disabled");
        return nosClient;
    }

    /**
     * 生成url
     * 
     * @param bucketName NOS桶
     * @param objectKey  对象key
     * @param nosClient  当前上传客户端
     * @return
     */
    private static String generateUrl(String bucketName, String objectKey, NosClient nosClient) {
        String strUrl = NOS_HOST_URL + "/" + bucketName  +"/"+ objectKey;
        if (PRIVATE_BUCKET_NAME.equals(bucketName)) {
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey);
            generatePresignedUrlRequest.setExpiration(new Date(System.currentTimeMillis() + 1000 * 3600 * 24 * 36500L));
            URL url = nosClient.generatePresignedUrl(generatePresignedUrlRequest);
            if (url != null) {
                try {
                    strUrl = url.toURI().toString();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return strUrl;
    }

    /**
     * 返回位置裁剪url
     *
     * @param baseUrl
     * @param cropX
     * @param cropY
     * @param cropWidth
     * @param cropHeight
     * @return
     */
    public static String generateCropUrl(String baseUrl, Integer cropX, Integer cropY, Integer cropWidth, Integer cropHeight) {
        baseUrl += "?imageView&crop=" + String.valueOf(cropX) + "_" + String.valueOf(cropY) + "_" + String.valueOf(cropWidth) + "_" + String.valueOf(cropHeight);
        return baseUrl;
    }
    
    
    /**
     * 获取文件字节
     * @param filePath
     * @return
     * @throws Exception
     */
    private static byte[] getFileByte(String filePath) throws Exception 
    {

    	File file = new File(filePath);
    	if(!file.exists())
    	{
    		return null;
    	}
    	FileInputStream fis = new FileInputStream(file);
    	BufferedInputStream bis = new BufferedInputStream(fis);
    	byte[] b = new byte[bis.available()];
    	bis.read(b);
    	bis.close();
    	fis.close();
        return b; 
    }

    /**
     * 去重上传
     * @param datas
     * @param bucketName
     * @return
     * @throws Exception
     */
    public static String uploadDedupFile(String filePath, String bucketName) throws Exception 
    {
    	byte[] buffers = getFileByte(filePath);
    	if(buffers!=null&&buffers.length>0)
    	{
    		return uploadDedupFile(buffers,bucketName);
    	}
    	return null;
    }

    /**
     * 去重上传
     * @param datas
     * @param bucketName
     * @return
     * @throws Exception
     */
    public static String uploadDedupFile(byte[] datas, String bucketName) throws Exception {
        return uploadDedupFile(datas, bucketName, null);
    }

    /**
     * 去重上传
     * @param datas
     * @param bucketName
     * @param objectKey
     * @return
     * @throws Exception
     */
    public static String uploadDedupFile(byte[] datas, String bucketName, String objectKey) throws Exception {
        //初始化
        NosClient nosClient = newNosClient(bucketName, true);
        
        
        //获取md5码
        String md5 = StringUtil.getMd5(datas);
		if(objectKey == null)
		{
			objectKey = PREFIX_NAME + md5 + ".jpg";
		}else
		{
			objectKey = PREFIX_NAME + objectKey;
		}
        //重复校验
        DeduplicateRequest request = new DeduplicateRequest(bucketName, objectKey);
        request.setMD5Digest(md5);
        if (nosClient.isDeduplicate(request)) {
            //说明存在同样MD5的文件，那么相当于创建了一个引用，your-bucketname/dedupObject指向已经存在的文件。
            String url = generateUrl(bucketName, objectKey, nosClient);
            return url;
        }
        ByteArrayInputStream inputStream = null;
        try {
            //上传
            inputStream = new ByteArrayInputStream(datas);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(datas.length);
            if (objectKey.endsWith("html")||objectKey.endsWith("txt")) {
                metadata.setContentType("text/html");
            } else {
                metadata.setContentType("image/jpeg");
            }
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream, metadata);
            nosClient.putObject(putObjectRequest);
            String url = generateUrl(bucketName, objectKey, nosClient);
            return url;
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw e;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.info("------------------------------>NOS Upload InputStream close failed err=" + e.getMessage());
            }
        }
    }
    
    /**
     * 去重上传
     * @param datas
     * @param bucketName
     * @param suffix
     * @param contentType
     * @return
     * @throws Exception
     */
    public static String uploadDedupFile(String filePath, String bucketName, String suffix, String contentType) throws Exception
    {
    	byte[] buffers = getFileByte(filePath);
    	if(buffers!=null&&buffers.length>0)
    	{
    		return uploadDedupFile(buffers,bucketName,suffix,contentType);
    	}
    	return null;    	
    }
    
    
    /**
     * 去重上传
     * @param datas
     * @param bucketName
     * @param suffix
     * @param contentType
     * @return
     * @throws Exception
     */
    public static String uploadDedupFile(byte[] datas, String bucketName, String suffix, String contentType) throws Exception {
        //初始化
        NosClient nosClient = newNosClient(bucketName, true);

        //获取md5码
        String md5 = StringUtil.getMd5(datas);
        String objectKey = PREFIX_NAME + md5 + suffix;
        //重复校验
        DeduplicateRequest request = new DeduplicateRequest(bucketName, objectKey);
        request.setMD5Digest(md5);
        if (nosClient.isDeduplicate(request)) {
            //说明存在同样MD5的文件，那么相当于创建了一个引用，your-bucketname/dedupObject指向已经存在的文件。
            String url = generateUrl(bucketName, objectKey, nosClient);
            return url;
        }
        ByteArrayInputStream inputStream = null;
        try {
            //上传
            inputStream = new ByteArrayInputStream(datas);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(datas.length);
            metadata.setContentType(contentType);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream, metadata);
            nosClient.putObject(putObjectRequest);
            return NOS_HOST_URL + "/" + bucketName +"/"+ objectKey;
        } catch (Exception e) {
        	logger.info(e.getMessage());
            throw e;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            	logger.info("------------------------------>NOS Upload InputStream close failed err=" + e.getMessage());
            }
        }
    }

    
    /**
     * 删除文件
     * 
     * @param objectkey
     * @param bucketName
     */
    public static void deleteFile(String objectkey, String bucketName) {
        NosClient nosClient = newNosClient(bucketName, true);
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, objectkey);
        try {
            nosClient.deleteObject(deleteObjectRequest);
            logger.info("delete ok");
        } catch (ServiceException e1) {
            e1.printStackTrace();
        } catch (ClientException e2) {
            e2.printStackTrace();
        }
    }    
   
    //图片转成Byte(有局限性)
    public static byte[] image2byte2(String path){
     ByteArrayOutputStream baos = null;
     try{
		 URL u = new URL(path);
		 BufferedImage image = ImageIO.read(u);     
		 baos = new ByteArrayOutputStream();
		 ImageIO.write(image, "jpg", baos);
		 baos.flush();    
     }catch (Exception e){
     e.printStackTrace();
     }finally{
		    if(baos != null){
		      try {
		          baos.close();
		       } catch (IOException e) {
		      }
		    }
	    }
      return baos.toByteArray();
  }
   


    public static void main(String[] args) throws Exception {

    	/*byte[] buffer = null;  
		File file = new File("E:\\test\\test.txt");
		FileInputStream fis = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		byte[] b = new byte[1024]; 
        int n;  
        while ((n = fis.read(b)) != -1)  
        {  
            bos.write(b, 0, n);  
        }  
        fis.close();  
        bos.close();  
        buffer = bos.toByteArray();		
		String result = NosUtil.uploadDedupFile(buffer, NosUtil.PUBLIC_BUCKET_NAME);
		System.out.println(result);*/
    	
    	
    	String url="http://ww1.sinaimg.cn/mw690/e286b988gw1f7dtu9ug95j20ku0mnabq.jpg";
    	byte[] urlByte=image2byte2(url);
    	String new_url=uploadDedupFile(urlByte,PUBLIC_BUCKET_NAME);
    	System.out.println("这是下载以后url："+new_url);
    }

}
