package com.netease.engine.service.nos.impl;

import com.netease.cloud.ClientException;
import com.netease.cloud.ServiceException;
import com.netease.cloud.auth.BasicCredentials;
import com.netease.cloud.services.nos.Nos;
import com.netease.cloud.services.nos.model.GeneratePresignedUrlRequest;
import com.netease.cloud.services.nos.model.GetObjectRequest;
import com.netease.cloud.services.nos.model.NOSObject;
import com.netease.cloud.services.nos.model.ObjectMetadata;
import com.netease.cloud.services.nos.transfer.TransferManager;
import com.netease.cloud.services.nos.transfer.TransferManagerConfiguration;
import com.netease.cloud.services.nos.transfer.Upload;
import com.netease.cloud.services.nos.transfer.model.UploadResult;
import com.netease.engine.service.nos.NosService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * nos实现类
 * 
 * @author handongming
 *
 */
public class NosServiceImpl implements NosService {
	
    public static final Logger logger = LoggerFactory.getLogger(NosServiceImpl.class);
    public static final int MIN_LARGE_OBJECT_SIZE = 50 * 1024 * 1024;
    public static final int MAX_PART_SIZE = 100 * 1024 * 1024;
    public static final String NOS_DOMAIN = "nos.netease.com";
    public static final String HTTP_PREFIX = "http://";
    private static final String ENCODE = "utf-8";
    /**
     * 计算文件md5生成文件名，外加文件名后缀构成文件名
     */
    public static final int NAME_TYPE_FILE_MD5 = 0;
    /**
     * 采用uuid生成文件名，外加文件名后缀构成文件名
     */
    public static final int NAME_TYPE_UUID = 1;
    protected Nos nosClient = null;
    protected TransferManager transferManager = null;
    private boolean isInitialized = false;
    
    
    protected String accessKey;
    protected String secretKey;
    protected String bucketName;
    
    public NosServiceImpl() {}

