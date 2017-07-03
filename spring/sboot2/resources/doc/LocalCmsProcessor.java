package com.sas.mkt.apigw.sdk.streaming.agent.listener.thirdparty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sas.mkt.apigw.sdk.streaming.agent.cms.CMSInterface;

/**
 *
 * @author ranune
 *
 */
public class LocalCmsProcessor implements CMSInterface {
	private final static Logger logger = LoggerFactory.getLogger(LocalCmsProcessor.class);
	
	public LocalCmsProcessor () {
		
	}

	@Override
	public String getDataFromCMS(String path, String queryParam) {
		String cmsJson = null;
        String urlValue = "http://d78618.na.sas.com:8181/rama.web.app/rest/assets?id=4";
        if(path != null) {
        	urlValue +="&path="+path;
        }
        if(queryParam != null) {
        	urlValue +="&q="+queryParam;
        }
        try {
			URL url = new URL(urlValue);
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
	        urlc.setDoOutput(true);
	        urlc.setRequestMethod("GET");
	        urlc.setAllowUserInteraction(false);
	        StringBuffer sb = new StringBuffer();
	        BufferedReader br = new BufferedReader(new InputStreamReader(urlc //I18NOK:IOE
	                .getInputStream()));
	            String l = null;
	            while ((l=br.readLine())!=null) {
	                sb.append(l);
	            }
	            br.close();
	       cmsJson = sb.toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cmsJson;
	}

	@Override
	public String convertDataToSAS360(String json) {
		return json;
	}

}

