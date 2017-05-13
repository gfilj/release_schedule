package test;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;



/**
 * 发送数据
 * 发送了: 0
发送了: 1
发送了: 2
发送了: 3
发送了: 4
发送了: 5
发送了: 6
发送了: 7
发送了: 8
发送了: 9
发送了: 10
发送了: 11
发送了: 12
发送了: 13
发送了: 14
发送了: 15
发送了: 16
发送了: 17
发送了: 18
 * @author zm
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
		Producer producer = createProducer();
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

	private Producer createProducer() {
		Properties properties = new Properties();
		properties.put("zookeeper.connect", "10.112.157.124:2181, 10.112.157.125:2181, 10.112.157.126:2181");//声明zk
		properties.put("serializer.class", StringEncoder.class.getName());
		properties.put("metadata.broker.list", "10.112.100.238:9092");// 声明kafka broker
		return new Producer<Integer, String>(new ProducerConfig(properties));
	 }
	
	
	public static void main(String[] args) {
		new kafkaProducer("test").start();// 使用kafka集群中创建好的主题 test 
		
	}
	 
}
