package ua.adeptius.asterisk.monitor;

import org.asteriskjava.manager.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.Call;
import ua.adeptius.asterisk.model.InnerPhone;
import ua.adeptius.asterisk.model.OuterPhone;
import ua.adeptius.asterisk.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.model.Call.CallState.*;
import static ua.adeptius.asterisk.model.Call.Direction.IN;
import static ua.adeptius.asterisk.utils.MyStringUtils.addZero;

public class AsteriskLogAnalyzer {

    private static HashMap<String, List<ManagerEvent>> chanelsAndEvents = new HashMap<>();
    private static LogsPrinter logsPrinter = new LogsPrinter();
    private static Logger LOGGER = LoggerFactory.getLogger(AsteriskLogAnalyzer.class.getSimpleName());
    public static HashMap<String, User> phonesAndUsers = new HashMap<>();

    /**
     * Получает первично отфильтрованные логи от астериска и групирует их по каналу ивента в мапу chanelsAndEvents
     * После этого отправляет полученный List<ManagerEvent> в метод makeCallObject
     */
    public static void analyze(ManagerEvent event){

        if (event instanceof NewChannelEvent) { // если это newChannel - добавляем в мапу новый ключ
            NewChannelEvent newChannelEvent = (NewChannelEvent) event;

            List<ManagerEvent> list = new ArrayList<>();
            list.add(newChannelEvent);
            chanelsAndEvents.put(newChannelEvent.getChannel(), list);
            return;
        }

        if (event instanceof NewExtenEvent) {
            NewExtenEvent newExtenEvent = (NewExtenEvent) event;
            List<ManagerEvent> list = chanelsAndEvents.get(newExtenEvent.getChannel());
            if (list == null){
                return;// null может быть только если NewChannelEvent был до запуска сервера
            }
            list.add(newExtenEvent);
            return;
        }


        if (event instanceof VarSetEvent) {
            VarSetEvent varSetEvent = (VarSetEvent) event;
            List<ManagerEvent> list = chanelsAndEvents.get(varSetEvent.getChannel());
            if (list == null){
                return;// null может быть только если NewChannelEvent был до запуска сервера
            }
            list.add(varSetEvent);
            return;
        }

        if (event instanceof HangupEvent) {
            HangupEvent hangupEvent = (HangupEvent) event;// если окончание разговора - удаляем из мапы и передаём список дальше
            List<ManagerEvent> list = chanelsAndEvents.remove(hangupEvent.getChannel());
            if (list == null){
                return;// null может быть только если NewChannelEvent был до запуска сервера
            }
            list.add(hangupEvent);

            makeCallObject(list);
//            logsPrinter.send(list);
        }
    }

    /**
     * Собирает обьект Call на основании списка логов
     * @param list
     */
    private static void makeCallObject(List<ManagerEvent> list){
//        System.err.println("Приступаю к сборке обьекта Call.");
//        for (ManagerEvent event : list) {
//            System.err.println(AsteriskMonitor.makePrettyLog(event));
//        }
//        System.err.println("------------------");

        Call call = new Call();
        for (ManagerEvent event : list) {
            if (event instanceof NewChannelEvent) { // если это newChannel - добавляем в мапу новый ключ
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
                        LOGGER.error("Не найдена связь с пользователем " + list);
                        return;
                    }
                }

                if (from.length()==7&&from.startsWith("2")&&to.length()==7&&to.startsWith("2")){
                    LOGGER.info("{}: Обнаружен внутренний звонок. {} -> {}. Не регистрируем...",user.getLogin(), from, to);
                    return;
                }

                if (direction == IN) {
                    OuterPhone outerPhone = user.getOuterPhoneByNumber(to);
                    call.setOuterPhone(outerPhone);
                }

                call.setAsteriskId(newChannelEvent.getUniqueId());
                call.setCalledTo(to);
//                call.setFirstCall(newChannelEvent.getExten());
                call.setCalledFrom(from);
                call.setUser(user);
                call.setCalledDate(newChannelEvent.getDateReceived());
                call.setDirection(direction);

                continue;
            }

            if (event instanceof NewExtenEvent) {
                NewExtenEvent newExtenEvent = (NewExtenEvent) event; // при AGI это не требуется

                continue;
            }


            if (event instanceof VarSetEvent) {
                VarSetEvent varSetEvent = (VarSetEvent) event;
                String variable = varSetEvent.getVariable();
                String value = varSetEvent.getValue();
                if (variable.equals("ANSWEREDTIME")){
                    call.setCallState(ANSWER);
                    call.setSecondsTalk(Integer.parseInt(value));

                }else if (variable.equals("DIALEDTIME")){
                    call.setSecondsToAnswer(Integer.parseInt(value));

                }else if (variable.equals("redirectedToSIP")){
                    call.setCalledTo(value);

                }else if (variable.equals("redirectedToGSM")){
                    call.setCalledTo(value);

                }else if (variable.equals("redirectedToGSMGroup")){//todo согласовать что делать с группой и как записывать её в БД
                    value = value.substring(1, value.indexOf(","));
                    call.setCalledTo(value);

                }else if (variable.equals("redirectedToSIPGroup")){
                    value = value.substring(1, value.indexOf(","));
                    call.setCalledTo(value);

                }else if (variable.equals("DIALEDPEERNUMBER")){ // value='Intertelekom_main/0995306914'
                    value = value.substring(value.lastIndexOf("/")+1);
                    call.setCalledTo(value);
                }


                continue;
            }

            if (event instanceof HangupEvent) {
                HangupEvent hangupEvent = (HangupEvent) event;// если окончание разговора - удаляем из мапы и передаём список дальше
                String cause = hangupEvent.getCauseTxt();
                if (cause == null){
                    call.setCallState(NOANSWER);

                }else if (cause.equals("User busy") || cause.equals("Call Rejected")){
                    call.setCallState(BUSY);

                }else if (cause.equals("Subscriber absent")){
                    call.setCallState(CHANUNAVAIL);
                }

//                String connectedlinenum = hangupEvent.getConnectedlinenum();// в случае с группой нужно посмотреть с кем же именно поговорили
//                if (connectedlinenum != null) {
//                    call.setCalledTo(connectedlinenum);
//                }
            }
        }
//        System.out.println(call);
//        MainController.onNewCall(call);
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
