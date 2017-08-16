package com.sas.mkt.kafka.examples;

import java.util.List;
import java.util.Map;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.slf4j.*;

import com.sas.mkt.kafka.clients.utils.KafkaUtilityService;
import com.sas.mkt.kafka.clients.utils.TestRecordGenerator;
import com.sas.mkt.kafka.domain.TestEvent;

public class SampleAsynchProducer {
	
	private static final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private String topic = "test-baldur6";
	private String clientID = "baldur1";
	
	public static void main(String[] args) {
		SampleAsynchProducer bp = new SampleAsynchProducer();
		TestRecordGenerator werg = new TestRecordGenerator();
		bp.asynchSendWithCallback(werg.getTestEventList(5000));
	}

	/**
	 * Send the messages asynchronously for high throughput but register callback so that you can determine
	 * if there were errors and log them. Kafka will still use the built-in retry if you set ackAndRetry=true
	 * when creating the Producer.
	 * 
	 * @param testEventList List of test events
	 */
	private void asynchSendWithCallback(List<TestEvent> testEventList) {
		// get the producer from our utility class
		Producer<String, SpecificRecordBase> producer = KafkaUtilityService.getKafkaProducer(clientID);
		
		// Send the messages logging acks / errors with a callback
		for(TestEvent testEvent: testEventList) {
			ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, testEvent.getTenant(), testEvent);
			// send record with registered callback
			producer.send(record, new TestAsynchCallback());
		}
		Map<MetricName,? extends Metric> metrics = producer.metrics();
		// give the buffer a chance to empty out
		try { Thread.sleep(2000); } catch (Exception ex) {ex.printStackTrace();}
		producer.close();
	}
	
	private class TestAsynchCallback implements Callback {
		@Override
		public void onCompletion(RecordMetadata rmd, Exception ex) {
			// ex will be null unless Kafka didn't store the record
			if (ex != null) {
				// there was an error that couldn't be dealt with using the built-in retry
				// do something here ...
				String s = String.format("error in topic: %s, partition: %s, at offset: %s%n", 
						rmd.topic(), rmd.partition(), rmd.offset());
				logger.info(s);
			}
		}
	}

}
