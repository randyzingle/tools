package com.bms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bms.quest.BraveKnight;
import com.bms.quest.Knight;
import com.bms.quest.Minstrel;
import com.bms.quest.Quest;
import com.bms.quest.SlayDragonQuest;

@Configuration
public class KnightConfig {
	
	@Bean
	public Knight knight() {
		return new BraveKnight(quest());
	}
	
	@Bean 
	public Quest quest() {
		return new SlayDragonQuest(System.out);
	}
	
	@Bean Minstrel minstrel() {
		return new Minstrel(System.out);
	}

}
