package ua.adeptius.asterisk.senders;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.amocrm.model.json.JsonAmoContact;
import ua.adeptius.amocrm.model.json.JsonAmoDeal;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.IdPairTime;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.NewCall;


import java.util.concurrent.LinkedBlockingQueue;

public class AmoCallSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(AmoCallSender.class.getSimpleName());
    private LinkedBlockingQueue<NewCall> blockingQueue = new LinkedBlockingQueue<>();

    public void send(NewCall call) {
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
                NewCall call = blockingQueue.take();
                prepareCreateContactAndDeal(call);
            } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
            }
        }
    }

    private void prepareCreateContactAndDeal(NewCall call) {
        if (call.getDirection() != NewCall.Direction.IN){
            return; // пока что занимаемся только входящими.
        }

        User user = call.getUser();
        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) { // Если у пользователя нет акка в AmoCrm - не отправляем ничего
            return;
        }

        LOGGER.info("Отправка сделки в Amo пользователя {}", user.getLogin());
        // Проверяем заполнены ли поля в amoAccount
        String domain = amoAccount.getDomain();
        String amoLogin = amoAccount.getAmoLogin();
        String amoApiKey = amoAccount.getApiKey();

        if (StringUtils.isAnyBlank(domain, amoLogin, amoApiKey)) {
            LOGGER.info("Минимум одно поле амо аккаунта - пустое. Отправки не будет.");
            return;
        }

        // Нужен phoneId и EnumId для создания контакта
        String phoneId = amoAccount.getPhoneId();
        String phoneEnumId = amoAccount.getPhoneEnumId();

        if (StringUtils.isAnyBlank(phoneId, phoneEnumId)) {
            try {
                LOGGER.trace("У пользователя {} есть аккаунт Amo, но нет айдишников телефонов.", user.getLogin());
                JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(domain, amoLogin, amoApiKey);
                phoneId = jsonAmoAccount.getPhoneId();
                phoneEnumId = jsonAmoAccount.getPhoneEnumId();
                LOGGER.trace("Айдишники телефонов получены - сохраняю в аккаунте пользователя {}", user.getLogin());
                //получили айдишники телефонов. Теперь сохраняем в бд
                amoAccount.setPhoneId(phoneId);
                amoAccount.setPhoneEnumId(phoneEnumId);
                HibernateDao.update(user);
            } catch (Exception e) {
                LOGGER.error("Не удалось получить айдишники телефонов или сохранить пользователя " + user.getLogin(), e);
                return;
            }
        }

        int startedLeadId = amoAccount.getLeadId(); // айдишник этапа сделки

