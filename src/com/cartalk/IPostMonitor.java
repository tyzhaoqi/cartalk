package com.cartalk;

import com.cartalk.io.ObdCommandJob;

/**
 * TODO put description
 */
public interface IPostMonitor {
        void setListener(IPostListener callback);

        boolean isRunning();

        void executeQueue();
        
        void addJobToQueue(ObdCommandJob job);
        
        void addJobToQueueNotRun(ObdCommandJob job);
}
