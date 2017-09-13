package ua.adeptius.asterisk.monitor;

import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.ManagerAction;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.event.*;
import org.asteriskjava.manager.response.CommandResponse;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.dao.Settings;
import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.StatusAction;
import ua.adeptius.asterisk.model.Call;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.utils.AsteriskActionsGenerator;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ua.adeptius.asterisk.utils.MyStringUtils.addZero;


public class AsteriskMonitor implements ManagerEventListener {

    private static Logger LOGGER = LoggerFactory.getLogger(AsteriskMonitor.class.getSimpleName());

    private ManagerConnection managerConnection;

    private static HashMap<String, Boolean> sipsFree = new HashMap<>();
    private static HashMap<String, Boolean> sipsFreeOrRinging = new HashMap<>();

    public AsteriskMonitor() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(Main.settings.getAsteriskUrl(),
                Main.settings.getAsteriskLogin(), Main.settings.getAsteriskPassword());
        this.managerConnection = factory.createManagerConnection();
    }

    public void run() throws IOException, AuthenticationFailedException,
            TimeoutException, InterruptedException {
        managerConnection.addEventListener(this);
        managerConnection.login();
        managerConnection.sendAction(new StatusAction());
        new SipsStateUpdater().start();
    }

    private class SipsStateUpdater extends Thread {

        public SipsStateUpdater() {
            setName("SipsStateUpdater");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(250);
                    updateSipsState();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception ignored) {
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
        CommandAction action = new CommandAction();
        action.setCommand("sip show inuse");
        CommandResponse response = (CommandResponse) managerConnection.sendAction(action);
        List<String> list = response.getResult();

        HashMap<String, Boolean> sipsFreeMap = new HashMap<>();
        HashMap<String, Boolean> sipsFreeOrRingingMap = new HashMap<>();
        for (String s : list) {
            if (s.startsWith("*")) {
                continue;
            }
            String number = s.substring(0, s.indexOf(" "));
            boolean busy = s.substring(26, 27).equals("1");
            boolean ringing = s.substring(28, 29).equals("1");
            sipsFreeMap.put(number, !busy);
            sipsFreeOrRingingMap.put(number, busy && ringing || !busy && !ringing);
        }
        sipsFree = sipsFreeMap;
        sipsFreeOrRinging = sipsFreeOrRingingMap;
    }

    public static HashMap<String, Boolean> getSipsFree() {
        return sipsFree;
    }

    public static HashMap<String, Boolean> getSipsFreeOrRinging() {
        return sipsFreeOrRinging;
    }


    public void onManagerEvent(ManagerEvent event) {
        try {
            if (event instanceof NewChannelEvent) {
                NewChannelEvent newChannelEvent = (NewChannelEvent) event;

                if (newChannelEvent.getExten().equals("s")) {
                    return; // Сомнительно блокировать.
                    // это фильтрует вторую цепочку логов при звонке с сип на сип
                }

                CallProcessor.processEvent(newChannelEvent);
                return;
            }


            if (event instanceof VarSetEvent) {
                VarSetEvent varSetEvent = (VarSetEvent) event;
                String variable = varSetEvent.getVariable();

                if (!variable.equals("ANSWEREDTIME") && !variable.equals("DIALEDTIME")
                        && !variable.equals("redirectedTo") && !variable.equals("DIALEDPEERNUMBER")) {
                    return;
                }

                if (varSetEvent.getChannel() == null) {
                    return;
                }

                String value = varSetEvent.getValue();
                if (value == null || value.equals("null")) {
                    return;
                }

                CallProcessor.processEvent(varSetEvent);

                return;
            }

            if (event instanceof HangupEvent) {
                HangupEvent hangupEvent = (HangupEvent) event;
                CallProcessor.processEvent(hangupEvent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}