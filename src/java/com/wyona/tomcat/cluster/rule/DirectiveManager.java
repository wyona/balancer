package com.wyona.tomcat.cluster.rule;

import com.wyona.tomcat.cluster.worker.Worker;

public class DirectiveManager {
    
    RoundRobin roundRobin;
    
    public DirectiveManager() {
        super();
        roundRobin = new RoundRobin();
    }
    
    public String getDefaultDirective() {
        return RoundRobin.name;
    }
    
    public boolean isValidDirecive(String d) {
        if (d.equals(RoundRobin.name)) {
            return true;
        } else {
            return false;
        }
    }
    
    public Worker getNextWorker(String d, Worker[] list) {
        if (d.equals(RoundRobin.name)) {
            return this.roundRobin.getNextWorker(list);
        } else { 
            return null;
        }
    }
    
}
