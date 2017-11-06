package com.bms.finnr.config.configserver;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ConfigServerClient {

    private final static Logger logger = LoggerFactory.getLogger(ConfigServerClient.class);

    @Autowired
    public ApplicationConfiguration appConfig;

    /*
     * Will throw a 404 not found if the ID can't be found
     */
    public void deleteProperty(ConfigProperty cp) throws RestClientException {
        String auditComponent = appConfig.getComponentName();
        RestTemplate restTemplate = new RestTemplate();
        String url = appConfig.getConfigServiceUrl() + "configproperties/" + cp.getId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("AuditComponent", auditComponent);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        logger.debug(responseEntity.getBody());

    }

    public void updateProperty(ConfigProperty cps) throws RestClientException {
        String auditComponent = appConfig.getComponentName();
        RestTemplate restTemplate = new RestTemplate();
        String url = appConfig.getConfigServiceUrl() + "configproperties/" + cps.getId();
        System.out.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("AuditComponent", auditComponent);
        headers.set("Content-Type", ConfigProperty.MEDIA_TYPE_JSON_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(cps);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }

        HttpEntity<ConfigProperty> request = new HttpEntity<ConfigProperty>(cps, headers);
        restTemplate.put(url, request, ConfigProperty.class);
    }

    /*
     * Create a new property in the config server Will throw a 400 bad request if the property already exists Will throw (unfortunately) a 500 internal server
     * error if the property is malformed
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
        String auditComponent = appConfig.getComponentName();
        RestTemplate restTemplate = new RestTemplate();
        String url = appConfig.getConfigServiceUrl() + "configproperties";
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
        String auditComponent = appConfig.getComponentName();
        // note all the RestTemplate stuff throws the RuntimeException: org.springframework.web.client.RestClientException
        RestTemplate restTemplate = new RestTemplate();
        String name = config.getName() == null ? "" : config.getName();
        String value = config.getValue() == null ? "" : config.getValue();
        String url = appConfig.getConfigServiceUrl() + "configproperties?tierNm=" + config.getTierNm() + "&componentNm=" + config.getComponentNm() + "&name="
                + name + "&value=" + value + "&limit=500";

        HttpHeaders headers = new HttpHeaders();
        headers.set("AuditComponent", auditComponent);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        ResponseEntity<ResourceCollection<ConfigProperty>> responseEntity = null;
        List<ConfigProperty> props = null;

        responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<ResourceCollection<ConfigProperty>>() {});
        ResourceCollection<ConfigProperty> collection = responseEntity.getBody();
        props = collection.items;

        return props;
    }

    public void printPropertyIds(List<ConfigProperty> props) {
        for (ConfigProperty cp : props) {
            logger.debug("{} {} {}", cp.getId(), cp.getName(), cp.getValue());
        }
    }

}
