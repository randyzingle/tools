package com.sas.mkt.kafka.clients;

import java.util.Properties;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;

public class GenericAvroProducer {
	
	String VALUE_TOPIC = "topicv";
	String KEY_VALUE_TOPIC = "topickv";

	public static void main(String[] args) {
		GenericAvroProducer gap = new GenericAvroProducer();	
		gap.sendValueOnlyMessages();
	}

	private Properties getKafkaProducerProps() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka0.cidev.sas.us:9092,kafka1.cidev.sas.us:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
		props.put("schema.registry.url", "http://kafka0.cidev.sas.us:8081");
		return props;
	}

	private void sendValueOnlyMessages() {
		Properties props = getKafkaProducerProps();
		Producer producer = new KafkaProducer(props);
		String userSchema = "{\"type\":\"record\"," +
                "\"name\":\"userrecord\"," +
                "\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";
		Schema.Parser parser = new Schema.Parser();
		Schema schema = parser.parse(userSchema);
		GenericRecord avroRecord = new GenericData.Record(schema);
		for (int i=0; i<5; i++) {
			avroRecord.put("f1", "value"+i);
			ProducerRecord<Object, Object> record = new ProducerRecord<>(VALUE_TOPIC, avroRecord);
			try {
				producer.send(record);
			} catch (SerializationException ex) {
				ex.printStackTrace();
			}
		}
		producer.close();
	}

}
