package com.wyona.tomcat.cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class RequestLogger {

    private PropertyFile propertyFile;
    private Logger log;
    
    private SimpleDateFormat format;
    private File logFile;
    private PrintWriter logWriter;
    
    public RequestLogger(PropertyFile propertyFile, Logger logger) {
        super();
        this.propertyFile = propertyFile;
        this.log = logger;
        this.format = new SimpleDateFormat("[d/MMM/yyyy:H:m:s Z]");
        
        if (this.propertyFile.getLogfile() != null) {
            initLogfile(new File(this.propertyFile.getLogfile()));
        }
    }
    
    /**
     * Initialises the logfile.
     * @param logFile the logfile.
     */
    private void initLogfile(File logFile) {
        this.logFile = logFile;
        
        log.debug("initialising the logfile: " + this.logFile.getAbsolutePath());
        
        try {
            this.logWriter = new PrintWriter(new FileOutputStream(this.logFile));
        } catch (FileNotFoundException e) {
            log.error("could not open logfile: " + this.logFile.getAbsolutePath() + " logging is disabled", e);
        }
    }
    
    /**
     * Log a request in the apache combined access log format
     * 
     * 127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326 "http://www.example.com/start.html" "Mozilla/4.08 [en] (Win98; I ;Nav)"
     * 
     * @param request
     * @param response
     */
    public synchronized void logHttpRequest(HttpServletRequest request, int status, int contentLength) {        
        if (this.logWriter != null) {
            
            if (!this.logFile.exists()) {
                // try to reinitialise logging incase files got rotated
                this.initLogfile(this.logFile);
            }
            
            String referer = request.getHeader("Referer");
            String userAgent = request.getHeader("User-agent");
            String user = request.getRemoteUser();
            
            String logEntry = 
                    request.getRemoteAddr() + " - " + 
                    (user != null ? user : "") + " " +
                    this.format.format(new Date()) + " " +
                    "\"" + request.getMethod() + " " + request.getRequestURI() + " " + request.getProtocol() + "\" " +
                    status + " " +
                    contentLength + " " +
                    "\"" + (referer != null ? referer : "") + "\" " +
                    "\"" + (userAgent != null ? userAgent : "") + "\" ";
            
            log.debug(logEntry);
            
            this.logWriter.println(logEntry);
            this.logWriter.flush();
        }
    }
}
