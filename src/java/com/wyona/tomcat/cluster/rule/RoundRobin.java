package com.wyona.tomcat.cluster.rule;

import com.wyona.tomcat.cluster.worker.Worker;

public class RoundRobin implements BalanceDirecitve {
    
    final static String name = "RoundRobin";
    private Worker lastWorker = null;
    
    public Worker getNextWorker(Worker[] workerList) {
        Worker nextWorker = null;
        int readyWorkers = 0;
        for (int i=0; i<workerList.length; i++) {
            switch (workerList[i].getState()) {
                case Worker.UNUSED:             
                case Worker.ALIVE:                    
                    readyWorkers++;
                break;
            }
        }
        for (int i=0; i<workerList.length && nextWorker == null; i++) {
            switch (workerList[i].getState()) {
                case Worker.UNUSED:
                    nextWorker = workerList[i];         
                break;
                case Worker.ALIVE: 
                    if (readyWorkers == 1) {
                        nextWorker = workerList[i];
                    } else { 
                        if (!lastWorker.equals(workerList[i])) {
                            nextWorker = workerList[i];
                        }
                    }
                break;
            }             
        }
        if (nextWorker != null) {                    
            nextWorker.incRequestCount();
            lastWorker = nextWorker;
        }
        return nextWorker;
    }
}
