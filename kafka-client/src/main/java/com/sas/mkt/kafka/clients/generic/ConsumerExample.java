package com.sas.mkt.kafka.clients.generic;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

public class ConsumerExample {

	private String topic = "page-views";

	public static void main(String[] args) {
		ConsumerExample ce = new ConsumerExample();
		ce.getMessagesFromPartition();
		ce.lookAtRawMessages();
	}

	private void getMessagesFromPartition() {
		
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka0.cidev.sas.us:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "baldur22");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("value.deserializer", io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
		props.put("key.deserializer", io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
		props.put("schema.registry.url", "http://kafka0.cidev.sas.us:8081");
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		TopicPartition tp = new TopicPartition(topic, 0);
		consumer.assign(Arrays.asList(tp));
		consumer.seek(tp, 0); // this puts you back at the start of the message queue each time
		
		System.out.println("assigned consumer to partition: " + tp.toString());
		int cnt = 0; 
		boolean done = false;
		while (!done) {
			cnt++;
			if (cnt > 6) // we'll bail after 5 records
				done = true;
			ConsumerRecords<String, String> records = consumer.poll(100);
			for (ConsumerRecord<String, String> record : records) {
				System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
				System.out.println("");
			}
		}
		consumer.close();
	}

	private void lookAtRawMessages() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "kafka0.cidev.sas.us:9092");
		props.put("group.id", "baldur22");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		TopicPartition tp = new TopicPartition(topic, 0);
		consumer.assign(Arrays.asList(tp));
		consumer.seek(tp, 0); // this puts you back at the start (0) of the message queue each time
		
		System.out.println("\nassigned consumer to partition: " + tp.toString());
		System.out.println("reading the raw messages off the queue");
		int cnt = 0; 
		boolean done = false;
		while (!done) {
			cnt++;
			if (cnt > 6) // we'll bail after 5 records
				done = true;
			ConsumerRecords<String, String> records = consumer.poll(100);
			for (ConsumerRecord<String, String> record : records) {
				System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
				System.out.println("");
			}
		}
		consumer.close();
	}

}
