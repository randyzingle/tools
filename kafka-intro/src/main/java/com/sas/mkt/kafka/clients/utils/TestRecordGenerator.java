package com.sas.mkt.kafka.clients.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sas.mkt.kafka.domain.TestEvent;

public class TestRecordGenerator {
    
    public List<TestEvent> getTestEventList(int nrecords) {
    	
    	Random rnd = new Random();
    	List<TestEvent> elist = new ArrayList<TestEvent>(nrecords);
    	int seq = GetSequence.getSequence();
    	for (int i=0; i<nrecords; i++) {
    		long time = System.currentTimeMillis();
    		String site = "www.razing.com";
    		String ip = "192.168.2." + rnd.nextInt(255);
    		String tenant = null;
    		int nexti = rnd.nextInt(5);
    		if (nexti == 0) tenant = "baldursoft"+rnd.nextInt(50);
    		if (nexti == 1) tenant = "mymir-ai"+rnd.nextInt(50);
    		if (nexti == 2) tenant = "butters"+rnd.nextInt(50);
    		if (nexti == 3) tenant = "gabriel"+rnd.nextInt(50);
    		if (nexti == 4) tenant = "rafael"+rnd.nextInt(50);
    		TestEvent.Builder bdr = TestEvent.newBuilder();
    		bdr.setIp(ip).setSite(site).setTime(time);
    		// mimic mixing old and new data sources where tenant is a new field and has a default value
    		if (tenant != null) bdr.setTenant(tenant);
    		if (seq != 0) bdr.setSequence(seq);
    		seq++;
    		elist.add(bdr.build());
    	}
    	GetSequence.setSequence(seq);
    	
    	return elist;
    }

}