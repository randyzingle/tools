package com.bms.base.time;

import java.time.Instant;

public class DateTimeJava8 {

	public static void main(String[] args) {
		DateTimeJava8 dt = new DateTimeJava8();
		dt.simpleNow();
	}

	private void simpleNow() {
		Instant now = Instant.now();
		System.out.println("Time right now, in GMT, down to the millisecond");
		System.out.println(now);
		System.out.println("Time zero in Unix-land");
		System.out.println(Instant.EPOCH);
		System.out.println("max and min time values");
		System.out.println(Instant.MAX);
		System.out.println(Instant.MIN);
		
	}

}
