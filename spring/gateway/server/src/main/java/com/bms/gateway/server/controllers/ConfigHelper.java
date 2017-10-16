package com.bms.gateway.server.controllers;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ConfigHelper {
	Integer tenantId = 120816103;
	String tenantServiceUrl;
	String tierName;


	public String getTierNameFromTenantService(int tenantId) {
		String tenantName = null;
		tenantServiceUrl = "http://tenantservice-dev.cidev.sas.us:8080";
		String url = "%s/tenantService/tenants%s";
		String params = "?tenantId="+tenantId;
		url = String.format(url, tenantServiceUrl, params);
		//url = "http://configservice-dev.cidev.sas.us:8080/configproperties?tierNm=tier_global&componentNm=mkt-extapigw&name=sas.mkt.designcenter.external.agent.sdk.path";
		System.out.println(url);
		// use RestTemplate to grab the resource at the above URL
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.set(ConfigProperty.AUDIT_COMPONENT_HEADER, "baldursoft");
		headers.set("Accept", ResourceCollection.MEDIA_TYPE_JSON_VALUE);
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		
		ResponseEntity<ResourceCollection<ConfigProperty>> responseEntity = null;
		try {
			responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
					new ParameterizedTypeReference<ResourceCollection<ConfigProperty>>() {
            });
			ResourceCollection<ConfigProperty> rc = responseEntity.getBody();
			HttpHeaders h = responseEntity.getHeaders();
			List<ConfigProperty> lc = rc.items;
			System.out.println(rc.toString());
			System.out.println("++++++++++++++++++++++++++++++++++++++");
			lc.toString();
			System.out.println("++++++++++++++++++++++++++++++++++++++");
			System.out.println(h.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return tenantName;
	}
}
