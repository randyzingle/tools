package com.bms.gateway.server.controllers;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ResourceCollection<T> implements Serializable {

	private static final long serialVersionUID = 1L;

    public static final String MEDIA_TYPE_JSON_VALUE = "application/vnd.sas.collection+json";
    public static final int SCHEMA_VERSION = 2;
    public static final String COLLECTION_NAME = "items";
    public static final int DEFAULT_LIMIT = 10;
 
    public String name = COLLECTION_NAME, accept;
 
    public Long start, count;
 
    public List<T> items;
 
    public Integer limit;

    public int version = SCHEMA_VERSION;

}
