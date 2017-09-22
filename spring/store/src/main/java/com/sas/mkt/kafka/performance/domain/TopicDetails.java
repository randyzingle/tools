package com.sas.mkt.kafka.performance.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;

import com.sas.mkt.kafka.clients.utils.KafkaConnectionUtils;

public class TopicDetails {
	
	private boolean isInternal;
	private String name;
	private long numberPartions;
	private long replicationFactor;

	private TopicDetails() {
	};

	public static TopicDetails getTopicDetails(String configServiceURL, String topic)  {
		TopicDetails topicDetails = new TopicDetails();
		try {
			KafkaConnectionUtils kcu = KafkaConnectionUtils.getInstance(configServiceURL);
			Properties props = kcu.getKafkaProducerProperties("admin");	
			AdminClient ac = AdminClient.create(props);
	
			Collection<String> topicNames = Collections.singletonList(topic);
			Map<String, TopicDescription> topicMap = ac.describeTopics(topicNames).all().get();
			if (topicMap.containsKey(topic)) {
				TopicDescription td = topicMap.get(topic);
	//			System.out.println("isInternal: " + td.isInternal());
	//			System.out.println("name: " + td.name());
				List<TopicPartitionInfo> tpi = td.partitions();
				int size = tpi.size();
	//			System.out.println("number partitions: " + size);
				int cnt = 0;
				for (TopicPartitionInfo t: tpi) {
					int partitionId = t.partition();
					int nrep = t.replicas().size();
					cnt += nrep;
	//					System.out.printf("%s %d %d%n", td.name(), partitionId, nrep);
				}
				int rep = cnt / size;
	//			System.out.println("replication factor: " + rep);
				topicDetails.isInternal = td.isInternal();
				topicDetails.name = td.name();
				topicDetails.numberPartions = size;
				topicDetails.replicationFactor = rep;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return topicDetails;
	}

	public boolean isInternal() {
		return isInternal;
	}

	public String getName() {
		return name;
	}

	public long getNumberPartions() {
		return numberPartions;
	}

	public long getReplicationFactor() {
		return replicationFactor;
	}

	@Override
	public String toString() {
		return "TopicDetails [isInternal=" + isInternal + ", name=" + name + ", numberPartions=" + numberPartions
				+ ", replicationFactor=" + replicationFactor + "]";
	}
	
	

}
