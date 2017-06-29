package ua.adeptius.asterisk.monitor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.dao.MySqlCalltrackDao;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;


/**
 * Класс-демон, который каждые 10 минут делает запрос в каждую из 3х БД для поддержания соединения
 * и избежания ошибки таймаута соединения с БД.
 */
public class ConnectionKeeper extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(ConnectionKeeper.class.getSimpleName());


    public ConnectionKeeper() {
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                checkHibernate();
                checkStatisticConnection();
                checkTrackingConnection();
                Thread.sleep(1000 * 60 * 10);// 10 мин
            } catch (Exception e) {
                LOGGER.error("Ошибка очистки", e);
            }
        }
    }

    private void checkHibernate() {
        try {
            HibernateDao.getUserByLogin("e404");
            LOGGER.debug("Тестовый пользователь получен с помощью Hibernate");
        } catch (Exception e) {
            LOGGER.error("Не удалось получить тестового пользователя", e);
        }
    }

    private void checkStatisticConnection() {
        try {
            MySqlCalltrackDao.getMelodies();
            LOGGER.debug("Список мелодий получен с помощью Jdbc из БД статистики");
        } catch (Exception e) {
            LOGGER.error("Не удалось получить список мелодий для поддержания соединения", e);
        }
    }

    private void checkTrackingConnection() {
        try {
            MySqlStatisticDao.getListOfTables();
            LOGGER.debug("Список таблиц статистик получен с помощью Jdbc");
        } catch (Exception e) {
            LOGGER.error("Не удалось получить список таблиц статистик для поддержания соединения", e);
        }
    }
}
