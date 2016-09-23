package com.bms.cd;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SteelWheels implements CompactDisc {
	
	private String title = "Steel Wheels";
	private String artist = "The Rolling Stones";

	@Override
	public void play() {
		System.out.println("Playing " + title + " by " + artist);
	}

}
