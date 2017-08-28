package ua.adeptius.asterisk.test;

import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.*;
import org.asteriskjava.manager.event.*;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.utils.AsteriskActionsGenerator;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public class HelloMonitor implements ManagerEventListener {

    private static Logger LOGGER = LoggerFactory.getLogger(HelloMonitor.class.getSimpleName());

    private ManagerConnection managerConnection;

    public HelloMonitor() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                "cstat.nextel.com.ua","adeptius","ccb6f130f89de0bab95df361669e32ba");
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
        while(true){
            Thread.sleep(1000);
        }
    }

//    public ManagerResponse sendAction(ManagerAction action, long timeout) throws IOException, TimeoutException {
//        return managerConnection.sendAction(action, timeout);
//    }
//
//
//    public ManagerResponse sendCallToOutsideAction(String from, String to) throws IOException, TimeoutException {
//        OriginateAction callToOutside = AsteriskActionsGenerator.callToOutside(from, to);
//        return managerConnection.sendAction(callToOutside, 10000);
//    }

    public void onManagerEvent(ManagerEvent event) {
        if (event instanceof VarSetEvent && event.toString().contains("value=null")){
            return;
        }

//        if (event instanceof NewChannelEvent){
//            NewChannelEvent newChannelEvent = (NewChannelEvent) event;
//            String callerIdNum = newChannelEvent.getCallerIdNum();
//            if (!"934027182".equals(callerIdNum)){
//                return;
//            }
//            String channel = newChannelEvent.getChannel();
////            RedirectAction redirectAction = AsteriskActionsGenerator.redirectChanelToSip(newChannelEvent.getChannel(), "2001036");
//
//
////            QueueAddAction queueAddAction = new QueueAddAction();
////            queueAddAction.setQueue("queue1");
////            queueAddAction.setPaused(true);
////            queueAddAction.setInterface();
//
//
////            RedirectAction action = new RedirectAction();
////            action.setChannel(channel);
////            action.setContext("from-internal");
////            action.setExten("2001036");
////            action.setPriority(2);
////            try {
////                System.err.println(action);
////                ManagerResponse managerResponse = managerConnection.sendAction(action, 10000);
////                System.err.println(managerResponse);
////            } catch (IOException e) {
////                e.printStackTrace();
////            } catch (TimeoutException e) {
////                e.printStackTrace();
////                System.out.println("TIMEOUT!!");
////                action = new RedirectAction();
////                action.setChannel(channel);
////                action.setContext("from-word");
////                action.setExten("443211118");
////                action.setPriority(1);
////                try {
////                    ManagerResponse managerResponse = managerConnection.sendAction(action, 10000);
////                    System.err.println(managerResponse);
////                } catch (IOException e1) {
////                    e1.printStackTrace();
////                } catch (TimeoutException e1) {
////                    System.out.println("Timeout2 ignored");
////                    e1.printStackTrace();
////                }
////            }
//        }


        System.out.println(makePrettyLog(event));
//        try{
//            if (event instanceof NewChannelEvent) {
//                CallProcessor.processEvent(event, ((NewChannelEvent) event).getUniqueId());
//
//            } else if (event instanceof HangupEvent) {
//                CallProcessor.processEvent(event, ((HangupEvent) event).getUniqueId());
//
//            } else if (event instanceof NewExtenEvent) {
//                NewExtenEvent extenEvent = (NewExtenEvent) event;
//                if (!(extenEvent.getApplication().equals("Dial"))) {
//                    return;
//                }
//                CallProcessor.processEvent(event, extenEvent.getUniqueId());
//
//            } else if (event instanceof VarSetEvent) {
//                VarSetEvent varSetEvent = (VarSetEvent) event;
//                String key = varSetEvent.getVariable();
//                String value = varSetEvent.getValue();
//                if (!key.equals("DIALSTATUS")) {
//                    return;
//                }
//                if (value == null) {
//                    return;
//                }
//                CallProcessor.processEvent(event, varSetEvent.getUniqueId());
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }

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
        return s;
    }

    private static String removeRegexFromString(String log, String regex) {
        Matcher regexMatcher = Pattern.compile(regex).matcher(log);
        if (regexMatcher.find()) {
            log = log.replaceAll(regexMatcher.group(), "");
        }
        return log;
    }
}