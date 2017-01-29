package ua.adeptius.asterisk.model;

import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.NewStateEvent;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.utils.MyLogger;
import ua.adeptius.asterisk.utils.Settings;
import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.StatusAction;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewChannelEvent;

import java.io.IOException;

public class AsteriskMonitor implements ManagerEventListener {

    private ManagerConnection managerConnection;

//    private DBController dbController;

//    public void setDbController(DBController dbController) {
//        this.dbController = dbController;
//    }

    public AsteriskMonitor() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                Settings.getSetting("asteriskAdress"),
                Settings.getSetting("asteriskLogin"),
                Settings.getSetting("asteriskPassword"));
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
//        if (event instanceof NewChannelEvent){
//            String callerIdNum = ((NewChannelEvent) event).getCallerIdNum();
//            String phoneReseive = ((NewChannelEvent) event).getExten();
//            MainController.onNewCall(callerIdNum, phoneReseive);
//        }


        if (event instanceof HangupEvent){
            System.out.println("Звонок завершен: "+ ((HangupEvent) event).getCallerIdNum());

        }
        if (event instanceof NewStateEvent){
            int code = ((NewStateEvent) event).getChannelState();
            if (code == 4){
                System.out.println("Звонит: "+((NewStateEvent) event).getCallerIdNum());
            }else if (code == 6){
                System.out.println("Отвечено: " + ((NewStateEvent) event).getCallerIdNum());
            }
        }
    }
}