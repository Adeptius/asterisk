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
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.interceptors.AccessControlOriginInterceptor;
import ua.adeptius.asterisk.model.RegisterQuery;
import ua.adeptius.asterisk.model.RecoverQuery;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.telephony.OuterPhone;
import ua.adeptius.asterisk.model.telephony.Rule;
import ua.adeptius.asterisk.model.telephony.Scenario;

import java.io.*;
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
     *  Asterisk reloading
     */
    private static boolean needToSipReload;
//    private static boolean needToOuterReload;
    private static Set<User> usersThatChangedRules = new HashSet<>();
    private static Set<User> usersThatChangedOuterNumbersOrBindings = new HashSet<>();

    public static void reloadSipOnNextScheduler() {
        needToSipReload = true;
    }

//    public static void reloadOuterOnNextScheduler() {
//        needToOuterReload = true;
//    }

    public static void reloadDialPlanForThisUserAtNextScheduler(User user){
        usersThatChangedRules.add(user);
    }


    public static void rewriteRulesFilesForThisUserAtNextScheduler(User user){
        usersThatChangedOuterNumbersOrBindings.add(user);
    }


    @Scheduled(cron = "0 * * * * ?") // каждую минуту
    private void startClean(){
        // что бы в этом методе по 2-3 раза не бутать ядро - делаем это один раз в конце, если true
        boolean needToReloadCoreAfterThisMethodEnds = false;


        if (needToSipReload){
            LOGGER.info("Выполняется плановая перезагрузка сип конфигов астериска");
            needToReloadCoreAfterThisMethodEnds = true;
            updatePhonesMapForCallProcessor();
            needToSipReload = false;
        }

        int size = usersThatChangedRules.size();
        if (size > 0){
            LOGGER.info("{} пользователей изменили правила. Заменяем правила в AGI процессоре на новые", size);
//            Main.monitor.reloadDialplan();
//            needToDialPlanReload = false;

            Iterator<User> iterator = usersThatChangedRules.iterator();
            while (iterator.hasNext()){
                User user = iterator.next();
                Set<OuterPhone> outerPhones = user.getOuterPhones();
                for (OuterPhone outerPhone : outerPhones) {
                    String phoneNumber = outerPhone.getNumber();
                    Integer scenarioId = outerPhone.getScenarioId();
                    if (scenarioId == null){
                        // если на номере не активирован сценарий - удаляем его с AGI
                        AgiInProcessor.replacePhoneAndRule(phoneNumber, null);
                        continue;
                    }

                    Scenario scenarioById = user.getScenarioById(scenarioId);
                    if (scenarioById == null){
                        LOGGER.warn("{}: в номере {} указан сценарий id {}, но такого не существует",
                                user.getLogin(), phoneNumber, scenarioId);
                        continue;
                    }

                    Rule rule = scenarioById.getRuleForNow();
                    if (rule == null){
                        LOGGER.warn("{}: брал правило на сейчас в сценарии id {}, но вернулся null",
                                user.getLogin(), scenarioId);
                        continue;
                    }
                    // на данный момент у нас есть номер телефона и правило, которое на нём должно прямо сейчас висеть. Меняем в AGI
                    AgiInProcessor.replacePhoneAndRule(phoneNumber, rule);
                }
                LOGGER.info("{}: были изменены правила. Изменения применены в AGI", user.getLogin());
                iterator.remove();
            }
        }

        int outerSize = usersThatChangedOuterNumbersOrBindings.size();
        if (outerSize > 0){
            LOGGER.info("{} пользователей удалили внешний номер или назначили сценарии на правила", outerSize);
            Iterator<User> iterator = usersThatChangedOuterNumbersOrBindings.iterator();
            while (iterator.hasNext()){
                User user = iterator.next();
                RulesConfigDAO.writeUsersRuleFile(user);
                iterator.remove();
            }
            needToReloadCoreAfterThisMethodEnds = true;
        }

        if (needToReloadCoreAfterThisMethodEnds){
            Main.monitor.reloadAsteriskCore();
        }
    }





    /**
     * Scenario writer
     */
    private static int scenarioTries = 0;
    @Scheduled(cron = "20 59 * * * ?") // в 20 секунд, 59 минут каждого часа
    private void generateConfig(){
        LOGGER.info("Начинается запись всех конфигов астериска в файлы.");

        try {
            RulesConfigDAO.writeAllNeededScenarios();
            LOGGER.info("Конфиги записаны в файлы.");
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

    @Scheduled(cron = "10 00 * * * ?") // в 10 секунд, 0 минут каждого часа
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
                LOGGER.error("Ошибка запуска мониторинга телефонии: {}", e.getMessage());
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