    public NosServiceImpl(String accessKey, String secretKey, String bucketName) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketName = bucketName;
    }

    @Override
    public synchronized void init() {
        if (isInitialized()) {
            return;
        }
        transferManager = new TransferManager(new BasicCredentials(accessKey, secretKey));
        TransferManagerConfiguration configuration = new TransferManagerConfiguration();
        configuration.setMinimumUploadPartSize(MIN_LARGE_OBJECT_SIZE);
        configuration.setMultipartUploadThreshold(MIN_LARGE_OBJECT_SIZE);
        transferManager.setConfiguration(configuration);
        this.nosClient = transferManager.getNosClient();
        isInitialized = true;
    }

    @Override
    public synchronized void unInit() {
        if (!isInitialized()) {
            return;
        }
        if(transferManager != null) {
            transferManager.shutdownNow();
        } 
        isInitialized = true;
    }


    @Override
    public String getText(String key) throws Exception {
        return getText(key, null);
    }

    @Override
    public String getText(String key, String versionId) throws Exception {
        NOSObject obj = this.getObject(key, versionId);
        return getText(obj);
    }

    @Override
    public NOSObject getObject(String key) throws Exception {
        return this.getObject(key, null);
    }

    @Override
    public NOSObject getObject(String key, String versionId) throws Exception {
        GetObjectRequest request = new GetObjectRequest(bucketName, key, versionId);
        return nosClient.getObject(request);
    }

    @Override
    public boolean copy(String key, File outFile) {
        return this.copy(key, null, outFile);
    }

    @Override
    public boolean copy(String key, String versionId, File outFile) {
        try {
            NOSObject obj = this.getObject(key, versionId);
            writeFile(obj, outFile);
            return true;
        } catch (Exception e) {
            logException(key, versionId, e);
        }
        return false;
    }

    @Override
    public boolean copy(String key, OutputStream output) {
        return this.copy(key, null, output);
    }

    @Override
    public boolean copy(String key, String versionId, OutputStream output) {
        try {
            NOSObject obj = this.getObject(key, versionId);
            writeOutputStream(obj, output);
            return true;
        } catch (Exception e) {
            logException(key, versionId, e);
        }
        return false;
    }

    @Override
    public UploadResult putText(String key, String text) throws Exception {
        text = StringUtils.trimToEmpty(text);
        return this.putObject(key, new ByteArrayInputStream(text.getBytes(ENCODE)), null);
    }

    @Override
    public UploadResult putObject(String key, InputStream input) throws Exception {
        return putObject(key, input, null);
    }

    @Override
    public UploadResult putObject(String key, File file) throws Exception {
        return putObject(key, file, null);
    }

    @Override
    public UploadResult putObject(String key, File file, ObjectMetadata metadata) throws Exception {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            if (metadata == null) {
                metadata = new ObjectMetadata();
            }
            if (StringUtils.isBlank(metadata.getContentType())) {
                ContentType ct = getContentTypeFromName(key);
                if (ct == ContentType.BIN) {
                    ct = getContentTypeFromFile(file);
                }
                metadata.setContentType(ct.getCt());
            }
            if (metadata.getContentLength() == 0) {
                metadata.setContentLength(file.length());
            }
            return this.putObject(key, in, metadata);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    public UploadResult putObject(String key, InputStream input, ObjectMetadata metadata) throws Exception {
        long st = System.currentTimeMillis();
        long len = input.available();
        if (metadata == null) {
            metadata = new ObjectMetadata();
        }
        if (StringUtils.isBlank(metadata.getContentType())) {
            ContentType ct = getContentTypeFromName(key);
            metadata.setContentType(ct.getCt());
        }
        if (metadata.getContentLength() == 0L) {
            metadata.setContentLength(input.available());
        }
        Upload upload = transferManager.upload(bucketName, key, input, metadata);
        try {
            UploadResult ret = upload.waitForUploadResult();
            return ret;
        } catch (ServiceException e) {
            throw new Exception(e);
        } catch (ClientException e) {
            throw new Exception(e);
        } catch (InterruptedException e) {
            throw new Exception(e);
        } finally {
            long ed = System.currentTimeMillis();
            logger.debug("upload {} kb file cost {} ms.", len / 1000, ed - st);
        }
    }

    @Override
    public void deleteObject(String key) throws Exception {
        this.deleteObject(key, null);
    }

    @Override
    public void deleteObject(String key, String versionId) throws Exception {
        nosClient.deleteObject(bucketName, key, versionId);
    }

    @Override
    public NOSObject getRangeObject(String key, long start, long end) throws Exception {
        GetObjectRequest request = new GetObjectRequest(bucketName, key);
        request.setRange(start, end);
        return nosClient.getObject(request);
    }

    @Override
    public String key() {
        return UUID.randomUUID().toString();
    }

    @Override
    public boolean existAt(String key) {
        return existAt(key, null);
    }

    @Override
    public boolean existAt(final String key, String versionId) {
        return nosClient.doesObjectExist(bucketName, key, versionId);
    }

    protected void writeFile(NOSObject obj, File outFile) throws Exception {
        OutputStream output = null;
        try {
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            output = new FileOutputStream(outFile);
            writeOutputStream(obj, output);
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    protected void writeOutputStream(NOSObject obj, OutputStream output) throws Exception {
        try {
            IOUtils.copy(obj.getObjectContent(), output);
        } catch (IOException e) {
            throw new Exception(e);
        } finally {
            IOUtils.closeQuietly(obj.getObjectContent());
        }
    }

    protected void logException(String key, String versionId, Exception e) {
        logger.error("key= " + key + " &version= " + versionId, e);
    }

    private ContentType getContentTypeFromFile(File file) {
        if (file.exists()) {
            return getContentTypeFromName(file.getName());
        }
        return ContentType.BIN;
    }

    private ContentType getContentTypeFromName(String name) {
        if (StringUtils.isBlank(name)) {
            return ContentType.BIN;
        }
        int index = name.lastIndexOf('.');
        if (index != -1) {
            String suffix = name.substring(index + 1);
            ContentType ct = ContentType.getContentTypeBySuffix(suffix);
            return ct;
        }
        return ContentType.BIN;
    }

    public ObjectMetadata updateContentType(String key, String fileName, ObjectMetadata metadata, String suffix) {
        if (metadata == null) {
            metadata = new ObjectMetadata();
        }
        if (StringUtils.isBlank(metadata.getContentType())) {
            ContentType ct = ContentType.getContentTypeBySuffix(suffix);
            if (ct == ContentType.BIN) {
                ct = getContentTypeFromName(key);
            }
            if (ct == ContentType.BIN) {
                ct = getContentTypeFromName(fileName);
            }
            if (ct == ContentType.BIN) {
                ct = ContentType.JPEG;
            }
            metadata.setContentType(ct.getCt());
        }
        return metadata;
    }

   protected String generateExpireUrl(String key, Date ExpireTime, Map<String, String> m) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key);
        generatePresignedUrlRequest.setExpiration(ExpireTime);

        URL url = transferManager.getNosClient().generatePresignedUrl(generatePresignedUrlRequest);
        StringBuilder sb = new StringBuilder();
        if (m != null && !m.isEmpty()) {

            try {
                for (String k : m.keySet()) {
                    sb.append("&").append(k).append("=").append(URLEncoder.encode(m.get(k), ENCODE));
                }
                return url.toExternalForm() + sb.toString();
            } catch (UnsupportedEncodingException e) {
            }
        }
        return url.toExternalForm();
    }

    public String generateExpireUrl(String key, Date ExpireTime) {
        return generateExpireUrl(key, ExpireTime, null);
    }

    public String generateExpireUrl(String key) {
        return generateExpireUrl(key, new Date(System.currentTimeMillis() + 10 * 60 * 1000L));
    }

    public String generatePublicUrl(String key) {
        return HTTP_PREFIX + this.bucketName + "." + NOS_DOMAIN + "/" + key;
    }


    public static String getFileSuffix(File file) {
        return file.getName().substring(file.getName().lastIndexOf((int) '.') + 1);
    }

    protected String getText(NOSObject obj) throws Exception {
        if (obj == null) {
            return null;
        }
        try {
            return IOUtils.toString(obj.getObjectContent(), ENCODE);
        } finally {
            IOUtils.closeQuietly(obj.getObjectContent());
        }

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

    public Nos getNosClient() {
        return nosClient;
    }
    
	/**
     * @return the isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}
