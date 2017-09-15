package ua.adeptius.asterisk;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.asteriskjava.Cli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.adeptius.asterisk.annotations.AfterSpringLoadComplete;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.monitor.*;
import ua.adeptius.asterisk.senders.EmailSender;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;


@Component
public class Main {

    private static Logger LOGGER = LoggerFactory.getLogger(Main.class.getSimpleName());
    public static boolean startedOnWindows;
    public static boolean remoteServerIsUp;
    public static AsteriskMonitor monitor;
    public static EmailSender emailSender;
    public static Settings settings = new Settings();



    @AfterSpringLoadComplete
    public void init() {
        Settings.load(this.getClass());
        checkIfNeedRestartAndSetVariables();
    }

    private void checkIfNeedRestartAndSetVariables() {
        Map<String, String> getenv = System.getenv();
        String os = getenv.get("OS");
        boolean itsLinux = !(os != null && os.equals("Windows_NT"));
        settings.setItsLinux(itsLinux);

        boolean firstStart = Settings.getSettingBoolean("firstStart");
        LOGGER.info("Сервер загружается");

        if (itsLinux) { // это линукс
            LOGGER.info("OS Linux");
            Settings.setSetting("folder.rules", "/var/www/html/admin/modules/core/etc/clients/");
            Settings.setSetting("folder.sips", "/etc/asterisk/sip_clients/");
            Settings.setSetting("folder.usermusic", "/var/lib/asterisk/sounds/user/");
            Settings.setSetting("SERVER_ADDRESS_FOR_SCRIPT", "cstat.nextel.com.ua:8443");

        } else { // Это винда
            LOGGER.info("OS Windows");
            startedOnWindows = true;
            Settings.setSetting("folder.rules", "D:\\home\\adeptius\\tomcat\\rules\\");
            Settings.setSetting("folder.sips", "D:\\home\\adeptius\\tomcat\\sips\\");
            Settings.setSetting("folder.usermusic", "D:\\home\\adeptius\\tomcat\\usermusic\\");
            Settings.setSetting("SERVER_ADDRESS_FOR_SCRIPT", "adeptius.pp.ua:8443");
        }

        if (firstStart && itsLinux) {
            Settings.setSetting("firstStart", "false");
            LOGGER.info("------------------- TOMCAT RESTARTING NOW!!! -------------------");
            try {
                String[] cmd = {"/bin/sh", "-c", "pkill -9 java; sleep 1; sh /home/adeptius/tomcat/apache-tomcat-9.0.0.M17/bin/startup.sh"};
                Runtime.getRuntime().exec(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.info("------------------- TOMCAT READY!!! -------------------");
            afterTomcatRebootInit();
        }
    }

    private void afterTomcatRebootInit() {
        emailSender = new EmailSender();
        if (startedOnWindows) {
            try {
                HttpResponse<String> response = Unirest
                        .post("http://cstat.nextel.com.ua/tracking/scenario/getMelodies")
                        .header("content-type", "application/json")
                        .asString();
                if (response.getStatus() == 200) {
                    System.out.println("!!!!!!!!!!!!!!!!!!!ВНИМАНИЕ! ЗАПУЩЕНА КОПИЯ НА УДАЛЁННОМ СЕРВЕРЕ!!!!!!!!!!!!!");
                    remoteServerIsUp = true;
                    settings.setRemoteServerIsUp(true);
                }
            } catch (Exception ignored) {
            }
        }

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
        } catch (Exception e) {
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
//        CallProcessor.updatePhonesHashMap(); // обновляем мапу для того что бы знать с кем связан номер
//        AsteriskLogAnalyzer.updatePhonesHashMap();
        CallProcessor.updatePhonesHashMap();

        try {
            RulesConfigDAO.writeAllNeededScenarios();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Cli().start(); // Запуск AGI интерфейса астериска

        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            platformMBeanServer.registerMBean(settings, new ObjectName("Adeptius", "name", "controller"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        new PhoneWatcher();
    }
}
