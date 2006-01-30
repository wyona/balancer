package com.wyona.tomcat.cluster.rule;

import com.wyona.tomcat.cluster.worker.Worker;

public interface BalanceDirecitve {

    public Worker getNextWorker(Worker[] workerList); 
    
}
