package com.bms.base.lambda;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BasicLambda {
	
	private int nloops = 2;

	public static void main(String[] args) {
		BasicLambda bl = new BasicLambda();
		bl.runOldSchoolExecutable();
		bl.runLambdaExecutable();
		System.out.println("Done in Main");
	}

	/*
	 * Lambda expression is substituted for interface/object that has single method 
	 * such as Runnable's run() method
	 * () = list of parameters passed to the method (where the method here is the expected run() method)
	 * -> = separates parameters from method call
	 * performTask(header) = method that will be called by the generated Runnable.run() method
	 */
	private void runLambdaExecutable() {
		String header = "LAMBDASCHOOL";
		ExecutorService es = Executors.newCachedThreadPool();
		System.out.printf("%s: Kicking off new thread from thread: %s%n", header, Thread.currentThread().getName());
		for (int i=0; i<nloops; i++) {
			es.execute(() -> performTask(header));
		}
		es.shutdown();
	}

	private void runOldSchoolExecutable() {
		String header = "OLDSCHOOL";
		ExecutorService es = Executors.newCachedThreadPool();
		System.out.printf("%s: Kicking off new thread from thread: %s%n", header, Thread.currentThread().getName());
		for (int i=0; i<nloops; i++) {
			es.execute(new Runnable(){
				public void run() {
					performTask(header);
				}
			});
		}
		es.shutdown();
	}
	
	private void performTask(String header) {
		try {Thread.sleep(1000);}catch(Exception ex){ex.printStackTrace();}
		System.out.printf("%s: In Anonymous Runnable: hello from thread: %s%n", header, Thread.currentThread().getName());
	}

}
