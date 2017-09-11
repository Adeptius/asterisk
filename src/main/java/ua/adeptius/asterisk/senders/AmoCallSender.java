package ua.adeptius.asterisk.senders;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.exceptions.AmoTooManyRequestsException;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyException;
import ua.adeptius.amocrm.javax_web_socket.MessageCallPhase;
import ua.adeptius.amocrm.javax_web_socket.WebSocket;
import ua.adeptius.amocrm.javax_web_socket.WsMessage;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.amocrm.model.json.JsonAmoContact;
import ua.adeptius.amocrm.model.json.JsonAmoDeal;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.*;

import java.util.concurrent.LinkedBlockingQueue;

import static ua.adeptius.amocrm.javax_web_socket.MessageCallPhase.*;
import static ua.adeptius.amocrm.javax_web_socket.MessageEventType.incomingCall;

@SuppressWarnings("Duplicates")
public class AmoCallSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(AmoCallSender.class.getSimpleName());
    private LinkedBlockingQueue<Call> blockingQueue = new LinkedBlockingQueue<>();


    public void send(Call call) {
        try {
            blockingQueue.put(call);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }

    public AmoCallSender() {
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

        if (Main.remoteServerIsUp){
            LOGGER.info("Работа локально. AmoCallSender отключен");
            return;
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

        int startedLeadId = amoAccount.getLeadId(); // айдишник этапа сделки

        //        Все данные готовы. Теперь отправляем инфу.


        Call.CallPhase callPhase = call.getCallPhase();

        if (callPhase == Call.CallPhase.NEW_CALL) {
            LOGGER.info("{}: Только позвонили - это первый редирект. Создаём или привязываем сделку", login);
            try {
                createOrFindDeal(amoAccount, startedLeadId, user, call);
            } catch (Exception e) {
                LOGGER.error(login + ": Не удалось создать сделку и контакт", e);
            }

        } else if (callPhase == Call.CallPhase.REDIRECTED) {
            LOGGER.info("{}: Еще никто не ответил на звонок, просто выполнился еще один редирект", login);
            // Тут ничего не делаем потому что всю работу выполняет AmoWSMessageSender


        } else if (callPhase == Call.CallPhase.ANSWERED) {
            LOGGER.info("{}: На звонок только что ответили. Трубку поднял {}", login, call.getCalledTo());


        } else if (callPhase == Call.CallPhase.ENDED) {
            LOGGER.info("{}: Звонок завершен проверяем отвечен ли он или нет", login);
            try {
                AmoDAO.addCallToNotes(amoAccount, call);
            } catch (AmoTooManyRequestsException e) {
                LOGGER.warn("{}: Очень много запросов на добавления звонка в АМО", login);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void createOrFindDeal(AmoAccount amoAccount, int startedLeadId, User user, Call call) throws Exception {
        String contactName = "Уточнить имя";
        String contactNumber = call.getCalledFrom();
        String login = user.getLogin();

        // Сначала проверяем есть ли уже такой контакт
        JsonAmoContact jsonAmoContact = AmoDAO.getContactIdByPhoneNumber(amoAccount, contactNumber);

        if (jsonAmoContact == null) { //если такого контакта еще нет - создаём и контакт и сделку.
            LOGGER.debug("{}: Контакт AMO по телефону {} не найден. Создаём новый контакт и сделку.", login, contactNumber);

            IdPairTime idPairTime = AmoDAO.addNewDealAndGetBackIdAndTime(amoAccount, "Nextel", startedLeadId);
            int dealId = idPairTime.getId();
            call.setAmoDealId(dealId); // ПРИВЯЗКА
            call.setLastOperationTime(idPairTime.getTime());

            int contactId = AmoDAO.addNewContactNewMethod(amoAccount, contactNumber, dealId, "Nextel", contactName);
            LOGGER.debug("{}: Новый контакт {} и сделка {} созданы!", login, contactId, dealId);
            call.setAmoContactId(contactId);

        } else { // контакт уже есть.
            LOGGER.debug("Контакт AMO по телефону {} найден. id={} Ищем и обновляем сделку.", contactNumber, jsonAmoContact.getId());
            call.setAmoContactId(jsonAmoContact.getId());
            // нужно найти сделки привязанные к этому контакту
            JsonAmoDeal latestActiveDial = AmoDAO.getContactsLatestActiveDial(amoAccount, jsonAmoContact);

            if (latestActiveDial != null) { // если у контакта есть уже открытая сделка - то просто добавляем комент
                call.setAmoDealId(latestActiveDial.getIdInt());
                call.setLastOperationTime(latestActiveDial.getServerResponseTime());

            } else { // если у контакта нет активной сделки - то создаём новую и в контакте её добавляем.
                IdPairTime idPairTime = AmoDAO.addNewDealAndGetBackIdAndTime(amoAccount, "Nextel", startedLeadId);
                call.setAmoDealId(idPairTime.getId());
                call.setLastOperationTime(idPairTime.getTime());
                jsonAmoContact.addLinked_leads_id("" + idPairTime.getId());
                AmoDAO.updateContact(amoAccount, jsonAmoContact);
            }
        }
    }
}
