package com.wyona.tomcat.cluster.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.wyona.tomcat.cluster.PropertyFile;
import com.wyona.tomcat.cluster.ProxyRetryHandler;
import com.wyona.tomcat.cluster.worker.Worker;
import com.wyona.tomcat.util.TemplateUtil;

public class HttpProxy implements ProtocolProxy {
    
    /* supported HTTP methods */
    static final String METHOD_GET = "GET";
    static final String METHOD_POST = "POST";
    static final String METHOD_PUT = "PUT";
    
    /* the servlet context */
    ServletContext ctx;
    
    /* The connection manager, all servlet threads access their method objects
     * through the connectionmanager
     */
    private MultiThreadedHttpConnectionManager connectionManager;
    
    /* HttpClient instance used by all filter threads */
    private HttpClient client;

    /* List of modified request/response headers that will not be copied*/
    private List excludeHeaders;

    /* HTTP Headers that will be filtered out from balancer responses */
    static final String[] FILTERED_HEADERS = {
        "X-Cocoon-Version",
        "Server",
        "Transfer-Encoding"
    };
    
    /* Servlet container host information */ 
    private String hostname;
    private int port;
    
    static final String HEADER_LOCATION = "Location";
    static final String HEADER_SET_COOKIE = "Set-Cookie";    
    static final String HEADER_COOKIE = "Cookie";
    static final String HEADER_CONTENT_LENGTH = "Content-Length";    
    
    private int maxConnections;
    private int socketTimeout;
    private Logger log;
    
    public HttpProxy(ServletContext ctx, PropertyFile propertyFile, Logger logger) {
        super();
        this.ctx = ctx;
        this.maxConnections = propertyFile.getMaxConnections();
        this.socketTimeout = propertyFile.getSocketTimeout();
        this.log = logger;
        this.excludeHeaders = new ArrayList();
        connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setMaxTotalConnections(this.maxConnections);
        client = new HttpClient(connectionManager);  
    }
    
    /**
     * 
     * @param worker
     * @param servletRequest
     * @param servletResponse
     * @return
     */
    public int proxyServletRequest(Worker worker, HttpServletRequest servletRequest, 
            HttpServletResponse servletResponse) {
        
        setServerInfo(servletRequest);
        
        int status = Worker.PROXY_WORKER_FAILED;        
        String requestMethod = servletRequest.getMethod();
        
        if (requestMethod.equals(METHOD_GET) ||
                requestMethod.equals(METHOD_POST) ||
                requestMethod.equals(METHOD_PUT)) {                
            status = proxyHttpRequest(worker, servletRequest, servletResponse);
        } else {
            log.error("request method not implemented " + requestMethod);
            status = HttpStatus.SC_METHOD_FAILURE;
        }        
        
        return status;
    }

