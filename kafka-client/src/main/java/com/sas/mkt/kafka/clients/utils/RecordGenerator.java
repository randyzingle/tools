package com.sas.mkt.kafka.clients.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import com.sas.mkt.kafka.domain.WebEvent;
import com.sas.mkt.kafka.domain.WebEvent.Builder;

public class RecordGenerator {
	
    private String WEBSCHEMA = "{\"namespace\": \"com.sas.mkt.kafka.domain\", \"type\": \"record\", " +
            "\"name\": \"WebEvent\"," +
            "\"fields\": [" +
             "{\"name\": \"time\", \"type\": \"long\"}," +
             "{\"name\": \"site\", \"type\": \"string\"}," +
             "{\"name\": \"ip\", \"type\": \"string\"}," +
             "{\"name\": \"tenant\", \"type\": \"string\", \"default\": \"ollivanders\"}" +
            "]}";
    
    public List<GenericRecord> getGenericRecordList(int nrecords) {
    	Schema.Parser parser = new Schema.Parser();
    	Schema schema = parser.parse(WEBSCHEMA);
    	
    	Random rnd = new Random();
    	ArrayList<GenericRecord> recordList = new ArrayList<>(nrecords);
    	for (int i=0; i<nrecords; i++) {
    		long rtime = System.currentTimeMillis();
    		String site = "www.razing.com";
    		String ip = "192.168.2." + rnd.nextInt(255);
    		GenericRecord pageview = new GenericData.Record(schema);
    		pageview.put("time", rtime);
    		pageview.put("site", site);
    		pageview.put("ip", ip);
    		if (rnd.nextBoolean()) {
    			pageview.put("tenant", "baldursoft");
    		} else {
    			pageview.put("tenant", "mymir-ai");
    		}
    		recordList.add(pageview);
    	}
    	return recordList;
    }
    
    public List<WebEvent> getWebEventList(int nrecords) {
    	
    	Random rnd = new Random();
    	List<WebEvent> elist = new ArrayList<WebEvent>(nrecords);
    	for (int i=0; i<nrecords; i++) {
    		long time = System.currentTimeMillis();
    		String site = "www.razing.com";
    		String ip = "192.168.2." + rnd.nextInt(255);
    		String tenant = null;
    		int nexti = rnd.nextInt(3);
    		if (nexti == 0) tenant = "baldursoft";
    		if (nexti == 1) tenant = "mymir-ai";
    		WebEvent.Builder bdr = WebEvent.newBuilder();
    		bdr.setIp(ip).setSite(site).setTime(time);
    		// mimic mixing old and new data sources where tenant is a new field and has a default value
    		if (tenant != null) bdr.setTenant(tenant);
    		elist.add(bdr.build());
    	}
    	
    	return elist;
    }

}
