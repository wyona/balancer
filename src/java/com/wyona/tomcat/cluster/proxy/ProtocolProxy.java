package com.wyona.tomcat.cluster.proxy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wyona.tomcat.cluster.worker.Worker;

public interface ProtocolProxy {

    public int proxyServletRequest(Worker worker, HttpServletRequest servletRequest, 
            HttpServletResponse servletResponse);    
    
}
