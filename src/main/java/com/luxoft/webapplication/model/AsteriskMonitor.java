package com.luxoft.webapplication.model;

import com.luxoft.webapplication.controllers.DBController;
import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.StatusAction;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewChannelEvent;

import java.io.IOException;

public class AsteriskMonitor implements ManagerEventListener {

    private ManagerConnection managerConnection;

    private DBController dbController;

    public void setDbController(DBController dbController) {
        this.dbController = dbController;
    }

    public AsteriskMonitor() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                "194.44.37.30", "adeptius", "ccb6f130f89de0bab95df361669e32ba");
        this.managerConnection = factory.createManagerConnection();
    }

    public void run() throws IOException, AuthenticationFailedException,
            TimeoutException, InterruptedException {
        managerConnection.addEventListener(this);
        managerConnection.login();
        managerConnection.sendAction(new StatusAction());
        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void onManagerEvent(ManagerEvent event) {
//        System.out.println(event);
        if (event instanceof NewChannelEvent){
            String callerIdNum = ((NewChannelEvent) event).getCallerIdNum();
            String phoneReseive = ((NewChannelEvent) event).getExten();
            System.out.println(callerIdNum + "->" + phoneReseive);
//            dbController.savePhone(callerIdNum);
        }
    }
}