package com.bms.redisx.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigPropertyShort
{
    public static final String MEDIA_TYPE_BASE_VALUE = "application/vnd.sas.configuration.configproperty";
    public static final String MEDIA_TYPE_JSON_VALUE = "application/vnd.sas.configuration.configproperty+json";
    public static final String COLLECTION_NAME = "configproperties";
    
    private String componentNm;
    private String tierNm;
    private String name;
    private String value;
    private List<ConfigLinks> links;
    
	public List<ConfigLinks> getLinks() {
		return links;
	}
	public void setLinks(List<ConfigLinks> links) {
		this.links = links;
	}
	public String getComponentNm() {
		return componentNm;
	}
	public void setComponentNm(String componentNm) {
		this.componentNm = componentNm;
	}
	public String getTierNm() {
		return tierNm;
	}
	public void setTierNm(String tierNm) {
		this.tierNm = tierNm;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "ConfigPropertyShort [componentNm=" + componentNm + ", tierNm=" + tierNm + ", name=" + name + ", value="
				+ value + "]";
	}

	
 

    
    
}