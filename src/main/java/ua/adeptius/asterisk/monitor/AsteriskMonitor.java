package ua.adeptius.asterisk.monitor;

import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.ManagerAction;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.event.*;
import org.asteriskjava.manager.response.CommandResponse;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.dao.Settings;
import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.StatusAction;
import ua.adeptius.asterisk.model.Call;
import ua.adeptius.asterisk.utils.AsteriskActionsGenerator;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("Duplicates")
public class AsteriskMonitor implements ManagerEventListener {

    private static Logger LOGGER = LoggerFactory.getLogger(AsteriskMonitor.class.getSimpleName());

    private ManagerConnection managerConnection;

    private static HashMap<String, Boolean> sipsFree = new HashMap<>();
    private static HashMap<String, Boolean> sipsFreeOrRinging = new HashMap<>();

    public AsteriskMonitor() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                Settings.getSetting("asterisk.url"),
                Settings.getSetting("asterisk.login"),
                Settings.getSetting("asterisk.password"));
        this.managerConnection = factory.createManagerConnection();
    }

    public void run() throws IOException, AuthenticationFailedException,
            TimeoutException, InterruptedException {
        managerConnection.addEventListener(this);
        managerConnection.login();
        managerConnection.sendAction(new StatusAction());
        new SipsStateUpdater().start();
    }

    private class SipsStateUpdater extends Thread{
        @Override
        public void run() {
            while (true){
                try{
                    Thread.sleep(250);
                    updateSipsState();
                }catch (Exception ignored){
                }
            }
        }
    }


    public ManagerResponse sendAction(ManagerAction action, long timeout) throws IOException, TimeoutException {
        return managerConnection.sendAction(action, timeout);
    }


    public ManagerResponse sendCallToOutsideAction(String from, String to, @Nullable String callerName) throws IOException, TimeoutException {
        OriginateAction callToOutside = AsteriskActionsGenerator.callToOutside(from, to, callerName);
        return managerConnection.sendAction(callToOutside, 30000);

    }

    public ManagerResponse callToOutsideFromOuter(String outerPhone, String destinationPhone, @Nullable String callerName) throws IOException, TimeoutException {
        OriginateAction callToOutside = AsteriskActionsGenerator.callToOutsideFromOuter(outerPhone, destinationPhone, callerName);
        return managerConnection.sendAction(callToOutside, 30000);
    }



    private void updateSipsState() throws IOException, TimeoutException {
        CommandAction action= new CommandAction();
        action.setCommand("sip show inuse");
        CommandResponse response = (CommandResponse) managerConnection.sendAction(action);
        List<String> list = response.getResult();

        HashMap<String, Boolean> sipsFreeMap = new HashMap<>();
        HashMap<String, Boolean> sipsFreeOrRingingMap = new HashMap<>();
        for (String s : list) {
            if (s.startsWith("*")){
                continue;
            }
            String number = s.substring(0, s.indexOf(" "));
            boolean busy = s.substring(26,27).equals("1");
            boolean ringing = s.substring(28,29).equals("1");
            sipsFreeMap.put(number, !busy);
            sipsFreeOrRingingMap.put(number, busy && ringing || !busy && !ringing);
        }
        sipsFree = sipsFreeMap;
        sipsFreeOrRinging = sipsFreeOrRingingMap;
    }

    public static HashMap<String, Boolean> getSipsFree() {
        return sipsFree;
    }

    public static void setSipsFree(HashMap<String, Boolean> sipsFree) {
        AsteriskMonitor.sipsFree = sipsFree;
    }

    public static HashMap<String, Boolean> getSipsFreeOrRinging() {
        return sipsFreeOrRinging;
    }

    public static void setSipsFreeOrRinging(HashMap<String, Boolean> sipsFreeOrRinging) {
        AsteriskMonitor.sipsFreeOrRinging = sipsFreeOrRinging;
    }

    public void onManagerEvent(ManagerEvent event) {
        try{
            if (event instanceof NewChannelEvent) {
                CallProcessor.processEvent(event, ((NewChannelEvent) event).getUniqueId());

            } else if (event instanceof HangupEvent) {
                CallProcessor.processEvent(event, ((HangupEvent) event).getUniqueId());

            } else if (event instanceof NewExtenEvent) {
                NewExtenEvent extenEvent = (NewExtenEvent) event;
                if (!(extenEvent.getApplication().equals("Dial"))) {
                    return;
                }
                CallProcessor.processEvent(event, extenEvent.getUniqueId());

            } else if (event instanceof VarSetEvent) {
                VarSetEvent varSetEvent = (VarSetEvent) event;
                String key = varSetEvent.getVariable();
                String value = varSetEvent.getValue();
                if (!key.equals("DIALSTATUS")) {
                    return;
                }
                if (value == null) {
                    return;
                }
                CallProcessor.processEvent(event, varSetEvent.getUniqueId());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}