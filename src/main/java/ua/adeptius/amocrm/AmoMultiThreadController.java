package ua.adeptius.amocrm;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyException;
import ua.adeptius.amocrm.javax_web_socket.MessageCallPhase;
import ua.adeptius.amocrm.javax_web_socket.WebSocket;
import ua.adeptius.amocrm.javax_web_socket.WsMessage;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.amocrm.model.json.JsonAmoContact;
import ua.adeptius.amocrm.model.json.JsonAmoDeal;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.*;

import java.util.HashMap;
import java.util.List;

import static ua.adeptius.amocrm.javax_web_socket.MessageCallPhase.*;
import static ua.adeptius.amocrm.javax_web_socket.MessageEventType.incomingCall;

@SuppressWarnings("Duplicates")
public class AmoMultiThreadController {

    private static Logger LOGGER = LoggerFactory.getLogger(AmoMultiThreadController.class.getSimpleName());

    private static ThreadLocal<Integer> threadLocalAmoDialId = new ThreadLocal<>();
    private static ThreadLocal<Integer> threadLocalLastOperationTime = new ThreadLocal<>();
    private static ThreadLocal<Integer> threadLocalAmoContactId = new ThreadLocal<>();
    private static ThreadLocal<Integer> threadLocalContactResponsibleUserId = new ThreadLocal<>();
    private static ThreadLocal<AmoAccount> threadLocalAmoAccount = new ThreadLocal<>();

    private static ThreadLocal<String> threadLocalFromNumber = new ThreadLocal<>();
    private static ThreadLocal<String> threadLocalAsteriskId = new ThreadLocal<>();

    public static void setThreadLocalFromNumber(String fromNumber) {
        threadLocalFromNumber.set(fromNumber);
    }

    public static void setThreadLocalAsteriskId(String asteriskId) {
        threadLocalAsteriskId.set(asteriskId);
    }

    public static void setAmoAccount(User user) {
        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            return;
        }

        String login = user.getLogin();

        // Проверяем заполнены ли поля в amoAccount
        String domain = amoAccount.getDomain();
        String amoLogin = amoAccount.getAmoLogin();
        String amoApiKey = amoAccount.getApiKey();

        if (StringUtils.isAnyBlank(domain, amoLogin, amoApiKey)) {
            LOGGER.debug("{}: Минимум одно поле амо аккаунта - пустое. Дальнейшая отправка невозможна", login);
            return;
        }

        // Нужен phoneId и EnumId для создания контактoв
        String phoneId = amoAccount.getPhoneId();
        String phoneEnumId = amoAccount.getPhoneEnumId();
        String apiUserId = amoAccount.getApiUserId();

