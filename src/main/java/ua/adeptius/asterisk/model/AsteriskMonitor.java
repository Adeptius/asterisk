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

import static ua.adeptius.asterisk.model.LogCategory.ANSWER_CALL;
import static ua.adeptius.asterisk.model.LogCategory.ENDED_CALL;
import static ua.adeptius.asterisk.model.LogCategory.INCOMING_CALL;

public class AsteriskMonitor implements ManagerEventListener {

    private ManagerConnection managerConnection;

//    private DBController dbController;

//    public void setDbController(DBController dbController) {
//        this.dbController = dbController;
//    }

    public AsteriskMonitor() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                Settings.getSetting("___asteriskAdress"),
                Settings.getSetting("___asteriskLogin"),
                Settings.getSetting("___asteriskPassword"));
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

        if (event instanceof NewChannelEvent){
            String callerIdNum = ((NewChannelEvent) event).getCallerIdNum();
            String phoneReseive = ((NewChannelEvent) event).getExten();
            MainController.onNewCall(INCOMING_CALL, callerIdNum, phoneReseive);
        }else if (event instanceof HangupEvent){
            String callerIdNum = ((HangupEvent) event).getCallerIdNum();
            MainController.onNewCall(ENDED_CALL, callerIdNum, "");

        }else if (event instanceof NewStateEvent){
            int code = ((NewStateEvent) event).getChannelState();
            String callerIdNum = ((NewStateEvent) event).getCallerIdNum();
            if (code == 6){
                MainController.onNewCall(ANSWER_CALL, callerIdNum, "");
            }
        }
    }
}