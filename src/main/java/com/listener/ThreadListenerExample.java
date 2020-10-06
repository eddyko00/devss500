/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.listener;



import com.example.herokudemo.RESTtimer;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ThreadListenerExample implements ServletContextListener {
    private ScheduledExecutorService    scheduler    = null;
    public void schedulerInitialized() {
        if ((scheduler == null) || (!scheduler.isTerminated())) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new ScheduledTask(), 10, 10, TimeUnit.SECONDS);
        }
    }
    public void contextInitialized(ServletContextEvent sce) {      
        if ((scheduler == null) || (!scheduler.isTerminated())) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new ScheduledTask(), 10, 10, TimeUnit.SECONDS);
        }
    }
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            System.out.println("Scheduler Shutting down successfully " + new Date());
            scheduler.shutdown();
        } catch (Exception ex) {
        }
    }
}

class ScheduledTask extends TimerTask {
    public static RESTtimer tr = new RESTtimer();
    public void run() {
//        System.out.println(new Date());
        tr.RestTimerHandler();
    }
}