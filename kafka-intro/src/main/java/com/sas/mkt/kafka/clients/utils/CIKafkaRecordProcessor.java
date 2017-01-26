package com.sas.mkt.kafka.clients.utils;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface CIKafkaRecordProcessor extends AutoCloseable{

	public void processRecord(ConsumerRecord<String, SpecificRecordBase> record);
	
}
