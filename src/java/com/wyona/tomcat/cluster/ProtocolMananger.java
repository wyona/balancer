package com.wyona.tomcat.cluster;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

import com.wyona.tomcat.cluster.proxy.HttpProxy;
import com.wyona.tomcat.cluster.worker.Worker;

public class ProtocolMananger {

    public final static String PROTO_HTTP = "http";
    public final static String PROTO_AJP13 = "ajp13";
    public final static String PROTO_AJP14 = "ajp14";
    
    /** The HTTP proxy used by http workers */
    private HttpProxy httpProxy;
    
    private int activeConnections;
    private int maxConnections;    
    private long refusedConnections;
    
    private PropertyFile propertyFile;
    private Logger log;
    
    public ProtocolMananger(ServletContext ctx, PropertyFile propertyFile, Logger logger) {
        super();
        this.propertyFile = propertyFile;
        this.log = logger;
        this.activeConnections = 0;
        this.refusedConnections = 0;
        this.maxConnections = this.propertyFile.getMaxConnections();
        httpProxy = new HttpProxy(ctx, this.propertyFile, this.log);
    }    
    
    public void proxyServletRequest(Worker worker, ServletRequest request, ServletResponse response, RequestStatus status) {
       
        if (activateConnection()) {
            if (worker.getType().equals(PROTO_HTTP)) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;                
                httpProxy.proxyServletRequest(worker, httpRequest, httpResponse, status);                
            } else {
                log.error("protocol not implemented: " + worker.getType());
                status.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
            deactivateConnection();
        } else {
            incRefusedConnections();
        }      
    }
    
    private synchronized boolean activateConnection() {
        if (this.activeConnections < this.maxConnections) {        
            this.activeConnections += 1;
            return true;
        } else {
            log.error("connection limit reached, discarding request " + this.activeConnections + "-" + this.maxConnections);
            return false;
        }
    }
    
    private synchronized void deactivateConnection() {           
        this.activeConnections -= 1;    
    }
    
    private synchronized void incRefusedConnections() {
        this.refusedConnections += 1;
    }
    
    public HttpProxy getHttpProxy() {
        return this.httpProxy;
    }

    /**
     * @return Returns the activeConnections.
     */
    public int getActiveConnections() {
        return this.activeConnections;
    }    
    
    /**
     * @return Returns the refusecConnections
     */
    public long getRefusedConnections() {
        return this.refusedConnections;
    }
    
}
