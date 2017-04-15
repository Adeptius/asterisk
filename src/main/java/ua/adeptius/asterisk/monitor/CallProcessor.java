package ua.adeptius.asterisk.monitor;


import org.asteriskjava.manager.event.*;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Customer;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.TelephonyCustomer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.monitor.Call.CallState.ANSWERED;
import static ua.adeptius.asterisk.monitor.Call.CallState.BUSY;

public class CallProcessor {

    //TODO нужно ли чистить мапу?
    private static HashMap<String, Call> calls = new HashMap<>();
    private static HashMap<String, Customer> phonesAndCustomers = new HashMap<>();

    public static void processEvent(ManagerEvent event) {

//        System.out.println(event);

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
            call.setFrom(newChannelEvent.getCallerIdNum());
            call.setCustomer(customer);
            // добавление 3 секунды из-за погрешности
            long time = 5000 + newChannelEvent.getDateReceived().getTime();
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
            Call call = calls.get(((NewExtenEvent) event).getUniqueId());
            if (call == null) return;
            String redirectedTo = ((NewExtenEvent) event).getAppData();
            redirectedTo = redirectedTo.substring(redirectedTo.lastIndexOf("/") + 1, redirectedTo.indexOf(","));
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
                List<String> phones = new ArrayList<>();
                phones.addAll(telephonyCustomer.getOuterPhonesList());
                phones.addAll(telephonyCustomer.getInnerPhonesList());
                for (String phone : phones) {
                    phonesAndCustomers.put(phone, customer);
                }
            }
        }
    }
}
