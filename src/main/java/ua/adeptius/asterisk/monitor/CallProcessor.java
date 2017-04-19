package ua.adeptius.asterisk.monitor;


import org.asteriskjava.manager.event.*;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.newmodel.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.monitor.Call.CallState.ANSWERED;
import static ua.adeptius.asterisk.monitor.Call.CallState.BUSY;

public class CallProcessor {

    //TODO нужно ли чистить мапу?
    private static HashMap<String, Call> calls = new HashMap<>();
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
//                    System.out.println(event);
                    return;
                }
            }
            // тут вычислять направление звонка
//            System.out.println("Звонок для " + customer.getName());
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
//            System.out.println("RedirectedTo: " + redirectedTo);
//            System.out.println(call);

        } else if (event instanceof HangupEvent) { // если это конец звонка
            HangupEvent hangupEvent = (HangupEvent) event;
            Call call = calls.get(hangupEvent.getUniqueId());
//            System.out.println("Looking for id " + hangupEvent.getUniqueId());
//            System.out.println(call);
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

            System.out.println(call);
            processCall(call);
        }
    }


    private static void processCall(Call call){ // TODO обработать
        User user = call.getUser();

//        if (customer instanceof OldSite){
//            MainController.onNewSiteCall(call);
//        }else if (customer instanceof TelephonyCustomer){
//            MainController.onNewTelephonyCall(call);
//        }
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

    public static void updatePhonesHashMap() { //TODO везде повставлять
        phonesAndUsers.clear();
        for (User user : MainController.users) {
            List<String> numbers = user.getSite()==null? new ArrayList<>() : user.getSite().getPhones().stream().map(Phone::getNumber).collect(Collectors.toList());
            numbers.addAll(user.getTelephony()==null? new ArrayList<>() : user.getTelephony().getOuterPhonesList());
            numbers.addAll(user.getTelephony()==null? new ArrayList<>() : user.getTelephony().getInnerPhonesList());
            numbers.forEach(s -> phonesAndUsers.put(s, user));
        }
    }
}
