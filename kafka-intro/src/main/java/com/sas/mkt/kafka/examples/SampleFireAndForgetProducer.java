package com.sas.mkt.kafka.examples;

import java.util.List;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.BufferExhaustedException;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.log4j.Logger;

import com.sas.mkt.kafka.clients.utils.KafkaUtilityService;
import com.sas.mkt.kafka.clients.utils.TestRecordGenerator;
import com.sas.mkt.kafka.domain.TestEvent;

public class SampleFireAndForgetProducer {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private String topic = "test-baldur1";
	private String clientID = "baldur1";
	private static int batchSize = 1000;
	private static int nbatches = 10;
	
	public static void main(String[] args) {
		SampleFireAndForgetProducer bp = new SampleFireAndForgetProducer();
		bp.fireAndForget(nbatches);
	}

	/**
	 * This will send the TestEvent(s) weblist to the brokers and will ignore the returned RecordMetadata object
	 * so we won't know if the message was properly received by the broker. We will still get any exceptions that
	 * deal with failed attempts to send the message to the broker.
	 * 
	 * This is useful for messages of low importance as it gives us the highest throughput.
	 * 
	 * @param weblist The list of messages to be sent. The message class must be a subclass of SpecificRecordBase
	 */
	private void fireAndForget(int nbatches) {
		// get the Fire-And-Forget Producer from our utility class
		Producer<String, SpecificRecordBase> producer = KafkaUtilityService.getKafkaProducer(clientID);

		List<TestEvent> weblist = null;
		TestRecordGenerator trg = new TestRecordGenerator();
		
		// create the ProducerRecord and send ignoring the returned RecordMetadata
		int cnt = 0;
		for (int i=0; i<nbatches; i++) {
			weblist = trg.getTestEventList(batchSize);
			for (TestEvent we: weblist) {
				cnt++;
				ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, we.getTenant(), we);
				// note, send() actually places the message in a buffer which is read and sent by a separate thread
				try {
					producer.send(record);
				} catch (SerializationException|BufferExhaustedException|InterruptException ex) {
					ex.printStackTrace();
				} 
				if (cnt%batchSize==0) {
					logger.info("sent message " + cnt);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("********* Sent total # of messages: " + cnt);
		// give the buffer a chance to empty out
		long sleep = nbatches / 25L;
		logger.info("Sleeping for " + sleep + " msec to let buffers drain");
		try { Thread.sleep(sleep); } catch (Exception ex) {ex.printStackTrace();}
		producer.close();

	}
}
