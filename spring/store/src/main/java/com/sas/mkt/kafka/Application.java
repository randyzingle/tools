package com.sas.mkt.kafka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.sas.mkt.kafka.performance.domain.FlatRun;
import com.sas.mkt.kafka.performance.domain.FlatRunRepository;
import com.sas.mkt.kafka.performance.domain.ProducerPerformanceRun;
import com.sas.mkt.kafka.performance.domain.ProducerPerformanceRunRepository;
import com.sas.mkt.kafka.performance.domain.ProducerProperty;

@SpringBootApplication
@EnableCaching
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);
	
	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(Application.class);

	}
	
	@Bean CommandLineRunner run1() {
		System.out.println("RUN22");
		return null;
	}
	
	@Bean
	public CommandLineRunner demo(ProducerPerformanceRunRepository prr, FlatRunRepository frr) {
		return (args) -> {
			// Write to file system and database
			String fileName = "src/main/resources/run.txt";
			FileWriter fw = new FileWriter(new File(fileName), false);
			BufferedWriter bw = new BufferedWriter(fw);
			log.info("READ FROM DATABASE *************************");
			List<ProducerPerformanceRun> runList = prr.findAll();
			String title = "runName, messagesPerSec, kbytesPerSec, status, instanceType, numberRecordsAttempted, percentMessagesReceived, "
					+"averageRecordSizeBytes, numberKeys, numberPartitions, numberProcessors, retries, compression.type, acks, batch.size, "
					+"linger.ms, max.in.flight.requests.per.connection";
			bw.write(title);
			bw.newLine();
			for (ProducerPerformanceRun run: runList) {	
				String s = getDbString(run);
				System.out.println(s);
				bw.write(s);
				bw.newLine();
				FlatRun flatRun = getFlatRun(run);
				frr.save(flatRun);
			}
			frr.flush();
			bw.flush();
			bw.close();
		};
	}
	
	private FlatRun getFlatRun(ProducerPerformanceRun run) {
		FlatRun fr = new FlatRun();
		fr.setRunName(run.getRunName());
		fr.setMessagesPerSec(run.getMessagesPerSec());
		fr.setKbytesPerSec(run.getKbytesPerSec());
		fr.setStatus(run.getStatus());
		fr.setInstanceType(run.getInstanceType());
		fr.setNumberRecordsAttempted(run.getNumberRecordsAttempted());
		fr.setPercentMessagesReceived(run.getPercentMessagesReceived());
		fr.setAverageRecordSizeBytes(run.getAverageRecordSizeBytes());
		fr.setNumberKeys(run.getNumberKeys());
		fr.setNumberPartitions(run.getNumberPartitions());
		fr.setNumberProcessors(run.getNumberProcessors());
		HashMap<String, String> propmap = new HashMap<>(5);
		Set<ProducerProperty> ppset = run.getProducerProperties();
		for (ProducerProperty pp: ppset) {
			propmap.put(pp.getName(), pp.getValue());
		}
		fr.setRetries(propmap.get("retries"));
		fr.setCompressionType(propmap.get("compression.type"));
		fr.setAcks(propmap.get("acks"));
		fr.setBatchSize(propmap.get("batch.size"));
		fr.setLingerMs(propmap.get("linger.ms"));
		fr.setMaxInFlightRequestsPerConnection(propmap.get("max.in.flight.requests.per.connection"));
		return fr;
	}
	
	private String getDbString(ProducerPerformanceRun run) {
		HashMap<String, String> propmap = new HashMap<>(5);
		StringBuffer s = new StringBuffer();
		s.append(run.getRunName()).append(",");
		s.append(run.getMessagesPerSec()).append(",");
		s.append(run.getKbytesPerSec()).append(",");
		s.append(run.getStatus()).append(",");
		s.append(run.getInstanceType()).append(",");
		s.append(run.getNumberRecordsAttempted()).append(",");
		s.append(run.getPercentMessagesReceived()).append(",");
		s.append(run.getAverageRecordSizeBytes()).append(",");
		s.append(run.getNumberKeys()).append(",");
		s.append(run.getNumberPartitions()).append(",");
		s.append(run.getNumberProcessors()).append(",");
		Set<ProducerProperty> ppset = run.getProducerProperties();
		for (ProducerProperty pp: ppset) {
			propmap.put(pp.getName(), pp.getValue());
		}
		s.append(propmap.get("retries")).append(",");
		s.append(propmap.get("compression.type")).append(",");
		s.append(propmap.get("acks")).append(",");
		s.append(propmap.get("batch.size")).append(",");
		s.append(propmap.get("linger.ms")).append(",");
		s.append(propmap.get("max.in.flight.requests.per.connection")).append(",");
		return s.toString();
	}

//	@Bean
//	public CommandLineRunner demo(CustomerRepository repository) {
//		return (args) -> {
//			// save a couple of customers
//			repository.save(new Customer("Jack", "Bauer"));
//			repository.save(new Customer("Chloe", "O'Brian"));
//			repository.save(new Customer("Kim", "Bauer"));
//			repository.save(new Customer("David", "Palmer"));
//			repository.save(new Customer("Michelle", "Dessler"));
//
//			// fetch all customers
//			log.info("Customers found with findAll():");
//			log.info("-------------------------------");
//			for (Customer customer : repository.findAll()) {
//				log.info(customer.toString());
//			}
//			log.info("");
//
//			// fetch an individual customer by ID
//			Customer customer = repository.findOne(1L);
//			log.info("Customer found with findOne(1L):");
//			log.info("--------------------------------");
//			log.info(customer.toString());
//			log.info("");
//
//			// fetch customers by last name
//			log.info("Customer found with findByLastName('Bauer'):");
//			log.info("--------------------------------------------");
//			for (Customer bauer : repository.findByLastName("Bauer")) {
//				log.info(bauer.toString());
//			}
//			log.info("");
//		};
//	}

}
