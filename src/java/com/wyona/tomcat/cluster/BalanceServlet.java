package com.wyona.tomcat.cluster;

import java.io.IOException;
import java.util.Timer;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

import com.wyona.tomcat.cluster.rule.DirectiveManager;
import com.wyona.tomcat.cluster.worker.Worker;
import com.wyona.tomcat.cluster.worker.WorkerMaintainer;
import com.wyona.tomcat.util.TemplateUtil;

public class BalanceServlet implements Servlet {
        
    private ServletContext ctx;
    private Logger log;        
    private Worker[] workers; 
    private String balanceMode;       
    private Timer timer;
    private int maintainIntervall;    
    private String stickyTag;
    private boolean failover;
    
    /** The property file */
    private PropertyFile propertyFile;
        
    /** The protocol manager */
    private ProtocolMananger protocolManager;
    
    /** The directive manager */
    private DirectiveManager directiveManager;   

    /** The request logger */
    private RequestLogger requestLogger;
    
    private final static String COOKIE_HEADER_NAME = "cookie";
    
    public final static String WORKERS_ATTR_NAME = "workers";
    public final static String PROTOCOL_MANAGER_ATTR_NAME = "activeConnections";
    public final static String PROPERTY_FILE_ATTR_NAME = "propertyFile";
        
    public void init(ServletConfig servletConfig) throws ServletException {         
        
        ctx = servletConfig.getServletContext();
        log = Logger.getLogger(BalanceServlet.class);  
        String configFilePath = ctx.getRealPath(servletConfig.getInitParameter("properties-file"));       
        
        directiveManager = new DirectiveManager();
        
        try {
            propertyFile = new PropertyFile(configFilePath, directiveManager, log); 
            workers = propertyFile.getWorkers();
            failover = propertyFile.getFailover();
            balanceMode = propertyFile.getBalanceType();
            maintainIntervall = propertyFile.getMaintainIntervall();
            stickyTag = propertyFile.getStickyTag();
        } catch (Exception e) {
            log.debug("Error while parsing config file", e);
            throw new ServletException(e);
        }
        
        protocolManager = new ProtocolMananger(ctx, propertyFile, log);

        requestLogger = new RequestLogger(propertyFile, log);
        
        timer = new Timer();                    
        timer.scheduleAtFixedRate(new WorkerMaintainer(workers, protocolManager, propertyFile.getRecoverTimeout(), log),        
                (long) (maintainIntervall * 1000), maintainIntervall * 1000);
        
        bindAttributes();
    }
    
    /**
     * The Servlet entry point
     */
    public void service(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        
        long tripStart = System.currentTimeMillis();
        
        HttpServletRequest httpreq = (HttpServletRequest) req;
        HttpServletResponse httpres = (HttpServletResponse) res;                                
        
        int status = Worker.PROXY_WORKER_FAILED;
        Worker worker = null;
        do {
            worker = getStickyWorker(httpreq);
            if (worker != null) {
                log.debug("trying worker: " + worker);
                status = protocolManager.proxyServletRequest(worker, req, res);
            }
        } while (status == Worker.PROXY_WORKER_FAILED && worker != null);
        
        if (status == Worker.PROXY_WORKER_FAILED) {
            log.debug("all workers failed to proxy the request");
            status = HttpStatus.SC_SERVICE_UNAVAILABLE;
            TemplateUtil.writeTemplate(ctx, status, httpres.getOutputStream());
            httpres.setStatus(status);            
        } else {
            worker.addTrip(System.currentTimeMillis() - tripStart);
        }
        
        requestLogger.logHttpRequest(httpreq, status, 0);
    }    
        
    /**
     * Gets the next Worker
     * Handles "Sticky"-sessions according to the cookie STICKY_TAG
     * The next worker is choosen according to the balance directive
     */
    private Worker getStickyWorker(HttpServletRequest servletRequest) {
       
        Worker worker = null;
        String cookie = servletRequest.getHeader(COOKIE_HEADER_NAME);
        if (cookie != null) {
            if (cookie.startsWith(stickyTag)) {
                String workerName = this.getWorkerName(cookie);
                if (workerName != null) {
                    worker = this.getAssignedWorker(workerName);
                }                    
            }
        }        
        if (worker == null) {
            worker = this.directiveManager.getNextWorker(balanceMode, workers);
            if (worker != null) {
                log.debug("assign to " + worker);                
            }
        } else {
            switch (worker.getState()) {
                case Worker.PROTOCOL_ERROR:
                case Worker.TRANSPORT_ERROR:
                    if (failover) {
                        worker = this.directiveManager.getNextWorker(balanceMode, workers);
                        log.debug("failover for non working worker");
                    } else {
                        worker = null;
                        log.debug("session belongs to a non working worker, giving up");                        
                    }
                break;
                default:
                    log.debug("recognized sessionid: " + cookie + " -> " + worker);
            }                                                                
        }
        
        return worker;
    }
    
    private String getWorkerName(String cookie) {
        
        String workerName = null;
        
        String patterns[] = cookie.split("=");
        if (patterns.length >= 2) {
            patterns = patterns[1].split("\\.");
            if (patterns.length >= 2) { 
                if (patterns[1].indexOf(";") != -1) {
                    workerName = patterns[1].split(";")[0];
                } else {
                    workerName = patterns[1];
                }
            } else {
                log.debug("sesionId is missing routing information: " + cookie);
            }
        }

        return workerName;
    }
    
    private Worker getAssignedWorker(String workerName) {
        
        Worker assignedWorker = null;
        for (int i=0; i<this.workers.length && assignedWorker == null; i++) {
            if (workers[i].getName().equals(workerName)) {
                assignedWorker = workers[i];
            }
        }
        if (assignedWorker == null) {
            log.debug("worker referenced in sessionId not found: " + workerName);
        }
        
        return assignedWorker;
    }
    
    /**
     * Bind servlet objects to the servlet context so other
     * servlets can make use of them
     */
    private void bindAttributes() {
        this.ctx.setAttribute(WORKERS_ATTR_NAME, this.workers);
        this.ctx.setAttribute(PROTOCOL_MANAGER_ATTR_NAME, this.protocolManager);
        this.ctx.setAttribute(PROPERTY_FILE_ATTR_NAME, this.propertyFile);
    }    
    
    public ServletConfig getServletConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getServletInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public void destroy() {
        // TODO Auto-generated method stub        
    }    
}
