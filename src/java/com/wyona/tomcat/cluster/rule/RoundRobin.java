package com.wyona.tomcat.cluster.rule;

import java.util.Arrays;
import java.util.Comparator;

import com.wyona.tomcat.cluster.worker.Worker;

public class RoundRobin implements BalanceDirective {
    
    public final static String name = "RoundRobin";
    
    public Worker getNextWorker(Worker[] workerList) {
        
        Worker nextWorker = null;

        // sort ascending
        Arrays.sort(workerList, new Comparator() {
            public int compare(Object o1, Object o2) {
                if (((Worker)o1).getRequestCount() > ((Worker)o2).getRequestCount()) {
                    return 1;
                } else if (((Worker)o2).getRequestCount() > ((Worker)o1).getRequestCount()){
                    return -1;
                } else {
                    return 0;
                }
            }           
        });
        
        for (int i=0; i<workerList.length && nextWorker == null; i++) {
            switch (workerList[i].getState()) {
                case Worker.UNUSED:
                case Worker.ALIVE:
                    nextWorker = workerList[i];
                break;
            }
        }
        
        return nextWorker;
    }

}
