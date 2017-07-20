package ua.adeptius.asterisk.monitor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.LogCategory.NUMBER_FREE;

public class ScenarioWatcher extends Thread {

    private static Logger LOGGER =  LoggerFactory.getLogger(ScenarioWatcher.class.getSimpleName());

    public ScenarioWatcher() {
        setDaemon(true);
        setName("ScenarioWatcher");
        start();
    }

    @Override
    public void run() {
        while (!isInterrupted()){
            try {
                Thread.sleep(5 * 1000 * 60); // 5 минут
                checkTime();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static int lastHourOfWorks = 9999;

    private void checkTime(){
        Calendar calendar = new GregorianCalendar();
        int minutes = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (minutes <= 50){ // если до нового часа более 10 минут
            LOGGER.trace("Конфиги не пишем: до нового часа более 10 минут.");
            return;
        }

        if (hour == lastHourOfWorks){ // в этом часу уже писали конфиги
            LOGGER.trace("Конфиги не пишем: в этом часу уже конфиги писались.");
            return;
        }

        lastHourOfWorks = hour;
        generateConfig();
    }

    private void generateConfig(){
        LOGGER.trace("Начинается запись всех конфигов астериска в файлы.");
        RulesConfigDAO.writeAllNeededScenarios();
    }
}
