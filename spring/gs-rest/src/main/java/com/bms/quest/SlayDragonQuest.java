package com.bms.quest;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;

public class SlayDragonQuest implements Quest {
	
	private PrintStream stream;
	
	public SlayDragonQuest(PrintStream stream) {
		this.stream = stream;
	}

	@Override
	public void embark() {
		stream.println("Embarking on a quest to slay a dragon!");
	}

}
