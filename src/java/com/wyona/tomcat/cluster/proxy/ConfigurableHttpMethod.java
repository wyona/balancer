package com.wyona.tomcat.cluster.proxy;

import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

/**
 * Configurable HTTP Method, used to implement additional request-types
 * required by protocols like WebDAV, etc..
 * 
 * @author greg
 */
public class ConfigurableHttpMethod extends EntityEnclosingMethod {

    private String method;

    public ConfigurableHttpMethod(String uri, String method) {
        super(uri);
        this.method = method;
    }
    
    public String getName() {
        return this.method;
    }
}
