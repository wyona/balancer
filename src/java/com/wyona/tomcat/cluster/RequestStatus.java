package com.wyona.tomcat.cluster;

public class RequestStatus {

    private int statusCode;
    
    private int contentLength;
    
    public RequestStatus(int statusCode, int contentLength) {
        super();
        this.statusCode = statusCode;
        this.contentLength = contentLength;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
}
