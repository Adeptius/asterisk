package ua.adeptius.asterisk.monitor;


import org.asteriskjava.manager.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.monitor.NewCall.CallState.*;
import static ua.adeptius.asterisk.monitor.NewCall.Service.TELEPHONY;
import static ua.adeptius.asterisk.monitor.NewCall.Service.TRACKING;

@SuppressWarnings("Duplicates")
public class CallProcessor {

    private static Logger LOGGER = LoggerFactory.getLogger(CallProcessor.class.getSimpleName());

    //    public static HashMap<String, Call> calls = new HashMap<>();
    public static HashMap<String, NewCall> calls = new HashMap<>();
    private static HashMap<String, User> phonesAndUsers = new HashMap<>();

    public static void processEvent(ManagerEvent event, String id) {

        /**
         * Только NewChannelEvent означает что это новый звонок
         * И только в случае, если он содержит номер какого-либо сервиса пользователя - то ID этого ивента
         * добавляется в мапу calls для его дальнейшего отслеживания.
         *
         * Другие NewChannelEvent, у которых связи с пользователями нет - игнорируются
         */
        if (event instanceof NewChannelEvent) { // если это новый звонок
            NewChannelEvent newChannelEvent = (NewChannelEvent) event;
            String from = addZero(newChannelEvent.getCallerIdNum());
            String to = addZero(newChannelEvent.getExten());
            if ("s".equals(to) && !("from-internal".equals(newChannelEvent.getContext()))) {
                return; // отбой странной ерунды при редиректе на сип
            }

            // Ищем связь с сервисом и определяем направление звонка
            User user = phonesAndUsers.get(to);
            NewCall.Direction direction = NewCall.Direction.IN;
            if (user == null) {
                user = phonesAndUsers.get(from);
                direction = NewCall.Direction.OUT;
                if (user == null) {
                    // Связь звонящих номеров с каким-либо сервисом не обнаружена
                    // Следовательно дальше не идём
                    return;
                }
            }

            LOGGER.trace("ID {} NewChannelEvent: {}", id, newChannelEvent);

            NewCall newCall = new NewCall();
            newCall.setAsteriskId(newChannelEvent.getUniqueId());
            newCall.setCalledTo(newChannelEvent.getExten());
            newCall.setFirstCall(newChannelEvent.getExten());
            newCall.setCalledFrom(newChannelEvent.getCallerIdNum());
            newCall.setUser(user);
            newCall.setCalledDate(newChannelEvent.getDateReceived());
            newCall.setDirection(direction);

//            newCall.addEvent(event);

            calls.put(newChannelEvent.getUniqueId(), newCall);
            LOGGER.debug("Поступил новый звонок {} ->", newCall.getCalledFrom());
            System.out.println(newChannelEvent.getChannel() + " " + newChannelEvent.getUniqueId());
            System.out.println(newChannelEvent);
//            print(event);
//            printMap();
//            System.out.println("НОВЫЙ ЗВОНОК!!!\n\n\n");
//            System.out.println(event);
        } else {
            //Если это событие не новый звонок - то убеждаемся по его id что соответствующий обьект Call c таким ID уже существует.
            //А если это прилетело что-то непонятное - дальше не идём и не захламляем логи.

            NewCall newCall = calls.get(id);
            if (newCall == null) {
                return;
            }
//            newCall.addEvent(event);

//            print(event);


//            } else
            if (event instanceof NewExtenEvent) {
                NewExtenEvent newExtenEvent = (NewExtenEvent) event;
                LOGGER.trace("ID {} NewExtenEvent: {}", id, newExtenEvent);
                String redirectedTo = newExtenEvent.getAppData();
                if (redirectedTo.contains(",")) {
                    redirectedTo = redirectedTo.substring(redirectedTo.lastIndexOf("/") + 1, redirectedTo.indexOf(","));
                } else {
                    redirectedTo = redirectedTo.substring(redirectedTo.lastIndexOf("/") + 1);
                }
                newCall.setCalledTo(redirectedTo);
                LOGGER.debug("Звонок перенаправлен на {} -> {}", newCall.getCalledFrom(), newCall.getCalledTo());
                //            System.out.println(event);

//                sip->gsm - newExtenEvent.getAppData(); режется SIP/Intertelekom_main/0934027182,300,Tt
                //Можно определять редирект по VarSetEvent OUTNUM, DIAL_NUMBER

//                asterisk->sip->gsm newExtenEvent.getAppData();режется SIP/Intertelekom_main/0934027182,300,Tt
                //Можно определять редирект по VarSetEvent OUTNUM, DIAL_NUMBER


//                System.out.println("РЕДИРЕКТ!!!!\n\n\n");

            } else if (event instanceof VarSetEvent) {
                VarSetEvent varSetEvent = (VarSetEvent) event;
                if (varSetEvent.getVariable().equals("DIALSTATUS")) {
//                    System.out.println("!!!DIALSTATUS=" + varSetEvent.getValue());
                    LOGGER.trace("ID {} VarSetEvent: {}", id,varSetEvent);

                    String dialStatus = varSetEvent.getValue();
                    if ("ANSWER".equals(dialStatus)) { // Кто-то взял трубку
                        // диалстатус бывает второй раз по завершению звонка, а он нам не нужен.
//                        if (newCall.getAnsweredDate() == null){
//                        } // ввёл эту защиту в сеттере объекта Call
                        newCall.setAnsweredDate(event.getDateReceived());
                        newCall.setCallState(ANSWER);
                    } else {
                        // добавить все остальные типы состояния
                        if ("BUSY".equals(dialStatus)) {
                            newCall.setCallState(BUSY);
                        } else if ("NOANSWER".equals(dialStatus) || "CANCEL".equals(dialStatus)) {
                            //CANCEL - это если звонить на внешний с редиректом на сип и сип не взял трубку за 90 сек
                            newCall.setCallState(NOANSWER);
                        } else if ("CHANUNAVAIL".equals(dialStatus)) {
                            //вызываемый номер был недоступен
                            newCall.setCallState(CHANUNAVAIL);
                        }
                        else {
                            LOGGER.error("ДОБАВИТЬ СТАТУС ЗВОНКА: {}", dialStatus);
                        }
                    }
                    LOGGER.debug("Состояние звонка установлено на: {}",newCall.getCallState());
                }
                return;
            }
            if (event instanceof HangupEvent) { // окончание звонка
                HangupEvent hangupEvent = (HangupEvent) event;
                LOGGER.trace("ID {} HangupEvent: {}", id, hangupEvent);

                calls.remove(id);
                newCall.setEndedDate(event.getDateReceived());

                // всегда определяет конец разговора не содержит никакой инфы при звонке sip->gsm

                if ("s".equals(newCall.getCalledTo())) {
                    LOGGER.trace("{} не дождался совершения исходящего звонка", newCall.getCalledTo());
                    return; // обязательно нужен этот отбойник для фильтрования второго звонка при звонке снаружи на сип (gsm - outer- sip)
                }

                detectService(newCall);
                LOGGER.info("Завершен разговор {} c {}", newCall.getCalledFrom(), newCall.getCalledTo());

                processCall(newCall);
                if (calls.size() > 5){
                    LOGGER.warn("Айдишников<->Звонков в мапе {}", calls.size());
                }

                if (newCall.getCallState() == null){
                    LOGGER.error("Завершен разговор c состоянием null! "+ newCall);
                }else {
                    LOGGER.debug("Завершен разговор: {}", newCall);
                }
            }
        }
    }

