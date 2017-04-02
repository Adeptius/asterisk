package ua.adeptius.asterisk.monitor;

import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.NewStateEvent;
import ua.adeptius.asterisk.tracking.TrackingController;
import ua.adeptius.asterisk.dao.Settings;
import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.StatusAction;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewChannelEvent;

import java.io.IOException;
import java.util.HashMap;

import static ua.adeptius.asterisk.utils.logging.LogCategory.ANSWER_CALL;
import static ua.adeptius.asterisk.utils.logging.LogCategory.ENDED_CALL;
import static ua.adeptius.asterisk.utils.logging.LogCategory.INCOMING_CALL;

public class AsteriskMonitor implements ManagerEventListener {

    private ManagerConnection managerConnection;

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
    }

    private HashMap<String, String> phonesFromAndPhonesTo = new HashMap<>();

    public void onManagerEvent(ManagerEvent event) {
        if (!(event instanceof NewChannelEvent) && !(event instanceof HangupEvent) && !(event instanceof NewStateEvent)) {
            return;
        }

//        System.out.println(event);
        if (event instanceof NewChannelEvent) {
            NewChannelEvent newChannelEvent = (NewChannelEvent) event;
            if (newChannelEvent.getChannel().startsWith("SIP/Intertelekom")) return;

//            System.out.println(newChannelEvent);
            String callerIdNum = addZero(newChannelEvent.getCallerIdNum());
            String phoneReseive = addZero(newChannelEvent.getExten());
            phonesFromAndPhonesTo.put(callerIdNum, phoneReseive);
            TrackingController.onNewCall(INCOMING_CALL, callerIdNum, phoneReseive, "");

        } else if (event instanceof HangupEvent) {
            HangupEvent hangupEvent = (HangupEvent) event;
            if (hangupEvent.getChannel().startsWith("SIP/Intertelekom")) return;

//            System.out.println(hangupEvent);
            String callUniqueId =  addZero(hangupEvent.getUniqueId());
            String callerIdNum =  addZero(hangupEvent.getCallerIdNum());
            String phoneReseive = phonesFromAndPhonesTo.get(callerIdNum);
            TrackingController.onNewCall(ENDED_CALL, callerIdNum, phoneReseive, callUniqueId);
            phonesFromAndPhonesTo.remove(callerIdNum);

        } else if (event instanceof NewStateEvent) {
            NewStateEvent newStateEvent = (NewStateEvent) event;
            if (newStateEvent.getChannel().startsWith("SIP/Intertelekom")) return;

//            System.out.println(newStateEvent);
            int code = newStateEvent.getChannelState();
            String callerIdNum =  addZero(newStateEvent.getCallerIdNum());
            String phoneReseive = phonesFromAndPhonesTo.get(callerIdNum);
            if (code == 6) {
                TrackingController.onNewCall(ANSWER_CALL, callerIdNum, phoneReseive, "");
            }
        }
    }

    public static String addZero(String source){
        try {
            if (source.length() == 9 && !source.startsWith("0")) {
                source = "0" + source;
            }
        }catch (Exception e){
            System.out.println("Ошибка добавления нолика. Пришло " + source);
        }
        return source;
    }
}