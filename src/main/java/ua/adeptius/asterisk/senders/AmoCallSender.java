package ua.adeptius.asterisk.senders;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyException;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.amocrm.model.json.JsonAmoContact;
import ua.adeptius.amocrm.model.json.JsonAmoDeal;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.model.telephony.Call;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.*;

import static ua.adeptius.amocrm.javax_web_socket.MessageCallPhase.answer;
import static ua.adeptius.amocrm.javax_web_socket.MessageCallPhase.answeredSomeoneElse;

@SuppressWarnings("Duplicates")
public class AmoCallSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(AmoCallSender.class.getSimpleName());
    private LinkedBlockingQueue<Call> blockingQueue = new LinkedBlockingQueue<>();
    private static Settings settings = Main.settings;
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            3, 20, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(30), new ThreadFactoryBuilder().setNameFormat("AmoCallSender-Pool-%d").build());


    public void send(Call call) {
        try {
            blockingQueue.put(call);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }

    public AmoCallSender() {
        setName("AmoCallSender-Manager");
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Call call = blockingQueue.take();
                EXECUTOR.submit(() -> prepareCreateContactAndDeal(call));
            } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
            }
        }
    }

    private void prepareCreateContactAndDeal(Call call) {

        if (!settings.isCallToAmoEnabled()) {
            LOGGER.info("AmoCallSender отключен в настройках");
            return;
        }

        if (call.getDirection() != Call.Direction.IN) {
            return; // пока что занимаемся только входящими.
        }

        if (Main.remoteServerIsUp) {
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

        LOGGER.info("{}: отправка в Amo {} звонка {}", login, domain, call);

        // Нужен phoneId и EnumId для создания контакта
        String phoneId = amoAccount.getPhoneId();
        String phoneEnumId = amoAccount.getPhoneEnumId();
        String apiUserId = amoAccount.getApiUserId();

        if (StringUtils.isAnyBlank(phoneId, phoneEnumId, apiUserId)) {
            try {
                LOGGER.debug("{}: Есть аккаунт Amo, но нет айдишников телефонов.", login);
                JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);
                phoneId = jsonAmoAccount.getPhoneId();
                phoneEnumId = jsonAmoAccount.getPhoneEnumId();
                apiUserId = jsonAmoAccount.getCurrent_user_id();
                LOGGER.debug("{}: Айдишники телефонов получены - сохраняю в аккаунте пользователя", login);
                //получили айдишники телефонов. Теперь сохраняем в бд
                amoAccount.setPhoneId(phoneId);
                amoAccount.setPhoneEnumId(phoneEnumId);
                amoAccount.setApiUserId(apiUserId);
                HibernateController.update(user);
            } catch (AmoWrongLoginOrApiKeyException e) {
                LOGGER.warn("{}: Не правильный логин или пароль к AMO аккаунту {}", login, amoAccount);
                return;
            } catch (Exception e) {
                LOGGER.error(login + ": Не удалось получить айдишники телефонов или сохранить пользователя", e);
                return;
            }
        }

        int startedLeadId = amoAccount.getLeadId(); // айдишник этапа сделки

        //        Все данные готовы. Теперь отправляем инфу.


        Call.CallPhase callPhase = call.getCallPhase();
        String calledFrom = call.getCalledFrom();


        if (callPhase == Call.CallPhase.NEW_CALL) {
            LOGGER.info("{}: Только позвонили с {}", login, calledFrom);
            // Тут находим id сделки и контакта


        } else if (callPhase == Call.CallPhase.REDIRECTED) {
            // Тут ничего не делаем потому что всю работу выполняет AmoWSMessageSender


        } else if (callPhase == Call.CallPhase.ANSWERED) {
            String calledTo = call.getCalledTo().get(0);
            LOGGER.debug("{}: На звонок только что ответили. Трубку поднял {}", login, calledTo);
            // надо понять какой из сотрудников поднял трубку и создать сделку и контакт, назначенные на него.

            String answeredWorkerId = amoAccount.getWorkersId(calledTo);

            try {
                createOrFindDeal(amoAccount, startedLeadId, user, call, answeredWorkerId);
            } catch (Exception e) {
                LOGGER.error(login + ": Не удалось создать сделку и контакт", e);
            }

            // todo может напрямую в сокет слать?
            AmoWSMessageSender.sendWsMessageOutgoingCall(amoAccount, call, answer);


        } else if (callPhase == Call.CallPhase.ENDED) {
            try {
                if (call.getCallState() == Call.CallState.ANSWER) {
                    LOGGER.debug("{}: Звонок завершен. На него ответили и сделка в Амо уже была создана", login);
                } else {
                    LOGGER.debug("{}: Звонок завершен. На него не ответили. Создаём контакт или сделку, если отсутствуют.", login);
                    int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                    String responsibleWorker = amoAccount.getResponsibleUserSchedule()[dayOfWeek -1];
                    LOGGER.debug("{}: Сегодня {} день недели. Ответственным будет оператор {}.", login, dayOfWeek, responsibleWorker);

                    if (responsibleWorker == null) {
                        responsibleWorker = apiUserId;
                        LOGGER.debug("{}: Ответственный оператор не указан. Им будет владелец API {}.", login, responsibleWorker);
                    }
                    createOrFindDeal(amoAccount, startedLeadId, user, call, responsibleWorker);
                }
                AmoDAO.addCallToNotes(amoAccount, call);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private void createOrFindDeal(AmoAccount amoAccount, int startedLeadId, User user, Call call, String answeredWorkerId) throws Exception {
        String contactNumber = call.getCalledFrom();
        String contactName = "Контакт " + contactNumber;
        String login = user.getLogin();

        // Сначала проверяем есть ли уже такой контакт
        JsonAmoContact jsonAmoContact = AmoDAO.getContactIdByPhoneNumber(amoAccount, contactNumber);

        if (jsonAmoContact == null) { //если такого контакта еще нет - создаём и контакт и сделку.
            LOGGER.debug("{}: Контакт AMO по телефону {} не найден. Создаём новый контакт и сделку.", login, contactNumber);

            IdPairTime idPairTime = AmoDAO.addNewDealAndGetBackIdAndTime(amoAccount, "Nextel", startedLeadId, answeredWorkerId);
            int dealId = idPairTime.getId();
            call.setAmoDealId(dealId); // ПРИВЯЗКА
            call.setLastOperationTime(idPairTime.getTime());

            int contactId = AmoDAO.addNewContactNewMethod(amoAccount, contactNumber, dealId, "Nextel", contactName, answeredWorkerId);
            LOGGER.debug("{}: Новый контакт {} и сделка {} созданы!", login, contactId, dealId);
            call.setAmoContactId(contactId);
            call.setAmoContactResponsibleId(amoAccount.getApiUserId());

        } else { // контакт уже есть.
            LOGGER.debug("Контакт AMO по телефону {} найден. id={} Ищем и обновляем сделку.", contactNumber, jsonAmoContact.getId());
            call.setAmoContactId(jsonAmoContact.getId());
            call.setAmoContactResponsibleId(jsonAmoContact.getResponsible_user_id());
            // нужно найти сделки привязанные к этому контакту
            JsonAmoDeal latestActiveDial = AmoDAO.getContactsLatestActiveDial(amoAccount, jsonAmoContact);

            if (latestActiveDial != null) { // если у контакта есть уже открытая сделка - то просто добавляем комент
                call.setAmoDealId(latestActiveDial.getIdInt());
                call.setLastOperationTime(latestActiveDial.getServerResponseTime());

            } else { // если у контакта нет активной сделки - то создаём новую и в контакте её добавляем.
                IdPairTime idPairTime = AmoDAO.addNewDealAndGetBackIdAndTime(amoAccount, "Nextel", startedLeadId, answeredWorkerId);
                call.setAmoDealId(idPairTime.getId());
                call.setLastOperationTime(idPairTime.getTime());
                jsonAmoContact.addLinked_leads_id("" + idPairTime.getId());
                AmoDAO.updateContact(amoAccount, jsonAmoContact);
            }
        }
    }
}
