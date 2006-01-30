package com.wyona.tomcat.cluster.worker;

import java.io.IOException;
import java.util.TimerTask;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.wyona.tomcat.cluster.ProtocolMananger;
import com.wyona.tomcat.cluster.ProxyRetryHandler;

public class WorkerMaintainer extends TimerTask {

    private Logger log;
    
    private Worker[] workers;
    private ProtocolMananger protocolManager;
    private int recoverTimeout;
    
    public WorkerMaintainer(Worker[] workers, ProtocolMananger protocolManager, int recoverTimeout, Logger log) {
        super();        
        this.workers = workers;
        this.log = log;
        this.protocolManager = protocolManager;
        this.recoverTimeout = recoverTimeout;
    }

    public void run() {
        log.debug("maintaining workers " + workers.length);
        for (int i=0; i<workers.length; i++) {
            switch (workers[i].getState()) {
                case Worker.TRANSPORT_ERROR:
                case Worker.PROTOCOL_ERROR:
                    if (workers[i].getType().equals(Worker.WORKER_TYPE_HTTP)) {
                        log.debug("trying to recover worker " + workers[i]);   
                        switch (dummyHttpRequest(workers[i])) {
                            case Worker.PROXY_WORKER_FAILED:
                                log.debug("recovery failed for worker " + workers[i]);
                            break;
                            default:
                                log.debug("recovery succeeded for worker " + workers[i]);
                                workers[i].setState(Worker.ALIVE);
                        }
                    } 
                break;
            }
        }
    }
    
    private int dummyHttpRequest(Worker worker) { 
        HttpMethod method = new GetMethod(worker.getUri());
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new ProxyRetryHandler());
        method.getParams().setIntParameter(HttpMethodParams.SO_TIMEOUT, recoverTimeout);
        int status = Worker.PROXY_WORKER_FAILED;          
        try {
            status = protocolManager.getHttpProxy().getClient().executeMethod(method);
        } catch (HttpException e) {
            worker.setState(Worker.PROTOCOL_ERROR);
            log.error("protocol error: " + e.getMessage());
        } catch (IOException e) {
            worker.setState(Worker.TRANSPORT_ERROR);
            log.error("transport error: " + e.getMessage());  
        } finally {
            method.releaseConnection();
        }                
        return status;
    }
}
