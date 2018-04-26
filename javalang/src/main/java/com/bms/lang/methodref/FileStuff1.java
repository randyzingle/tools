package com.bms.lang.methodref;

import java.io.File;

public class FileStuff1 {

	public static void main(String[] args) {
		try {
			listFiles();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void listFiles() throws Exception {
		File[] files = new File(".").listFiles();
		System.out.println("All files...");
		for (File f: files) {
			String s = f.isDirectory() ? "dir" : "file";
			System.out.println(s + ": " + f.getName());
		}
		// pass in filtering criteria as a method ( :: method reference syntax = use this method as a value)
		// File::isHidden creates a method reference just like new X() creates an object reference
		// a lambda is just an un-named function reference (anonymous function)
		File[] hidden = new File(".").listFiles(File::isHidden);
		System.out.println("Hidden files...");
		for (File f: hidden) {
			String s = f.isDirectory() ? "dir" : "file";
			System.out.println(s + ": " + f.getName());
		}
	}

}
