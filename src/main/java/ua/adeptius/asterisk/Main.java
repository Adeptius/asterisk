package ua.adeptius.asterisk;



import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.model.TimePairCookie;
import ua.adeptius.asterisk.annotations.AfterSpringLoadComplete;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.model.OuterPhone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.monitor.*;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@EnableWebMvc
@EnableScheduling
public class Main {

    private static Logger LOGGER =  LoggerFactory.getLogger(Main.class.getSimpleName());
    private static boolean startedOnWindows;
    public static AsteriskMonitor monitor;

    @AfterSpringLoadComplete
    public void init() {
        Settings.load(this.getClass());
        checkIfNeedRestartAndSetVariables();
    }

    private void checkIfNeedRestartAndSetVariables(){
        boolean itsLinux = "root".equals(System.getenv().get("USER"));
        boolean firstStart = Settings.getSettingBoolean("firstStart");
        LOGGER.info("Сервер загружается");

        if (itsLinux){ // это линукс
            LOGGER.info("OS Linux");
            Settings.setSetting("folder.rules","/var/www/html/admin/modules/core/etc/clients/");
            Settings.setSetting("folder.sips","/etc/asterisk/sip_clients/");
            Settings.setSetting("SERVER_ADDRESS_FOR_SCRIPT","cstat.nextel.com.ua:8443");

        }else { // Это винда
            LOGGER.info("OS Windows");
            startedOnWindows = true;
            Settings.setSetting("folder.rules","D:\\home\\adeptius\\tomcat\\rules\\");
            Settings.setSetting("folder.sips","D:\\home\\adeptius\\tomcat\\sips\\");
            Settings.setSetting("SERVER_ADDRESS_FOR_SCRIPT","adeptius.pp.ua:8443");
        }

        if (firstStart && itsLinux) {
            Settings.setSetting("firstStart", "false");
            LOGGER.info("------------------- TOMCAT RESTARTING NOW!!! -------------------");
            try {
                String[] cmd = { "/bin/sh", "-c", "pkill -9 java; sleep 1; sh /home/adeptius/tomcat/apache-tomcat-9.0.0.M17/bin/startup.sh" };
                Runtime.getRuntime().exec(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            LOGGER.info("------------------- TOMCAT READY!!! -------------------");
            afterTomcatRebootInit();

//            try {
//                Scenario scenario = new Scenario();
//                scenario.setFromList(Arrays.asList("0443211129"));
//                scenario.setToList(Arrays.asList("0994803031"));
//                scenario.setDays(new boolean[]{true, true, true, true, true, true, true});
//                scenario.setDestinationType(DestinationType.GSM);
//                scenario.setForwardType(ForwardType.TO_ALL);
//                scenario.setAwaitingTime(60);
//                scenario.setEndHour(24);
//                scenario.setMelody("none");
//                RulesConfigDAO.writeToFile(scenario);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
        }
    }

    private void afterTomcatRebootInit(){

        LOGGER.info("Начало инициализации модели");
        try {
            LOGGER.info("MYSQL: Инициализация");
            MySqlStatisticDao.init();
            LOGGER.info("MYSQL: Инициализирован");
        } catch (Exception e) {
            LOGGER.error("MYSQL: Ошибка инициализации", e);
            throw new RuntimeException();
        }

//        Загрузка обьектов
        try {
            LOGGER.info("Hibernate: Загрузка всех пользователей");
            UserContainer.setUsers(HibernateController.getAllUsers());
            LOGGER.info("Hibernate: Загружено {} пользователей", UserContainer.getUsers().size());
        }catch (Exception e){
            LOGGER.error("Hibernate: Ошибка загрузки пользователей", e);
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ПОЛЬЗОВАТЕЛЕЙ");
        }


        try {
            LOGGER.info("MySQL: Синхронизация таблиц статистики");
            MySqlStatisticDao.createOrCleanStatisticsTables();
            LOGGER.info("MySQL: Таблицы статистики синхронизированы");
        } catch (Exception e) {
            LOGGER.error("MySQL: Ошибка синхронизации таблиц статистики", e);
            throw new RuntimeException("ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТЕЛЕФОНИИ");
        }

        // создаём файлы конфигов номеров, если их нет
        try {
            LOGGER.info("Синхронизация файлов конфигов");
            SipConfigDao.synchronizeSipFilesAndInnerDb();
            LOGGER.info("Синхронизация файлов конфигов завершена");
        } catch (Exception e) {
            LOGGER.error("Ошибка синхронизации файлов конфигов", e);
            throw new RuntimeException();
        }

        LOGGER.info("Кеширование номеров и пользователей");
        CallProcessor.updatePhonesHashMap(); // обновляем мапу для того что бы знать с кем связан номер

        if (startedOnWindows){
            try{
                HttpResponse<String> response = Unirest
                        .post("http://cstat.nextel.com.ua/tracking/scenario/getMelodies")
                        .header("content-type", "application/json")
                        .asString();
                if (response.getStatus()==200){
                    System.out.println("!!!!!!!!!!!!!!!!!!!ВНИМАНИЕ! ЗАПУЩЕНА КОПИЯ НА УДАЛЁННОМ СЕРВЕРЕ!!!!!!!!!!!!!");
                }
            }catch (Exception ignored){
            }
        }
    }



    /**
     * ConnectionKeeper
     */
    @Scheduled(fixedDelay = 60000) // Каждую минуту
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
        LOGGER.trace("Очистка карты number <-> Call");
        CallProcessor.calls.clear();
    }


    /**
     * Scenario writer
     */
//    @Scheduled(cron = "0 55 * * * ?") // в 55 минут каждого часа
    private void generateConfig(){
        LOGGER.trace("Начинается запись всех конфигов астериска в файлы.");
        RulesConfigDAO.writeAllNeededScenarios();
    }

    @Scheduled(cron = "0 00 * * * ?") // в 0 минут каждого часа
    private void updatePhonesMapForCallProcessor(){
        CallProcessor.updatePhonesHashMap();
    }

    @Scheduled(fixedDelay = 20000)
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


}
