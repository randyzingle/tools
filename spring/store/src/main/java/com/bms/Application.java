package com.bms;

import java.sql.Timestamp;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sas.mkt.kafka.performance.domain.ProducerPerformanceRun;
import com.sas.mkt.kafka.performance.domain.ProducerPerformanceRunRepository;
import com.sas.mkt.kafka.performance.domain.ProducerProperty;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}
	
	@Bean
	public CommandLineRunner demo(ProducerPerformanceRunRepository prr) {
		return (args) -> {
			ProducerProperty pp1 = new ProducerProperty("compression", "snappy", "String");
			ProducerProperty pp2 = new ProducerProperty("linger.ms", "10", "String");
			ProducerProperty pp3 = new ProducerProperty("buffer.size", "16544", "String");
			ProducerPerformanceRun pr = new ProducerPerformanceRun("run1", new Timestamp(System.currentTimeMillis()), 1000L);
			pr.addProducerProperty(pp1);
			pr.addProducerProperty(pp2);
			pr.addProducerProperty(pp3);
			prr.save(pr);
			
			log.info("SAVED RUN TO DATABASE *********************");
			
			ObjectMapper mapper = new ObjectMapper();
			String s = mapper.writeValueAsString(pr);
			log.info(s);
			
			// pull out properties from json string
			ProducerPerformanceRun npr = mapper.readValue(s, ProducerPerformanceRun.class);
			Set<ProducerProperty> set = npr.getProducerProperties();
			for (ProducerProperty p: set) {
				System.out.println(p);
			}
			
			log.info("READ FROM DATABASE *************************");
			ProducerPerformanceRun dbrun = prr.findOne(pr.getRunName());
			log.info(dbrun.toString());
		};
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
