package com.bms.base.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileIo {
	
	private String file = "src/main/resources/xanadu.txt";

	public static void main(String[] args) {
		System.out.println("Firing up FileIo");
		FileIo fio = new FileIo();
		fio.readWriteColeridgeAsBytes();
		fio.readOneByteAtATime();
	}

	/**
	 * Simple byte stream file IO. Read as bytes and write as bytes with no interpretation
	 * Here we're sucking in the entire file at once and writing it out at once
	 */
	private void readWriteColeridgeAsBytes() {
		try(FileInputStream fis = new FileInputStream(file);
				FileOutputStream fos = new FileOutputStream(file+".out")) {
			int available = fis.available();
			byte[] input = new byte[available];
			fis.read(input);
			fos.write(input);
			fos.flush();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}
	
	private void readOneByteAtATime() {
		try(FileInputStream fis = new FileInputStream(file)) {
			int data;
			while ((data = fis.read()) != -1) {
				// note ASCII characters are stored as one byte in java
				System.out.print((char)data);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}

}
