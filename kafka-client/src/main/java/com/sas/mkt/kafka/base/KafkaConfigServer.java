package com.sas.mkt.kafka.base;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

public class KafkaConfigServer {
	
	private Properties kp = new Properties();
	private Producer<String, String> producer;
	
	public Producer<String, String> getProducer() {
		return producer;
	}

	public KafkaConfigServer() {
		kp.put("bootstrap.servers", "kafka0.cidev.sas.us:9092,kafka1.cidev.sas.us:9092,kafka2.cidev.sas.us:9092");
		kp.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		kp.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
		producer = new KafkaProducer<>(kp);
	}
}
