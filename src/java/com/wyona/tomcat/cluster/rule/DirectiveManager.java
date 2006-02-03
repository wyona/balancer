package com.wyona.tomcat.cluster.rule;

import java.util.HashMap;

import com.wyona.tomcat.cluster.worker.Worker;

public class DirectiveManager {
    
    HashMap directives = new HashMap();    
    
    public DirectiveManager() {                
        directives.put(RoundRobin.name, new RoundRobin());
        directives.put(RTTEquiv.name, new RTTEquiv());        
    }
    
    public String getDefaultDirective() {
        return RoundRobin.name;
    }
    
    public boolean isValidDirecive(String d) {
        return directives.containsKey(d);
    }
    
    public Worker getNextWorker(String d, Worker[] list) {
        BalanceDirective directive = (BalanceDirective) directives.get(d);
        if (directive != null) {
            return directive.getNextWorker(list);
        } else {
            return null;
        }        
    }
    
}
