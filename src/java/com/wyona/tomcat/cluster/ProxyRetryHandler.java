package com.wyona.tomcat.cluster;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;

public class ProxyRetryHandler implements HttpMethodRetryHandler {

    public ProxyRetryHandler() {
        super();
    }

    public boolean retryMethod(HttpMethod arg0, IOException arg1, int arg2) {
        return false;
    }

}
