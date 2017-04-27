package ua.adeptius.asterisk.monitor;


import org.asteriskjava.manager.event.*;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.monitor.Call.CallState.ANSWERED;
import static ua.adeptius.asterisk.monitor.Call.CallState.BUSY;
import static ua.adeptius.asterisk.utils.logging.LogCategory.ENDED_CALL;

public class CallProcessor {


    public static HashMap<String, Call> calls = new HashMap<>();
    private static HashMap<String, User> phonesAndUsers = new HashMap<>();

    public static void processEvent(ManagerEvent event) {

//        System.out.println(event);

        if (event instanceof NewChannelEvent) { // если это новый звонок
            NewChannelEvent newChannelEvent = (NewChannelEvent) event;
//            System.out.println(event);
            String from = addZero(newChannelEvent.getCallerIdNum());
            String to = addZero(newChannelEvent.getExten());
            if ("s".equals(to)){
                return; // отбой странной ерунды при редиректе на сип
            }

            User user = phonesAndUsers.get(to);
            Call.Direction direction = Call.Direction.IN;
            if (user == null) {
                user = phonesAndUsers.get(from);
                direction = Call.Direction.OUT;
                if (user == null) {
//                    System.out.println("Связь не обнаружена:");
                    return;
                }
            }
            Call call = new Call();
            call.setId(newChannelEvent.getUniqueId());
            call.setTo(newChannelEvent.getExten());
            call.setFirstCall(newChannelEvent.getExten());
            call.setFrom(newChannelEvent.getCallerIdNum());
            call.setUser(user);
            // добавление 3 секунды из-за погрешности
            long time = 2000 + newChannelEvent.getDateReceived().getTime();
            call.setCalledMillis(time);
            call.setDateForDb(time);
            call.setDirection(direction);
            calls.put(newChannelEvent.getUniqueId(), call);


        } else if (event instanceof NewStateEvent) { // если это ответ на звонок
            NewStateEvent newStateEvent = (NewStateEvent) event;
            Call call = calls.get(newStateEvent.getUniqueId());
            if (call == null) return;
            if (newStateEvent.getChannelState() == 6) { // Если ответили
                int answeredIn = (int) (newStateEvent.getDateReceived().getTime() - call.getCalledMillis())/1000;
                call.setAnswered(answeredIn);
            }
            if (newStateEvent.getChannelStateDesc().equals("Up")){
                call.setCallState(ANSWERED);
            }else{ // newStateEvent.getChannelStateDesc().equals("Busy")
                call.setCallState(BUSY);
                call.setAnswered(0);
            }

        } else if (event instanceof NewExtenEvent) { // если это ответ на звонок
            NewExtenEvent newExtenEvent = (NewExtenEvent) event;
            Call call = calls.get(newExtenEvent.getUniqueId());
            if (call == null) return;
            String redirectedTo = newExtenEvent.getAppData();
            if (redirectedTo.contains(",")){
                redirectedTo = redirectedTo.substring(redirectedTo.lastIndexOf("/") + 1, redirectedTo.indexOf(","));
            }else {
                redirectedTo = redirectedTo.substring(redirectedTo.lastIndexOf("/") + 1);
            }
            call.setTo(redirectedTo);

        } else if (event instanceof HangupEvent) { // если это конец звонка
            HangupEvent hangupEvent = (HangupEvent) event;
            Call call = calls.get(hangupEvent.getUniqueId());
            calls.remove(hangupEvent.getUniqueId());
            if (call == null) return;

            if ("Call Rejected".equals(hangupEvent.getCauseTxt())){
                call.setCallState(Call.CallState.BUSY);
            }
            if (call.getCallState() == null){
                call.setCallState(Call.CallState.FAIL);
            }
            if (call.getCallState() == ANSWERED){
                int ended = (int) ((event.getDateReceived().getTime() - call.getCalledMillis())/1000)-call.getAnswered();
                call.setEnded(ended);
            }else {
                call.setEnded(0);
            }

            detectService(call);
            MyLogger.log(ENDED_CALL, call.getUser().getLogin() + ": завершен разговор " + call.getFrom() + " c " + call.getTo());

            System.out.println(call);
            processCall(call);
        }
    }

    private static void detectService(Call call) {
        User user = call.getUser();

        String from = call.getFrom();
        String to = call.getTo();

        if (user.getTracking() != null){
            List<String> list = user.getTracking().getPhones().stream().map(Phone::getNumber).collect(Collectors.toList());
            if (list.contains(to) || list.contains(from)){
                call.setService(Call.Service.TRACKING);
                return;
            }
        }

        if (user.getTelephony() != null) {
            List<String> inner = user.getTelephony().getInnerPhonesList();
            List<String> outer = user.getTelephony().getOuterPhonesList();
            if (inner.contains(to) || inner.contains(from) || outer.contains(to) || outer.contains(from)){
                call.setService(Call.Service.TELEPHONY);
            }
        }
    }


    private static void processCall(Call call){
        if (call.getService() == Call.Service.TRACKING){
            MainController.onNewSiteCall(call);
        }else if (call.getService() == Call.Service.TELEPHONY){
            MainController.onNewTelephonyCall(call);
        }
    }




    public static String addZero(String source) {
        try {
            if (source.length() == 9 && !source.startsWith("0")) {
                source = "0" + source;
            }
        } catch (Exception e) {
//            System.out.println("Ошибка добавления нолика. Пришло " + source);
        }
        return source;
    }

    public static void updatePhonesHashMap() {
        phonesAndUsers.clear();
        for (User user : UserContainer.getUsers()) {
            List<String> numbers = user.getTracking()==null? new ArrayList<>() : user.getTracking().getPhones().stream().map(Phone::getNumber).collect(Collectors.toList());
            numbers.addAll(user.getTelephony()==null? new ArrayList<>() : user.getTelephony().getOuterPhonesList());
            numbers.addAll(user.getTelephony()==null? new ArrayList<>() : user.getTelephony().getInnerPhonesList());
            numbers.forEach(s -> phonesAndUsers.put(s, user));
        }
    }
}
