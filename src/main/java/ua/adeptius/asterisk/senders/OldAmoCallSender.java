package ua.adeptius.asterisk.senders;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyException;
import ua.adeptius.amocrm.javax_web_socket.MessageCallPhase;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.*;
import java.util.concurrent.LinkedBlockingQueue;
import static ua.adeptius.amocrm.javax_web_socket.MessageCallPhase.*;

public class OldAmoCallSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(OldAmoCallSender.class.getSimpleName());
    private LinkedBlockingQueue<Call> blockingQueue = new LinkedBlockingQueue<>();


    public void send(Call call) {
        try {
            blockingQueue.put(call);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }

    public OldAmoCallSender() {
        setName("AmoCallSender");
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Call call = blockingQueue.take();
                prepareCreateContactAndDeal(call);
            } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
            }
        }
    }

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
        if (call.getCallState() == null) {
            LOGGER.info("{}: Еще никто не ответил на звонок, просто выполнился еще один редирект", login);
            sendWsMessage(amoAccount, call, dial);
            return;
        }

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
        if (call.getCallState() == Call.CallState.ANSWER) { // на звонок ответили
            LOGGER.info("{}: На звонок только что ответили. Трубку поднял {}", login, call.getCalledTo());
            sendWsMessage(amoAccount, call, answer);
        }


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

    private void sendWsMessage(AmoAccount amoAccount, Call call, MessageCallPhase callPhase) {
//        String workersId = amoAccount.getWorkersId(call.getCalledTo());
//        if (workersId != null) {// мы знаем id работника.
//            String login = amoAccount.getUser().getLogin();
//            WsMessage message = new WsMessage(incomingCall);
//            message.setFrom(call.getCalledFrom());
//            message.setDealId("" + call.getAmoDealId());
//            message.setCallId(call.getAsteriskId());
//            message.setCallPhase(callPhase);
//            WebSocket.sendMessage(workersId, message);// отправляем
//            if (callPhase == noanswer) {
//                LOGGER.trace("{}: Отправлено WS сообщение, что звонок был пропущен.", login);
//            } else if (callPhase == answer || callPhase == ended) {
//                LOGGER.trace("{}: Отправлено WS сообщение, что ответ на звонок был.", login);
//            } else if (callPhase == dial) {
//                LOGGER.trace("{}: Отправили WS сообщение о новом звонке", login);
//            }
//        }
    }


    private void createOrFindDeal(AmoAccount amoAccount, int startedLeadId, User user, Call call) throws Exception {
//        String contactName = "Уточнить имя";
//        String contactNumber = call.getCalledFrom();
//        String login = user.getLogin();
//
//        // Сначала проверяем есть ли уже такой контакт
//        JsonAmoContact jsonAmoContact = AmoDAO.getContactIdByPhoneNumber(amoAccount, contactNumber);
//
//        if (jsonAmoContact == null) { //если такого контакта еще нет - создаём и контакт и сделку.
//            LOGGER.debug("{}: Контакт AMO по телефону {} не найден. Создаём новый контакт и сделку.", login, contactNumber);
//
//            IdPairTime idPairTime = AmoDAO.addNewDealAndGetBackIdAndTime(amoAccount, "Nextel", startedLeadId);
//            int dealId = idPairTime.getId();
//            call.setAmoDealId(dealId); // ПРИВЯЗКА
//            call.setLastOperationTime(idPairTime.getTime());
//
//            int contactId = AmoDAO.addNewContactNewMethod(amoAccount, contactNumber, dealId, "Nextel", contactName);
//            LOGGER.debug("{}: Новый контакт {} и сделка {} созданы!", login, contactId, dealId);
//            call.setAmoContactId(contactId);
//
//        } else { // контакт уже есть.
//            LOGGER.debug("Контакт AMO по телефону {} найден. id={} Ищем и обновляем сделку.", contactNumber, jsonAmoContact.getId());
//            call.setAmoContactId(jsonAmoContact.getId());
//            // нужно найти сделки привязанные к этому контакту
//            JsonAmoDeal latestActiveDial = AmoDAO.getContactsLatestActiveDial(amoAccount, jsonAmoContact);
//
//            if (latestActiveDial != null) { // если у контакта есть уже открытая сделка - то просто добавляем комент
//                call.setAmoDealId(latestActiveDial.getIdInt());
//                call.setLastOperationTime(latestActiveDial.getServerResponseTime());
//
//            } else { // если у контакта нет активной сделки - то создаём новую и в контакте её добавляем.
//                IdPairTime idPairTime = AmoDAO.addNewDealAndGetBackIdAndTime(amoAccount, "Nextel", startedLeadId);
//                call.setAmoDealId(idPairTime.getId());
//                call.setLastOperationTime(idPairTime.getTime());
//                jsonAmoContact.addLinked_leads_id("" + idPairTime.getId());
//                AmoDAO.updateContact(amoAccount, jsonAmoContact);
//            }
//        }
    }
}
