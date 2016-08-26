package com.sas.mkt.kafka.clients.specific;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import com.sas.mkt.kafka.domain.WebEvent;

public class SpecificConsumerExample {

	public static void main(String[] args) {
		SpecificConsumerExample sce = new SpecificConsumerExample();
		sce.getMessagesFromPartition();
	}

	/*
	 * This looks a lot like the GenericConsumer except we are creating a KafkaConsumer<String, WebEvent>
	 * instead of a KafkaConsumer<String, String>.
	 * This gives us back a record of type ConsumerRecord<String, WebEvent> instead of ConsumerRecord<String, String>
	 * and record.value() == WebEvent instance not a string. 
	 */
	private void getMessagesFromPartition() {
		String topic = "web-events";
		
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka0.cidev.sas.us:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "baldur22");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("value.deserializer", io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
		props.put("key.deserializer", io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
		props.put("schema.registry.url", "http://kafka0.cidev.sas.us:8081");
		
		KafkaConsumer<String, WebEvent> consumer = new KafkaConsumer<>(props);
		TopicPartition tp = new TopicPartition(topic, 0);
		consumer.assign(Arrays.asList(tp));
		consumer.seek(tp, 0); // this puts you back at the start of the message queue each time
		
		System.out.println("assigned consumer to partition: " + tp.toString());
		int cnt = 0, maxRequests = 20;
		boolean done = false;
		while (!done) {
			cnt++;
			if (cnt > maxRequests) // we'll bail after polling maxRequest times (= maxRequest/10 sec)
				done = true;
			ConsumerRecords<String, WebEvent> records = consumer.poll(100);
			for (ConsumerRecord<String, WebEvent> record : records) {
				System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
				System.out.println("");
			}
		}
		consumer.close();
	}

}
