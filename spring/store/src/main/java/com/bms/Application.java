package com.bms;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.bms.domain.PerformanceRun;
import com.bms.domain.PerformanceRunRepository;
import com.bms.domain.ProducerProperties;
import com.bms.domain.ProducerPropertiesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}
	
	@Bean
	public CommandLineRunner demo(PerformanceRunRepository prr, ProducerPropertiesRepository ppr) {
		return (args) -> {
			ProducerProperties pp1 = new ProducerProperties("compression", "snappy");
			ProducerProperties pp2 = new ProducerProperties("linger.ms", "10");
			ProducerProperties pp3 = new ProducerProperties("buffer.size", "16544");
			PerformanceRun pr = new PerformanceRun("run1", new Timestamp(System.currentTimeMillis()), 1000L);
			Set<ProducerProperties> producerProperties = new HashSet<>();
			producerProperties.add(pp1);
			producerProperties.add(pp2);
			producerProperties.add(pp3);
			pr.setProducerProperties(producerProperties);
			prr.save(pr);
			
			ObjectMapper mapper = new ObjectMapper();
			String s = mapper.writeValueAsString(pr);
			log.info(s);

			log.info("");
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
