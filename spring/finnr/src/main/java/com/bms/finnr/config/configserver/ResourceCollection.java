package com.bms.finnr.config.configserver;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ResourceCollection<T> implements Serializable
{
    /**
     * String representation of the base media type value; "application/vnd.sas.collection"
     */
    public static final String MEDIA_TYPE_BASE_VALUE = "application/vnd.sas.collection";
 
    /**
     * String representation of the base media type value; "application/vnd.sas.collection+json"
     */
    public static final String MEDIA_TYPE_JSON_VALUE = MEDIA_TYPE_BASE_VALUE + "+json";
 
    /**
     * The collection representation schema version.
     * Version 1 was Collection but without links, start, limit, count or explicit version
     * Version 2 is this ResourceCollection, the current version for collection resources,
     * which includes the properties links, start, limit, count, and version.
     */
    public static final int SCHEMA_VERSION = 2;
 
    /**
     * The default name for the collection items, "items"
     */
    public static final String COLLECTION_NAME = "items";
 
    /**
     * The default limit (or maximal sublist size) for a collection subset.  A value of 10 is used by default,
     * but services may specify a different value within the appropriate endpoint.
     */
    public static final int DEFAULT_LIMIT = 10;
 
    public String name = COLLECTION_NAME, accept;
 
    public Long start, count;
 
    public List<T> items;
 
    public Integer limit;
    // The schema version
    public int version = SCHEMA_VERSION;
}


