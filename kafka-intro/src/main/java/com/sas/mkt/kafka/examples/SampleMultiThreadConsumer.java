package com.sas.mkt.kafka.examples;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sas.mkt.kafka.clients.utils.PostgreSQLWriter;

public class SampleMultiThreadConsumer {

	private String topic = "test-baldur1";
	private String groupID = "baldur2";
	
	public static void main(String[] args) {
		SampleMultiThreadConsumer bc = new SampleMultiThreadConsumer();
		try {
			bc.threadedConsumer();
		} catch (Exception ex) {
			// don't care
		}
	}
	
	private void threadedConsumer() throws Exception {
		int nconsumers = 5;
		ExecutorService pool = Executors.newFixedThreadPool(nconsumers);
		ArrayList<SampleConsumerThread> threadList = new ArrayList<>(nconsumers);
		Class<?> processorClass = PostgreSQLWriter.class;
		for (int i=0; i<nconsumers; i++) {
			// this will create and register the consumer
			SampleConsumerThread sct = new SampleConsumerThread(topic, groupID, processorClass);
			threadList.add(sct);
			pool.submit(sct);
		}
		
		// we want to make sure the consumers notify the brokers that they are leaving
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown(threadList, pool);
			}
		});
	
		// This is only used in sample code - real code will run until application shutdown
		Thread.sleep(10000);
		System.exit(0);
		// END This is only used in sample code ...
	}
	
	private void shutdown(ArrayList<SampleConsumerThread> threadList, ExecutorService pool) {
		for (SampleConsumerThread sct: threadList) {
			sct.shutdown();
		}
		pool.shutdown();
	}
}
