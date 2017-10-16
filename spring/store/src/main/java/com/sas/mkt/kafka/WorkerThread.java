package com.sas.mkt.kafka;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class WorkerThread implements Runnable {

	private int nx;
	private int n = 50000;
//	private int n = 5;

	public WorkerThread(int nx) {
		this.nx = nx;
	}

	@Override
	public void run() {
		try {
			String s = "hello there this is a short string. Once upon a time in a land far foar away there were three little pigs that didn't like wolves.";
			GZIPInputStream gs = null;
			ByteArrayOutputStream bs = null;
			GZIPOutputStream go = null;
			for (int i = 0; i < nx; i++) {
				System.out.println(Thread.currentThread().getName() + ":" + i);
				for (int j = 0; j < n; j++) {	
					bs = new ByteArrayOutputStream();
					go = new GZIPOutputStream(bs, true);
					byte[] b = s.getBytes();
					go.write(b, 0, b.length);
					go.flush();
					byte[] zb = bs.toByteArray();
					gs = new GZIPInputStream(new ByteArrayInputStream(zb));
					byte[] uz = new byte[b.length];
					gs.read(uz);
					String so = new String(uz);
//					System.out.println(s);
					gs.close();
					go.close();
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
