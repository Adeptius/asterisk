package ua.adeptius.asterisk.test;

import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.*;
import org.asteriskjava.manager.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public class HelloMonitor implements ManagerEventListener {

    private static Logger LOGGER = LoggerFactory.getLogger(HelloMonitor.class.getSimpleName());

    private ManagerConnection managerConnection;

    public HelloMonitor() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                "cstat.nextel.com.ua", "adeptius", "ccb6f130f89de0bab95df361669e32ba");
        this.managerConnection = factory.createManagerConnection();
    }

    public void run() throws IOException, AuthenticationFailedException,
            TimeoutException, InterruptedException {
        managerConnection.addEventListener(this);
        managerConnection.login();
        managerConnection.sendAction(new StatusAction());
    }

    public static void main(String[] args) throws Exception {
        HelloMonitor helloMonitor = new HelloMonitor();
        helloMonitor.run();
        while (true) {
            Thread.sleep(1000);
        }
    }

    public void onManagerEvent(ManagerEvent event) {

        if (event instanceof NewChannelEvent) {
            NewChannelEvent newChannelEvent = (NewChannelEvent) event;

//            if (chanelToShow == null) {
//                chanelToShow = newChannelEvent.getChannel();
//            }
//            if (!chanelToShow.equals(newChannelEvent.getChannel())) return;

            System.out.println(makePrettyLog(event));

            return;
        }


//        if (chanelToShow == null) {
//            return;
//        }


        if (event instanceof HangupEvent) {
            HangupEvent hangupEvent = (HangupEvent) event;

//            if (!chanelToShow.equals(hangupEvent.getChannel())) return;


            System.out.println(makePrettyLog(event));

            return;
        }


        if (event instanceof NewExtenEvent) {
            NewExtenEvent newExtenEvent = (NewExtenEvent) event;
//            if (!chanelToShow.equals(newExtenEvent.getChannel())) return;
//            if (
//                    newExtenEvent.getExtension().equals("recordcheck")
//                            || newExtenEvent.getContext().equals("sub-record-check")
//                            || newExtenEvent.getApplication().equals("Set")
//                            || newExtenEvent.getApplication().equals("GotoIf")
//                            || newExtenEvent.getApplication().equals("ExecIf")
//                            || newExtenEvent.getApplication().equals("GosubIf")
//                            || newExtenEvent.getApplication().equals("Macro")
//                            || newExtenEvent.getApplication().equals("MacroExit")
//                            || newExtenEvent.getApplication().equals("AGI")
//                            || newExtenEvent.getApplication().equals("NoOp")
//                            || newExtenEvent.getApplication().equals("Gosub")
//                    ) {
//                return;
//            }


            System.out.println(makePrettyLog(event));

            return;
        }


        if (event instanceof VarSetEvent) {
            VarSetEvent varSetEvent = (VarSetEvent) event;
//            if (!chanelToShow.equals(varSetEvent.getChannel())) return;
            List<String> variablesToSkip = new ArrayList<>();
//            variablesToSkip.add("MACRO_DEPTH");
//            variablesToSkip.add("NOW");
//            variablesToSkip.add("__DAY");
//            variablesToSkip.add("__MONTH");
//            variablesToSkip.add("__YEAR");
//            variablesToSkip.add("__TIMESTR");
//            variablesToSkip.add("__MON_FMT");
//            variablesToSkip.add("__REC_POLICY_MODE");
//            variablesToSkip.add("__CALLFILENAME");
//            variablesToSkip.add("MIXMONITOR_FILENAME");
//            variablesToSkip.add("LOCAL_MIXMON_ID");
//            variablesToSkip.add("__MIXMON_ID");
//            variablesToSkip.add("__RECORD_ID");
//            variablesToSkip.add("__REC_STATUS");
//            variablesToSkip.add("LOCAL(ARG1)");
//            variablesToSkip.add("LOCAL(ARG2)");
//            variablesToSkip.add("LOCAL(ARG3)");
//            variablesToSkip.add("LOCAL(ARGC)");
//            variablesToSkip.add("SIPDOMAIN");
//            variablesToSkip.add("AJ_AGISTATUS");
//            variablesToSkip.add("AGISTATUS");
//            variablesToSkip.add("BRIDGEPVTCALLID");
//            variablesToSkip.add("BRIDGEPEER");
//            variablesToSkip.add("FROMEXTEN");
//            variablesToSkip.add("NoOp");
//            variablesToSkip.add("SIPCALLID");
//            variablesToSkip.add("num");
//            variablesToSkip.add("");
//            variablesToSkip.add("__FROM_DID");
//            variablesToSkip.add("MACRO_EXTEN");
//            variablesToSkip.add("MACRO_CONTEXT");
//            variablesToSkip.add("MACRO_PRIORITY");
//            variablesToSkip.add("ARG1");
//            variablesToSkip.add("ARG2");
//            variablesToSkip.add("TOUCH_MONITOR");
//            variablesToSkip.add("AMPUSER");
//            variablesToSkip.add("MOHCLASS");
//            variablesToSkip.add("ARG4");
//            variablesToSkip.add("DIAL_TRUNK");
//            variablesToSkip.add("OUTBOUND_GROUP");
//            variablesToSkip.add("DB_RESULT");
//            variablesToSkip.add("TRUNKOUTCID"); // внешний шлюз
//            variablesToSkip.add("REALCALLERIDNUM");
//            variablesToSkip.add("TRUNKCIDOVERRIDE");
//            variablesToSkip.add("OUTNUM");
//            variablesToSkip.add("DIAL_NUMBER");
//            variablesToSkip.add("custom");
//            variablesToSkip.add("MACRO_IN_HANGUP");
//            variablesToSkip.add("SIPURI");
//            variablesToSkip.add("DIALSTATUS"); // содержит CHANUNAVAIL ANSWER
//            variablesToSkip.add("DID");
//            variablesToSkip.add("DIALEDPEERNAME");
//            variablesToSkip.add("DIALEDPEERNUMBER");// показывает номер того кто ответил в этот момент, но эта инфа есть в HangupEvent - connectedlinename

            String variable = varSetEvent.getVariable();
            String value = varSetEvent.getValue();
//            if (value == null || value.equals("null") || variable.startsWith("RTPAUDIOQOS") || variablesToSkip.contains(variable)) {
//                return;
//            }

            System.out.println(makePrettyLog(event));
        }
    }

    private static int i = 0;

    private static String makePrettyLog(ManagerEvent event) {
        String s = event.toString();
        s = s.substring(31);
        if (s.contains("timestamp=null,")) {
            s = s.replaceAll("timestamp=null,", "");
        }
        if (s.contains("sequencenumber=null,")) {
            s = s.replaceAll("sequencenumber=null,", "");
        }
        if (s.contains("server=null,")) {
            s = s.replaceAll("server=null,", "");
        }
        if (s.contains("actionid=null,")) {
            s = s.replaceAll("actionid=null,", "");
        }
        if (s.contains("connectedlinenum=null,")) {
            s = s.replaceAll("connectedlinenum=null,", "");
        }
        if (s.contains("accountcode=null,")) {
            s = s.replaceAll("accountcode=null,", "");
        }
        if (s.contains("connectedlinename=null,")) {
            s = s.replaceAll("connectedlinename=null,", "");
        }
        if (s.contains("calleridname=null,")) {
            s = s.replaceAll("calleridname=null,", "");
        }
        s = removeRegexFromString(s, "dateReceived='.*2017',");
        s = removeRegexFromString(s, "systemHashcode=\\d{8,10}");
        s = removeRegexFromString(s, "channel='SIP\\/\\d*-[\\d|\\w]*',");
        s = removeRegexFromString(s, "privilege='\\w*,\\w*',");
        s = removeRegexFromString(s, "privilege='\\w*,\\w*',");
        s = removeRegexFromString(s, "priority='\\d*',");
        return s;
//        System.out.println(s);
//        return event.toString();
    }

    private static String removeRegexFromString(String log, String regex) {
        Matcher regexMatcher = Pattern.compile(regex).matcher(log);
        if (regexMatcher.find()) {
            log = log.replaceAll(regexMatcher.group(), "");
        }
        return log;
    }
}