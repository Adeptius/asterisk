package ua.adeptius.asterisk;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.AsteriskMonitor;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.monitor.DailyCleaner;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.monitor.PhonesWatcher;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicInteger;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;

@Component
public class Main {

    public static AsteriskMonitor monitor;
    private static Logger LOGGER =  LoggerFactory.getLogger(Main.class.getSimpleName());

//    @Autowired
//    NewTestDao newTestDao;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
    }


    @PostConstruct
    private void init() {
        LOGGER.info("Начало инициализации модели");
        Settings.load(this.getClass());

        try {
            LOGGER.info("MYSQL: Инициализация");
            MySqlDao.init();
            LOGGER.info("MYSQL: Инициализирован");
        } catch (Exception e) {
            LOGGER.error("MYSQL: Ошибка инициализации", e);
        }

        // Чистка сервисов, если пользователь по какой-то причине удалён, а сервис остался
        LOGGER.info("Начало подготовки");
        try {
            LOGGER.info("Hibernate: Очистка сервисов владельцев которых больше нет");
            HibernateController.cleanServices();
            LOGGER.info("Hibernate: Очищено");
        }catch (Exception e){
            LOGGER.error("Hibernate: Ошибка очистки услуг", e);
            throw new RuntimeException("ОШИБКА УДАЛЕНИЯ УСЛУГ ВЛАДЕЛЬЦЕВ КОТОРЫХ БОЛЬШЕ НЕТ");
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
            MyLogger.log(DB_OPERATIONS, "ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТЕЛЕФОНИИ");
            throw new RuntimeException("ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТЕЛЕФОНИИ");
        }

        try {
            LOGGER.info("MySQL:Синхронизация номеров с БД");
            PhonesController.scanAndClean();
            LOGGER.info("MySQL: Номера синхронизированы");
        } catch (Exception e) {
            LOGGER.error("MySQL: Ошибка синхронизации номеров", e);
            MyLogger.log(DB_OPERATIONS, "ОШИБКА ОЧИСТКИ ЗАНЯТЫХ НОМЕРОВ В БАЗЕ");
            throw new RuntimeException("ОШИБКА ОЧИСТКИ ЗАНЯТЫХ НОМЕРОВ В БАЗЕ");
        }


        // создаём файлы конфигов номеров, если их нет
        try {
            LOGGER.info("Синхронизация файлов конфигов");
            SipConfigDao.synchronizeFilesAndDb();
            LOGGER.info("Синхронизация файлов конфигов завершена");
        } catch (Exception e) {
            LOGGER.error("Ошибка синхронизации файлов конфигов", e);
        }


        // Инициализация всех номеров телефонов
        LOGGER.info("Инициализация номеров трекинга");
        final AtomicInteger trackingCount = new AtomicInteger(0);
        UserContainer.getUsers().stream().filter(user -> user.getTracking() != null).map(User::getTracking).forEach(site -> {
            try {
                trackingCount.incrementAndGet();
                site.updateNumbers();
            } catch (Exception e) {
                LOGGER.error("Недостаточно номеров для трекинга " + site.getLogin(), e);
                throw new RuntimeException("Недостаточно номеров");
            }
        });
        LOGGER.info("Инициализированы номера для {} услуг трекинга", trackingCount);


        LOGGER.info("Инициализация номеров телефонии");
        final AtomicInteger telephonyCount = new AtomicInteger(0);
        UserContainer.getUsers().stream().filter(user -> user.getTelephony() != null).map(User::getTelephony).forEach(telephony -> {
            try {
                telephony.updateNumbers();
                telephonyCount.incrementAndGet();
            } catch (Exception e) {
                LOGGER.error("Недостаточно номеров для телефонии " + telephony.getLogin(), e);
                throw  new RuntimeException("Недостаточно номеров");
            }
        });
        LOGGER.info("Инициализированы номера для {} услуг телефонии", telephonyCount);


        // Загрузка правил
        LOGGER.info("Загрузка правил переадресации для всех пользователей");
        UserContainer.getUsers().forEach(User::loadRules);
        LOGGER.info("Правила загружены");


        LOGGER.info("Кеширование номеров и пользователей");
        CallProcessor.updatePhonesHashMap(); // обновляем мапу для того что бы знать с кем связан номер

        // Загрузка наблюдателя. Только для сайтов
        new PhonesWatcher();
        new DailyCleaner();


        Thread thread = new Thread(() -> initMonitor());
        thread.setDaemon(true);
        thread.start();

        // Чистим правила всех пользователей
        for (User user : UserContainer.getUsers()) {
            System.out.println(user);
            try {
                RulesConfigDAO.removeFileIfNeeded(user);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        Calendar calendar = new GregorianCalendar();
        MyLogger.log(DB_OPERATIONS, "Сервер был загружен в " + calendar.get(Calendar.HOUR_OF_DAY) + " часов, " + calendar.get(Calendar.MINUTE) + " минут.");
        LOGGER.info("Сервер запущен!");
    }

    private void initMonitor() {
        LOGGER.info("Запуск мониторинга астериска");
        try {
            monitor = new AsteriskMonitor();
            monitor.run();
            LOGGER.info("Монитор астериска запущен");
        } catch (Exception e) {
            LOGGER.error("Ошибка запуска мониторинга телефонии {}", e.getMessage());
            MyLogger.log(DB_OPERATIONS, "ОШИБКА ЗАПУСКА МОНИТОРИНГА ТЕЛЕФОНИИ " + e.getMessage());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } finally {
            }
            LOGGER.info("Повторный запуск монитора телефонии");
            MyLogger.log(DB_OPERATIONS, "ПОВТОРНО ЗАПУСКАЮ ТЕЛЕФОНИЮ");
            initMonitor();
        }
    }
}
