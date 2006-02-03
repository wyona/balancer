/**
 * 
 */
package com.wyona.tomcat.cluster.rule;

import java.util.Arrays;
import java.util.Comparator;

import com.wyona.tomcat.cluster.worker.Worker;

/**
 * @author greg
 *
 */
public class RTTEquiv implements BalanceDirective {

    public final static String name = "RTTEquiv";

    private long count = 0;
    
    
    /* (non-Javadoc)
     * @see com.wyona.tomcat.cluster.rule.BalanceDirecitve#getNextWorker(com.wyona.tomcat.cluster.worker.Worker[])
     */
    public Worker getNextWorker(Worker[] workerList) {
        Worker nextWorker = null;
                        
        // every 10'th request select the worker with the least requests
        // to give slow workers a chance to improve their RTT
        if (count % 10 == 0) {
            
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
            
        } else {
            
            // sort ascending
            Arrays.sort(workerList, new Comparator() {
                public int compare(Object o1, Object o2) {
                    if (((Worker)o1).getLastRoundTripTime() > ((Worker)o2).getLastRoundTripTime()) {
                        return 1;
                    } else if (((Worker)o2).getLastRoundTripTime() > ((Worker)o1).getLastRoundTripTime()){
                        return -1;
                    } else {
                        return 0;
                    }
                }           
            });
            
        }

        for (int i=0; i<workerList.length && nextWorker == null; i++) {
            switch (workerList[i].getState()) {
                case Worker.UNUSED:
                case Worker.ALIVE:
                    nextWorker = workerList[i];
                break;
            }
        }
        
        count += 1;
        
        return nextWorker;
    }

}
