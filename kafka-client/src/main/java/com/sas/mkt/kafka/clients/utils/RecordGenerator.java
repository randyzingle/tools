package com.sas.mkt.kafka.clients.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

public class RecordGenerator {
	
    private String WEBSCHEMA = "{\"namespace\": \"example.avro\", \"type\": \"record\", " +
            "\"name\": \"pageviews\"," +
            "\"fields\": [" +
             "{\"name\": \"time\", \"type\": \"long\"}," +
             "{\"name\": \"site\", \"type\": \"string\"}," +
             "{\"name\": \"ip\", \"type\": \"string\"}" +
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
    		recordList.add(pageview);
    	}
    	return recordList;
    }

}
