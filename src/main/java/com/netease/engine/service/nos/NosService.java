package com.netease.engine.service.nos;

import com.netease.cloud.services.nos.model.NOSObject;
import com.netease.cloud.services.nos.model.ObjectMetadata;
import com.netease.cloud.services.nos.transfer.model.UploadResult;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * nos接口
 * 
 * @author handongming
 *
 */
public interface NosService {

    void init();

    void unInit();

    String getText(String key) throws Exception;

    String getText(String key, String versionId) throws Exception;

    NOSObject getObject(String key) throws Exception;

    NOSObject getObject(String key, String versionId) throws Exception;

    boolean copy(final String key, File outFile);

    boolean copy(final String key, String versionId, File outFile);

    boolean copy(final String key, OutputStream output);

    boolean copy(final String key, String versionId, OutputStream output);

    UploadResult putText(String key, final String text) throws Exception;

    UploadResult putObject(String key, InputStream input) throws Exception;

    UploadResult putObject(String key, File file) throws Exception;

    UploadResult putObject(String key, File file, ObjectMetadata metadata) throws Exception;

    UploadResult putObject(String key, InputStream input, ObjectMetadata metadata) throws Exception;

    void deleteObject(String key) throws Exception;

    void deleteObject(String key, String versionId) throws Exception;

    NOSObject getRangeObject(String nosKey, long start, long end) throws Exception;

    public String key();

    public boolean existAt(final String key);

    public boolean existAt(final String key, String versionId);

    public static enum ContentType {
        JPG("jpg", "image/jpeg"), JPEG("jpeg", "image/jpeg"), JPE("jpe", "image/jpeg"), JFIF("jfif", "image/jpeg"),
        TIF("tif", "image/tiff"), TIFF("tiff", "image/tiff"), FAX("fax", "image/fax"), GIF("gif", "image/gif"),
        ICO("ico", "image/x-icon"), NET("net", "image/pnetvue"), PNG("png", "image/png"), RP("rp",
                                                                                             "image/vnd.rn-realpix"),
        WBMP("wbmp", "image/vnd.wap.wbmp"), BMP("bmp", "application/x-bmp"), BIN("bin", "application/octet-stream");

        private static Map<String, ContentType> m = Collections.synchronizedMap(new HashMap<String, ContentType>());
        private String suffix;
        private String ct;
        static {
            for (ContentType c : ContentType.values()) {
                m.put(c.getSuffix(), c);
            }
        }

        private ContentType(String suffix, String ct) {
            this.suffix = suffix;
            this.ct = ct;
        }

        public static ContentType getContentTypeBySuffix(String suffix) {
            suffix = StringUtils.trimToEmpty(suffix);
            if (StringUtils.isBlank(suffix)) {
                return BIN;
            }
            suffix = suffix.toLowerCase();
            ContentType ct = m.get(suffix);
            if (ct == null) {
                ct = BIN;
            }
            return ct;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getCt() {
            return ct;
        }

    }
}
