package com.bms.base;

public class MainApplication {

	public static void main(String[] args) {
		String name = "Nobody";
		if (args.length > 0) name = args[0];
		System.out.println("Hello from the base application: " + name);
		System.out.println("I was passed " + args.length + " cmd line args");
	}

}
