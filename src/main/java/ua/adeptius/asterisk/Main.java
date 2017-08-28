package ua.adeptius.asterisk;



import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ua.adeptius.asterisk.annotations.AfterSpringLoadComplete;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.monitor.*;


@Component
//@EnableWebMvc
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

        try {
            RulesConfigDAO.writeAllNeededScenarios();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
}
