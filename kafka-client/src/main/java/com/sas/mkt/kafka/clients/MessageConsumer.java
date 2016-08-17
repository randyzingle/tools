package com.sas.mkt.kafka.clients;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import com.sas.mkt.kafka.base.KafkaConfigServer;

public class MessageConsumer {
	
	private static Properties props;
	private static String topic = "int-topic";

	public static void main(String[] args) {
	     props = new KafkaConfigServer().getConsumerProperties();

	     props.put("group.id", "baldur1");
	     props.put("enable.auto.commit", "true");
	     props.put("auto.commit.interval.ms", "1000");
	     props.put("session.timeout.ms", "30000");

		MessageConsumer mc = new MessageConsumer();
		mc.getMessages();

	}

	private void getMessages() {
		Properties props = new Properties();
	     props.put("bootstrap.servers", "kafka0.cidev.sas.us:9092");
	     props.put("group.id", "baldurxxx");
	     props.put("enable.auto.commit", "true");
	     props.put("auto.commit.interval.ms", "1000");
	     props.put("session.timeout.ms", "30000");
	     props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	     props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	     KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
	     consumer.subscribe(Arrays.asList(topic));
	     System.out.println("subscribed to topics...");
	     TopicPartition tp = new TopicPartition(topic, 1);
//	     consumer.seek(tp, 0);
	     while (true) {
	         ConsumerRecords<String, String> records = consumer.poll(100);
	         for (ConsumerRecord<String, String> record : records) {
	             System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
	             System.out.println("");
	         }
	         
	     }
	 
	}

}
