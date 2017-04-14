package ua.adeptius.asterisk.monitor;


import org.asteriskjava.manager.event.*;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Customer;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.TelephonyCustomer;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.monitor.Call.CallState.ANSWERED;
import static ua.adeptius.asterisk.monitor.Call.CallState.BUSY;

public class CallProcessor {


    private static HashMap<String, String> phonesFromAndPhonesTo = new HashMap<>();

    private static HashMap<String, Call> calls = new HashMap<>();

    private static HashMap<String, Customer> phonesAndCustomers = new HashMap<>();

    public static void processEvent(ManagerEvent event) {
        if (event instanceof NewChannelEvent) { // если это новый звонок
            NewChannelEvent newChannelEvent = (NewChannelEvent) event;
//            System.out.println(event);
            String from = addZero(newChannelEvent.getCallerIdNum());
            String to = addZero(newChannelEvent.getExten());


            Customer customer = phonesAndCustomers.get(to);
            Call.Direction direction = Call.Direction.IN;
            if (customer == null) {
                customer = phonesAndCustomers.get(from);
                direction = Call.Direction.OUT;
                if (customer == null) {
                    return;
                }
            }
            // тут вычислять направление звонка

            Call call = new Call();
            call.setId(newChannelEvent.getUniqueId());
            call.setTo(newChannelEvent.getExten());
            call.setFrom(newChannelEvent.getCallerIdNum());
            call.setCustomer(customer);
            call.setCalled(newChannelEvent.getDateReceived());
            call.setDirection(direction);
            calls.put(newChannelEvent.getUniqueId(), call);

        } else if (event instanceof NewStateEvent) { // если это ответ на звонок
            NewStateEvent newStateEvent = (NewStateEvent) event;
            Call call = calls.get(newStateEvent.getUniqueId());
            if (call == null) return;
            if (newStateEvent.getChannelState() == 6) { // Если ответили
                call.setAnswered(newStateEvent.getDateReceived());
            }
            if (newStateEvent.getChannelStateDesc().equals("Up")){
                call.setCallState(ANSWERED);
            }else{ // newStateEvent.getChannelStateDesc().equals("Busy")
                call.setCallState(BUSY);
            }

        } else if (event instanceof NewExtenEvent) { // если это ответ на звонок
            Call call = calls.get(((NewExtenEvent) event).getUniqueId());
            if (call == null) return;
            String redirectedTo = ((NewExtenEvent) event).getAppData();
            redirectedTo = redirectedTo.substring(redirectedTo.lastIndexOf("/") + 1, redirectedTo.indexOf(","));
            call.setTo(redirectedTo);
//            System.out.println("RedirectedTo: " + redirectedTo);

        } else if (event instanceof HangupEvent) { // если это конец звонка
            Call call = calls.get(((HangupEvent) event).getUniqueId());
            if (call == null) return;
            call.setEnded(event.getDateReceived());
            processCall(call);
        }


//        if (event instanceof NewChannelEvent) {
//            NewChannelEvent newChannelEvent = (NewChannelEvent) event;
//            if (newChannelEvent.getChannel().startsWith("SIP/Intertelekom")) return;
//
////            System.out.println(newChannelEvent);
//            String callerIdNum = addZero(newChannelEvent.getCallerIdNum());
//            String phoneReseive = addZero(newChannelEvent.getExten());
//            phonesFromAndPhonesTo.put(callerIdNum, phoneReseive);
//            MainController.onNewCall(INCOMING_CALL, callerIdNum, phoneReseive, "");
//
//        } else if (event instanceof HangupEvent) {
//            HangupEvent hangupEvent = (HangupEvent) event;
//            if (hangupEvent.getChannel().startsWith("SIP/Intertelekom")) return;
//
////            System.out.println(hangupEvent);
//            String callUniqueId =  addZero(hangupEvent.getUniqueId());
//            String callerIdNum =  addZero(hangupEvent.getCallerIdNum());
//            String phoneReseive = phonesFromAndPhonesTo.get(callerIdNum);
//            MainController.onNewCall(ENDED_CALL, callerIdNum, phoneReseive, callUniqueId);
//            phonesFromAndPhonesTo.remove(callerIdNum);
//
//        } else if (event instanceof NewStateEvent) {
//            NewStateEvent newStateEvent = (NewStateEvent) event;
//            if (newStateEvent.getChannel().startsWith("SIP/Intertelekom")) return;
//
////            System.out.println(newStateEvent);
//            int code = newStateEvent.getChannelState();
//            String callerIdNum =  addZero(newStateEvent.getCallerIdNum());
//            String phoneReseive = phonesFromAndPhonesTo.get(callerIdNum);
//            if (code == 6) {
//                MainController.onNewCall(ANSWER_CALL, callerIdNum, phoneReseive, "");
//            }
//        }
    }


    private static void processCall(Call call){
        Customer customer = call.getCustomer();
        if (customer instanceof Site){
            MainController.onNewSiteCall(call);
        }else if (customer instanceof TelephonyCustomer){
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
        phonesAndCustomers.clear();
        List<Customer> allCustomers = MainController.getAllCustomers();
        for (Customer customer : allCustomers) {
            if (customer instanceof Site) {
                List<String> phones = ((Site) customer).getPhones().stream().map(Phone::getNumber).collect(Collectors.toList());
                for (String phone : phones) {
                    phonesAndCustomers.put(phone, customer);
                }
            } else if (customer instanceof TelephonyCustomer) {
                TelephonyCustomer telephonyCustomer = (TelephonyCustomer) customer;
                List<String> phones = telephonyCustomer.getOuterPhonesList();
                phones.addAll(telephonyCustomer.getInnerPhonesList());
                for (String phone : phones) {
                    phonesAndCustomers.put(phone, customer);
                }
            }
        }
    }
}
