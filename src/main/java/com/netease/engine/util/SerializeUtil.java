package com.netease.engine.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.log4j.Logger;

/**
 * 序列化-tool
 * 
 * @author handongming
 *
 */
public class SerializeUtil {

	protected static Logger logger = Logger.getLogger(SerializeUtil.class);

	/**
	 * 反序列化
	 * 
	 * @param str
	 * @return
	 */
	public static Object deserialize(byte[] array) {

		ObjectInputStream ois = null;
		ByteArrayInputStream bais = null;
		try {

			bais = new ByteArrayInputStream(array);
			ois = new ObjectInputStream(bais);
			Object o = ois.readObject();
			ois.close();
			bais.close();
			return o;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (Throwable thex) {
				}
			}
			if (bais != null) {
				try {
					bais.close();
				} catch (Throwable thex) {
				}
			}
		}

		return null;
	}

	/**
	 * 序列化
	 * 
	 * @param obj
	 * @return
	 */
	public static byte[] serialize(Object obj) {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			byte[] abc = baos.toByteArray();
			oos.close();
			baos.close();
			return abc;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			logger.error("序列化时产生错误 ", ex);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (Throwable thex) {
				}
			}
			if (baos != null) {
				try {
					baos.close();
				} catch (Throwable thex) {
				}
			}
		}
		return null;
	}

}
