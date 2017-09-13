package ua.adeptius.asterisk.monitor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.model.TimePairCookie;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.interceptors.AccessControlOriginInterceptor;
import ua.adeptius.asterisk.model.OuterPhone;
import ua.adeptius.asterisk.model.RegisterQuery;
import ua.adeptius.asterisk.model.RecoverQuery;
import ua.adeptius.asterisk.model.Site;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.Main.monitor;


@Component
@EnableScheduling
public class Scheduler{

    private static Logger LOGGER = LoggerFactory.getLogger(Scheduler.class.getSimpleName());


    /**
     * ConnectionKeeper
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 60000) // Каждую минуту
    public void keepConnectHibernate(){
        try {
            HibernateController.getUserByLogin("e404");
        } catch (Exception e) {
            LOGGER.error("Не удалось получить тестового пользователя", e);
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
    //todo сделать отдельным классом
    @Scheduled(initialDelay = 10000, fixedRate = 3000) // каждые 3 секунды
    private void checkAllPhones(){
        List<Site> sites = UserContainer.getUsers().stream()
                .flatMap(user -> user.getSites().stream())
                .collect(Collectors.toList());

        for (Site site : sites) {
            List<OuterPhone> outerPhones = site.getOuterPhones();
            for (OuterPhone outerPhone : outerPhones) {
                if (!outerPhone.isFree()){
                    processBusyPhone(site, outerPhone);
                }
            }
        }
    }

    private void processBusyPhone(Site site, OuterPhone phone){
        long currentTime = new GregorianCalendar().getTimeInMillis();
        long phoneTime = phone.getUpdatedTime();

        // продливаем время аренды номера
        long past = currentTime - phoneTime;
        int timeToDeleteOldPhones = Integer.parseInt(Settings.getSetting("SECONDS_TO_REMOVE_OLD_PHONES"))*1000;
        if (past > timeToDeleteOldPhones) {
//            MyLogger.log(NUMBER_FREE, tracking.getLogin() + ": номер " + oldPhone.getNumber() + " освободился. Был занят " + oldPhone.getBusyTimeText());
            phone.markFree();
        }

        if (phone.getUpdatedTime() != 0) {
            // считаем сколько времени номер занят
            past = currentTime - phone.getStartedBusy();
            phone.setBusyTimeMillis(past);
        }

        long timeToBlock = site.getTimeToBlock()*60*1000;

        if (past > timeToBlock){
            try {
//                MyLogger.log(NUMBER_FREE, tracking.getLogin() + ": IP " + oldPhone.getIp() + " заблокирован по времени.");
                site.addIpToBlackList(phone.getIp());
                phone.markFree();
            } catch (Exception e) {
//                MyLogger.log(DB_OPERATIONS, tracking.getLogin() + ": ошибка добавления " + oldPhone.getIp() + " в БД");
            }
        }
    }


    /**
     * Amo cookie cleaner
     */
    @Scheduled(initialDelay = 10000, fixedRate = 50000) // каждые 50 секунд
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
    @Scheduled(cron = "0 0 1 * * ?") // ежедневно в час ночи
    private void startClean(){
//        LOGGER.trace("Очистка карты number <-> Call");
//        CallProcessor.calls.clear();
    }


    /**
     * Scenario writer
     */
    private static int scenarioTries = 0;
    @Scheduled(cron = "0 58 * * * ?") // в 55 минут каждого часа
    private void generateConfig(){
        LOGGER.trace("Начинается запись всех конфигов астериска в файлы.");

        try {
            RulesConfigDAO.writeAllNeededScenarios();
            LOGGER.trace("Конфиги записаны в файлы.");
            scenarioTries = 0;
        } catch (Exception e) {
            scenarioTries++;
            LOGGER.error("Ошибка записи конфигов. Повтор через 30 секунд", e);
            if (scenarioTries > 2){
                LOGGER.error("2 ошибки записи конфигов. прекращаю запись");
                return;
            }
            new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                generateConfig();
            }).start();
        }
    }

    @Scheduled(cron = "10 00 * * * ?") // в 0 минут каждого часа
    private void updatePhonesMapForCallProcessor(){
        CallProcessor.updatePhonesHashMap();
    }

    @Scheduled(initialDelay = 1000 ,fixedDelay = 20000)
    private void initMonitor() {
        if (monitor == null) {
            try {
                monitor = new AsteriskMonitor();
                monitor.run();
                LOGGER.info("Монитор астериска запущен");
            } catch (Exception e) {
                LOGGER.error("Ошибка запуска мониторинга телефонии: ", e);
                monitor = null;
            }
        }
    }

    @Scheduled(fixedRate = 600000)
    private void cleanPendingQueries(){
        List<RegisterQuery> registerQueries = HibernateController.getAllRegisterQueries();
        for (RegisterQuery registerQuery : registerQueries) {
            try{
                long timeCreated = registerQuery.getDate().getTime();
                long timeNow = new Date().getTime();
                long pastTime = timeNow - timeCreated;
                int pastMinutes = (int)(pastTime / 1000 / 60);
                if (pastMinutes>180){
                    LOGGER.info("Запрос регистрации пользователя {} удалён. Прошло {} минут", registerQuery.getLogin(), pastMinutes);
                    HibernateController.removeRegisterQuery(registerQuery);
                }
            }catch (Exception e){
                LOGGER.error("Ошибка удаления запроса регистрации пользователя: " + registerQuery.getLogin() ,e);
            }
        }
    }

    @Scheduled(fixedRate = 600000)
    private void cleanRecoverQueries(){
        List<RecoverQuery> recoverQueries = HibernateController.getAllRecoverQueries();
        for (RecoverQuery recoverQuery : recoverQueries) {
            try{
                long timeCreated = recoverQuery.getDate().getTime();
                long timeNow = new Date().getTime();
                long pastTime = timeNow - timeCreated;
                int pastMinutes = (int)(pastTime / 1000 / 60);
                if (pastMinutes>180){
                    LOGGER.info("Запрос восстановления пароля {} удалён. Прошло {} минут", recoverQuery.getLogin(), pastMinutes);
                    HibernateController.removeRecoverQuery(recoverQuery);
                }
            }catch (Exception e){
                LOGGER.error("Ошибка удаления запроса восстановления пароля: " + recoverQuery.getLogin() ,e);
            }
        }
    }

    @Scheduled(fixedRate = 5000)
    private void print(){
        if (Main.settings.isShowProfilingResultNow()){
            Main.settings.setShowProfilingResultNow(false);
            AccessControlOriginInterceptor.printProfiling();
        }
    }
}
