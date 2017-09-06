package ua.adeptius.asterisk.monitor;


import org.asteriskjava.manager.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.senders.AmoCallSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.model.Call.CallPhase.*;
import static ua.adeptius.asterisk.model.Call.CallState.*;
import static ua.adeptius.asterisk.model.Call.Direction.IN;
import static ua.adeptius.asterisk.utils.MyStringUtils.addZero;
import static ua.adeptius.asterisk.utils.MyStringUtils.makePrettyLog;


public class CallProcessor {

    private static Logger LOGGER = LoggerFactory.getLogger(CallProcessor.class.getSimpleName());
    public static HashMap<String, Call> calls = new HashMap<>();
    public static HashMap<String, User> phonesAndUsers = new HashMap<>();
    private static AmoCallSender amoCallSender = new AmoCallSender();


    public static void processEvent(ManagerEvent event, String id) {

        /**
         * Только NewChannelEvent означает что это новый звонок
         * И только в случае, если он содержит номер какого-либо сервиса пользователя - то ID этого ивента
         * добавляется в мапу chanelsAndCalls для его дальнейшего отслеживания.
         *
         * Другие NewChannelEvent, у которых связи с пользователями нет - игнорируются
         */
        if (event instanceof NewChannelEvent) { // Событие обозначает новый звонок или создание канала редиректа между внутренними линиями.
            NewChannelEvent newChannelEvent = (NewChannelEvent) event;
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

            System.err.println(makePrettyLog(event));
            String login = user.getLogin();
            if (from.length()==7&&from.startsWith("2")&&to.length()==7&&to.startsWith("2")){
                LOGGER.info("{}: Обнаружен внутренний звонок. {} -> {}. Не регистрируем...", login, from, to);
                return;
            }

//            LOGGER.trace("ID {} NewChannelEvent: {}", id, makePrettyLog(newChannelEvent));

            Call call = new Call();
            if (direction == IN) {
                OuterPhone outerPhone = user.getOuterPhoneByNumber(to);
                call.setOuterPhone(outerPhone);
            }

            call.setAsteriskId(id);
            call.setCalledTo(to);
            call.setCalledFrom(from);
            call.setUser(user);
            call.setCalledDate(newChannelEvent.getDateReceived());
            call.setDirection(direction);
            call.setCallPhase(NEW_CALL);

//            AmoWSMessageSender.addCallToSender(call);

            calls.put(newChannelEvent.getUniqueId(), call);
            LOGGER.info("{}: Поступил новый звонок {} ->", login, call.getCalledFrom());
            amoCallSender.send(call);
            return;
        }


        //Если это событие не новый звонок - то убеждаемся по его id что соответствующий обьект Call c таким ID уже существует.
        //А если это прилетело что-то непонятное - дальше не идём и не захламляем логи.
        Call call = calls.get(id);
        if (call == null) { // null тут может быть только если сервер запустился тогда, когда уже кто-то разговаривал
            return;
        }
        String login = call.getUser().getLogin();

        if (event instanceof VarSetEvent) {
            System.err.println(makePrettyLog(event));
            VarSetEvent varSetEvent = (VarSetEvent) event;
            String variable = varSetEvent.getVariable();
            String value = varSetEvent.getValue();
            if (variable.equals("ANSWEREDTIME")){
                call.setCallState(ANSWER);
                call.setSecondsTalk(Integer.parseInt(value));

            }else if (variable.equals("DIALEDTIME")){
                call.setSecondsFullTime(Integer.parseInt(value));

            }else if (variable.equals("redirectedToSIP")){
                call.setCalledTo(value);
                call.setCallPhase(REDIRECTED);
                LOGGER.info("{}: Звонок перенаправлен на SIP {} -> {}", login, call.getCalledFrom(), call.getCalledTo());
                amoCallSender.send(call);


            }else if (variable.equals("redirectedToGSM")){
                call.setCalledTo(value);
                call.setCallPhase(REDIRECTED);
                LOGGER.info("{}: Звонок перенаправлен на GSM {} -> {}", login, call.getCalledFrom(), call.getCalledTo());
                amoCallSender.send(call);


            }else if (variable.equals("redirectedToGSMGroup")){//todo согласовать что делать с группой и как записывать её в БД
                value = value.substring(1, value.indexOf(","));
                call.setCalledTo(value);
                call.setCallPhase(REDIRECTED);
                LOGGER.info("{}: Звонок перенаправлен на группу GSM {} -> {}",login, call.getCalledFrom(), call.getCalledTo());
                amoCallSender.send(call);


            }else if (variable.equals("redirectedToSIPGroup")){
                value = value.substring(1, value.indexOf(","));
                call.setCalledTo(value);
                call.setCallPhase(REDIRECTED);
                LOGGER.info("{}: Звонок перенаправлен на группу SIP {} -> {}", login, call.getCalledFrom(), call.getCalledTo());
                amoCallSender.send(call);


            }else if (variable.equals("DIALEDPEERNUMBER")){ // value='Intertelekom_main/0995306914'
                value = value.substring(value.lastIndexOf("/")+1);
                call.setCalledTo(value);
                call.setCallPhase(ANSWERED);
                LOGGER.info("{}: На звонок ответил {} -> {}", login, call.getCalledFrom(), call.getCalledTo());
                amoCallSender.send(call);

            }
            return;
        }


        if (event instanceof HangupEvent) {
            calls.remove(id);
            System.err.println(makePrettyLog(event));
            HangupEvent hangupEvent = (HangupEvent) event;// если окончание разговора - удаляем из мапы и передаём список дальше
            String cause = hangupEvent.getCauseTxt();
            if (cause == null){
                call.setCallState(NOANSWER);

            }else if (cause.equals("User busy") || cause.equals("Call Rejected")){
                call.setCallState(BUSY);

            }else if (cause.equals("Subscriber absent")){
                call.setCallState(CHANUNAVAIL);
            }

            LOGGER.info("{}: Завершен разговор {} c {}", login, call.getCalledFrom(), call.getCalledTo());
            call.setCallPhase(ENDED);
            MainController.onNewCall(call);
            amoCallSender.send(call);

        }
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
