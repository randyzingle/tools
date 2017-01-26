package com.sas.mkt.kafka.examples;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.log4j.Logger;

import com.sas.mkt.kafka.clients.utils.CIKafkaRecordProcessor;
import com.sas.mkt.kafka.clients.utils.KafkaUtilityService;

public class SampleConsumerThread implements Runnable {
	
	private Logger logger = Logger.getLogger(this.getClass());
	private KafkaConsumer<String, SpecificRecordBase> consumer;
	
	private CIKafkaRecordProcessor recordProcessor;
	
	public SampleConsumerThread(String topic, String groupID, Class<?> processorClass) {
		this.consumer = KafkaUtilityService.getKafkaConsumer(topic, groupID);
		try {
			this.recordProcessor = (CIKafkaRecordProcessor)processorClass.newInstance();
		} catch (InstantiationException|IllegalAccessException ex) {
			logger.warn(ex.toString());
		}
	}

	@Override
	public void run() {
		try {
			while(true) {
				ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(1000);
				for(ConsumerRecord<String, SpecificRecordBase> record: records) {
					recordProcessor.processRecord(record);
				}
			}
		} catch (WakeupException wex) {
			logger.info("shutting down " + Thread.currentThread().getName());
		} finally {
			consumer.close();
			try {
				recordProcessor.close();
			} catch (Exception ex) {logger.warn(ex.toString());}
		}
	}
	
	public void shutdown() {	
		consumer.wakeup();
	}

}
