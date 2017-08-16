package com.sas.mkt.kafka.clients.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.*;

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

public class KafkaUtilityService {

//	public static String DEFAULT_KAFKA_CLUSTER = "kafka-kafka1.cidev.sas.us:9092,kafka-kafka2.cidev.sas.us:9092,kafka-kafka3.cidev.sas.us:9092";
//	public static String DEFAULT_KAFKA_SCHEMA_REGISTRY = "http://kafka-schema-registry.cidev.sas.us";
	public static String DEFAULT_KAFKA_CLUSTER = "kafkass.cidev.sas.us:9092";
	public static String DEFAULT_KAFKA_SCHEMA_REGISTRY = "http://kafkass.cidev.sas.us:8081";
	public static String CLUSTER_KEY = "KAFKA_CLUSTER";
	public static String REGISTRY_KEY = "KAFKA_SCHEMA_REGISTRY";
	
	private static Logger logger = LoggerFactory.getLogger(KafkaUtilityService.class);
	
	public static long getBufferMemoryConfig() {
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heap = mbean.getHeapMemoryUsage();
		String s = String.format("Memory committed=%d, init=%d, max=%d, used=%d%n", heap.getCommitted(), heap.getInit(), heap.getMax(), heap.getUsed());
		logger.info(s);
		// we'll use 75% of the available heap for the producer's buffer
		// heap.getMax() can be undetermined in which case it will return -1
		double ab = (heap.getMax() - heap.getUsed()) * 0.75;
		long bufferSize = 33554432L; // default value for Kafka producer buffer
		if (ab > 0) bufferSize = Math.max((long)ab, bufferSize); // don't want the buffer too small if app is temporarily spiking mem
		logger.info(bufferSize/1.0e6 + " MBytes used for Kafka Producer Buffer");
		return bufferSize;
	}
	
	public static Properties getBaseKafkaProducerProperties() {
		Map<String, String> map = KafkaUtilityService.getEnv();
		String kafkaCluster = map.get(CLUSTER_KEY);
		String kafkaSchemaRegistry = map.get(REGISTRY_KEY);
		Properties props = new Properties();
		// Mandatory fields
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCluster);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				org.apache.kafka.common.serialization.StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				io.confluent.kafka.serializers.KafkaAvroSerializer.class);
		props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaSchemaRegistry);
		return props;
	}

	public static Properties getKafkaProducerProperties(String clientID, boolean waitForACK, int numRetries, int maxInflightRequests) {
		// Mandatory fields
		Properties props = getBaseKafkaProducerProperties();

		// Optional fields
		long bufferSize = KafkaUtilityService.getBufferMemoryConfig();
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferSize); // want the producer to use as much of the heap as possible
		props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // using default size
		props.put(ProducerConfig.CLIENT_ID_CONFIG, clientID); // used to log logical application name vs just IP/port
		props.put(ProducerConfig.RETRIES_CONFIG, numRetries); // will only be used with  retry-able errors
		props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInflightRequests);
		props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none"); // none, gzip, snappy, lz4
		if (waitForACK) {
			props.put(ProducerConfig.ACKS_CONFIG, "all");
		} else {
			props.put(ProducerConfig.ACKS_CONFIG, "0");
		}
		return props;
	}
	
	public static Properties getBaseKafkaConsumerProps() {
		Map<String, String> map = KafkaUtilityService.getEnv();
		String kafkaCluster = map.get(CLUSTER_KEY);
		String kafkaSchemaRegistry = map.get(REGISTRY_KEY);
		Properties props = new Properties();
		// Mandatory fields
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCluster);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				org.apache.kafka.common.serialization.StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
		props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
		props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaSchemaRegistry);
		return props;
	}

	public static Properties getKafkaConsumerProps() {
		// Mandatory fields
		Properties props = getBaseKafkaConsumerProps();
		// Optional fields
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		// if a heartbeat isn't sent within this time the consumer will be considered dead and will be removed from the group
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		return props;
	}

	public static Producer<String, SpecificRecordBase> getKafkaProducer(String clientID) {
		Properties props = KafkaUtilityService.getKafkaProducerProperties(clientID, false, 0, 5);
		Producer<String, SpecificRecordBase> producer = new KafkaProducer<>(props);
		return producer;
	}
	
	public static Producer<String, SpecificRecordBase> getGuaranteedOrderedKafkaProducer(String clientID) {
		Properties props = KafkaUtilityService.getKafkaProducerProperties(clientID, true, 2, 1);
		Producer<String, SpecificRecordBase> producer = new KafkaProducer<>(props);
		return producer;
	}

	public static KafkaConsumer<String, SpecificRecordBase> getKafkaConsumer(String topic, String groupID) {
		Properties props = KafkaUtilityService.getKafkaConsumerProps();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		KafkaConsumer<String, SpecificRecordBase> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList(topic));
		return consumer;
	}

	public static Map<String, String> getEnv() {
		// TODO we want to look 1) for property server via REST, 2) environment variables, 3) property file (hardcoded for now)
		Map<String, String> map = System.getenv();
		HashMap<String, String> hmap = new HashMap<>(2);
		if (!map.containsKey(CLUSTER_KEY)) {
			hmap.put(CLUSTER_KEY, DEFAULT_KAFKA_CLUSTER);
		} else {
			hmap.put(CLUSTER_KEY, map.get(CLUSTER_KEY));
		}
		if (!map.containsKey(REGISTRY_KEY)) {
			hmap.put(REGISTRY_KEY, DEFAULT_KAFKA_SCHEMA_REGISTRY);
		} else {
			hmap.put(REGISTRY_KEY, map.get(REGISTRY_KEY));
		}
		return hmap;
	}

}