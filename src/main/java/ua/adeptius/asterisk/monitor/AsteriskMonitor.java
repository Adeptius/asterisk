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

    private class SipsStateUpdater extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(250);
                    updateSipsState();
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

//    public static void setSipsFree(HashMap<String, Boolean> sipsFree) {
//        AsteriskMonitor.sipsFree = sipsFree;
//    }

    public static HashMap<String, Boolean> getSipsFreeOrRinging() {
        return sipsFreeOrRinging;
    }

//    public static void setSipsFreeOrRinging(HashMap<String, Boolean> sipsFreeOrRinging) {
//        AsteriskMonitor.sipsFreeOrRinging = sipsFreeOrRinging;
//    }

    /**
     * 1 Мониторинг логов астериска
     * 2 Первичная фильтрация от ненужных элементов
     * 3 Передача информативных логов в call processor
     *
     * @see AsteriskLogAnalyzer
     */
    public void onManagerEvent(ManagerEvent event) {
        try {
            if (event instanceof NewChannelEvent) {
                NewChannelEvent newChannelEvent = (NewChannelEvent) event;

                if (newChannelEvent.getExten().equals("s")) {
                    return; // Сомнительно блокировать.
                    // это фильтрует вторую цепочку логов при звонке с сип на сип
                }

                CallProcessor.processEvent(newChannelEvent, newChannelEvent.getUniqueId());
                return;
            }


            if (event instanceof NewExtenEvent) {
                NewExtenEvent newExtenEvent = (NewExtenEvent) event;

                String extension = newExtenEvent.getExtension();
                if (extension != null && extension.equals("recordcheck")) {
                    return;
                }

                String context = newExtenEvent.getContext();
                if (context != null) {
                    if (context.equals("app-blacklist-check") || context.equals("sub-record-check")) {
                        return;
                    }
                }

                String application = newExtenEvent.getApplication();
                if (application != null) {
                    if (application.equals("Set") || application.equals("GotoIf")
                            || application.equals("ExecIf") || application.equals("GosubIf")
                            || application.equals("Macro") || application.equals("MacroExit")
                            || application.equals("AGI") || application.equals("NoOp")
                            || application.equals("Gosub") || application.equals("Hangup")) {
                        return;
                    }
                }

                CallProcessor.processEvent(newExtenEvent, newExtenEvent.getUniqueId());
                return;
            }


            if (event instanceof VarSetEvent) {
                VarSetEvent varSetEvent = (VarSetEvent) event;
                if (varSetEvent.getChannel() == null) {
                    return;
                }

                String variable = varSetEvent.getVariable();
                String value = varSetEvent.getValue();
                if (value == null || value.equals("null") || variable.startsWith("RTPAUDIOQOS") || getVariablesToSkip().contains(variable)) {
                    return;
                }

                CallProcessor.processEvent(varSetEvent, varSetEvent.getUniqueId());
                return;
            }

            if (event instanceof HangupEvent) {
                HangupEvent hangupEvent = (HangupEvent) event;
                CallProcessor.processEvent(hangupEvent, hangupEvent.getUniqueId());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private static List<String> variablesToSkip;

    public static List<String> getVariablesToSkip() {
        if (variablesToSkip == null) {
            variablesToSkip = new ArrayList<>();
            variablesToSkip.add("MACRO_DEPTH");
            variablesToSkip.add("NOW");
            variablesToSkip.add("__DAY");
            variablesToSkip.add("__MONTH");
            variablesToSkip.add("__YEAR");
            variablesToSkip.add("__TIMESTR");
            variablesToSkip.add("__MON_FMT");
            variablesToSkip.add("__REC_POLICY_MODE");
            variablesToSkip.add("__CALLFILENAME");
            variablesToSkip.add("MIXMONITOR_FILENAME");
            variablesToSkip.add("LOCAL_MIXMON_ID");
            variablesToSkip.add("__MIXMON_ID");
            variablesToSkip.add("__RECORD_ID");
            variablesToSkip.add("__REC_STATUS");
            variablesToSkip.add("LOCAL(ARG1)");
            variablesToSkip.add("LOCAL(ARG2)");
            variablesToSkip.add("LOCAL(ARG3)");
            variablesToSkip.add("LOCAL(ARGC)");
            variablesToSkip.add("SIPDOMAIN");
            variablesToSkip.add("AJ_AGISTATUS");
            variablesToSkip.add("AGISTATUS");
            variablesToSkip.add("BRIDGEPVTCALLID");
            variablesToSkip.add("BRIDGEPEER");
            variablesToSkip.add("FROMEXTEN");
            variablesToSkip.add("NoOp");
            variablesToSkip.add("SIPCALLID");
            variablesToSkip.add("num");
            variablesToSkip.add("");
            variablesToSkip.add("__FROM_DID");
            variablesToSkip.add("MACRO_EXTEN");
            variablesToSkip.add("MACRO_CONTEXT");
            variablesToSkip.add("MACRO_PRIORITY");
            variablesToSkip.add("ARG1");
            variablesToSkip.add("ARG2");
            variablesToSkip.add("TOUCH_MONITOR");
            variablesToSkip.add("AMPUSER");
            variablesToSkip.add("MOHCLASS");
            variablesToSkip.add("ARG4");
            variablesToSkip.add("DIAL_TRUNK");
            variablesToSkip.add("OUTBOUND_GROUP");
            variablesToSkip.add("DB_RESULT");
            variablesToSkip.add("TRUNKOUTCID"); // внешний шлюз
            variablesToSkip.add("REALCALLERIDNUM");
            variablesToSkip.add("TRUNKCIDOVERRIDE");
            variablesToSkip.add("OUTNUM");
            variablesToSkip.add("DIAL_NUMBER");
            variablesToSkip.add("custom");
            variablesToSkip.add("MACRO_IN_HANGUP");
            variablesToSkip.add("SIPURI");
            variablesToSkip.add("DIALSTATUS"); // содержит CHANUNAVAIL ANSWER
            variablesToSkip.add("DID");
            variablesToSkip.add("DIALEDPEERNAME");
            // показывает номер того кто ответил в этот момент, но эта инфа есть в HangupEvent - connectedlinename
            // но необходимо при звонках на gsm
//            variablesToSkip.add("DIALEDPEERNUMBER");
            variablesToSkip.add("CALLED_BLACKLIST");
            variablesToSkip.add("DIAL_TRUNK_OPTIONS");
            variablesToSkip.add("__FROMEXTEN");
            variablesToSkip.add("PLAYBACKSTATUS");
        }
        return variablesToSkip;
    }
}