        if (StringUtils.isAnyBlank(phoneId, phoneEnumId, apiUserId)) {
            try {
                LOGGER.trace("{}: Есть аккаунт Amo, но нет айдишников телефонов.", login);
                JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);
                phoneId = jsonAmoAccount.getPhoneId();
                phoneEnumId = jsonAmoAccount.getPhoneEnumId();
                apiUserId = jsonAmoAccount.getCurrent_user_id();
                LOGGER.trace("{}: Айдишники телефонов получены - сохраняю в аккаунте пользователя", login);
                //получили айдишники телефонов. Теперь сохраняем в бд
                amoAccount.setPhoneId(phoneId);
                amoAccount.setPhoneEnumId(phoneEnumId);
                amoAccount.setApiUserId(apiUserId);
                HibernateController.update(user);
            } catch (AmoWrongLoginOrApiKeyException e) {
                LOGGER.debug("{}: Не правильный логин или пароль к AMO аккаунту {}", login, amoAccount);
                return;
            } catch (Exception e) {
                LOGGER.error(login + ": Не удалось получить айдишники телефонов или сохранить пользователя", e);
                return;
            }
        }

        LOGGER.debug("{}: AMO аккаунт подключен.", login);
        threadLocalAmoAccount.set(amoAccount);
    }

    public static void createOrFindDeal(String calledFrom) throws Exception {
        AmoAccount amoAccount = threadLocalAmoAccount.get();
        if (amoAccount == null) { // Amo account отсутствует
            return;
        }
        User user = amoAccount.getUser();

        int startedLeadId = amoAccount.getLeadId(); // айдишник этапа сделки

        String contactName = "Уточнить имя";
        String login = user.getLogin();

        // Сначала проверяем есть ли уже такой контакт
        JsonAmoContact jsonAmoContact = AmoDAO.getContactIdByPhoneNumber(amoAccount, calledFrom);

        if (jsonAmoContact == null) { //если такого контакта еще нет - создаём и контакт и сделку.
            LOGGER.debug("{}: Контакт AMO по телефону {} не найден. Создаём новый контакт и сделку.", login, calledFrom);

            IdPairTime idPairTime = AmoDAO.addNewDealAndGetBackIdAndTime(amoAccount, "Nextel", startedLeadId);
            threadLocalAmoDialId.set(idPairTime.getId());
            threadLocalLastOperationTime.set(idPairTime.getTime());

            int contactId = AmoDAO.addNewContactNewMethod(amoAccount, calledFrom, idPairTime.getId(), "Nextel", contactName);
            LOGGER.debug("{}: Новый контакт {} и сделка {} созданы!", login, contactId, idPairTime.getId());
            threadLocalAmoContactId.set(contactId);

        } else { // контакт уже есть.
            LOGGER.debug("{}: Контакт AMO по телефону {} найден. id={} Ищем и обновляем сделку.", login, calledFrom, jsonAmoContact.getId());
            threadLocalAmoContactId.set(jsonAmoContact.getId());
            // нужно найти сделки привязанные к этому контакту
            JsonAmoDeal latestActiveDial = AmoDAO.getContactsLatestActiveDial(amoAccount, jsonAmoContact);

            if (latestActiveDial != null) { // если у контакта есть уже открытая сделка - то просто добавляем комент
                threadLocalAmoDialId.set(latestActiveDial.getIdInt());
                threadLocalLastOperationTime.set(latestActiveDial.getServerResponseTime());

            } else { // если у контакта нет активной сделки - то создаём новую и в контакте её добавляем.
                IdPairTime idPairTime = AmoDAO.addNewDealAndGetBackIdAndTime(amoAccount, "Nextel", startedLeadId);
                threadLocalAmoDialId.set(idPairTime.getId());
                threadLocalLastOperationTime.set(idPairTime.getTime());
                jsonAmoContact.addLinked_leads_id("" + idPairTime.getId());
                AmoDAO.updateContact(amoAccount, jsonAmoContact);
            }
        }

        System.out.println("ThreadName " + Thread.currentThread().getName());
        System.out.println("threadLocalAmoDialId " + threadLocalAmoDialId.get());
        System.out.println("threadLocalLastOperationTime " + threadLocalLastOperationTime.get());
        System.out.println("threadLocalAmoContactId " + threadLocalAmoContactId.get());
        System.out.println("threadLocalAmoAccount " + threadLocalAmoAccount.get());
    }


    public static String getResponsibleUserId(String calledFrom) throws Exception {
        AmoAccount amoAccount = threadLocalAmoAccount.get();
        if (amoAccount == null) { // Amo account отсутствует
            return null;
        }
        User user = amoAccount.getUser();
        JsonAmoContact jsonAmoContact = AmoDAO.getContactIdByPhoneNumber(amoAccount, calledFrom);
        String responsible_user_id = jsonAmoContact.getResponsible_user_id();
        return responsible_user_id;
    }

    public static String getResponsibleUserPhone(String calledFrom) throws Exception {
        AmoAccount amoAccount = threadLocalAmoAccount.get();
        if (amoAccount == null) { // Amo account отсутствует
            return null;
        }
        User user = amoAccount.getUser();

        String responsibleUserId = AmoMultiThreadController.getResponsibleUserId(calledFrom);
        if (!StringUtils.isBlank(responsibleUserId)) {
            String apiUserId = amoAccount.getApiUserId();
            if (responsibleUserId.equals(apiUserId)) {
                LOGGER.debug("{}: номер телефона ответственного сотрудника не найден, потому что это api", user.getLogin());
                return null;
            }

            AmoOperatorLocation operatorLocation = user.getOperatorLocation();
            HashMap<String, String> amoUserIdAndInnerNumber = operatorLocation.getAmoUserIdAndInnerNumber();
            String responsibleOperatorNumber = amoUserIdAndInnerNumber.get(responsibleUserId);

            return responsibleOperatorNumber;
        } else {
            LOGGER.debug("{}: номер телефона ответственного сотрудника не найден", user.getLogin());
            return null;
        }
    }


    public static void sendWsMessageCallingToNumber(String to) {
        AmoAccount amoAccount = threadLocalAmoAccount.get();
        if (amoAccount == null) { // Amo account отсутствует
            return;
        }
        User user = amoAccount.getUser();
        sendWsMessage(amoAccount, dial, threadLocalFromNumber.get(), to, threadLocalAsteriskId.get());
    }

    public static void sendWsMessageCallingToNumber(List<String> to) {
        for (String s : to) {
            sendWsMessageCallingToNumber(s);
        }
    }

    private static void sendWsMessage(AmoAccount amoAccount, MessageCallPhase callPhase, String from, String to, String asteriskId) {
        String workersId = amoAccount.getWorkersId(to);
        if (workersId != null) {// мы знаем id работника.
            String login = amoAccount.getUser().getLogin();
            WsMessage message = new WsMessage(incomingCall);
            message.setFrom(from);
            message.setDealId("" + threadLocalAmoDialId.get());
            message.setCallId(asteriskId);
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


//    public static void


    private void prepareCreateContactAndDeal(Call call) {
        if (call.getDirection() != Call.Direction.IN) {
            return; // пока что занимаемся только входящими.
        }

        User user = call.getUser();
        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) { // Если у пользователя нет акка в AmoCrm - не отправляем ничего
            return;
        }

        String login = user.getLogin();
        String domain = amoAccount.getDomain();
        // Проверяем заполнены ли поля в amoAccount
        String amoLogin = amoAccount.getAmoLogin();
        String amoApiKey = amoAccount.getApiKey();

        if (StringUtils.isAnyBlank(domain, amoLogin, amoApiKey)) {
            LOGGER.debug("Минимум одно поле амо аккаунта - пустое. Отправки не будет.");
            return;
        }

        LOGGER.debug("{}: отправка в Amo {} звонка {}", login, domain, call);


        // Нужен phoneId и EnumId для создания контакта
        String phoneId = amoAccount.getPhoneId();
        String phoneEnumId = amoAccount.getPhoneEnumId();

        if (StringUtils.isAnyBlank(phoneId, phoneEnumId)) {
            try {
                LOGGER.trace("{}: Есть аккаунт Amo, но нет айдишников телефонов.", login);
                JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);
                phoneId = jsonAmoAccount.getPhoneId();
                phoneEnumId = jsonAmoAccount.getPhoneEnumId();
                LOGGER.trace("{}: Айдишники телефонов получены - сохраняю в аккаунте пользователя", login);
                //получили айдишники телефонов. Теперь сохраняем в бд
                amoAccount.setPhoneId(phoneId);
                amoAccount.setPhoneEnumId(phoneEnumId);
                HibernateController.update(user);
            } catch (AmoWrongLoginOrApiKeyException e) {
                LOGGER.debug("{}: Не правильный логин или пароль к AMO аккаунту {}", login, amoAccount);
                return;
            } catch (Exception e) {
                LOGGER.error(login + ": Не удалось получить айдишники телефонов или сохранить пользователя", e);
                return;
            }
        }

        int startedLeadId = amoAccount.getLeadId(); // айдишник этапа сделки

//        Все данные готовы. Теперь отправляем инфу.
//        if (call.getAmoDealId() == 0) {
//            LOGGER.info("{}: Только позвонили - это первый редирект. Создаём или привязываем сделку", login);
//            try {
//                createOrFindDeal(amoAccount, startedLeadId, user, call);
//                // Сделка создана или была и найдена. Теперь оповещаем пользователя о том, что ему звонят
//                sendWsMessage(amoAccount, call, dial);
//
//            } catch (Exception e) {
//                LOGGER.error(login + ": Не удалось создать сделку и контакт", e);
//            }
//            return;
//        }

        //Сделку уже создали или нашли. Возможно сотрудник не ответил и это редирект другому сотруднику.
        // CallState null только если еще никто не ответил, а просто выполнился еще один редирект
//        if (call.getCallState() == null) {
//            LOGGER.info("{}: Еще никто не ответил на звонок, просто выполнился еще один редирект", login);
//            sendWsMessage(amoAccount, call, dial);
//            return;
//        }

        //Проверим сначала не завершен ли звонок
//        if (call.isCallIsEnded()) {
//            LOGGER.info("{}: Звонок завершен проверяем отвечен ли он или нет", login);
//            try {
//                AmoDAO.addCallToNotes(amoAccount, call);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            if (call.getCallState() == Call.CallState.ANSWER) {
//                sendWsMessage(amoAccount, call, ended);
//                return;
//
//            } else {
//                sendWsMessage(amoAccount, call, noanswer);
//                return;
//            }
//
//        }


        // если мы здесь - значит звонок не завершен. Скорее всего на него ответили
//        if (call.getCallState() == Call.CallState.ANSWER) { // на звонок ответили
//            LOGGER.info("{}: На звонок только что ответили. Трубку поднял {}", login, call.getCalledTo());
//            sendWsMessage(amoAccount, call, answer);
//        }


//        else { // на звонок не ответили. Просто сообщаем что был звонок
//            try {
//                AmoDAO.addNewComent(amoAccount, call.getAmoDealId(), "На звонок не ответили. Возможно сбой", call.getCalculatedModifiedTime());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            LOGGER.trace("{}: Произошел какой-то сбой при звонке и ответа не было", login);
//            sendWsMessage(amoAccount, call, noanswer);
//            throw new RuntimeException("По какой-то причине звонок не завершен и нам вернулся STATE="+call.getCallState()+" call="+call);
//        }
    }
}
