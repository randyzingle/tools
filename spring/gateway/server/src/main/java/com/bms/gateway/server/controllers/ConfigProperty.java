package com.bms.gateway.server.controllers;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigProperty
{
    public static final String MEDIA_TYPE_BASE_VALUE = "application/vnd.sas.configuration.configproperty";
    public static final String MEDIA_TYPE_JSON_VALUE = "application/vnd.sas.configuration.configproperty+json";
    public static final String COLLECTION_NAME = "configproperties";
	public static final String AUDIT_COMPONENT_HEADER = "AuditComponent";
 
    public int version;
    public String id;
    public String name;
    public String value;
    public String description;
    public String date;
    public String tierId;
    public String tierNm;
    public String tierDesc;
    public String componentId;
    public String componentNm;
    public String componentDesc;
	@Override
	public String toString() {
		return "ConfigProperty [version=" + version + ", id=" + id + ", name=" + name + ", value=" + value
				+ ", description=" + description + ", date=" + date + ", tierId=" + tierId + ", tierNm=" + tierNm
				+ ", tierDesc=" + tierDesc + ", componentId=" + componentId + ", componentNm=" + componentNm
				+ ", componentDesc=" + componentDesc + "]";
	}
}

