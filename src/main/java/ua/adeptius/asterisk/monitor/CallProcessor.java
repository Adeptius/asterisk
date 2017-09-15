package ua.adeptius.asterisk.monitor;


import org.asteriskjava.manager.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.TrackingController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.senders.AmoCallSender;
import ua.adeptius.asterisk.senders.AmoWSMessageSender;

import java.util.*;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.model.Call.CallPhase.*;
import static ua.adeptius.asterisk.model.Call.CallState.*;
import static ua.adeptius.asterisk.model.Call.Direction.IN;
import static ua.adeptius.asterisk.utils.MyStringUtils.addZero;


public class CallProcessor {

    private static Logger LOGGER = LoggerFactory.getLogger(CallProcessor.class.getSimpleName());
    public static HashMap<String, Call> calls = new HashMap<>();
    public static HashMap<String, User> phonesAndUsers = new HashMap<>();
    private static AmoCallSender amoCallSender = new AmoCallSender();
    private static AmoWSMessageSender amoWSMessageSender = new AmoWSMessageSender();

    /**
     * Только NewChannelEvent означает что это новый звонок
     * И только в случае, если он содержит номер какого-либо сервиса пользователя - то ID этого ивента
     * добавляется в мапу chanelsAndCalls для его дальнейшего отслеживания.
     *
     * Другие NewChannelEvent, у которых связи с пользователями нет - игнорируются
     */
    public static void processEvent(NewChannelEvent newChannelEvent) {
        // Событие обозначает новый звонок или создание канала редиректа между внутренними линиями.
        String from = addZero(newChannelEvent.getCallerIdNum());
        String to = addZero(newChannelEvent.getExten());

        // Ищем связь с сервисом и определяем направление звонка
        User user = phonesAndUsers.get(to);
        Call.Direction direction = IN;

        if (user == null) {
            user = phonesAndUsers.get(from);
            direction = Call.Direction.OUT;
            if (user == null) {
                // Связь звонящих номеров с каким-либо сервисом не обнаружена
                // Следовательно дальше не идём
                return;
            }
        }

//            System.err.println(makePrettyLog(event));
        String login = user.getLogin();
        if (from.length() == 7 && from.startsWith("2") && to.length() == 7 && to.startsWith("2")) {
            LOGGER.info("{}: Обнаружен внутренний звонок. {} -> {}. Не регистрируем...", login, from, to);
            return;
        }

//            LOGGER.trace("ID {} NewChannelEvent: {}", id, makePrettyLog(newChannelEvent));

        Call call = new Call();
        if (direction == IN) {
            OuterPhone outerPhone = user.getOuterPhoneByNumber(to);
            call.setOuterPhone(outerPhone);
        }

        call.setAsteriskId(newChannelEvent.getUniqueId());
        call.setCalledTo(Collections.singletonList(to));
        call.setCalledFrom(from);
        call.setUser(user);
        call.setCalledDate(newChannelEvent.getDateReceived());
        call.setDirection(direction);
        call.setCallPhase(NEW_CALL);

        if (direction == IN && !Main.remoteServerIsUp) {
            amoWSMessageSender.addCallToSender(call);
        }

//            if (direction == IN){
//                amoWSMessageSender.addCallToSender(call);
//            }

        calls.put(newChannelEvent.getUniqueId(), call);
        LOGGER.debug("{}: Поступил новый звонок {} ->", login, call.getCalledFrom());
        amoCallSender.send(call);
    }

    public static void processEvent(VarSetEvent varSetEvent) {
        Call call = calls.get(varSetEvent.getUniqueId());
        if (call == null) { // null тут может быть только если сервер запустился тогда, когда уже кто-то разговаривал
            return;
        }
        String login = call.getUser().getLogin();

        //            System.err.println(makePrettyLog(event));
        String variable = varSetEvent.getVariable();
        String value = varSetEvent.getValue();
        if (variable.equals("ANSWEREDTIME")) {
            call.setCallState(ANSWER);
            call.setSecondsTalk(Integer.parseInt(value));

        } else if (variable.equals("DIALEDTIME")) {
            call.setSecondsFullTime(Integer.parseInt(value));

        } else if (variable.equals("redirectedTo")) {
            call.setCallPhase(REDIRECTED);

            List<String> sips = Arrays.asList(value.substring(1, value.length() - 1).split(", "));
            call.setCalledTo(sips);

            LOGGER.trace("{}: Звонок перенаправлен на группу SIP {} -> {}", login, call.getCalledFrom(), call.getCalledTo());
            amoCallSender.send(call);


        } else if (variable.equals("DIALEDPEERNUMBER")) { // value='Intertelekom_main/0995306914'
            value = value.substring(value.lastIndexOf("/") + 1);
            call.setCalledTo(Collections.singletonList(value));
            call.setCallPhase(ANSWERED);
            LOGGER.trace("{}: На звонок ответил {} -> {}", login, call.getCalledFrom(), call.getCalledTo());
            amoCallSender.send(call);

        }

    }

    // если окончание разговора - удаляем из мапы и передаём список дальше
    public static void processEvent(HangupEvent hangupEvent) {
        String uniqueId = hangupEvent.getUniqueId();
        Call call = calls.get(uniqueId);
        if (calls.size() > numberOfInternalPhones){
            LOGGER.warn("В списке calls {} звонков, а всего внутренних телефонов {}", calls.size(), numberOfInternalPhones);
        }
        if (call == null) { // null тут может быть только если сервер запустился тогда, когда уже кто-то разговаривал
            return;
        }
        String login = call.getUser().getLogin();

        calls.remove(uniqueId);
//            System.err.println(makePrettyLog(event));
        String cause = hangupEvent.getCauseTxt();
        if (cause == null) {
            call.setCallState(NOANSWER);

        } else if (cause.equals("User busy") || cause.equals("Call Rejected")) {
            call.setCallState(BUSY);

        } else if (cause.equals("Subscriber absent")) {
            call.setCallState(CHANUNAVAIL);
        }

        LOGGER.debug("{}: Завершен разговор {} c {}", login, call.getCalledFrom(), call.getCalledTo());
        call.setCallPhase(ENDED);
        TrackingController.onNewCall(call);
        amoCallSender.send(call);
    }


    private static int numberOfInternalPhones;

    public static void updatePhonesHashMap() {
        LOGGER.trace("Обновление карты Number <-> User");
        HashMap<String, User> newCache = new HashMap<>();
        for (User user : UserContainer.getUsers()) {
            List<String> numbers = new ArrayList<>();
            List<String> outerNumbers = user.getOuterPhones().stream().map(OuterPhone::getNumber).collect(Collectors.toList());
            numbers.addAll(outerNumbers);
            List<String> innerNumbers = user.getInnerPhones().stream().map(InnerPhone::getNumber).collect(Collectors.toList());
            numbers.addAll(innerNumbers);
            numbers.forEach(s -> newCache.put(s, user));
            numberOfInternalPhones = innerNumbers.size();
        }
        phonesAndUsers = newCache;
    }
}
