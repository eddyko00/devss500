/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.herokudemo;

import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author eddy
 */
public class RESTtimer {

    protected static Logger logger = Logger.getLogger("RESTtimer");

    public void RestTimerHandler() {
        System.out.println(new Date());
        AFSleep();
    }

    public static void AFSleep() {
        // delay seems causing openshif not working        
        if (true) {
            return;
        }
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
        }
    }
}