    private static void printMap() {
        System.out.println("-----СОДЕРЖИМОЕ МАПЫ-----");
        calls.forEach((s, newCall) -> System.out.println("id " + s + " call: " + newCall.getCalledFrom() + "->" + newCall.getCalledTo()));
    }

    private static void print(ManagerEvent event) {
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
        s = removeRegexFromString(s, "dateReceived='.*2017',");
        s = removeRegexFromString(s, "systemHashcode=\\d{8,10}");
        s = removeRegexFromString(s, "channel='SIP\\/\\d*-[\\d|\\w]*',");
        s = removeRegexFromString(s, "privilege='\\w*,\\w*',");
//        s = removeRegexFromString(s, "uniqueid='\\d*.\\d*',");


        System.out.println(s);
    }


    private static String removeRegexFromString(String log, String regex) {
        Matcher regexMatcher = Pattern.compile(regex).matcher(log);
        if (regexMatcher.find()) {
            log = log.replaceAll(regexMatcher.group(), "");
        }
        return log;
    }

    private static void detectService(NewCall call) {
        User user = call.getUser();

        String from = call.getCalledFrom();
        String to = call.getCalledTo();

        if (user.getTracking() != null) {
            List<String> list = user.getTracking().getPhones().stream().map(Phone::getNumber).collect(Collectors.toList());
            if (list.contains(to) || list.contains(from)) {
                call.setService(TRACKING);
                return;
            }
        }

        if (user.getTelephony() != null) {
            List<String> inner = user.getTelephony().getInnerPhonesList();
            List<String> outer = user.getTelephony().getOuterPhonesList();
            if (inner.contains(to) || inner.contains(from) || outer.contains(to) || outer.contains(from)) {
                call.setService(TELEPHONY);
            }
        }
    }


