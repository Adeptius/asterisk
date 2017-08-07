package ua.adeptius.asterisk;



import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ua.adeptius.asterisk.annotations.AfterSpringLoadComplete;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.monitor.*;


@Component
public class Main {

    public static AsteriskMonitor monitor;
    private static Logger LOGGER =  LoggerFactory.getLogger(Main.class.getSimpleName());
    private static boolean startedOnWindows;


//    public Main(SessionFactory sessionFactory) {
//        HibernateDao.sessionFactory = sessionFactory;
//    }
//    @Autowired
//    SecondDao secondDao;


    @AfterSpringLoadComplete
    public void init() {
        Settings.load(this.getClass());
//        User e404 = secondDao.getUser("e404");
//        System.out.println(e404);
//        secondDao.changePass("e404", "222");
        checkIfNeedRestartAndSetVariables();
    }

    private void checkIfNeedRestartAndSetVariables(){
        boolean itsLinux = "root".equals(System.getenv().get("USER"));
        boolean firstStart = Settings.getSettingBoolean("firstStart");
        LOGGER.info("Сервер загружается");

        if (itsLinux){ // это линукс
            LOGGER.info("OS Linux");
            Settings.setSetting("forwardingRulesFolder","/var/www/html/admin/modules/core/etc/clients/");
            Settings.setSetting("sipConfigsFolder","/etc/asterisk/sip_clients/");
            Settings.setSetting("SERVER_ADDRESS_FOR_SCRIPT","cstat.nextel.com.ua:8443");

        }else { // Это винда
            LOGGER.info("OS Windows");
            startedOnWindows = true;
            Settings.setSetting("forwardingRulesFolder","D:\\home\\adeptius\\tomcat\\rules\\");
            Settings.setSetting("sipConfigsFolder","D:\\home\\adeptius\\tomcat\\sips\\");
            Settings.setSetting("SERVER_ADDRESS_FOR_SCRIPT","adeptius.pp.ua:8443");
        }

        if (firstStart && itsLinux) {
            Settings.setSetting("firstStart", "false");
            LOGGER.info("------------------- TOMCAT RESTARTING NOW!!! -------------------");
            try {
                String[] cmd = { "/bin/sh", "-c", "pkill java; sleep 4; sh /home/adeptius/tomcat/apache-tomcat-9.0.0.M17/bin/startup.sh" };
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
            MySqlDao.init();
            LOGGER.info("MYSQL: Инициализирован");
        } catch (Exception e) {
            LOGGER.error("MYSQL: Ошибка инициализации", e);
            throw new RuntimeException();
        }

//        Загрузка обьектов
        try {
            LOGGER.info("Hibernate: Загрузка всех пользователей");
            UserContainer.setUsers(HibernateDao.getAllUsers());
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

//        try {  Это не нужно так как теперь при удалении обьекта - номера освобождаются автоматически хибернейтом.
//            LOGGER.info("MySQL:Синхронизация номеров с БД");
//            PhonesController.scanAndClean();
//            LOGGER.info("MySQL: Номера синхронизированы");
//        } catch (Exception e) {
//            LOGGER.error("MySQL: Ошибка синхронизации номеров", e);
//            throw new RuntimeException("ОШИБКА ОЧИСТКИ ЗАНЯТЫХ НОМЕРОВ В БАЗЕ");
//        }


        // создаём файлы конфигов номеров, если их нет
        try {
            LOGGER.info("Синхронизация файлов конфигов");
            SipConfigDao.synchronizeSipFilesAndInnerDb();
            LOGGER.info("Синхронизация файлов конфигов завершена");
        } catch (Exception e) {
            LOGGER.error("Ошибка синхронизации файлов конфигов", e);
            throw new RuntimeException();
        }


        // Инициализация всех номеров телефонов
//        LOGGER.info("Инициализация номеров трекинга");
//        final AtomicInteger trackingCount = new AtomicInteger(0);
//        UserContainer.getUsers().stream().filter(user -> user.getTracking() != null).map(User::getTracking).forEach(site -> {
//            try {
//                trackingCount.incrementAndGet();
//                site.updateNumbers();
//            } catch (Exception e) {
//                LOGGER.error("Недостаточно номеров для трекинга " + site.getLogin(), e);
//                throw new RuntimeException("Недостаточно номеров");
//            }
//        });
//        LOGGER.info("Инициализированы номера для {} услуг трекинга", trackingCount);


//        LOGGER.info("Инициализация номеров телефонии");
//        final AtomicInteger telephonyCount = new AtomicInteger(0);
//        UserContainer.getUsers().stream().filter(user -> user.getTelephony() != null).map(User::getTelephony).forEach(telephony -> {
//            try {
//                telephony.updateNumbers();
//                telephonyCount.incrementAndGet();
//            } catch (Exception e) {
//                LOGGER.error("Недостаточно номеров для телефонии " + telephony.getLogin(), e);
//                throw new RuntimeException("Недостаточно номеров");
//            }
//        });
//        LOGGER.info("Инициализированы номера для {} услуг телефонии", telephonyCount);

        LOGGER.info("Кеширование номеров и пользователей");
        CallProcessor.updatePhonesHashMap(); // обновляем мапу для того что бы знать с кем связан номер

        Thread thread = new Thread(() -> initMonitor());
        thread.setDaemon(true);
        thread.start();

//        Calendar calendar = new GregorianCalendar();
//        MyLogger.log(DB_OPERATIONS, "Сервер был загружен в " + calendar.get(Calendar.HOUR_OF_DAY) + " часов, " + calendar.get(Calendar.MINUTE) + " минут.");
//        LOGGER.info("Сервер запущен!");

        if (startedOnWindows){
            try{
                HttpResponse<String> response = Unirest
                        .post("http://cstat.nextel.com.ua/tracking/rules/getMelodies")
                        .header("content-type", "application/json")
                        .asString();
                if (response.getStatus()==200){
                    System.out.println("!!!!!!!!!!!!!!!!!!!ВНИМАНИЕ! ЗАПУЩЕНА КОПИЯ НА УДАЛЁННОМ СЕРВЕРЕ!!!!!!!!!!!!!");
                }
            }catch (Exception ignored){
            }
        }
    }

    private void initMonitor() {
        LOGGER.info("Запуск мониторинга астериска");
        try {
            monitor = new AsteriskMonitor();
            monitor.run();
            LOGGER.info("Монитор астериска запущен");
        } catch (Exception e) {
            LOGGER.error("Ошибка запуска мониторинга телефонии {}", e.getMessage());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } finally {
            }
            LOGGER.info("Повторный запуск монитора телефонии");
            initMonitor();
        }
    }
}
