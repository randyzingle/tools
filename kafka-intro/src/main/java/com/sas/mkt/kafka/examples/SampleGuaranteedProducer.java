package com.sas.mkt.kafka.examples;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.*;

import com.sas.mkt.kafka.clients.utils.KafkaUtilityService;
import com.sas.mkt.kafka.clients.utils.TestRecordGenerator;
import com.sas.mkt.kafka.domain.TestEvent;

public class SampleGuaranteedProducer {
	
	private static final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private String topic = "test-baldur6";
	private String clientID = "baldur1";
	
	public static void main(String[] args) {
		SampleGuaranteedProducer bp = new SampleGuaranteedProducer();
		TestRecordGenerator werg = new TestRecordGenerator();
		bp.synchSend(werg.getTestEventList(5000));
	}

	/**
	 * Send the messages synchronously and wait for the response. Use only if message delivery is imperative.
	 * This is several orders of magnitude slower than fire-and-forget.
	 * 
	 * Messages may fail to be sent because of potentially transient reasons such as connection errors,
	 * or because Kafka is in the process of electing a new leader for the partition the message is being sent to. 
	 * The Producer returned from KafkaUtilityService.getKafkaProducer() is configured to retry sending these 
	 * messages automatically. 
	 * 
	 * If the automatic retries fail you'll have to deal with manually attempting to re-send the record
	 * in the catch block or storing it for further analysis.
	 *  
	 * @param weblist List of test events
	 */
	private void synchSend(List<TestEvent> weblist) {
		// get the Producer from our utility class
		logger.info("sending " + weblist.size() + " messages");
		Producer<String, SpecificRecordBase> producer = KafkaUtilityService.getGuaranteedOrderedKafkaProducer(clientID);
		
		int cnt = 0;
		// create the ProducerRecord and send ignoring the returned RecordMetadata
		for (TestEvent we: weblist) {
			cnt++;
			ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, we.getTenant(), we);

			// this will send the message and wait for the response from the Kafka Brokers
			try {
				// the producer is configured to automatically retry sending the record if it gets a potentially
				// transient error from the broker. Calling get will make it wait for an ACK before proceeding
				producer.send(record).get();
			} catch (ExecutionException|InterruptedException ex) {
				// deal with re-sending/storing the unsent record
				ex.printStackTrace();
			} 
			if (cnt%500==0) logger.info("sent message " + cnt);
		}
		producer.close();
	}

}
