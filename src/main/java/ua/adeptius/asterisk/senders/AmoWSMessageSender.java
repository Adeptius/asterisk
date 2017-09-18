package ua.adeptius.asterisk.senders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.javax_web_socket.MessageCallPhase;
import ua.adeptius.amocrm.javax_web_socket.WebSocket;
import ua.adeptius.amocrm.javax_web_socket.WsMessage;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.AmoOperatorLocation;
import ua.adeptius.asterisk.model.telephony.Call;
import ua.adeptius.asterisk.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static ua.adeptius.amocrm.javax_web_socket.MessageCallPhase.*;
import static ua.adeptius.amocrm.javax_web_socket.MessageEventType.incomingCall;
import static ua.adeptius.amocrm.javax_web_socket.MessageEventType.outgoingCall;

public class AmoWSMessageSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(AmoWSMessageSender.class.getSimpleName());
    private static Queue<Call> queue = new ConcurrentLinkedQueue<>(); // сюда доствляют обьекты Call другие потоки
    private static Set<Call> calls = new HashSet<>(); // коллекция наполняется из очереди когда поток просыпается
    private static Settings settings = Main.settings;

    public AmoWSMessageSender() {
        setDaemon(true);
        setName("AmoWSMessageSender");
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (!queue.isEmpty()){ // берём все пришедшие Call из очереди и добавляем в Set<Call> calls
                Call call = queue.poll();
                calls.add(call);
                LOGGER.debug("{}: взят на контроль звонок от {}", call.getUser().getLogin(), call.getCalledFrom());
            }

            try {
                long t0 = System.nanoTime()/1000000;
                sendMessages();
                long t1 = System.nanoTime()/1000000;
                long difference = t1 - t0;
                if (difference > 100){
                    LOGGER.warn("На отправку уведомлений потребовалось {}мс. Звонков {}", difference, calls.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addCallToSender(Call call) {
        if (!settings.isCallToAmoWSEnabled()){
            LOGGER.debug("AmoWSMessageSender отключен в настройках");
            return;
        }
        queue.offer(call);
    }

    private void sendMessages() throws Exception {
        Iterator<Call> iterator = calls.iterator(); // итератор потому что надо удалять
        while(iterator.hasNext()){
            Call call = iterator.next();
            Call.CallPhase callPhase = call.getCallPhase();
            User user = call.getUser();

            AmoAccount amoAccount = user.getAmoAccount();
            if (amoAccount == null) {
                iterator.remove();
                LOGGER.trace("{}: звонок от {} больше не отслеживается - оказалось нет амо акка", call.getUser().getLogin(), call.getCalledFrom());
                continue;
            }

            AmoOperatorLocation operatorLocation = user.getOperatorLocation();
            if (operatorLocation == null) {
                iterator.remove();
                LOGGER.trace("{}: звонок от {} больше не отслеживается - нет инфы о местоположении операторов", call.getUser().getLogin(), call.getCalledFrom());
                continue;
            }

            if (callPhase == Call.CallPhase.NEW_CALL) {


            } else if (callPhase == Call.CallPhase.REDIRECTED) {
                sendWsMessageOutgoingCall(amoAccount, call, MessageCallPhase.dial);


            } else if (callPhase == Call.CallPhase.ANSWERED) {
//                if (!call.isSendedAnswerWsMessage()) {
////                    LOGGER.debug("{}: отправляю сообщение оператору на телефоне {}, что он ответил на звонок",
////                            call.getUser().getLogin(), call.getCalledTo().get(0));
//
//                    sendWsMessageOutgoingCall(amoAccount, call, MessageCallPhase.answer);
//                    // использую метод ToAll так как при ответе там всегда только 1 элемент
//                    call.setSendedAnswerWsMessage(true);
//
//                }

            } else if (callPhase == Call.CallPhase.ENDED) {

                if (call.getCallState() == Call.CallState.ANSWER) {
                    sendWsMessageOutgoingCall(amoAccount, call, MessageCallPhase.ended);
                }
                iterator.remove();
                LOGGER.debug("{}: звонок от {} больше не отслеживается", call.getUser().getLogin(), call.getCalledFrom());
                if (calls.size() > 5) {
                    LOGGER.warn("Размер мапы {} - если пользователей мало - то что-то не удаляется", calls.size());
                }
            }
        }
    }


    public static void sendWsMessageOutgoingCall(AmoAccount amoAccount, Call call, MessageCallPhase callPhase) {
        List<String> calledToList = call.getCalledTo();
        for (String calledTo : calledToList) {
            String workersId = amoAccount.getWorkersId(calledTo);
            if (workersId != null) {// мы знаем id работника.
                String login = amoAccount.getUser().getLogin();
                WsMessage message = new WsMessage(incomingCall);
                message.setFrom(call.getCalledFrom());
//                message.setDealId("" + call.getAmoDealId());
                message.setCallPhase(callPhase);
                WebSocket.sendMessage(workersId, message);// отправляем
                if (callPhase == noanswer) {
                    LOGGER.trace("{}: Отправлено WS сообщение, что звонок был пропущен.", login);
                } else if (callPhase == answer || callPhase == ended) {
                    LOGGER.trace("{}: Отправлено WS сообщение, что ответ на звонок был.", login);
                } else if (callPhase == dial) {
//                    LOGGER.trace("{}: Отправили WS сообщение о новом звонке", login);
                }
            }
        }
    }

    public static void sendWsMessageOutgoingCall(String workersId, String to) {
        if (workersId != null) {// мы знаем id работника.
            WsMessage message = new WsMessage(outgoingCall);
            message.setContent(to);
            WebSocket.sendMessage(workersId, message);
        }
    }
}
