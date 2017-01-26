package com.sas.mkt.kafka.clients.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetSequence {
	
	private static String fileName = "build/temp-seq.txt";

	public static void main(String[] args) {
		System.out.println(getSequence());
	}
	
	public static int getSequence() {
		int seq = 0;
		// create new file if this is the first run and set seq to 0
		if (Files.notExists(Paths.get(fileName))){
			setSequence(seq);
		}
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(fileName));
			String s = null;
			while( (s=buff.readLine()) != null) {
				seq = Integer.parseInt(s.trim());
			}
			buff.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return seq;
	}
	
	public static void setSequence(int seq) {
		try {
			PrintWriter out = new PrintWriter(fileName);
			out.print(seq);
			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
