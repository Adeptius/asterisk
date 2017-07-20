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
        if (event instanceof NewChannelEvent) { // Событие обозначает новый звонок или создание канала редиректа между внутренними линиями.
            NewChannelEvent newChannelEvent = (NewChannelEvent) event;
            String from = addZero(newChannelEvent.getCallerIdNum());
            String to = addZero(newChannelEvent.getExten());
            if ("s".equals(to) && !("from-internal".equals(newChannelEvent.getContext()))) { // та самая внутренняя линия
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

            calls.put(newChannelEvent.getUniqueId(), newCall);
            LOGGER.debug("Поступил новый звонок {} ->", newCall.getCalledFrom());

        } else {
            //Если это событие не новый звонок - то убеждаемся по его id что соответствующий обьект Call c таким ID уже существует.
            //А если это прилетело что-то непонятное - дальше не идём и не захламляем логи.

            NewCall newCall = calls.get(id);
            if (newCall == null) {
                return;
            }

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
                MainController.amoCallSender.send(newCall); // Создаём сделку в Amo или обновляем существующую


            } else if (event instanceof VarSetEvent) {
                VarSetEvent varSetEvent = (VarSetEvent) event;
                if (varSetEvent.getVariable().equals("DIALSTATUS")) {
                    LOGGER.trace("ID {} VarSetEvent: {}", id, varSetEvent);

                    //TODO создавать сделку здесь
                    String dialStatus = varSetEvent.getValue();
                    if ("ANSWER".equals(dialStatus)) { // Кто-то взял трубку
                        // диалстатус бывает второй раз по завершению звонка, а он нам не нужен.
//                        if (newCall.getAnsweredDate() == null){
//                        } // ввёл эту защиту в сеттере объекта Call
//                        и этот же call возвращает true если это первый диал статус
//                        Поэтому если тру - отправляем звонок в Амо.
                        boolean firstDialStatus = newCall.setAnsweredDate(event.getDateReceived());
                        newCall.setCallState(ANSWER);
//                        if (firstDialStatus){
//                            MainController.amoCallSender.send(newCall);
//                        } // Дублируем эту отправку в конец метода на случай если состояние звонка будет не ANSWER

                    } else {
                        if ("BUSY".equals(dialStatus)) {
                            newCall.setCallState(BUSY);
                        } else if ("NOANSWER".equals(dialStatus) || "CANCEL".equals(dialStatus)) {
                            //CANCEL - это если звонить на внешний с редиректом на сип и сип не взял трубку за 90 сек
                            newCall.setCallState(NOANSWER);
                        } else if ("CHANUNAVAIL".equals(dialStatus)) {
                            //вызываемый номер был недоступен
                            newCall.setCallState(CHANUNAVAIL);
                        } else {
                            LOGGER.error("ДОБАВИТЬ СТАТУС ЗВОНКА: {}", dialStatus);
                        }
                    }
                    MainController.amoCallSender.send(newCall); // Обновляем статус
                    LOGGER.debug("Состояние звонка установлено на: {}", newCall.getCallState());
                }
                return;
            }
            if (event instanceof HangupEvent) { // Событие определяет окончание звонка. не содержит никакой инфы при звонке sip->gsm
                HangupEvent hangupEvent = (HangupEvent) event;
                LOGGER.info("Завершен разговор {} c {}", newCall.getCalledFrom(), newCall.getCalledTo());
                LOGGER.trace("ID {} HangupEvent: {}", id, hangupEvent);

                calls.remove(id); // конец звонка. айди звонка больше не будет отслеживатся так как он завершен. Удаляем.
                if (calls.size() > 5) {// По идее мапа должна чистится calls.remove(id), но я не знаю что будет в будущем.
                    LOGGER.warn("Айдишников<->Звонков в мапе {}", calls.size());
                }// Если в мапе будут накапливатся айдишники, а такое наверное может быть если астериск по какой-то причине
                // создаст новый канал, а в конце не выдаст по нему hangUpEvent, то надо будет что-то думать.

                newCall.setEndedDate(event.getDateReceived());

                if ("s".equals(newCall.getCalledTo())) {
                    LOGGER.trace("{} не дождался совершения исходящего звонка", newCall.getCalledTo());
                    return; // обязательно нужен этот отбойник для фильтрования второго звонка при звонке снаружи на сип (gsm - outer- sip)
                }

                detectService(newCall); // смотрит по сервисам пользователя и добавляет в call тип сервиса

                MainController.amoCallSender.send(newCall); // Обновляем

                processCall(newCall); // отправляет законченный обьект call для дальнейшей отправки в различные сервисы

                if (newCall.getCallState() == null) {
                    LOGGER.error("Завершен разговор c состоянием null! " + newCall);
                } else {
                    LOGGER.debug("Завершен разговор: {}", newCall);
                }
            }
        }
    }

    /**
     * Выводит на экран размер мапы айдишников и звонков.
     * Нужен для дебага, если там скопилось много айдишников, что бы понять что там именно.
     */
    private static void printMap() {
        System.out.println("-----СОДЕРЖИМОЕ МАПЫ-----");
        calls.forEach((s, newCall) -> System.out.println("id " + s + " call: " + newCall.getCalledFrom() + "->" + newCall.getCalledTo()));
    }

    /**
     * Просто печатает event на экран, предварительно убрав лишнюю информацию.
     * Метод нужен только для дебага.
     */
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


    private static void     processCall(NewCall call) {
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
}
