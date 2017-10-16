package com.sas.mkt.kafka;

public class NoiseThread implements Runnable {
	
	private boolean done = false;

	@Override
	public void run() {
		while (!done) {
			try {
				System.out.println("NOISE!!!!");
				Thread.sleep(1000);
			} catch (Exception ex) {
				done = true;
			}
		}
		
	}

}
