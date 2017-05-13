package com.netease.engine.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;


/**
 * 
 * @author bj_yangsong
 *
 */
public class FileTransferUtil {
	public static final String PATH="/home/bj_yangsong/pics/";
    /**
     * 将文件下载到本地
     * @param source 远程文件url
     * @param dst 本地文件
     * */
    public static void download(String source, String dst){

        URL urlFile = null;
        HttpURLConnection connection = null;
        HttpsURLConnection  httpsConnection = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        File localFile = new File(dst);

        // always verify the host - dont check for certificate
        final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        try {
            urlFile = new URL(source);
            connection = (HttpURLConnection) urlFile.openConnection();
            if(connection instanceof HttpsURLConnection){
                httpsConnection = (HttpsURLConnection) connection;
                httpsConnection.setSSLSocketFactory(getSSLSocketFactory());
                httpsConnection.setHostnameVerifier((DO_NOT_VERIFY));
            }
            connection.connect();
            bis = new BufferedInputStream(connection.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(localFile));

            int len = 2048;
            byte[] b = new byte[len];
            while ((len = bis.read(b)) != -1)
            {
                bos.write(b, 0, len);
            }
            bos.flush();
            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try
            {
                if(null!=bis){
                    bis.close();
                }
                if(null!=bos){
                    bos.close();
                }
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
    
    public static byte[] getByteForURL(String source) throws Exception{
        URL urlFile = null;
        HttpURLConnection connection = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream output =null;
        try {
            urlFile = new URL(source);
            connection = (HttpURLConnection) urlFile.openConnection();
            connection.connect();
            bis = new BufferedInputStream(connection.getInputStream());
            output= new ByteArrayOutputStream();
            int len = 2048;
            byte[] b = new byte[len];
            while ((len = bis.read(b)) != -1)
            {
            	output.write(b, 0, len);
            }
            byte[] data  = output.toByteArray();
        	return data;
        } catch (Exception e) {
        	throw e;
        }finally {
            try
            {
                if(null!=bis){
                    bis.close();
                }
                if(output!=null){
                	output.close();
                }
                if(connection!=null){
                	connection.disconnect();
                }
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    
    public static InputStream getInputStreamURL(String source){
        URL urlFile = null;
        HttpURLConnection connection = null;
        BufferedInputStream bis = null;
        try {
            urlFile = new URL(source);
            connection = (HttpURLConnection) urlFile.openConnection();
            connection.connect();
            bis = new BufferedInputStream(connection.getInputStream());
        	return bis;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try
            {
                if(null!=bis){
                    bis.close();
                }
                if(connection!=null){
                	connection.disconnect();
                }
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public static String getKey() {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(10);
        String key =  today + uuid;
        return key;
    }
    /**
     * 上传到nos
     * @param file
     * @return
     */
    public static String upload(String file){
    	return null;
    }

    public static String getFileName(String url){
    	if(url!=null&&!StringUtil.isEmpty(url)){
    		return url.substring(url.lastIndexOf("/")+1);
    	}
    	return "";
    }

    private static SSLSocketFactory getSSLSocketFactory(){
        return sslContextForTrustedCertificates().getSocketFactory();
    }

	@SuppressWarnings("finally")
	private static SSLContext sslContextForTrustedCertificates() {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            //javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (KeyManagementException e) {
            e.printStackTrace();
        }finally {
            return sc;
        }
    }

    static class miTM implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }
        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }
    
    public static void deleteFile(String filePath){
    	File file=new File(filePath);
    	if(file!=null){
    		file.delete();
    	}
    }
    
    public static String getMd5ByFile(File file){
        String value = null;
        FileInputStream in=null;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
		    MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
		    MessageDigest md5 = MessageDigest.getInstance("MD5");
		    md5.update(byteBuffer);
		    BigInteger bi = new BigInteger(1, md5.digest());
		    value = bi.toString(16);
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
	            if(null != in) {
		            try {
			        in.close();
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
		    }
		}
		return value;
    } 
}
