package com.bms.quest;

import java.io.PrintStream;

public class Minstrel {
	
	private PrintStream stream;
	
	public Minstrel(PrintStream stream) {
		this.stream = stream;
	}

	public void singBeforeQuest() {
		stream.println("The brave knight begins his quest!");
	}
	
	public void singAfterQuest() {
		stream.println("The brave knight bravely runs away!");
	}

}
