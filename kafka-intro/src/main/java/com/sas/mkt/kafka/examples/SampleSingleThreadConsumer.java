package com.sas.mkt.kafka.examples;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import com.sas.mkt.kafka.clients.utils.CIKafkaRecordProcessor;
import com.sas.mkt.kafka.clients.utils.KafkaUtilityService;
import com.sas.mkt.kafka.clients.utils.PostgreSQLWriter;

public class SampleSingleThreadConsumer {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private String topic = "test-baldur6";
	private String groupID = "baldur2";
	
	public static void main(String[] args) {
		SampleSingleThreadConsumer bc = new SampleSingleThreadConsumer();
		bc.simpleConsumeMessages();
	}
	
	private void simpleConsumeMessages() {
		try (CIKafkaRecordProcessor recordProcessor = new PostgreSQLWriter()) {
			// Set up the consumer
			KafkaConsumer<String, SpecificRecordBase> consumer = KafkaUtilityService.getKafkaConsumer(topic, groupID);
			
			// Consume Messages
			int cnt = 0, maxNumberRequests = 10;
			boolean done = false;
			while (!done) {
				cnt++;
				if (cnt > maxNumberRequests) 
					done = true;
				ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(1000);
				for (ConsumerRecord<String, SpecificRecordBase> record: records) {
					recordProcessor.processRecord(record);
				}
			}
			consumer.close();
		} catch (Exception ex) {logger.warn(ex.toString());};
	}
}