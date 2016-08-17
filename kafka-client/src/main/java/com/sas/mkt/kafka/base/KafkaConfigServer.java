package com.sas.mkt.kafka.base;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

public class KafkaConfigServer {
	
	private Properties producerProps = new Properties();
	private Properties consumerProps = new Properties();
	
	public Properties getProducerProperties() {
		producerProps.put("bootstrap.servers", "kafka0.cidev.sas.us:9092,kafka1.cidev.sas.us:9092,kafka2.cidev.sas.us:9092");
		producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return producerProps;
	}
	
	public Properties getConsumerProperties() {
		consumerProps.put("bootstrap.servers", "kafka0.cidev.sas.us:9092,kafka1.cidev.sas.us:9092,kafka2.cidev.sas.us:9092");
		consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		return consumerProps;
	}

	public KafkaConfigServer() {
	}
}
