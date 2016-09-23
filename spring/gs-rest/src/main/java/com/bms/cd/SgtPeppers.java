package com.bms.cd;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
//beans are created as singletons by default - here request a new instance each time this is injected
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) 
@Qualifier("sgtPeppers")
public class SgtPeppers implements CompactDisc {
	
	private String title = "Sgt. Pepper's Lonely Hearts Club Band";
	private String artist = "The Beatles";
	
	private int nrequests = 0;

	public int getNrequests() {
		return nrequests;
	}

	public void setNrequests(int nrequests) {
		this.nrequests = nrequests;
	}

	@Override
	public void play() {
		System.out.println("Playing " + title + " by " + artist);
	}

}
