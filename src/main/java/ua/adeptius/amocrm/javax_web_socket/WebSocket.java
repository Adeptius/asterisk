package ua.adeptius.amocrm.javax_web_socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.User;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static ua.adeptius.amocrm.javax_web_socket.MessageEventType.click2call;
import static ua.adeptius.amocrm.javax_web_socket.MessageEventType.copySession;
import static ua.adeptius.amocrm.javax_web_socket.MessageEventType.wrongMessage;

@ServerEndpoint(value = "/ws/{amoDomain}/{userId}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class WebSocket {

    private Session session;
    private static final Set<WebSocket> chatEndpoints = new CopyOnWriteArraySet<>();
    private static ConcurrentHashMap<String, Set<Session>> usersAndSessions = new ConcurrentHashMap<>();
    private static Logger LOGGER = LoggerFactory.getLogger(WebSocket.class.getSimpleName());


    @OnOpen
    public void onOpen(Session session, @PathParam("amoDomain") String amoDomain, @PathParam("userId") String userId) throws IOException {
        this.session = session;
        chatEndpoints.add(this);

        Set<Session> curentUserSessions = usersAndSessions.get(userId);
        if (curentUserSessions == null) {
            curentUserSessions = new HashSet<>();
            curentUserSessions.add(session);
            usersAndSessions.put(userId, curentUserSessions);
        } else {
            usersAndSessions.get(userId).add(session);
        }

        LOGGER.debug("{}: пользователь {} сессий: {}", amoDomain, userId, usersAndSessions.get(userId).size());
        LOGGER.debug("{}: пользователь {} подключился. EndPoints({}), Users({})", amoDomain, userId, chatEndpoints.size(), usersAndSessions.size());
    }

    @OnMessage
    public void onMessage(Session session, WsMessage wsMessage, @PathParam("amoDomain") String amoDomain, @PathParam("userId") String userId) throws IOException {
        LOGGER.debug("{}: пользователь {} прислал сообщение: {}", amoDomain, userId, wsMessage);
        processMessage(session, wsMessage, amoDomain, userId);
    }

    @OnClose
    public void onClose(Session session, @PathParam("amoDomain") String amoDomain, @PathParam("userId") String userId) throws IOException {
        chatEndpoints.remove(this);
        Set<Session> curentUserSessions = usersAndSessions.get(userId);
        curentUserSessions.remove(session);
        LOGGER.debug("{}: пользователь {} сессий: {}", amoDomain, userId, usersAndSessions.get(userId).size());
        LOGGER.debug("{}: пользователь {} отключился. EndPoints({}), Users({})", amoDomain, userId, chatEndpoints.size(), usersAndSessions.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("amoDomain") String amoDomain, @PathParam("userId") String userId) {
        chatEndpoints.remove(this);
        Set<Session> curentUserSessions = usersAndSessions.get(userId);
        curentUserSessions.remove(session);
        LOGGER.debug("{}: пользователь {} ошибка соединения. Сессий: {}", amoDomain, userId, usersAndSessions.get(userId).size());
    }

    public static void sendMessage(String userId, WsMessage wsMessage) {
        Set<Session> sessions = usersAndSessions.get(userId);
        try {
            for (Session session : sessions) {
                session.getBasicRemote().sendObject(wsMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (session != null) {// Пользователь подключен в данный момент
//        } else { // Пользователь не подключен
//
//        }
    }


    //TODO где-то здесь кешировать id сотрудников и их номера

    private void processMessage(Session session, WsMessage wsMessage, String amoDomain, String userId) {
        MessageEventType type = wsMessage.getEventType();
        if (type == null) {
            LOGGER.error("{}: пользователь {} прислал сообщение c null типом {}", amoDomain, userId, wsMessage);
            return;
        }
        if (type == wrongMessage) {
            LOGGER.debug("{}: пользователь {} прислал неправильный JSON {}", amoDomain, userId, wsMessage);
            return;

        }
        if (type == click2call) {
            String callTo = wsMessage.getCallTo();
            if (callTo == null) {
                LOGGER.debug("{}: пользователь {} использует click2call для звонка но адресат не указан {}", amoDomain, userId, wsMessage);
                return;
            }
            LOGGER.debug("{}: пользователь {} использует click2call для звонка на {}", amoDomain, userId, callTo);
            try {
                callTo = cleanAndValidatePhoneNumber(callTo);
            } catch (IllegalArgumentException e) {
                //TODO вернуть ошибку пользователю
            }

            List<User> users = UserContainer.getUsers();
            for (User user : users) {
                AmoAccount amoAccount = user.getAmoAccount();
                if (amoAccount != null) {
                    String workersNumber = amoAccount.getWorkersPhone(userId);
                    if (workersNumber != null) {
                        try {
                            Main.monitor.sendCallToOutsideAction(workersNumber, callTo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    public static String cleanAndValidatePhoneNumber(String number) throws IllegalArgumentException {
        if (number == null) {
            throw new IllegalArgumentException();
        }
        number = number.replaceAll("\\D+", "");

        if (number.startsWith("380")) {
            number = number.substring(2);
        } else if (number.startsWith("80")) {
            number = number.substring(1);
        }
        System.out.println("result filtering: " + number);
        if (number.length() == 10 && number.startsWith("0")) {
            return number;
        }
        throw new IllegalArgumentException();
    }


}