package com.bms.finnr.config.configserver;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.bms.finnr.config.ApplicationConfiguration;
import com.bms.finnr.config.GlobalConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class ConfigServerClient {

	@Autowired
	public ApplicationConfiguration appProps;
	
	@Autowired
	public GlobalConfiguration globalProps;
	
	// this needs to be the application name
	String auditComponent = "baldur";

	public static void main(String[] args) {
		String tierName = "razing";
		String componentName = "mkt-events";	
		String name = "";
		String value = null;
			
		ConfigServerClient cc = new ConfigServerClient();
		cc.appProps = new ApplicationConfiguration();
		cc.globalProps = new GlobalConfiguration();
		cc.globalProps.setConfigServiceUrl("http://configservice-dev.cidev.sas.us:8080/");
		
		// create a new property
		name = "kafkaTopicPrefix";
		value = "bar-configServer";
		cc.createSomeProps(tierName, componentName, name, value);
		
		// read the property back from the CS and print it out
		cc.readSomeProps(tierName, componentName, name, value);
		
		// update it with some random # added to the value
		cc.updateSomeProps(tierName, componentName, name, value);
		
//		// delete the property
		ConfigProperty cp = cc.getProperties(tierName, componentName, name, value).get(0);
		cc.deleteProperty(cp);
		
	}
	
	/*
	 * TEST METHODS
	 */
	private void createSomeProps(String tierName, String componentName, String name, String value) {
		ConfigProperty cps = new ConfigProperty();
		cps.setTierNm(tierName);
		cps.setComponentNm(componentName);
		cps.setName(name);
		cps.setValue(value);
		List<ConfigProperty> properties = new ArrayList<ConfigProperty>();
		properties.add(cps);
		createProperties(properties);
		readSomeProps(tierName, componentName, name, value);
	}	
	private void updateSomeProps(String tierName, String componentName, String name, String value) {
		List<ConfigProperty> props = getProperties(tierName, componentName, name, value);
		 
		if (props != null && props.size() == 1) {
			ConfigProperty cps = props.get(0);
			cps.setValue(cps.getValue() + Math.random());
			updateProperty(cps);
		}
	}	
	private void readSomeProps(String tierName, String componentName, String name, String value) {
		long startTime = System.currentTimeMillis();
		List<ConfigProperty> props = getProperties(tierName, componentName, name, value);
		long endTime = System.currentTimeMillis();
		System.out.println("HTTP query time: " + (endTime - startTime) + " ms");
		int count=0;
		for (ConfigProperty cp : props) {
			count++;
			System.out.println(count + ": " + cp);
		}
		System.out.println();
		printIdUrl(props);
	}
	// END TEST METHODS
	
	/*
	 * Will throw a 404 not found if the ID can't be found
	 */
	public void deleteProperty(ConfigProperty cp) throws RestClientException {
		RestTemplate restTemplate = new RestTemplate();
		String url = globalProps.getConfigServiceUrl() + "configproperties/" + cp.getId();
		HttpHeaders headers = new HttpHeaders();
		headers.set("AuditComponent", auditComponent);
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
		System.out.println(responseEntity);
		
	}
	
	public void updateProperty(ConfigProperty cps) throws RestClientException {
		RestTemplate restTemplate = new RestTemplate();
		String url = globalProps.getConfigServiceUrl() + "configproperties/" + cps.getId();
		System.out.println(url);
		HttpHeaders headers = new HttpHeaders();
		headers.set("AuditComponent", auditComponent);
		headers.set("Content-Type", ConfigProperty.MEDIA_TYPE_JSON_VALUE);
		
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String json = objectMapper.writeValueAsString(cps);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		
		HttpEntity<ConfigProperty> request = new HttpEntity<ConfigProperty>(cps, headers);
		restTemplate.put(url, request, ConfigProperty.class);
	}
	
	/*
	 * Create a new property in the config server
	 * Will throw a 400 bad request if the property already exists 
	 * Will throw (unfortunately) a 500 internal server error if the property is malformed
	 */
	public ConfigProperty createProperty(String tierName, String componentName, String name, String value) throws RestClientException {
		ConfigProperty cps = new ConfigProperty(tierName, componentName, name, value);
		List<ConfigProperty> props = new ArrayList<>();
		props.add(cps);
		props = createProperties(props);
		if (props != null && props.size() == 1) {
			return props.get(0);
		} else {
			return null;
		}
	}

	public List<ConfigProperty> createProperties(List<ConfigProperty> properties) throws RestClientException {
		RestTemplate restTemplate = new RestTemplate();
		String url = globalProps.getConfigServiceUrl() + "configproperties";
		HttpHeaders headers = new HttpHeaders();
		headers.set("AuditComponent", auditComponent);
		headers.set("Content-Type", ResourceCollection.MEDIA_TYPE_JSON_VALUE);
		
		ResourceCollection<ConfigProperty> collection = new ResourceCollection<ConfigProperty>();
		collection.name = ResourceCollection.COLLECTION_NAME;
		collection.accept = ResourceCollection.MEDIA_TYPE_BASE_VALUE;
		collection.items = properties;
		
		HttpEntity<ResourceCollection<ConfigProperty>> request = new HttpEntity<ResourceCollection<ConfigProperty>>(collection, headers);
		ResourceCollection<ConfigProperty> response = restTemplate.postForObject(url, request, ResourceCollection.class);
		List<ConfigProperty> props = response.items;
		return props;
	}
	
	public List<ConfigProperty> getProperties(String tierName, String componentName, String name, String value) throws RestClientException {
		ConfigProperty cps = new ConfigProperty(tierName, componentName, name, null);
		List<ConfigProperty> props = getProperties(cps);
		return props;
	}
	
	public List<ConfigProperty> getProperties(ConfigProperty config) throws RestClientException {
		// note all the RestTemplate stuff throws the RuntimeException: org.springframework.web.client.RestClientException
		RestTemplate restTemplate = new RestTemplate();
		String name = config.getName() == null ? "" : config.getName();
		String value = config.getValue() == null ? "" : config.getValue();
		String url = globalProps.getConfigServiceUrl() + "configproperties?tierNm=" + config.getTierNm() + "&componentNm=" 
				+ config.getComponentNm() + "&name=" + name + "&value=" + value + "&limit=500";
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("AuditComponent", auditComponent);
		headers.set("Accept", "application/json");
		
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

		ResponseEntity<ResourceCollection<ConfigProperty>> responseEntity = null;
		List<ConfigProperty> props = null;

		responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
				new ParameterizedTypeReference<ResourceCollection<ConfigProperty>>() {
				});
		ResourceCollection<ConfigProperty> collection = responseEntity.getBody();
		System.out.println("Found " + collection.count + " items.");
		props = collection.items;

		return props;
	}
	
	private void printIdUrl(List<ConfigProperty> props) {
		for (ConfigProperty cp : props) {
			System.out.printf("%s %s %s %n", cp.getId(), cp.getName(), cp.getValue());
		}
	}

}
