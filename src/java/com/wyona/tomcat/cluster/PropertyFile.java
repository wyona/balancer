package com.wyona.tomcat.cluster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.wyona.tomcat.cluster.rule.DirectiveManager;
import com.wyona.tomcat.cluster.worker.Worker;

public class PropertyFile {

    private Logger log;
    
    private File propFile;    
    private List workers;    
        
    private final static String WORKER = "worker";
    private final static String WORKER_TYPE = "type";
    private final static String WORKER_NAME = "name";
    private final static String WORKER_PORT = "port";
    private final static String WORKER_HOST = "host";
    private final static String WORKER_MAINTAIN = "maintain";
    private final static String SOCKET_TIMEOUT = "socket-timeout";
    private final static String RECOVER_TIMEOUT = "recover-timeout";    
    private final static String STICKY_TAG = "sticky-tag";    
    private final static String MAX_CONNECTIONS = "max-connections";    
    private final static String FAILOVER = "failover";
    
    private final static int DEFAULT_MAINTAIN = 60; 
    private final static int DEFAULT_SOCKET_TIMEOUT = 0;
    private final static int DEFAULT_RECOVER_TIMEOUT = 17;
    private final static String DEFAULT_STICKY_TAG = "JSESSIONID";
    private final static int DEFAULT_MAX_CONNECTIONS = 57;
    private final static String DEFAULT_FAILOVER = "true";
    
    private String balanceType;
    private String stickyTag;
    private int maintainIntervall;    
    private int socketTimeout;        
    private int recoverTimeout;
    private int maxConnections;
    private String templateDir;
    private Boolean failover;
    
    /** The directive manager */
    private DirectiveManager directiveManager;
    
    public PropertyFile(String fileName, DirectiveManager directiveManager, Logger log) throws ParserConfigurationException, SAXException, IOException {
        super();
        propFile = new File(fileName);
        this.directiveManager = directiveManager;
        balanceType = this.directiveManager.getDefaultDirective();
        this.log = log;
        parse();
    }
    
    private void parse() throws ParserConfigurationException, SAXException, IOException {                
        workers = new ArrayList();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document config = builder.parse(propFile);
        
        Node workers = config.getFirstChild();
        NamedNodeMap attrs = workers.getAttributes();
        
        balanceType = attrs.getNamedItem("balance-type").getNodeValue();
        if (!this.directiveManager.isValidDirecive(balanceType)) {
            throw new SAXException("Unknown directive: " + balanceType);
        }
        
        maintainIntervall = getElementValueAsInt(config, WORKER_MAINTAIN, DEFAULT_MAINTAIN);                                                       
        socketTimeout = getElementValueAsInt(config, SOCKET_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);        
        stickyTag = getElementValueAsString(config, STICKY_TAG, DEFAULT_STICKY_TAG);                        
        recoverTimeout = getElementValueAsInt(config, RECOVER_TIMEOUT, DEFAULT_RECOVER_TIMEOUT);
        maxConnections = getElementValueAsInt(config, MAX_CONNECTIONS, DEFAULT_MAX_CONNECTIONS);
        failover = Boolean.valueOf(getElementValueAsString(config, FAILOVER, DEFAULT_FAILOVER));
        
        NodeList worker = config.getElementsByTagName(WORKER);
        for (int i=0; i<worker.getLength(); i++) {
            Node wn = worker.item(i);       
            NodeList wns = wn.getChildNodes();
            Worker newWorker = new Worker();            
            for (int j=0; j<wns.getLength(); j++) {
                if (wns.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    String name = wns.item(j).getNodeName();
                    if (name.equals(WORKER_NAME)) {          
                        newWorker.setName(wns.item(j).getFirstChild().getNodeValue());
                    } else if (name.equals(WORKER_TYPE)) {
                        newWorker.setType(wns.item(j).getFirstChild().getNodeValue());
                    } else if (name.equals(WORKER_PORT)) {
                        newWorker.setPort(new Integer(wns.item(j).getFirstChild().getNodeValue()).intValue());
                    } else if (name.equals(WORKER_HOST)) {                        
                        newWorker.setHost(wns.item(j).getFirstChild().getNodeValue());
                    } else {
                        throw new SAXException("Unknown node in worker configuration: " + name);
                    }
                }
            }                                  
            this.workers.add(newWorker);
            log.debug("added new worker " + newWorker);
        }
        log.debug("parsed property file");
    }
    
    private int getElementValueAsInt(Document doc, String name, int defaultValue) {
        NodeList list = doc.getElementsByTagName(name);
        if (list.item(0) != null) {
            return new Integer(list.item(0).getFirstChild().getNodeValue()).intValue();
        } else {
            return defaultValue;
        }
    }
    
    private String getElementValueAsString(Document doc, String name, String defaultValue) {
        NodeList list = doc.getElementsByTagName(name);
        if (list.item(0) != null) {
            return list.item(0).getFirstChild().getNodeValue();
        } else {
            return defaultValue;
        }
    }
    
    public Worker[] getWorkers() {        
        Worker[] workers = new Worker[this.workers.size()];
        for (int i=0; i<workers.length; i++) {
            workers[i] = (Worker) this.workers.get(i);
        }
        return workers;
    }

    /**
     * @return Returns the balanceType.
     */
    public String getBalanceType() {
        return balanceType;
    }

    /**
     * @param balanceType The balanceType to set.
     */
    public void setBalanceType(String balanceType) {
        this.balanceType = balanceType;
    }

    /**
     * @return Returns the maintainIntervall.
     */
    public int getMaintainIntervall() {
        return maintainIntervall;
    }

    /**
     * @param maintainIntervall The maintainIntervall to set.
     */
    public void setMaintainIntervall(int maintainIntervall) {
        this.maintainIntervall = maintainIntervall;
    }

    /**
     * @return Returns the maxConnections.
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * @param maxConnections The maxConnections to set.
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * @return Returns the recoverTimeout.
     */
    public int getRecoverTimeout() {
        return recoverTimeout;
    }

    /**
     * @param recoverTimeout The recoverTimeout to set.
     */
    public void setRecoverTimeout(int recoverTimeout) {
        this.recoverTimeout = recoverTimeout;
    }

    /**
     * @return Returns the socketTimeout.
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * @param socketTimeout The socketTimeout to set.
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * @return Returns the stickyTag.
     */
    public String getStickyTag() {
        return stickyTag;
    }

    /**
     * @param stickyTag The stickyTag to set.
     */
    public void setStickyTag(String stickyTag) {
        this.stickyTag = stickyTag;
    }

    /**
     * @return Returns the templateDir.
     */
    public String getTemplateDir() {
        return templateDir;
    }

    /**
     * @param templateDir The templateDir to set.
     */
    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    /**
     * @return Returns the failover.
     */
    public boolean getFailover() {
        return failover.booleanValue();
    }

    /**
     * @param failover The failover to set.
     */
    public void setFailover(Boolean failover) {
        this.failover = failover;
    }            
 
    
    
}
