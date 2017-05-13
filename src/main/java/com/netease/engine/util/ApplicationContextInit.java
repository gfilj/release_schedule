package com.netease.engine.util;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 实例化bean对象
 * @author handongming
 *
 */
public class ApplicationContextInit implements ApplicationContextAware {
	
	protected static final Logger logger = Logger.getLogger(ApplicationContextInit.class);

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;

	}

	public static ApplicationContext getApplicationContext() {
		if (applicationContext == null) {
			logger.info("Load application context manual form xml file");
			applicationContext = (ApplicationContext) (new ClassPathXmlApplicationContext(
					"config/spring-application.xml"));
		}
		return applicationContext;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanName){
		return (T) getApplicationContext().getBean(beanName);
	}
	
	public static<T> T getBean(Class<T> clazz)
	{
		return getApplicationContext().getBean(clazz);
	}

}
