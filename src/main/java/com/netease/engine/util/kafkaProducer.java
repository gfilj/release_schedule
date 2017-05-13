package com.netease.engine.util;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;



/**
 * 
 * @author handongming
 *
 */
public class kafkaProducer extends Thread{

	private String topic;
	
	public kafkaProducer(String topic){
		super();
		this.topic = topic;
	}
	
	
	@Override
	public void run() {
		Producer<Integer, String> producer = createProducer();
		int i=0;
		while(true){
			producer.send(new KeyedMessage<Integer, String>(topic, "message: " + i));
			System.out.println("发送了: " + i);
			try {
				TimeUnit.SECONDS.sleep(1);
				i++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * producer
	 * @return
	 */
	private Producer<Integer, String> createProducer() {
		Properties properties = new Properties();
		properties.put("zookeeper.connect", "10.112.157.124:2181, 10.112.157.125:2181, 10.112.157.126:2181");//声明zk
		properties.put("serializer.class", StringEncoder.class.getName());
		properties.put("metadata.broker.list", "10.112.100.238:9092");// 声明kafka broker
		return new Producer<Integer, String>(new ProducerConfig(properties));
	 }
	
	
	public static void main(String[] args) {
		new kafkaProducer("test").start(); // topic:test 
		
	}
	 
}
