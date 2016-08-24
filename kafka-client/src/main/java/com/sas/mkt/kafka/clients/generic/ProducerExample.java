package com.sas.mkt.kafka.clients.generic;

import java.util.List;
import java.util.Properties;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;

import com.sas.mkt.kafka.clients.utils.RecordGenerator;

public class ProducerExample {
	
	public static void main(String[] args) {
		ProducerExample pex = new ProducerExample();	
		pex.sendMessages();
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
	
	/**
	 * Send nrecords messages to the topic "page-views" defined by the schema WEBSCHEMA in RecordGenerator.
	 * The ip address will be used as the key and the full record will be used as the value.
	 */
	private void sendMessages() {
		String topic = "page-views";
		Properties props = getKafkaProducerProps();
		Producer<String, GenericRecord> producer = new KafkaProducer<String, GenericRecord>(props);
		
		int nrecords = 5;
		RecordGenerator rg = new RecordGenerator();
		List<GenericRecord> recordList = rg.getGenericRecordList(nrecords);
		for (GenericRecord genericRecord : recordList) {
			ProducerRecord<String, GenericRecord> record = 
					new ProducerRecord<>(topic, genericRecord);
			System.out.println("sent message ...");
			try {
				producer.send(record);
			} catch (SerializationException ex) {
				ex.printStackTrace();
			}
		}
		
		producer.close();
	}

}
