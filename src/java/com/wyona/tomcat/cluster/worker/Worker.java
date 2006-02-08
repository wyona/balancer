package com.wyona.tomcat.cluster.worker;

import java.net.MalformedURLException;

public class Worker {    
    
    private long roundTripTime;
    private long lastTripTime;    
    private long requestCount;
    private int state;    
    
    private String name;
    private String type;
    private String host;
    private int port;
    
    public final static String WORKER_TYPE_HTTP = "http";
    //TODO implement the ajp13/ajp14 workers
    public final static String WORKER_TYPE_AJP13 = "ajp13";
    public final static String WORKER_TYPE_AJP14 = "ajp14";
    
    private final static String DEFAULT_TYPE = WORKER_TYPE_HTTP;
    private final static String DEFAULT_HOST = "127.0.0.1";
    private final static String DEFAULT_NAME = "worker";
    private final static int DEFAULT_PORT = 8080;    
    
    final public static int UNUSED = 0;
    final public static int ALIVE = 1;    
    final public static int TRANSPORT_ERROR = 2;   
    final public static int PROTOCOL_ERROR = 3; 
    final public static int DEACTIVATED = 4;     
    
    final public static int PROXY_WORKER_FAILED = -1;
    
    public Worker() throws MalformedURLException {
        this.roundTripTime = 0;
        this.lastTripTime = 0;
        this.requestCount = 0;
        this.state = UNUSED;
        this.name = DEFAULT_NAME;
        this.type = DEFAULT_TYPE;
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
    }

    /**
     *  @return Returns the requestCount
     */
    public synchronized long getRequestCount() {
        return requestCount;
    }

    /**
     *  Increment the requestCount
     */
    public synchronized void incRequestCount() {
        this.requestCount += 1;
    }
            
    /**
     * @return Returns the host.
     */
    public synchronized String getHost() {
        return host;
    }

    /**
     * @param host The host to set.
     */
    public synchronized void setHost(String host) {
        this.host = host;
    }

    /**
     * @return Returns the name.
     */
    public synchronized String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public synchronized void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the port.
     */
    public synchronized int getPort() {
        return port;
    }

    /**
     * @param port The port to set.
     */
    public synchronized void setPort(int port) {
        this.port = port;
    }

    /**
     * @return Returns the state.
     */
    public synchronized int getState() {
        return state;
    }

    /**
     * @param state The state to set.
     */
    public synchronized void setState(int state) {
        this.state = state;
    }

    /**
     * @return Returns the type.
     */
    public synchronized String getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public synchronized void setType(String type) {
        this.type = type;
    }
    
    /**
     * @return Returns the worker uri
     */
    public synchronized String getUri() {
        return this.type + "://" + this.host + ":" + this.port;
    }
    
    /**
     * @return Returns the average round trip time [millisec]
     */
    public synchronized double getAvgRoundTripTime() {
        if (this.requestCount == 0) {
            return (double)0.0;
        } else {
            return (double) this.roundTripTime / this.requestCount;
        }
    }
    
    /**
     * @return Returns the last round trip time [millisec]
     */
    public synchronized long getLastRoundTripTime() {
        return this.lastTripTime;
    }
    
    /**
     * Add one round trip timeslice to the pool
     * @param duration
     */
    public synchronized void addTrip(long duration) {
        this.roundTripTime += duration;
        this.lastTripTime = duration;
        this.incRequestCount();
    }
        
    public String toString() {
        return "#Worker{" +name + "-" + type + "-" + host + "-" +
            port + "-" + requestCount + "-" + "}";
    }
}