    private static void processCall(NewCall call) {
        if (call.getService() == NewCall.Service.TRACKING) {
            MainController.onNewSiteCall(call);
        } else if (call.getService() == NewCall.Service.TELEPHONY) {
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
        LOGGER.trace("Обновление карты Number <-> User");
        phonesAndUsers.clear();
        for (User user : UserContainer.getUsers()) {
            List<String> numbers = user.getTracking() == null ? new ArrayList<>() : user.getTracking().getPhones().stream().map(Phone::getNumber).collect(Collectors.toList());
            numbers.addAll(user.getTelephony() == null ? new ArrayList<>() : user.getTelephony().getOuterPhonesList());
            numbers.addAll(user.getTelephony() == null ? new ArrayList<>() : user.getTelephony().getInnerPhonesList());
            numbers.forEach(s -> phonesAndUsers.put(s, user));
        }
    }


    /**
     //        System.out.println(event);
     if (event instanceof NewChannelEvent) { // если это новый звонок
     NewChannelEvent newChannelEvent = (NewChannelEvent) event;
     //            System.out.println(event);
     String from = addZero(newChannelEvent.getCallerIdNum());
     String to = addZero(newChannelEvent.getExten());
     if ("s".equals(to) && !("from-internal".equals(newChannelEvent.getContext()))) {
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
     LOGGER.trace("Поступил новый звонок {} ->", call.getFrom());
     //            System.out.println(event);


     } else if (event instanceof NewStateEvent) { // если это ответ на звонок
     NewStateEvent newStateEvent = (NewStateEvent) event;
     Call call = calls.get(newStateEvent.getUniqueId());
     if (call == null) return;
     if (newStateEvent.getChannelState() == 6) { // Если ответили
     //                int answeredIn = (int) (newStateEvent.getDateReceived().getTime() - call.getCalledMillis()) / 1000;
     //                call.setAnswered(answeredIn);
     }
     if (newStateEvent.getChannelStateDesc().equals("Up")) {
     call.setCallState(ANSWER);
     } else { // newStateEvent.getChannelStateDesc().equals("Busy")
     call.setCallState(BUSY);
     call.setAnswered(0);
     }
     //            System.out.println(event);


     } else if (event instanceof NewExtenEvent) { // если это перенаправление звонка
     NewExtenEvent newExtenEvent = (NewExtenEvent) event;
     Call call = calls.get(newExtenEvent.getUniqueId());
     if (call == null) return;
     String redirectedTo = newExtenEvent.getAppData();
     if (redirectedTo.contains(",")) {
     redirectedTo = redirectedTo.substring(redirectedTo.lastIndexOf("/") + 1, redirectedTo.indexOf(","));
     } else {
     redirectedTo = redirectedTo.substring(redirectedTo.lastIndexOf("/") + 1);
     }
     call.setTo(redirectedTo);
     LOGGER.trace("Звонок перенаправлен на {} -> {}", call.getFrom(), call.getTo());
     //            System.out.println(event);

     } else if (event instanceof VarSetEvent) { // если это сообщение о смене статуса звонка
     VarSetEvent varSetEvent = (VarSetEvent) event;
     if (varSetEvent.getVariable().equals("DIALSTATUS")){
     if (varSetEvent.getValue().equals("null")){
     return;
     }
     Call call = calls.get(varSetEvent.getUniqueId());
     if (call == null) return;

     if (varSetEvent.getValue().equals("ANSWER") && (call.getAnswered() == 0)){
     int answeredIn = (int) (varSetEvent.getDateReceived().getTime() - call.getCalledMillis()) / 1000;
     call.setAnswered(answeredIn);
     }

     //                System.out.println(varSetEvent);
     //                System.out.println("value: " + varSetEvent.getValue());

     }
     return;

     } else if (event instanceof HangupEvent) { // если это конец звонка
     HangupEvent hangupEvent = (HangupEvent) event;
     Call call = calls.get(hangupEvent.getUniqueId());
     calls.remove(hangupEvent.getUniqueId());
     if (call == null) return;

     if ("s".equals(call.getTo())) {
     LOGGER.trace("{} не дождался совершения исходящего звонка", call.getTo());
     return;
     }
     if ("Call Rejected".equals(hangupEvent.getCauseTxt())) {
     call.setCallState(Call.CallState.BUSY);
     }
     if (call.getCallState() == null) {
     call.setCallState(Call.CallState.FAIL);
     }
     if (call.getCallState() == ANSWER) {
     int ended = (int) ((event.getDateReceived().getTime() - call.getCalledMillis()) / 1000) - call.getAnswered();
     call.setEnded(ended);
     } else {
     call.setEnded(0);
     }
     LOGGER.trace("Звонок перенаправлен на {} -> {}", call.getFrom(), call.getTo());
     //            System.out.println(event);


     detectService(call);
     MyLogger.log(ENDED_CALL, call.getUser().getLogin() + ": завершен разговор " + call.getFrom() + " c " + call.getTo());

     //            System.out.println(call);
     processCall(call);
     LOGGER.trace("Айдишников<->Звонков в мапе {}", calls.size());
     }
     **/
}
