package com.sas.mkt.kafka.clients;

import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;

public class GenericAvroConsumer {
	
	String VALUE_TOPIC = "topicv";
	
	public static void main(String[] args) {
		GenericAvroConsumer gac = new GenericAvroConsumer();	
		gac.readValueOnlyMessages();
	}
	
	private Properties getKafkaConsumerProps() {
		Properties props = new Properties();
		props.put("zookeeper.connect", "kafka0.cidev.sas.us:2181,kafka1.cidev.sas.us:2181");
		props.put("group.id", "group1");
		props.put("schema.registry.url", "http://kafka0.cidev.sas.us:8081");
		return props;
	}

	private void readValueOnlyMessages() {
		Properties props = getKafkaConsumerProps();
		
		
	}

}
