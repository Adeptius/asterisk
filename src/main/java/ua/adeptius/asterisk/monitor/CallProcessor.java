package ua.adeptius.asterisk.monitor;


import org.asteriskjava.manager.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.model.Call.CallState.*;
import static ua.adeptius.asterisk.model.Call.Direction.IN;

@SuppressWarnings("Duplicates")
public class CallProcessor {

    private static Logger LOGGER = LoggerFactory.getLogger(CallProcessor.class.getSimpleName());

    //    public static HashMap<String, Call> chanelsAndCalls = new HashMap<>();
    public static HashMap<String, Call> calls = new HashMap<>();
    public static HashMap<String, User> phonesAndUsers = new HashMap<>();


    public static void processEvent(ManagerEvent event, String id) {
//
//        /**
//         * Только NewChannelEvent означает что это новый звонок
//         * И только в случае, если он содержит номер какого-либо сервиса пользователя - то ID этого ивента
//         * добавляется в мапу chanelsAndCalls для его дальнейшего отслеживания.
//         *
//         * Другие NewChannelEvent, у которых связи с пользователями нет - игнорируются
//         */
//        if (event instanceof NewChannelEvent) { // Событие обозначает новый звонок или создание канала редиректа между внутренними линиями.
//            NewChannelEvent newChannelEvent = (NewChannelEvent) event;
//            String from = addZero(newChannelEvent.getCallerIdNum());
//            String to = addZero(newChannelEvent.getExten());
////            if ("s".equals(to) && ("from-internal".equals(newChannelEvent.getContext()))) { // та самая внутренняя линия
////                return; // отбой странной ерунды при редиректе на сип
////                    //генерирует дубли. При звонках снаружи на сип и с сипа на сип можно обойтись без этого s
////            Но при click2Call не видит звонок. Так что закоментировано
////            }
//
//            // Ищем связь с сервисом и определяем направление звонка
//            User user = phonesAndUsers.get(to);
//            Call.Direction direction = IN;
//
//            if (user == null) {
//                user = phonesAndUsers.get(from);
//                direction = Call.Direction.OUT;
//                if (user == null) {
//                    // Связь звонящих номеров с каким-либо сервисом не обнаружена
//                    // Следовательно дальше не идём
//                    return;
//                }
//            }
//
//            if (from.length()==7&&from.startsWith("2")&&to.length()==7&&to.startsWith("2")){
//                LOGGER.info("{}: Обнаружен внутренний звонок. {} -> {}. Не регистрируем...",user.getLogin(), from, to);
//                return;
//            }
//
//            LOGGER.trace("ID {} NewChannelEvent: {}", id, makePrettyLog(newChannelEvent));
//
//            Call call = new Call();
//            if (direction == IN) {
//                OuterPhone outerPhone = user.getOuterPhoneByNumber(to);
//                call.setOuterPhone(outerPhone);
//            }
//
//            call.setAsteriskId(newChannelEvent.getUniqueId());
//            call.setCalledTo(newChannelEvent.getExten());
//            call.setFirstCall(newChannelEvent.getExten());
//            call.setCalledFrom(newChannelEvent.getCallerIdNum());
//            call.setUser(user);
//            call.setCalledDate(newChannelEvent.getDateReceived());
//            call.setDirection(direction);
//
//            calls.put(newChannelEvent.getUniqueId(), call);
//            LOGGER.info("Поступил новый звонок {} ->", call.getCalledFrom());
//            return;
//        }
//
//
//        //Если это событие не новый звонок - то убеждаемся по его id что соответствующий обьект Call c таким ID уже существует.
//        //А если это прилетело что-то непонятное - дальше не идём и не захламляем логи.
//        Call call = calls.get(id);
//        if (call == null) { // null тут может быть только если сервер запустился тогда, когда уже кто-то разговаривал
//            return;
//        }
//
//        if (event instanceof NewExtenEvent) {
//            NewExtenEvent newExtenEvent = (NewExtenEvent) event;
//            LOGGER.trace("ID {} NewExtenEvent: {}", id, makePrettyLog(newExtenEvent));
//            String redirectedTo = newExtenEvent.getAppData();
//            if (redirectedTo.contains(",")) {
//                redirectedTo = redirectedTo.substring(redirectedTo.lastIndexOf("/") + 1, redirectedTo.indexOf(","));
//            } else {
//                redirectedTo = redirectedTo.substring(redirectedTo.lastIndexOf("/") + 1);
//            }
//            call.setCalledTo(redirectedTo);
//            LOGGER.info("Звонок перенаправлен на {} -> {}", call.getCalledFrom(), call.getCalledTo());
//            MainController.amoCallSender.send(call); // Создаём сделку в Amo или обновляем существующую
//            return;
//        }
//
//        if (event instanceof VarSetEvent) {
//            VarSetEvent varSetEvent = (VarSetEvent) event;
//            if (varSetEvent.getVariable().equals("DIALSTATUS")) {
//                LOGGER.trace("ID {} VarSetEvent: {}", id, makePrettyLog(varSetEvent));
//                String dialStatus = varSetEvent.getValue();
//                if (call.isStateWasAlreadySetted()) {
//                    LOGGER.debug("Состояние звонка не меняется. Это событие - дубль: {}", dialStatus);
//                    return;
//                }
//
//                if ("ANSWER".equals(dialStatus)) { // Кто-то взял трубку
//                    call.setAnsweredDate(event.getDateReceived());
//                    call.setCallState(ANSWER);
//                    MainController.amoCallSender.send(call); // Обновляем статус только если ANSWER.
//                    // Иначе нет смысла - пользователю потом всё-равно hangup отправится и будет дубль.
//
//                } else if ("BUSY".equals(dialStatus)) {
//                    call.setCallState(BUSY);
//                } else if ("NOANSWER".equals(dialStatus) || "CANCEL".equals(dialStatus)) {
//                    //CANCEL - это если звонить на внешний с редиректом на сип и сип не взял трубку за 90 сек
//                    call.setCallState(NOANSWER);
//                } else if ("CHANUNAVAIL".equals(dialStatus)) {
//                    //вызываемый номер был недоступен
//                    call.setCallState(CHANUNAVAIL);
//                } else {
//                    LOGGER.error("ДОБАВИТЬ СТАТУС ЗВОНКА: {}", dialStatus);
//                }
//
//                LOGGER.info("Состояние звонка установлено на: {}", call.getCallState());
//            }
//            return;
//        }
//
//        if (event instanceof HangupEvent) { // Событие определяет окончание звонка. не содержит никакой инфы при звонке sip->gsm
//            HangupEvent hangupEvent = (HangupEvent) event;
//            LOGGER.trace("ID {} HangupEvent: {}", id, makePrettyLog(hangupEvent));
//            LOGGER.info("Завершен разговор {} c {}", call.getCalledFrom(), call.getCalledTo());
//
//            calls.remove(id); // конец звонка. айди звонка больше не будет отслеживатся так как он завершен. Удаляем.
//            if (calls.size() > 5) {// По идее мапа должна чистится chanelsAndCalls.remove(id), но я не знаю что будет в будущем.
//                LOGGER.warn("Айдишников<->Звонков в мапе {}", calls.size());
//            }// Если в мапе будут накапливатся айдишники, а такое наверное может быть если астериск по какой-то причине
//            // создаст новый канал, а в конце не выдаст по нему hangUpEvent, то надо будет что-то думать.
//
//            call.setEndedDate(event.getDateReceived());
//
//            if ("s".equals(call.getCalledTo())) {
//                LOGGER.info("{} не дождался совершения исходящего звонка", call.getCalledTo());
//                return; // обязательно нужен этот отбойник для фильтрования второго звонка при звонке снаружи на сип (gsm - outer- sip)
//            }
//
//            MainController.amoCallSender.send(call); // Обновляем
//
//            MainController.onNewCall(call);// отправляет законченный обьект call для дальнейшей отправки в различные сервисы
//
//            if (call.getCallState() == null) {
//                LOGGER.error("Завершен разговор c состоянием null! " + call);
//            } else {
//                LOGGER.info("Завершен разговор: {}", call);
//            }
//        }
    }


    public static void updatePhonesHashMap() {
        LOGGER.trace("Обновление карты Number <-> User");
        HashMap<String, User> newCache = new HashMap<>();
        for (User user : UserContainer.getUsers()) {
            List<String> numbers = new ArrayList<>();
            numbers.addAll(user.getOuterPhones().stream().map(OuterPhone::getNumber).collect(Collectors.toList()));
            numbers.addAll(user.getInnerPhones().stream().map(InnerPhone::getNumber).collect(Collectors.toList()));
            numbers.forEach(s -> newCache.put(s, user));
        }
        phonesAndUsers = newCache;
    }
}
