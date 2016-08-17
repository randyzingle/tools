package com.sas.mkt.kafka.clients;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import com.sas.mkt.kafka.base.KafkaConfigServer;

public class MessageProducer {
	
	private static Properties props = null;
	
	public static void main(String[] args) {
		MessageProducer sender = new MessageProducer();
//		sender.sendTestMessages();
		sender.sendMessageWithCallback();
	}

	private void printMetrics(Producer<String, String> producer) {
		Map<MetricName, ? extends Metric> metrics = producer.metrics();
		Set<MetricName> keyset = metrics.keySet();
		for (MetricName mn: keyset) {
			System.out.println(mn.name() + ": " + metrics.get(mn).value());
		}
	}

	private void sendTestMessages() {
		props = new KafkaConfigServer().getProducerProperties();
		Producer<String, String> producer = new KafkaProducer<>(props);
		for (int i=101; i<501; i++) {
			ProducerRecord<String, String> pr = new ProducerRecord<>("int-topic", Integer.toString(i), "hello " +Integer.toString(i));
			// send returns a Java Future object with RecordMetadata -> we can see if the message was
			// sent successfully. If we want we can ignore this for fire and forget messages. 
			producer.send(pr);
//			Future<RecordMetadata> fr = producer.send(pr);
//			try {
//				RecordMetadata rmd = producer.send(pr).get(); // wait for ack
//			} catch (CancellationException | ExecutionException | InterruptedException ex) {
//				ex.printStackTrace();
//			}
		}
		this.printMetrics(producer);
		producer.close();
	}
	
	private void sendMessageWithCallback() {
		props = new KafkaConfigServer().getProducerProperties();
		Producer<String, String> producer = new KafkaProducer<>(props);
		for (int i=555; i<666; i++) {
			ProducerRecord<String, String> pr = new ProducerRecord<>("int-topic", Integer.toString(i), "hello " +Integer.toString(i));
			producer.send(pr, new SampleCallback());
		}
		producer.close();
	}
	
	private class SampleCallback implements Callback {
		@Override
		public void onCompletion(RecordMetadata metadata, Exception exception) {
			if (exception != null) {
				exception.printStackTrace();
				return;
			}
			System.out.println(metadata.topic() + " " + metadata.timestamp() + " " + metadata.offset() + " " + metadata.partition());
		}		
	}
		
	
}
