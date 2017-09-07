package ua.adeptius.asterisk.senders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.javax_web_socket.MessageCallPhase;
import ua.adeptius.amocrm.javax_web_socket.WebSocket;
import ua.adeptius.amocrm.javax_web_socket.WsMessage;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.AmoOperatorLocation;
import ua.adeptius.asterisk.model.Call;
import ua.adeptius.asterisk.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ua.adeptius.amocrm.javax_web_socket.MessageCallPhase.*;
import static ua.adeptius.amocrm.javax_web_socket.MessageEventType.incomingCall;
import static ua.adeptius.amocrm.javax_web_socket.MessageEventType.outgoingCall;

@SuppressWarnings("Duplicates")
//@Deprecated
public class AmoWSMessageSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(AmoWSMessageSender.class.getSimpleName());

    public AmoWSMessageSender() {
        setDaemon(true);
        setName("AmoWSMessageSender");
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendMessages();
        }
    }

    public static void addCallToSender(Call call) {
        calls.add(call);
    }

    private static Set<Call> calls = new HashSet<>();

    private void sendMessages() {
        for (Call call : calls) {
            try {
                sendMessage(call);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(Call call) throws Exception {
        Call.CallPhase callPhase = call.getCallPhase();
        User user = call.getUser();

        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            return;
        }

        AmoOperatorLocation operatorLocation = user.getOperatorLocation();
        if (operatorLocation == null) {
            return;
        }

        if (callPhase == Call.CallPhase.NEW_CALL) {


        } else if (callPhase == Call.CallPhase.REDIRECTED) {
            sendWsMessageOutgoingCall(amoAccount, call, MessageCallPhase.dial);


        } else if (callPhase == Call.CallPhase.ANSWERED) {
            if (!call.isSendedAnswerWsMessage()) {
                LOGGER.debug("{}: отправляю сообщение оператору на телефоне {}, что он ответил на звонок",
                        call.getUser().getLogin(), call.getCalledTo().get(0));

                sendWsMessageOutgoingCall(amoAccount, call, MessageCallPhase.answer);
                // использую метод ToAll так как при ответе там всегда только 1 элемент
                call.setSendedAnswerWsMessage(true);

            }

        } else if (callPhase == Call.CallPhase.ENDED) {


            if (call.getCallState() == Call.CallState.ANSWER) {
                sendWsMessageOutgoingCall(amoAccount, call, MessageCallPhase.ended);
            }
            calls.remove(call);
            if (calls.size() > 5) {
                LOGGER.warn("Размер мапы {} - если пользователей мало - то что-то не удаляется", calls.size());
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
                message.setDealId("" + call.getAmoDealId());
                message.setCallId(call.getAsteriskId());
                message.setCallPhase(callPhase);
                WebSocket.sendMessage(workersId, message);// отправляем
                if (callPhase == noanswer) {
                    LOGGER.trace("{}: Отправлено WS сообщение, что звонок был пропущен.", login);
                } else if (callPhase == answer || callPhase == ended) {
                    LOGGER.trace("{}: Отправлено WS сообщение, что ответ на звонок был.", login);
                } else if (callPhase == dial) {
                    LOGGER.trace("{}: Отправили WS сообщение о новом звонке", login);
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
