package com.sas.mkt.kafka.producer;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import com.sas.mkt.kafka.base.KafkaConfigServer;

public class MessageSender {
	
	private static Properties props = null;
	
	public static void main(String[] args) {
		MessageSender sender = new MessageSender();
		
		sender.sendTestMessages();
		
		
	}

	private void printMetrics(Producer producer) {
		Map<MetricName, ? extends Metric> metrics = producer.metrics();
		Set<MetricName> keyset = metrics.keySet();
		for (MetricName mn: keyset) {
			System.out.println(mn.name() + ": " + metrics.get(mn).value());
		}
	}

	private void sendTestMessages() {
		props = new KafkaConfigServer().getProducerConfig();
		Producer<String, String> producer = new KafkaProducer<>(props);
		for (int i=101; i<501; i++) {
			ProducerRecord<String, String> pr = new ProducerRecord<>("int-topic", Integer.toString(i), "hello " +Integer.toString(i));
			producer.send(pr);
		}
		this.printMetrics(producer);
		producer.close();
	}
}
