package com.bms.finnr.config.configserver;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigProperty
{
	public static final String MEDIA_TYPE_BASE_VALUE = "application/vnd.sas.configuration.configproperty";
    public static final String MEDIA_TYPE_JSON_VALUE = "application/vnd.sas.configuration.configproperty+json";
    public static final String COLLECTION_NAME = "configproperties";
 
    public int version = 1;
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
    
	public ConfigProperty() {}
    
    public ConfigProperty(String tierNm, String componentNm, String name, String value) {
    		this.tierNm = tierNm;
    		this.componentNm = componentNm;
    		this.name = name;
    		this.value = value;
    }

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTierId() {
		return tierId;
	}

	public void setTierId(String tierId) {
		this.tierId = tierId;
	}

	public String getTierNm() {
		return tierNm;
	}

	public void setTierNm(String tierNm) {
		this.tierNm = tierNm;
	}

	public String getTierDesc() {
		return tierDesc;
	}

	public void setTierDesc(String tierDesc) {
		this.tierDesc = tierDesc;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getComponentNm() {
		return componentNm;
	}

	public void setComponentNm(String componentNm) {
		this.componentNm = componentNm;
	}

	public String getComponentDesc() {
		return componentDesc;
	}

	public void setComponentDesc(String componentDesc) {
		this.componentDesc = componentDesc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((componentNm == null) ? 0 : componentNm.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tierNm == null) ? 0 : tierNm.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigProperty other = (ConfigProperty) obj;
		if (componentNm == null) {
			if (other.componentNm != null)
				return false;
		} else if (!componentNm.equals(other.componentNm))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tierNm == null) {
			if (other.tierNm != null)
				return false;
		} else if (!tierNm.equals(other.tierNm))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConfigProperty [version=" + version + ", name=" + name + ", value=" + value + ", date=" + date
				+ ", tierNm=" + tierNm + ", componentNm=" + componentNm + "]";
	}
    
}