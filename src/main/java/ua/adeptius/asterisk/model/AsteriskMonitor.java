package ua.adeptius.asterisk.model;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.DefaultAsteriskServer;
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
import java.util.HashMap;

import static ua.adeptius.asterisk.model.LogCategory.ANSWER_CALL;
import static ua.adeptius.asterisk.model.LogCategory.ENDED_CALL;
import static ua.adeptius.asterisk.model.LogCategory.INCOMING_CALL;

public class AsteriskMonitor implements ManagerEventListener {

    private ManagerConnection managerConnection;
//    private AsteriskServer asteriskServer;

    public AsteriskMonitor() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                Settings.getSetting("___asteriskAdress"),
                Settings.getSetting("___asteriskLogin"),
                Settings.getSetting("___asteriskPassword"));
        this.managerConnection = factory.createManagerConnection();
//        asteriskServer = new DefaultAsteriskServer(managerConnection);
    }

    public void run() throws IOException, AuthenticationFailedException,
            TimeoutException, InterruptedException {
        managerConnection.addEventListener(this);
        managerConnection.login();
        managerConnection.sendAction(new StatusAction());
//        new Thread(() -> {
//            try {
//                while (true) {
//                    Thread.sleep(100);
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    private HashMap<String, String> phonesFromAndPhonesTo = new HashMap<>();

    public void onManagerEvent(ManagerEvent event) {
//        System.out.println(event);
        if (event instanceof NewChannelEvent) {
//            System.out.println(event);
            String callerIdNum = ((NewChannelEvent) event).getCallerIdNum();
            String phoneReseive = ((NewChannelEvent) event).getExten();
            phonesFromAndPhonesTo.put(callerIdNum, phoneReseive);
            MainController.onNewCall(INCOMING_CALL, callerIdNum, phoneReseive, "");

        } else if (event instanceof HangupEvent) {
//            System.out.println(event);
            String callUniqueId = ((HangupEvent) event).getUniqueId();
            String callerIdNum = ((HangupEvent) event).getCallerIdNum();
            String phoneReseive = phonesFromAndPhonesTo.get(callerIdNum);
            MainController.onNewCall(ENDED_CALL, callerIdNum, phoneReseive, callUniqueId);
            phonesFromAndPhonesTo.remove(callerIdNum);

        } else if (event instanceof NewStateEvent) {
//            System.out.println(event);
            int code = ((NewStateEvent) event).getChannelState();
            String callerIdNum = ((NewStateEvent) event).getCallerIdNum();
            String phoneReseive = phonesFromAndPhonesTo.get(callerIdNum);
            if (code == 6) {
                MainController.onNewCall(ANSWER_CALL, callerIdNum, phoneReseive, "");
            }
        }
    }
}