//        Все данные готовы. Теперь отправляем инфу.


        if (call.getAmoDealId() == 0) {// только позвонили - это первый редирект. Создаём или привязываем сделку
            try {
                sendFirstCall(domain, amoLogin, amoApiKey, phoneId, phoneEnumId, call.getCalledFrom(), startedLeadId, user, call);
            } catch (Exception e) {
                LOGGER.error("Не удалось создать сделку и контакт " + user.getLogin(), e);
            }
            return;
        }


        // если мы здесь - значит первый редирект уже был - просто обновляем таги.
        if (call.getCallState() == null) { // CallState null только если еще никто не ответил, а просто выполнился еще один редирект
            //  меняем тег на
            String tags = "Звонок -> " + call.getCalledTo();
            try {
                AmoDAO.setTagsToDeal(domain, amoLogin, amoApiKey, tags, call.getAmoDealId(), call.getCalculatedModifiedTime());
            }catch (Exception e){
                e.printStackTrace();
            }
            return;
        }

        // если мы здесь - значит CallState не null. Проверим сначала не завершен ли звонок
        // потому что дальше проверка на ANSWER, а звонок мог завершится с таким состоянием.
        if (call.isCallIsEnded()){
//            Убираем тэги
            try {
                if (call.getCallState() == NewCall.CallState.ANSWER){
                    AmoDAO.setTagsToDeal(domain, amoLogin, amoApiKey, "Отвечено", call.getAmoDealId(), call.getCalculatedModifiedTime());
                }else {
                    AmoDAO.setTagsToDeal(domain, amoLogin, amoApiKey, "Звонок не принят", call.getAmoDealId(), call.getCalculatedModifiedTime());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return;
        }


        // если мы здесь - значит CallState не null - произошел либо ответ либо сбой. выясняем
        if (call.getCallState() == NewCall.CallState.ANSWER) { // на звонок ответили
            String tags = "Разговор -> " + call.getCalledTo();
            try {
                AmoDAO.setTagsToDeal(domain, amoLogin, amoApiKey, tags, call.getAmoDealId(), call.getCalculatedModifiedTime());
            }catch (Exception e){
                e.printStackTrace();
            }
        } else { // на звонок не ответили. Просто пишем что был звонок
            String tags = "Пропущенный звонок";
            try {
                AmoDAO.setTagsToDeal(domain, amoLogin, amoApiKey, tags, call.getAmoDealId(), call.getCalculatedModifiedTime());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private void sendFirstCall(String domain, String userLogin, String userApiKey, String phoneId, String phoneEnumId,
                               String contactNumber, int startedLeadId, User user, NewCall call) throws Exception {
        String tags = "Звонок -> " + call.getCalledTo();
        String contactName = "Уточнить имя";

        // Сначала проверяем есть ли уже такой контакт
        JsonAmoContact jsonAmoContact = AmoDAO.getContactIdByPhoneNumber(domain, userLogin, userApiKey, contactNumber);

        if (jsonAmoContact == null) { //если такого контакта еще нет - создаём и контакт и сделку.
            LOGGER.debug("Контакт AMO по телефону {} не найден. Создаём новый контакт и сделку.", contactNumber);

//            int dealId = AmoDAO.addNewDeal(domain, userLogin, userApiKey, tags, startedLeadId);
            IdPairTime idPairTime = AmoDAO.addNewDealAndGetBackIdAndTime(domain, userLogin, userApiKey, tags, startedLeadId);
            call.setAmoDealId(idPairTime.getId()); // ПРИВЯЗКА
            call.setLastOperationTime(idPairTime.getTime());

            AmoDAO.addNewContactNewMethod(domain, userLogin, userApiKey, contactNumber, idPairTime.getId(), phoneId, phoneEnumId, "Nextel", contactName);
            LOGGER.debug("Новый контакт и сделка для пользователя {} созданы!", user.getLogin());

        } else { // контакт уже есть.
            LOGGER.debug("Контакт AMO по телефону {} найден. id={} Ищем и обновляем сделку.", contactNumber, jsonAmoContact.getId());
            // нужно найти сделки привязанные к этому контакту
            JsonAmoDeal latestActiveDial = AmoDAO.getContactsLatestActiveDial(domain, userLogin, userApiKey, jsonAmoContact);

            if (latestActiveDial != null) { // если у контакта есть уже открытая сделка - то просто добавляем комент
                AmoDAO.addNewComent(domain, userLogin, userApiKey, latestActiveDial.getIdInt(), "Повторный звонок от клиента.", call.getCalculatedModifiedTime());
                call.setAmoDealId(latestActiveDial.getIdInt());
                call.setLastOperationTime(latestActiveDial.getServerResponseTime());

            } else { // если у клиента нет активной сделки - то создаём новую и в контакте её добавляем.
                IdPairTime idPairTime = AmoDAO.addNewDealAndGetBackIdAndTime(domain, userLogin, userApiKey, tags, startedLeadId);
                call.setAmoDealId(idPairTime.getId());
                call.setLastOperationTime(idPairTime.getTime());
                jsonAmoContact.addLinked_leads_id("" + idPairTime.getId());
                AmoDAO.updateContact(domain, userLogin, userApiKey, jsonAmoContact);
            }
        }
    }
}