    /**
     * 
     * @param servletRequest
     * @param servletResponse
     * @param assignedWorker
     * @param status
     */
    private int proxyHttpRequest(Worker worker, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {        
        
        boolean success = true;
        int status = Worker.PROXY_WORKER_FAILED;
        
        HttpMethod method = null;
        if (servletRequest.getMethod().equals(METHOD_GET)) {
            method = new GetMethod(worker.getUri());        
        } else if (servletRequest.getMethod().equals(METHOD_POST)) {
            method = new PostMethod(worker.getUri());            
        } else if (servletRequest.getMethod().equals(METHOD_PUT)) {
            method = new PutMethod(worker.getUri());
        }
        
        setHttpMethodOptions(method);        
        try {
            
            // request to the backend
            excludeHeaders.clear();             
            copyRequestBody(servletRequest, method);
            rewriteRequestHeaders(servletRequest, method);            
            copyRequestHeaders(servletRequest, method);  
            status = client.executeMethod(method);            
           
            // response to the client
            servletResponse.setStatus(status);            
            excludeHeaders.clear();
            // send our own page for error messages with a defined template
            if (status >= HttpStatus.SC_BAD_REQUEST && TemplateUtil.hasTemplate(ctx, status)) {
                excludeHeaders.add(HEADER_CONTENT_LENGTH);
            }
            rewriteResponseHeaders(servletResponse, method, status);
            copyResponseHeaders(servletResponse, method);
            copyResponseBody(servletResponse, method, status);
            
        } catch (HttpException e) {
            success = false;               
            worker.setState(Worker.PROTOCOL_ERROR);
            log.error("protocol error: " + e.getMessage());
        } catch (IOException e) {
            success = false;
            worker.setState(Worker.TRANSPORT_ERROR);
            log.error("transport error: " + e.getMessage());            
        } finally {
            if (success) {
                worker.setState(Worker.ALIVE);
            } else {
                status = Worker.PROXY_WORKER_FAILED;
            }
            log.debug("released connection");
            method.releaseConnection();        
        }
        return status;
    }

    private void setHttpMethodOptions(HttpMethod method) {
        method.setFollowRedirects(false);          
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        HttpMethodRetryHandler retryhandler = new ProxyRetryHandler();        
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
        method.getParams().setIntParameter(HttpMethodParams.SO_TIMEOUT, socketTimeout);        
    }
    
    private void copyRequestBody(HttpServletRequest servletRequest, HttpMethod method) throws IOException {
        if (servletRequest.getMethod().equals(METHOD_POST)) {
            ((PostMethod)method).setRequestBody(servletRequest.getInputStream());
        } else if (servletRequest.getMethod().equals(METHOD_PUT)) {
            ((PutMethod)method).setRequestBody(servletRequest.getInputStream());
        }        
    }
    
    private void copyRequestHeaders(HttpServletRequest servletRequest, HttpMethod method) {
        method.setPath(servletRequest.getRequestURI());
        if (servletRequest.getQueryString() != null) {  
            method.setQueryString(servletRequest.getQueryString());
        }
        Enumeration headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {            
            String headerName = (String) headerNames.nextElement();
            if (!ignoreHeader(headerName)) {
                String headerValue = servletRequest.getHeader(headerName);
                method.setRequestHeader(headerName, headerValue);
            }
        }        
    }
    
    private void rewriteRequestHeaders(HttpServletRequest servletRequest, HttpMethod method) {        
    
    }
        
    private void rewriteResponseHeaders(HttpServletResponse servletResponse, HttpMethod method, int status) {   
        switch (status) {
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_MOVED_TEMPORARILY:
                rewriteLocation(servletResponse, method);
            break;
        }                 
    }
    
    private void copyResponseHeaders(HttpServletResponse servletResponse, HttpMethod method) {
        Header[] headers = method.getResponseHeaders();
       
        for (int i=0; i<headers.length; i++) {                       
            if (!ignoreHeader(headers[i].getName())) {              
                if (servletResponse.containsHeader(headers[i].getName())) {  
                    servletResponse.setHeader(headers[i].getName(), headers[i].getValue());
                } else{
                    servletResponse.addHeader(headers[i].getName(), headers[i].getValue());
                } 
            }
        }        
    }
        
    private void copyResponseBody(HttpServletResponse servletResponse, HttpMethod method, int status) throws IOException {
        switch (status) {            
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_MOVED_TEMPORARILY:
                servletResponse.getWriter().write("0\r\n\r\n");    
            break;
            default:
                // send our own page for error messages with a defined template
                if (status >= HttpStatus.SC_BAD_REQUEST && TemplateUtil.hasTemplate(ctx, status)) {                    
                    TemplateUtil.writeTemplate(ctx, status, servletResponse.getOutputStream());
                } else {
                    InputStream in = method.getResponseBodyAsStream();
                    OutputStream out = servletResponse.getOutputStream();                    

                    if (in != null) {
                        int rb;
                        byte[] buf = new byte[4096];                    
                        while ((rb = in.read(buf)) > 0) {
                            out.write(buf, 0, rb);
                        }             
                        out.close();
                    }                    
                }
            break;
        }
    }
    
    private void rewriteLocation(HttpServletResponse res, HttpMethod method) {
        String location = method.getResponseHeader(HEADER_LOCATION).getValue();
        location =  "http://" + hostname + ":" + port + location.substring(location.indexOf("/", 8));
        rewriteResponseHeader(res, HEADER_LOCATION, location);
    }
    
    private void rewriteResponseHeader(HttpServletResponse res, String name, String value) {
        if (res.containsHeader(name)) {
            res.setHeader(name, value);
        } else {
            res.addHeader(name, value);
        }
        excludeHeaders.add(name);  
    }
    
    private boolean ignoreHeader(String name) {
        boolean exclude = false;
        if (excludeHeaders.contains(name)) {
            exclude = true;
        } else {
            for (int i=0; i<FILTERED_HEADERS.length && !exclude; i++) {
                if (name.equalsIgnoreCase(FILTERED_HEADERS[i])) {
                    exclude = true;
                }
            }
        }
        return exclude;
    }
    
    private void setServerInfo(ServletRequest req) {
        this.hostname = req.getServerName();
        this.port = req.getServerPort();
    }

    public HttpClient getClient() {
        return client;
    }

    public MultiThreadedHttpConnectionManager getConnectionManager() {
        return connectionManager;
    }
    
}
