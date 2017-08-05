package ua.adeptius.asterisk.monitor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.model.TimePairCookie;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.model.OldPhone;
import ua.adeptius.asterisk.model.Tracking;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
@EnableScheduling
public class Scheduler{

    private static Logger LOGGER = LoggerFactory.getLogger(Scheduler.class.getSimpleName());


    /**
     * ConnectionKeeper
     */
    @Scheduled(fixedDelay = 60000)
    public void keepConnectHibernate(){
        try {
            HibernateDao.getUserByLogin("e404");
        } catch (Exception e) {
            LOGGER.error("Не удалось получить тестового пользователя", e);
        }
        try {
            MySqlCalltrackDao.getMelodies();
        } catch (Exception e) {
            LOGGER.error("Не удалось получить список мелодий для поддержания соединения", e);
        }
        try {
            MySqlStatisticDao.getListOfTables();
        } catch (Exception e) {
            LOGGER.error("Не удалось получить список таблиц статистик для поддержания соединения", e);
        }
    }


    /**
     * Tracking phone Watcher
     */
//    @Scheduled(initialDelay = 10000, fixedRate = 3000)
//    private void checkAllPhones(){
//        for (Tracking tracking : UserContainer.getAllSites()) {
//            List<OldPhone> oldPhones = tracking.getOldPhones();
//            for (OldPhone oldPhone : oldPhones) {
//                if (!oldPhone.isFree()) { // если телефон не простаивает
//                    processBusyPhone(tracking, oldPhone);
//                }
//            }
//        }
//    }

//    private void processBusyPhone(Tracking tracking, OldPhone oldPhone){
//        long currentTime = new GregorianCalendar().getTimeInMillis();
//        long phoneTime = oldPhone.getUpdatedTime();
//
//        // продливаем время аренды номера
//        long past = currentTime - phoneTime;
//        int timeToDeleteOldPhones = Integer.parseInt(Settings.getSetting("SECONDS_TO_REMOVE_OLD_PHONES"))*1000;
//        if (past > timeToDeleteOldPhones) {
////            MyLogger.log(NUMBER_FREE, tracking.getLogin() + ": номер " + oldPhone.getNumber() + " освободился. Был занят " + oldPhone.getBusyTimeText());
//            oldPhone.markFree();
//        }
//
//        if (oldPhone.getUpdatedTime() != 0) {
//            // считаем сколько времени номер занят
//            past = currentTime - oldPhone.getStartedBusy();
//            oldPhone.setBusyTime(past);
//        }
//
//        long timeToBlock = tracking.getTimeToBlock()*60*1000;
//
//        if (past > timeToBlock){
//            try {
////                MyLogger.log(NUMBER_FREE, tracking.getLogin() + ": IP " + oldPhone.getIp() + " заблокирован по времени.");
//                tracking.addIpToBlackList(oldPhone.getIp());
//                oldPhone.markFree();
//            } catch (Exception e) {
////                MyLogger.log(DB_OPERATIONS, tracking.getLogin() + ": ошибка добавления " + oldPhone.getIp() + " в БД");
//            }
//        }
//    }


    /**
     * Amo cookie cleaner
     */
    @Scheduled(initialDelay = 10000, fixedRate = 50000)
    private void cleanCookie() {
        List<String> cookieToRemove = new ArrayList<>();
        long currentTime = new GregorianCalendar().getTimeInMillis();

        for (Map.Entry<String, TimePairCookie> entry : AmoDAO.cookiesRepo.entrySet()) {
            if ((currentTime - entry.getValue().getTimeCreated()) > TimeUnit.MINUTES.toMillis(14)){
                LOGGER.trace("Cookie пользователя {} устарел", entry.getKey());
                cookieToRemove.add(entry.getKey());
            }
        }

        for (String s : cookieToRemove) {
            AmoDAO.cookiesRepo.remove(s);
        }
    }


    /**
     *  Call processor cleaning
     */
    @Scheduled(cron = "0 0 1 * * ?")
    private void startClean(){ // TODO точно срабатывает?
        LOGGER.trace("Очистка карты number <-> Call");
        CallProcessor.calls.clear();
    }


    /**
     * Scenario writer
     */
    @Scheduled(cron = "0 55 * * * ?")
    private void generateConfig(){
        LOGGER.trace("Начинается запись всех конфигов астериска в файлы.");
        RulesConfigDAO.writeAllNeededScenarios();
    }
}
