package com.sas.mkt.kafka.clients.specific;

import java.util.List;
import java.util.Properties;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.sas.mkt.kafka.clients.utils.RecordGenerator;
import com.sas.mkt.kafka.domain.WebEvent;

public class SpecificProducerExample {

	public static void main(String[] args) {
		SpecificProducerExample spe = new SpecificProducerExample();
		spe.publishMessages();
	}
	
	private Properties getKafkaProducerProps() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka0.cidev.sas.us:9092,kafka1.cidev.sas.us:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
		props.put("schema.registry.url", "http://kafka0.cidev.sas.us:8081");
		props.put("retries", 0);
		return props;
	}

	/* 
	 * If we run this before creating the topic or registering the schemas they will be created/registered for us
	 * check topic: bin/kafka-topics --list --zookeeper kafka0.cidev.sas.us:2181
	 * check registry: curl -X GET -i http://kafka0.cidev.sas.us:8081/subjects   
	 * 
	 * Once we've run it the topic list command will show the web-event topic which we can examine with:
	 * bin/kafka-topics --describe --zookeeper kafka0.cidev.sas.us:2181 --topic web-events
	 *  Topic:web-events       	PartitionCount:1       	ReplicationFactor:1    	Configs:
     *  Topic: web-events      	Partition: 0   	Leader: 2      	Replicas: 2    	Isr: 2
     *  
     *  and we can look at the registered schemas for the key and value:
     *  curl -X GET -i http://kafka0.cidev.sas.us:8081/subjects/web-events-key/versions/latest
     *  curl -X GET -i http://kafka0.cidev.sas.us:8081/subjects/web-events-value/versions/latest
     *  
     *  We can read the raw data that we've written to the topic with:
     *  bin/kafka-console-consumer --zookeeper kafka0.cidev.sas.us:2181 --topic web-events --from-beginning
     *  
     *  Or we can use the schema to deserialize the full data set:
     *  bin/kafka-avro-console-consumer --zookeeper kafka0.cidev.sas.us:2181 --topic web-events --from-beginning --property schema.registry.url=http://kafka0.cidev.sas.us:8081
     *  
	 */
	private void publishMessages() {
		String topic = "web-events";
		Properties props = getKafkaProducerProps();
		Producer<String, WebEvent> producer = new KafkaProducer<String, WebEvent>(props);
		
		int nrecords = 5;
		RecordGenerator rg = new RecordGenerator();
		List<WebEvent> eventList = rg.getWebEventList(nrecords);
		for (WebEvent webEvent: eventList) {
			ProducerRecord<String, WebEvent> record = new ProducerRecord<>(topic, webEvent.getTenant(), webEvent);
			producer.send(record);
		}
		producer.close();
	}

}
