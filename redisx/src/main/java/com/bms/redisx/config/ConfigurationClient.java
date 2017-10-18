package com.bms.redisx.config;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ConfigurationClient {

	public static String configServiceUrl = "http://configservice-dev.cidev.sas.us:8080";

	public static void main(String[] args) {
		String tierName = "tier_global";
		String componentName = "";	
		String name = "";
//		String name = "kafkaCluster";
		
		String auditComponent = "baldur";
		ConfigurationClient cc = new ConfigurationClient();
		long startTime = System.currentTimeMillis();
		List<ConfigPropertyShort> props = cc.getPropertyFromConfigServer(tierName, componentName, name, auditComponent);
		long endTime = System.currentTimeMillis();
		System.out.println("HTTP query time: " + (endTime - startTime) + " ms");
		int count=0;
		for (ConfigPropertyShort cp : props) {
			count++;
			System.out.println(count + ": " + cp);
		}
		System.out.println();

//		cc.printDeleteUrls(props);
	}

	private void printDeleteUrls(List<ConfigPropertyShort> props) {
		for (ConfigPropertyShort cp : props) {
			ConfigLinks del = null;
			List<ConfigLinks> links = cp.getLinks();
			for (ConfigLinks cl : links) {
				if (cl.getMethod().equals("DELETE"))
					del = cl;
			}
			if (del != null) {
				String url = configServiceUrl + del.getUri();
				System.out.println(url + "  " + cp.getName());
			}
		}
	}

	public static List<ConfigPropertyShort> getPropertyFromConfigServer(String tierName, String componentName, String name, String auditComponent) {
		String url = configServiceUrl + "configproperties?tierNm=" + tierName + "&componentNm=" + componentName
				+ "&name=" + name + "&limit=500";
		RestTemplate rt = new RestTemplate();
		List<ConfigPropertyShort> props = null;

		HttpHeaders headers = new HttpHeaders();
		headers.set("AuditComponent", auditComponent);
		headers.set("Accept", "application/json");
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

		ResponseEntity<ResourceCollection<ConfigPropertyShort>> responseEntity = null;

		try {
			responseEntity = rt.exchange(url, HttpMethod.GET, entity,
					new ParameterizedTypeReference<ResourceCollection<ConfigPropertyShort>>() {
					});
			ResourceCollection<ConfigPropertyShort> collection = responseEntity.getBody();
			System.out.println("Found " + collection.count + " items.");
			props = collection.items;

		} catch (Exception rce) {
			rce.printStackTrace();
		}

		return props;
	}

	public <T> ResponseEntity<ResourceCollection<T>> doGet(String url, HttpHeaders headers,
			ParameterizedTypeReference<ResourceCollection<T>> responseType) {
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> headerEntity = new HttpEntity<>(headers);
		return restTemplate.exchange(url, HttpMethod.GET, headerEntity, responseType);
	}

}
